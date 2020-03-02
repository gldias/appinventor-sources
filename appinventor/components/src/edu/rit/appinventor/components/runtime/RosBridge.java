// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package edu.rit.appinventor.components.runtime;

import android.os.Build;
import android.support.annotation.RequiresApi;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;

import org.ros.address.InetAddressFactory;
import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.*;
import org.ros.node.topic.Publisher;

import java.net.URI;
import java.net.URISyntaxException;

@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
@DesignerComponent(version = YaVersion.BARCODESCANNER_COMPONENT_VERSION,
        description = "Component for using the ROS interface",
        category = ComponentCategory.EXTENSION,
        nonVisible = true)
@SimpleObject(external = true)
@UsesLibraries(libraries = "rosjava-0.3.6.jar,std_msgs-0.5.10.jar,message_generation-0.3.3.jar," +
        ",guava-14.0.1.jar,apache_xmlrpc_client-0.3.6.jar,apache_xmlrpc_common-0.3.6.jar" +
        ",apache_xmlrpc_server-0.3.6.jar,commons-codec-1.3.jar,commons-httpclient-3.1.jar" +
        ",netty-3.5.2.Final.jar" + // ,commons-logging-1.1.1.jar,commons-pool-1.6.jar
        ",ws-commons-util-1.0.2.jar,rosgraph_msgs-1.11.2.jar")
public class RosBridge extends AndroidNonvisibleComponent {

    private NodeCommander commander;
    private ComponentContainer container;

    public RosBridge(ComponentContainer container) {
        super(container.$form());
        this.container = container;
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
        this.commander.executeNode(new NodeMain() {
            @Override
            public GraphName getDefaultNodeName() {
                return GraphName.of("test_app/talker");
            }

            @Override
            public void onStart(ConnectedNode connectedNode) {
                final Publisher<std_msgs.String> publisher =
                        connectedNode.newPublisher("chatter", std_msgs.String._TYPE);
                // This CancellableLoop will be canceled automatically when the node shuts
                // down.
                connectedNode.executeCancellableLoop(new CancellableLoop() {
                    private int sequenceNumber;

                    @Override
                    protected void setup() {
                        sequenceNumber = 0;
                    }

                    @Override
                    protected void loop() throws InterruptedException {
                        std_msgs.String str = publisher.newMessage();
                        str.setData("Hello world! " + sequenceNumber);
                        publisher.publish(str);
                        sequenceNumber++;
                        Thread.sleep(1000);
                    }
                });

            }

            @Override
            public void onShutdown(Node node) {

            }

            @Override
            public void onShutdownComplete(Node node) {

            }

            @Override
            public void onError(Node node, Throwable throwable) {

            }
        });
    }

}
