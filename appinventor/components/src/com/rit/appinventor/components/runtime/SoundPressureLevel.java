package com.rit.appinventor.components.runtime;

import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.*;
import android.Manifest;
import android.media.AudioRecord;
import android.content.pm.PackageManager;

import static android.media.AudioFormat.CHANNEL_IN_MONO;
import static android.media.AudioFormat.ENCODING_PCM_16BIT;
import static android.media.MediaRecorder.AudioSource.MIC;

@DesignerComponent(version = YaVersion.SOUNDPRESSURELEVEL_COMPONENT_VERSION,
        description = "Non-visible component that can collect sound pressure level data",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "images/extension.png")
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.RECORD_AUDIO")
public class SoundPressureLevel extends AndroidNonvisibleComponent
        implements OnStopListener, OnResumeListener, Deleteable {

    private final static String LOG_TAG = "SoundPressureLevel";
    private boolean isEnabled;
    private static final int audioSource = MIC;
    private static final int sampleRateInHz = 44100;
    private static final int channelConfig = CHANNEL_IN_MONO;
    private static final int audioFormat = ENCODING_PCM_16BIT;
    private AudioRecord recorder;
    Handler splHandler;
    private static final int minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz,channelConfig,audioFormat);
    private double currentSoundPressureLevel = 0;
    private double currentWeightedSoundPressureLevel = 0;
    private boolean isListening;
    Thread soundChecker;
    private boolean threadSuspended;
    private boolean isRecording;
    private boolean threadRunning = true;
    private int listenIntervalMilliSeconds = 200;
    private boolean hasPermission = false;
    private Object recordingLock = new Object();
    // Below constants dependent upon FFT being 1024 samples.
    private double fastWeightingCoefficient = 0.83047;
    private double slowWeightingCoefficient = 0.9770475;
    private double newAndOldWeightingCoefficientDiff = 0.999;

    public SoundPressureLevel(ComponentContainer container) {
        super(container.$form());

        recorder = new AudioRecord(MIC, sampleRateInHz, channelConfig, audioFormat, minBufferSize);
        form.registerForOnResume(this);
        form.registerForOnStop(this);
        Enabled(true);
        splHandler = new Handler();
        soundChecker = new Thread(new Runnable(){
            @Override
            public void run() {
                while(threadRunning){
                    Log.d(LOG_TAG, "spl thread loop");
                    if (checkPermissions()) {
                        if (getRecording()) {
                            Log.d(LOG_TAG, "spl thread isRecording");
                            final Pair<Complex[], Integer> tuple = analyzeSoundData();
                            form.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onSoundPressureLevelChanged(tuple);
                                }
                            });
                        }
                        else {
                            Log.d(LOG_TAG,"spl recording not enabled");
                        }
                    }
                    else {
                        Log.d(LOG_TAG,"spl Permission to record audio not granted, cannot calculate sound pressure level.");
                    }
                    try {
                        Thread.sleep( (long) listenIntervalMilliSeconds);
                    } catch (InterruptedException e) {
                        Log.d(LOG_TAG, "spl thread sleep error");
                    }
                }
                Log.d(LOG_TAG, "spl thread end");
            }
        });
        if (isListening == false) {
            startListening();
        }
        soundChecker.start();
        Log.d(LOG_TAG, "spl created");
    }

    @Override
    public void onDelete() {
        if (isEnabled) {
            try {
                Log.d(LOG_TAG, "spl joining thread");
                threadRunning = false;
                soundChecker.join();
            }
            catch (InterruptedException e) {
                Log.d(LOG_TAG,"spl error joining thread");
            }
            stopListening();
        }
    }

    @Override
    public void onResume() {
        if (isEnabled) {
            startListening();
            if (threadSuspended) {
                Log.d(LOG_TAG, "spl restarting thread");
                soundChecker.start();
                threadSuspended = false;
            }
        }
    }

    @Override
    public void onStop() {
        if (isEnabled) {
            stopListening();
            if (!threadSuspended) {
                Log.d(LOG_TAG, "spl suspend thrad");
                threadSuspended = true;
                soundChecker.suspend();
            }
        }
    }

    public void onSoundPressureLevelChanged(Pair<Complex[], Integer> tuple) {
        if (isEnabled) {
            Log.d(LOG_TAG, "spl onSoundPressueLevelChange");
            Complex[] soundData = tuple.first;
            Integer length = tuple.second;
            double data = 0;
            int lengthOfFFT = 1024; // Needs to be of the form 2^n
            Complex[] toFFT = new Complex[lengthOfFFT];
            Complex[] FFTOutput = null;
            double[] weightedBins = new double[lengthOfFFT];
            double freqOfBin = 0.0;
            boolean failedFFT = false;
            double currentWeightedValue = 0.0;
            double oldWeightedValue = 0.0;

            int i = 0;
            int numEffectiveSPLs = 0;
            while (i+lengthOfFFT<=soundData.length){ //Only use soundData if there's enough left.
                for (int j = 0; j < lengthOfFFT; j++) {
                    Log.d(LOG_TAG,String.format("spl sound data %f",soundData[i].re()));
                    toFFT[j] = soundData[i+j];
                }
                try {
                    FFTOutput = FFT.fft(toFFT);

                    Log.d(LOG_TAG,String.format("spl weigh FFT bins."));
                    for (int j = 0; j < FFTOutput.length; j++) {
                        freqOfBin = ((double)j/(double)lengthOfFFT)*(double)sampleRateInHz;
                        weightedBins[j] = 2*magnitudeOfImaginaryNumber(FFTOutput[j])*
                                calcCWeightCoefficient(freqOfBin); //TODO Currently hardcoded as C-Weighted as that's closest to no weighting. Need to find a dynamic way to switch between the two.
                        currentWeightedValue += weightedBins[j];
                    }
//                    currentWeightedValue = calcRootMeanSquare(weightedBins,weightedBins.length);

                    oldWeightedValue = Math.sqrt(
                            (1/.125) *
                            (fastWeightingCoefficient * Math.pow(oldWeightedValue,2) +
                            (newAndOldWeightingCoefficientDiff - fastWeightingCoefficient) *
                                    Math.pow(currentWeightedValue,2)));

                    //Increment counters.
                    numEffectiveSPLs++;
                    i+=lengthOfFFT; //Move to the front of the next clip of sound to FFT.
                } catch (IllegalArgumentException e){
                    Log.d(LOG_TAG,e.getMessage());
                    failedFFT = true;
                }
            }

            if(!failedFFT) {
                double weightedDb = calcDeciBels(oldWeightedValue/51805.5336); // Same division used when converting mic units to pascals.
                Log.d(LOG_TAG,String.format("spl update display with weighted dB: %f", weightedDb));
                WeightedSoundPressureLevelChanged(weightedDb);
            }

            // TODO The below is old code for comparison to the weighted SPL. Delete once the above is confirmed to work.

            //Convert data from mic to pressure in pascals.
            double[] soundSamplePressure = convertMicVoltageToPressure(soundData);

            //Find root mean square of sound.
            double rms = calcRootMeanSquare(soundSamplePressure,length);
            Log.d(LOG_TAG,String.format("spl RMS %f",rms));

            //Find SPL of sound.
            double dBs = calcDeciBels(rms);
            Log.d(LOG_TAG,String.format("spl %f dBs",dBs));

            //Round to the tenths decimal place.
//            dBs = Math.round(dBs*10)/10;

            SoundPressureLevelChanged(dBs);
        }
    }

    /**
     * Find the magnitude of a complex number.
     * (real^2+imaginary^2)^0.5
     * @param complex
     * @return
     */
    private double magnitudeOfImaginaryNumber(Complex complex){
        return Math.sqrt(Math.pow(complex.re(),2)+Math.pow(complex.im(),2));
    }

    /**
     * Calculate the Coefficient for A-Weighting an FFT Frequency bin.
     * https://en.wikipedia.org/wiki/A-weighting#Function_realisation_of_some_common_weightings
     * @param Hz
     * @return
     */
    private double calcAWeightCoefficient(double Hz){ //TODO Figure out what magnitude the freq needs to be in, Hz/KHz/MHz
        double R_a = (Math.pow(12194,2)*Math.pow(Hz,4))/
                ((Math.pow(Hz,2)+Math.pow(20.6,2))*
                        Math.sqrt((Math.pow(Hz,2)+Math.pow(107.7,2))*
                                (Math.pow(Hz,2)+Math.pow(737.9,2)))*
                        (Math.pow(Hz,2)+Math.pow(12914,2)));
        return R_a;
    }

    /**
     * Calculate the Coefficient for A-Weighting an FFT Frequency bin.
     * https://en.wikipedia.org/wiki/A-weighting#Function_realisation_of_some_common_weightings
     * @param Hz
     * @return
     */
    private double calcCWeightCoefficient(double Hz){ //TODO Figure out what magnitude the freq needs to be in, Hz/KHz/MHz
        double R_c = (Math.pow(12194,2)*Math.pow(Hz,2))/
                ((Math.pow(Hz,2)+Math.pow(20.6,2)) *
                        (Math.pow(Hz,2)+Math.pow(12914,2)));
//        double numerator = (Math.pow(12194,2)*Math.pow(Hz,2));
//        double denominator = ((Math.pow(Hz,2)+Math.pow(20.6,2)) // 20.6^2 not 20.6^6
//                *(Math.pow(Hz,2)+Math.pow(12914,2)));
//        Log.d(LOG_TAG,String.format("spl Numerator: %f",numerator));
//        Log.d(LOG_TAG,String.format("spl Denominator: %f",denominator));
//        Log.d(LOG_TAG,String.format("spl divide %f",numerator/denominator));
        Log.d(LOG_TAG,String.format("spl Calculating C Weight Coefficient. R_c: %f",R_c));
        return R_c;
    }

    /**
     * Converts Mic Voltage represented by a short to the pressure experienced by the mic.
     *
     * Max short value is 32,767, most smartphone microphones are accurate until about
     * 90dB or 0.6325 pascals. 32,767/0.6325 = 51,805.5336, which will be the value used
     * to convert between microphone data and pascals.
     *
     * @param soundData
     * @return
     */
    private double[] convertMicVoltageToPressure(Complex[] soundData) {
        double[] soundPressures = new double[soundData.length];
        for (int i = 0; i < soundData.length; i++) {
            soundPressures[i] = magnitudeOfImaginaryNumber(soundData[i])/51805.5336;
        }
        return soundPressures;
    }

    /**
     * Calculates the root mean square of sound data.
     * Follows the formula rms=sqrt((p^2)_average)
     * @param soundData
     * @param numSamples
     * @return
     */
    private double calcRootMeanSquare(double[] soundData, int numSamples) {
        //Find Root Square Mean of sound clip.
        double rms;
        double data = 0;
        for (int i = 0; i < numSamples; i++) {
            data+=Math.pow(soundData[i],2);
        }
        data = (data/numSamples);
        rms = Math.sqrt(data);
        return rms;
    }

    /**
     * Calculates the Sound Pressure Level in dBs of the sound pressue in pascals.
     * Follows the formula spl = 20*log10(p/pRef) where p is the current pressue in pascals,
     * pRef is smallest sound humans can hear at 2*10^-5 pascals.
     * @param p
     * @return
     */
    private double calcDeciBels(double p) {
        double pRef = 2*Math.pow(10,-5);
        double dBs = 20*Math.log10(p/pRef);
        return dBs;
    }

    public Pair<Complex[], Integer> analyzeSoundData() {
        Log.d(LOG_TAG, "spl analyzeSoundData");
        short spldata = 0;
        short recAudioData [] = new short[minBufferSize];
        int length = recorder.read(recAudioData, 0, minBufferSize);

        //Translate shorts recieved to Complex numbers for FFT.
        Complex complexAudioRecordData [] = new Complex[minBufferSize];
        for (int i = 0; i <recAudioData.length; i++) {
            complexAudioRecordData[i] = new Complex((double)recAudioData[i],0.0);
        }
        Pair<Complex[], Integer> tuple = new Pair<Complex[],Integer>(complexAudioRecordData,length);
        return tuple;
    }

    /**
     * Assumes that audioRecord has been initialized, which happens in constructor
     */
    private void startListening() {
        if (checkPermissions() && recorder != null) {
            Log.d(LOG_TAG,"spl start listening");
            int RecordingState;
            int initState = recorder.getState();
            if(initState == AudioRecord.STATE_UNINITIALIZED){
                recorder = new AudioRecord(MIC, sampleRateInHz, channelConfig, audioFormat, minBufferSize);
            }
            RecordingState = recorder.getRecordingState();
            if(RecordingState == AudioRecord.RECORDSTATE_STOPPED){
                recorder.startRecording();
            }
            setRecording(true);
        }
    }

    /**
     * Assumes that audioRecord has been initialized, which happens in constructor
     */
    private void stopListening() {
        if (checkPermissions() && recorder != null) {
            Log.d(LOG_TAG,"spl stop listening");
            int RecordingState;
            int initState = recorder.getState();
            if(initState == AudioRecord.STATE_UNINITIALIZED){
                recorder = new AudioRecord(MIC, sampleRateInHz, channelConfig, audioFormat, minBufferSize);
            }
            RecordingState = recorder.getRecordingState();
            if(RecordingState == AudioRecord.RECORDSTATE_RECORDING){
                recorder.stop();
            }
            setRecording(false);
        }
    }

    /**
     * Specifies whether the recorder should start recording audio.  If true,
     * the recorder will record audio.  Otherwise, no data is
     * recorded even if the device microphone is active.
     *
     * @param enabled {@code true} enables audio recording,
     *                {@code false} disables it
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
            defaultValue = "True")
    @SimpleProperty(
            description = "Set whether or not the Sound Pressure Level is enabled and listening."
    )
    public void Enabled(boolean enabled) {
        Log.d(LOG_TAG,"spl is enabled call");
        if (this.isEnabled != enabled) {
            Log.d(LOG_TAG,"spl change enabled status");
            this.isEnabled = enabled;
            if (enabled) {
                startListening();
            } else {
                stopListening();
            }
        }
    }

    /**
     * Available property getter method (read-only property).
     *
     * @return {@code true} indicates that the device has a microphone,
     * {@code false} that it isn't
     */
    @SimpleProperty(
            category = PropertyCategory.BEHAVIOR,
            description = "Returns true if there is a microphone available to listen with.")
    public boolean Available() {
        boolean isAvailable = false;
        if (checkPermissions()) {
            Log.d(LOG_TAG, "spl Available call");
            AudioRecord testRecorder = new AudioRecord(MIC, sampleRateInHz, channelConfig, audioFormat, minBufferSize);
            testRecorder.startRecording();
            isAvailable = testRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING; //Would be RECORDSTATE_STOPPED if no mic is available
            testRecorder.stop();
            testRecorder.release();
            Log.d(LOG_TAG, "spl Availability: " + String.valueOf(isAvailable));
        }
        else{
            Log.d(LOG_TAG,"spl Permission to record audio not granted, cannot check if microphone is available.");
        }
        return isAvailable;
    }

    /**
     * If true, the sensor will generate events.  Otherwise, no events
     * are generated
     *
     * @return {@code true} indicates that the sensor generates events,
     * {@code false} that it doesn't
     */
    @SimpleProperty(
            category = PropertyCategory.BEHAVIOR,
            description = "Returns whether the Sound Pressure Level is enabled and listening.")
    public boolean Enabled() {
        return isEnabled;
    }

    @SimpleProperty(
            category = PropertyCategory.BEHAVIOR,
            description = "Gets the last measured sound pressure level in decibels.")
    public double SoundPressureLevel() {
        double currSPL;
        if (checkPermissions()) {
            currSPL = currentSoundPressureLevel;
        }
        else {
            currSPL = -1;
        }
        return currSPL;
    }

    /**
     * Indicates the sound pressure level has changed
     */
    @SimpleEvent(
            description = "Event that is called on a set time period to update the sound pressure level."
    )
    public void SoundPressureLevelChanged(double decibels) {
        this.currentSoundPressureLevel = decibels;
        EventDispatcher.dispatchEvent(this, "SoundPressureLevelChanged", this.currentSoundPressureLevel);
    }

    /**
     * Indicates the sound pressure level has changed
     */
    @SimpleEvent(
            description = "Event that is called on a set time period to update the sound pressure level."
    )
    public void WeightedSoundPressureLevelChanged(double decibels) {
        this.currentWeightedSoundPressureLevel = decibels;
        EventDispatcher.dispatchEvent(this, "WeightedSoundPressureLevelChanged", this.currentWeightedSoundPressureLevel);
    }

    /**
     * Set the current wait time for the thread that reads the mic data.
     * The wait time will be in milliseconds (ms).
     * @param milliSeconds
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER,
            defaultValue = "200")
    @SimpleProperty (
            description = "Set the interval of time to listen in milliseconds.",
            category = PropertyCategory.BEHAVIOR)
    public void ListenIntervalMilliseconds(int milliSeconds) {
        if (milliSeconds > 0 && milliSeconds < Integer.MAX_VALUE) {
            this.listenIntervalMilliSeconds = milliSeconds;
        }
    }

    /**
     * Get the current wait time for the thread that reads the mic data.
     * The current wait time will be in milliseconds (ms).
     */
    @SimpleProperty (
            description = "Get the current interval of time spent listening in milliseconds.",
            category = PropertyCategory.BEHAVIOR)
    public int ListenIntervalMilliseconds() {
        return listenIntervalMilliSeconds;
    }

    private boolean checkPermissions() {
        PackageManager pm = form.getPackageManager();
        int permissionCode = pm.checkPermission(Manifest.permission.RECORD_AUDIO,form.getPackageName());
        boolean isPermissionGranted = permissionCode == PackageManager.PERMISSION_GRANTED;
        if (isPermissionGranted != hasPermission && isPermissionGranted == true) {
            //Change in permissions from false to true
            Log.d(LOG_TAG,"spl permission recently granted.");
            hasPermission = true;
            Enabled(isPermissionGranted);
        }
        else if (isPermissionGranted != hasPermission && isPermissionGranted == false){
            //Change in permissions from true to false
            Log.d(LOG_TAG,"spl permission recently revoked.");
            hasPermission = false;
            Enabled(isPermissionGranted);
        }
        return isPermissionGranted;
    }

    /**
     * Set whether or not recorder is listening in a thread-safe way.
     * @param recording
     */
    private void setRecording(boolean recording) {
        synchronized (recordingLock) {
            this.isRecording = recording;
        }
    }

    /**
     * Set whether or not recorder is listening in a thread-safe way.
     * @return
     */
    private boolean getRecording() {
        synchronized (recordingLock) {
            return this.isRecording;
        }
    }
}
