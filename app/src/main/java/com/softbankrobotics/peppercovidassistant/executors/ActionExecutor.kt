package com.softbankrobotics.peppercovidassistant.executors

import android.util.Log
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.conversation.BaseQiChatExecutor
import com.softbankrobotics.peppercovidassistant.MainActivity
import com.softbankrobotics.peppercovidassistant.fragments.MainFragment

class ActionExecutor(qiContext: QiContext, private var mainActivity: MainActivity) : BaseQiChatExecutor(qiContext) {


    companion object {
        private const val TAG = "MSI_ActionExecutor"
    }

    /**
     * @param params: List of action to execute, only one expected
     */
    override fun runWith(params: List<String>) {
        if (params.isEmpty())
            return
        when (params[0]) {
            // Launch message based on the action required
            "protective_measure" -> {
                Log.d(TAG, "Action show message protective_measure")
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MAIN)
                    mainActivity.runOnUiThread{(mainActivity.fragment as MainFragment).protectiveMeasureMessage()}
            }
            "wash_hands" -> {
                Log.d(TAG, "Action show message wash_hands")
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MAIN)
                    mainActivity.runOnUiThread{(mainActivity.fragment as MainFragment).washHandsMessage()}
            }
            "mask" -> {
                Log.d(TAG, "Action show message mask")
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MAIN)
                    mainActivity.runOnUiThread{(mainActivity.fragment as MainFragment).maskMessage()}
            }
            "symptoms" -> {
                Log.d(TAG, "Action show message symptoms")
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MAIN)
                    mainActivity.runOnUiThread{(mainActivity.fragment as MainFragment).symptomMessage()}
            }
            // Skip : dismiss the current dialogue
            "skip" -> {
                Log.d(TAG, "Action dismiss popup dialog")
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MAIN) {
                    if ((mainActivity.fragment as MainFragment).noMaskDialog != null && (mainActivity.fragment as MainFragment).noMaskDialog?.isVisible!!) {
                        (mainActivity.fragment as MainFragment).noMaskDialog?.dismiss()
                        (mainActivity.fragment as MainFragment).skipMask = true
                    }
                }
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_SPLASH) {
                    if (mainActivity.informationDialog != null && mainActivity.informationDialog?.isVisible!!) {
                        mainActivity.informationDialog?.dismiss()
                        mainActivity.multiChannelDetection?.cancelMappingAndLocalize()
                        mainActivity.multiChannelDetection?.isRobotReady = true
                        mainActivity.multiChannelDetection?.activity?.onRobotReady(true)
                    }
                }
            }
        }
    }

    override fun stop() {}

}