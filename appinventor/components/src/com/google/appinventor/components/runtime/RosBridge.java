// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.util.OnInitializeListener;
import org.ros.address.InetAddressFactory;
import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.*;
import org.ros.node.topic.Publisher;

import java.net.URI;
import java.net.URISyntaxException;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@DesignerComponent(version = YaVersion.BARCODESCANNER_COMPONENT_VERSION,
        description = "Component for using the ROS interface",
        category = ComponentCategory.EXTENSION,
        nonVisible = true)
@UsesLibraries(libraries = "rosjava-0.3.6.jar,std_msgs-0.5.10.jar,message_generation-0.3.3.jar," +
        ",guava-14.0.1.jar,apache_xmlrpc_client-0.3.6.jar,apache_xmlrpc_common-0.3.6.jar" +
        ",apache_xmlrpc_server-0.3.6.jar,commons-codec-1.3.jar,commons-httpclient-3.1.jar" +
        ",netty-3.5.2.Final.jar,commons-logging-1.1.1.jar,commons-pool-1.6.jar" + //
        ",ws-commons-util-1.0.2.jar,rosgraph_msgs-1.11.2.jar")
public class RosBridge extends AndroidNonvisibleComponent implements OnInitializeListener {

    private final static String LOG_TAG = "RosBridge";
    private NodeCommander commander;
    private ComponentContainer container;
    private boolean hasPermission = false;

    public RosBridge(ComponentContainer container) {
        super(container.$form());
        this.container = container;
    }

    @Override
    public void onInitialize(){
        Log.d(LOG_TAG,"RosBridge onInitialize");
        if(!checkPermissions()){
            Log.d(LOG_TAG,"RosBridge permissions not granted yet");
            requestPermission("onInitialize");
        }
        else {
            Log.d(LOG_TAG,"RosBridge permissions granted already");
        }
    }

    @SimpleFunction()
    public void createCommander() {
        this.commander = new NodeCommander(DefaultNodeMainExecutor.newDefault());
    }

    @SimpleFunction()
    public void updateHostAddress(String url) {
        try {
            this.commander.addConfiguration(NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback().getHostAddress(), new URI(url)));
        } catch (URISyntaxException uriSyntaxException) {
            System.out.println(uriSyntaxException.toString());
        }
    }

    @SimpleFunction()
    public void runTestNode() {
        NodeMain test = new MyNode();
        this.commander.executeNode(test);
    }

    private boolean checkPermissions() {
        PackageManager pm = form.getPackageManager();
        int permissionCode = pm.checkPermission(Manifest.permission.INTERNET,form.getPackageName());
        boolean isPermissionGranted = permissionCode == PackageManager.PERMISSION_GRANTED;
        if (isPermissionGranted != hasPermission && isPermissionGranted) {
            //Change in permissions from false to true
            Log.d(LOG_TAG,"internet permission recently granted.");
            hasPermission = true;
        }
        else if (isPermissionGranted != hasPermission && !isPermissionGranted){
            //Change in permissions from true to false
            Log.d(LOG_TAG,"internet permission recently revoked.");
            hasPermission = false;
        }
        return isPermissionGranted;
    }

    private void requestPermission(final String caller) {
        final RosBridge me = this;
        form.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                form.askPermission(Manifest.permission.INTERNET,
                        new PermissionResultHandler() {
                            @Override
                            public void HandlePermissionResponse(String permission, boolean granted) {
                                if (granted) {
                                    hasPermission = true;
                                    Log.d(LOG_TAG,"RosBridge INTERNET Permission granted");
                                } else {
                                    form.dispatchPermissionDeniedEvent(me, caller,
                                            Manifest.permission.INTERNET);
                                    Log.d(LOG_TAG,"RosBridge INTERNET Permission denied");
                                }
                            }
                        });
            }
        });
    }
}
