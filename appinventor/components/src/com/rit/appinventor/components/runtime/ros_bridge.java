package com.rit.appinventor.components.runtime;

import android.os.Build;
import android.support.annotation.RequiresApi;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
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
     * Allows the user to send a specified message.
     * Topic and Message provided
     * as string parameters by the user
     */
    @SimpleFunction
    public void sendMessage(String parameters, String topic) {
        //TODO
    }

    /**
     * Allows user to connect to ROS master URI.
     * URI provided as a string parameter
     */
    @DesignerProperty
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
     * Full control
     */
    @SimpleFunction
    public void twist() {
        //TODO
    }

    /**
     * Turn robot
     */
    @SimpleFunction
    public void turn() {
        //TODO
    }
}
