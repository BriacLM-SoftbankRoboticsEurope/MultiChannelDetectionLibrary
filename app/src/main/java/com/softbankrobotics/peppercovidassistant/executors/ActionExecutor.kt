package com.softbankrobotics.peppercovidassistant.executors

import android.util.Log
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.conversation.BaseQiChatExecutor
import com.softbankrobotics.peppercovidassistant.MainActivity
import com.softbankrobotics.peppercovidassistant.fragments.MainFragment
import com.softbankrobotics.peppercovidassistant.fragments.MessageFragment

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
                    mainActivity.runOnUiThread{
                        mainActivity.showFragment(MainActivity.ID_FRAGMENT_MESSAGE, MessageFragment.MEASURE_MESSAGE_ID.toString())
                    }
            }
            "wash_hands" -> {
                Log.d(TAG, "Action show message wash_hands")
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MAIN)
                    mainActivity.runOnUiThread{
                        mainActivity.showFragment(MainActivity.ID_FRAGMENT_MESSAGE, MessageFragment.WASH_MESSAGE_ID.toString())
                    }
            }
            "mask" -> {
                Log.d(TAG, "Action show message mask")
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MAIN)
                    mainActivity.runOnUiThread{
                        mainActivity.showFragment(MainActivity.ID_FRAGMENT_MESSAGE, MessageFragment.MASK_MESSAGE_ID.toString())
                    }
            }
            "symptoms" -> {
                Log.d(TAG, "Action show message symptoms")
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MAIN)
                    mainActivity.runOnUiThread{
                        mainActivity.showFragment(MainActivity.ID_FRAGMENT_MESSAGE, MessageFragment.SYMPTOM_MESSAGE_ID.toString())
                    }
            }
            // Skip : dismiss the current dialogue
            "skip" -> {
                Log.d(TAG, "Action dismiss popup dialog")
                if (mainActivity.fragment?.noMaskDialog != null && mainActivity.fragment?.noMaskDialog?.isVisible!!) {
                    mainActivity.fragment?.noMaskDialog?.dismiss()
                    mainActivity.skipMaskDetection = true
                }
                if (mainActivity.fragment?.noTouchDialog != null && mainActivity.fragment?.noTouchDialog?.isVisible!!)
                    mainActivity.fragment?.noTouchDialog?.dismiss()
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_SPLASH) {
                    if (mainActivity.textDialog != null && mainActivity.textDialog?.isVisible!!) {
                        mainActivity.textDialog?.dismiss()
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