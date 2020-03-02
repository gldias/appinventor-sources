package edu.rit.appinventor.components.runtime;


import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;

public class NodeCommander {
    NodeMainExecutor nodeMainExecutor;
    NodeConfiguration nodeConfiguration;
    public NodeCommander(NodeMainExecutor nodeMainExecutor) {
        this.nodeMainExecutor = nodeMainExecutor;
    }

    public void addConfiguration(NodeConfiguration nodeConfiguration) {
        this.nodeConfiguration = nodeConfiguration;
    }

    public void executeNode(NodeMain thing) {
        this.nodeMainExecutor.execute(thing, nodeConfiguration);
    }
}
