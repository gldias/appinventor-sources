package com.rit.appinventor.components.runtime;

import android.os.Build;
import android.support.annotation.RequiresApi;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Deleteable;
import com.google.appinventor.components.runtime.OnResumeListener;
import com.google.appinventor.components.runtime.OnStopListener;
import com.google.appinventor.components.runtime.ComponentContainer;

@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
@DesignerComponent(version = 1,
        description = "Non-visible component that allows the user to connect to a robot running on ROS",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "images/extension.png")
@SimpleObject(external = true)
public class ros_bridge extends AndroidNonvisibleComponent implements OnStopListener, OnResumeListener, Deleteable {

    public ros_bridge(ComponentContainer container) {
        super(container.$form());
    }

    @Override
    public void onDelete() {
        //TODO
    }

    @Override
    public void onResume() {
        //TODO
    }

    @Override
    public void onStop() {
        //TODO
    }

    /**
     * Allows user to connect to ROS master URI.
     * URI provided as a string parameter
     */
    @SimpleFunction
    public void connectToMaster(String URI) {
        //TODO
    }

    /**
     * Tells the robot to stop moving
     */
    @SimpleFunction
    public void stopRobot() {
        //TODO
    }

    /**
     * Full Control
     * @param speed the speed at which the robot should move
     * @param angle the angle the robot should face
     */
    @SimpleFunction
    public void twist(float speed, float angle) {
        //TODO
    }

    /**
     * Spin clockwise indefinitely
     * @param speed the speed at which the robot should spin at
     */
    @SimpleFunction
    public void spinClockwiseIndefinitely(float speed) {
        //TODO
    }

    /**
     * Spin counter-clockwise indefinitely
     * @param speed the speed at which the robot should spin at
     */
    @SimpleFunction
    public void spinCounterClockwiseIndefinitely(float speed) {
        //TODO
    }

    /**
     * Move forward indefinitely
     * @param speed the speed at which the robot should move at
     */
    @SimpleFunction
    public void moveForwardIndefinitely(float speed) {
        //TODO
    }

    /**
     * Move backwards indefinitely
     * @param speed the speed at which the robot should move at
     */
    @SimpleFunction
    public void moveBackwardIndefinitely(float speed) {
        //TODO
    }

    /**
     * Turn the robot to face a given angle
     * @param angle the angle that the robot should face
     */
    @SimpleFunction
    public void turn(float angle) {
        //TODO
    }
}
