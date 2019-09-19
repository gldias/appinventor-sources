package com.rit.appinventor.components.runtime;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.*;

@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
@DesignerComponent(version = YaVersion.BAROMETER_COMPONENT_VERION,
        description = "Non-visible component that can collect barometer pressure data",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "images/extension.png")
@SimpleObject(external = true)
public class BarometerSensor extends AndroidNonvisibleComponent
        implements OnStopListener, OnResumeListener, SensorComponent, SensorEventListener, Deleteable {

    private final static String LOG_TAG = "BarometerSensor";
    private final int PRESSURE_SENSOR_DATA_INDEX = 0;
    private final SensorManager sensorManager;
    private boolean isEnabled;
    private Sensor barometerSensor = null;
    private float currentMillibar = 0f;

    public BarometerSensor(ComponentContainer container) {
        super(container.$form());

        sensorManager = (SensorManager) container.$context().getSystemService(Context.SENSOR_SERVICE);
        if (Available()) {
            barometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        }
        form.registerForOnResume(this);
        form.registerForOnStop(this);
        Log.d(LOG_TAG, "barometer created");
    }

    @Override
    public void onDelete() {
        stopListening();
    }

    @Override
    public void onResume() {
        startListening();
    }

    @Override
    public void onStop() {
        stopListening();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (barometerSensor != null) {
            BarometerChanged(sensorEvent.values[PRESSURE_SENSOR_DATA_INDEX]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // TODO(markf): Figure out if we actually need to do something here.
    }

    /**
     * Assumes that sensorManager has been initialized, which happens in constructor
     */
    private void startListening() {
        if (barometerSensor != null) {
            sensorManager.registerListener(this, barometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /**
     * Assumes that sensorManager has been initialized, which happens in constructor
     */
    private void stopListening() {
        if (barometerSensor != null) {
            sensorManager.unregisterListener(this);
            currentMillibar = 0f;
        }
    }

    /**
     * Specifies whether the sensor should generate events.  If true,
     * the sensor will generate events.  Otherwise, no events are
     * generated even if the device is accelerated or shaken.
     *
     * @param enabled {@code true} enables sensor event generation,
     *                {@code false} disables it
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
            defaultValue = "True")
    @SimpleProperty(
            description = "Start the barometer with True and Stop with False"
    )
    public void Enabled(boolean enabled) {
        if (this.isEnabled != enabled && barometerSensor != null) {
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
     * @return {@code true} indicates that an accelerometer sensor is available,
     * {@code false} that it isn't
     */
    @SimpleProperty(
            description = "Returns true if device has a barometer sensor",
            category = PropertyCategory.BEHAVIOR)
    public boolean Available() {
        return (sensorManager.getSensorList(Sensor.TYPE_PRESSURE).size() > 0);
    }

    /**
     * If true, the sensor will generate events.  Otherwise, no events
     * are generated
     *
     * @return {@code true} indicates that the sensor generates events,
     * {@code false} that it doesn't
     */
    @SimpleProperty(
            description = "If true, the sensor is generating events",
            category = PropertyCategory.BEHAVIOR)
    public boolean Enabled() {
        return isEnabled;
    }

    @SimpleProperty(
            description = "Current millibar of barometer",
            category = PropertyCategory.BEHAVIOR)
    public float Millibar() {
        return currentMillibar;
    }

    /**
     * Indicates the barometer changed.
     */
    @SimpleEvent(
            description = "Event when the barometer sends new data"
    )
    public void BarometerChanged(float millibar) {
        this.currentMillibar = millibar;
        EventDispatcher.dispatchEvent(this, "BarometerChanged", this.currentMillibar);
    }
}
