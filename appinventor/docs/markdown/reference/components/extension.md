---
layout: documentation
title: Extension
---

[&laquo; Back to index](index.html)
# Extension

Table of Contents:

* [ros_bridge](#ros_bridge)

## ros_bridge  {#ros_bridge}

Component for ros_bridge



### Properties  {#ros_bridge-Properties}

{:.properties}
None


### Events  {#ros_bridge-Events}

{:.events}
None


### Methods  {#ros_bridge-Methods}

{:.methods}

{:id="ros_bridge.connectToMaster" class="method"} <i/> connectToMaster(*uri*{:.text})
: Allows user to connect to ROS master URI.
 URI provided as a string parameter

{:id="ros_bridge.sendMessage" class="method"} <i/> sendMessage(*parameters*{:.text},*topic*{:.text})
: Allows the user to send a specified message.
 Topic and Message provided
 as string parameters by the user

{:id="ros_bridge.stopRobot" class="method"} <i/> stopRobot()
: Tells the robot to stop moving

{:id="ros_bridge.turn" class="method"} <i/> turn()
: Turn robot

{:id="ros_bridge.twist" class="method"} <i/> twist()
: Full control
