package com.rit.appinventor.components.runtime;

import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Publisher;

public class publisher_node extends AbstractNodeMain implements NodeMain {

    @Override
    public void onStart(ConnectedNode connectedNode) {
        final Publisher<std_msgs.String> command_publisher = connectedNode.newPublisher("topicName", "messageType");
        final CancellableLoop loop = new CancellableLoop() {

            @Override
            protected void loop() throws InterruptedException {
                //loop logic for node goes here
            }
        };

        connectedNode.executeCancellableLoop(loop);
    }

    @Override
    public void onShutdown(Node node) {
        //TODO
    }

    @Override
    public void onShutdownComplete(Node node) {
        //TODO
    }

    @Override
    public void onError(Node node, Throwable throwable) {
        //TODO
    }
}
