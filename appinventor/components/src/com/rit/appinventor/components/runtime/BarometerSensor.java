
package com.rit.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import com.google.appinventor.components.runtime.*;

@DesignerComponent(version = YaVersion.BAROMETER_COMPONENT_VERION,
        description = "Non-visible component that can detect shaking and " +
                "measure barometer ",
        category = ComponentCategory.SENSORS,
        nonVisible = true,
        iconName = "images/accelerometersensor.png")
@SimpleObject(external = true)
public class BarometerSensor extends AndroidNonvisibleComponent
        implements OnStopListener, OnResumeListener, SensorComponent, SensorEventListener, Deleteable {

    private final static String LOG_TAG = "BarometerSensor";
    private final static boolean DEBUG = true;

    // Indicates whether the barometer should generate events
    private boolean enabled;
    private final SensorManager sensorManager;
    private Sensor barometerSensor;
    private float currentMbar = 0f;

    public BarometerSensor(ComponentContainer container) {
        super(container.$form());

        sensorManager = (SensorManager) container.$context().getSystemService(Context.SENSOR_SERVICE);
        barometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        form.registerForOnResume(this);
        form.registerForOnStop(this);
        Enabled(true);
        Log.d(LOG_TAG, "barometer created");
    }

    @Override
    public void onDelete() {
        if (enabled) {
            stopListening();
        }
    }

    @Override
    public void onResume() {
        if (enabled) {
            startListening();
        }
    }

    @Override
    public void onStop() {
        if (enabled) {
            stopListening();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (enabled) {
            final float[] values = sensorEvent.values.clone();
            BarometerChanged(values[0]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // TODO(markf): Figure out if we actually need to do something here.
    }


    // Assumes that sensorManager has been initialized, which happens in constructor
    private void startListening() {
        sensorManager.registerListener(this, barometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // Assumes that sensorManager has been initialized, which happens in constructor
    private void stopListening() {
        sensorManager.unregisterListener(this);
        currentMbar = 0f;
    }

    /**
     * Specifies whether the sensor should generate events.  If true,
     * the sensor will generate events.  Otherwise, no events are
     * generated even if the device is accelerated or shaken.
     *
     * @param enabled  {@code true} enables sensor event generation,
     *                 {@code false} disables it
     */
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
            defaultValue = "True")
    @SimpleProperty
    public void Enabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
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
     *         {@code false} that it isn't
     */
    @SimpleProperty(
            category = PropertyCategory.BEHAVIOR)
    public boolean Available() {
        return (sensorManager.getSensorList(Sensor.TYPE_PRESSURE).size() > 0);
    }

    /**
     * If true, the sensor will generate events.  Otherwise, no events
     * are generated even if the device is accelerated or shaken.
     *
     * @return {@code true} indicates that the sensor generates events,
     *         {@code false} that it doesn't
     */
    @SimpleProperty(
            category = PropertyCategory.BEHAVIOR)
    public boolean Enabled() {
        return enabled;
    }

    @SimpleProperty(
            category = PropertyCategory.BEHAVIOR)
    public float mbar() {
        return currentMbar;
    }

    /**
     * Indicates the barometer changed.
     */
    @SimpleEvent
    public void BarometerChanged(float mbar) {
        this.currentMbar = mbar;
        EventDispatcher.dispatchEvent(this, "BarometerChanged", this.currentMbar);
    }
}
