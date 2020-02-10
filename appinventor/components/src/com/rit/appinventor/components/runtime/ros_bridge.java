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
import org.ros.address.InetAddressFactory;
import org.ros.node.DefaultNodeMainExecutor;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.net.URISyntaxException;

@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
@DesignerComponent(version = 1,
        description = "Non-visible component that allows the user to connect to a robot running on ROS",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "images/extension.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries(libraries =
        "apache_xmlrpc_client-0.3.6.jar,apache_xmlrpc_common-0.3.6.jar,apache_xmlrpc_server-0.3.6.jar," +
        "commons-codec-1.3.jar,commons-httpclient-3.1.jar,commons-logging-1.1.1.jar,commons-pool-1.6.jar," +
        "guava-12.0.1.jar,message_generation-0.3.3.jar,netty-3.5.2.jar,rosgraph_msgs-1.11.2.jar," +
        "rosjava-0.3.6.jar,std_msgs-0.5.10.jar,ws-commons-util-1.0.2.jar")
@SimpleObject(external = true)
public class ros_bridge extends AndroidNonvisibleComponent implements OnStopListener, OnResumeListener, Deleteable {
    NodeConfiguration configuration;
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
    @SimpleFunction
    public void connectToMaster(String uri) throws URISyntaxException {
        java.net.URI masterUri = new URI(uri);

        configuration = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(), masterUri);
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
