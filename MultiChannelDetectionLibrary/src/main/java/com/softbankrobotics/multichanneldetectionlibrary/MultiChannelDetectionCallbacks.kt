package com.softbankrobotics.multichanneldetectionlibrary

import com.aldebaran.qi.sdk.`object`.human.Human
import com.aldebaran.qi.sdk.design.activity.RobotActivity

interface MultiChannelDetectionCallbacks {

    // context
    var robotActivity: RobotActivity

    // Robot is ready to engage
    fun onRobotReady(isRobotReady: Boolean)
    // Robot reach a step : MAPPING, LOCALIZING etc ...
    fun onStepReach(step: Int, finished: Boolean)
    // Listener : Charging Flap State Change Open/Close
    fun onChargingFlapStateChanged(open: Boolean)
    // human detected by HumanAwareness
    fun onHumanDetected(human: Human?, faces: List<MultiChannelDetection.FaceDetected>?)
}