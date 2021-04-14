package com.softbankrobotics.peppercovidassistant.executors

import android.util.Log
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.conversation.BaseQiChatExecutor
import com.softbankrobotics.peppercovidassistant.MainActivity
import com.softbankrobotics.peppercovidassistant.fragments.MessageFragment

class CovidExecutor(qiContext: QiContext, private var mainActivity: MainActivity) : BaseQiChatExecutor(qiContext) {

    companion object {
        private const val TAG = "MSI_CovidExecutor"
    }

    /**
     * @param params: List of action to execute, only one expected
     */
    override fun runWith(params: List<String>) {
        if (params.isEmpty())
            return
        val topic = params[0]
        Log.d(TAG, "topic :$topic")
        when (topic) {
            "COVID_1"  -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).showMessage(2)
            }
            "COVID_2" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).showMessage(3)
            }
            "COVID_3" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).showMessage(4)
            }
            "COVID_4" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).showMessage(5)
            }
            "COVID_5" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).endMessage()
            }
            "WASH_1" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).showMessage(2)
            }
            "WASH_2" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).showMessage(3)
            }
            "WASH_3" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).showMessage(4)
            }
            "WASH_4" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).showMessage(5)
            }
            "WASH_5" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).showMessage(6)
            }
            "WASH_6" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).showMessage(7)
            }
            "WASH_7" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).showMessage(8)
            }
            "WASH_8" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).endMessage()
            }
            "MASK_1" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).showMessage(2)
            }
            "MASK_2" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).showMessage(3)
            }
            "MASK_3" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).showMessage(4)
            }
            "MASK_4" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).showMessage(5)
            }
            "MASK_5" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).showMessage(6)
            }
            "MASK_6" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).showMessage(7)
            }
            "MASK_7" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).showMessage(8)
            }
            "MASK_8" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).endMessage()
            }
            "SYMPTOM_1" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).showMessage(2)
            }
            "SYMPTOM_2" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).showMessage(3)
            }
            "SYMPTOM_3" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).showMessage(4)
            }
            "SYMPTOM_4" -> {
                if (mainActivity.currentFragmentId == MainActivity.ID_FRAGMENT_MESSAGE)
                    (mainActivity.fragment as MessageFragment).endMessage()
            }
        }
    }

    override fun stop() {}

}