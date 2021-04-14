package com.softbankrobotics.peppercovidassistant.executors

import android.util.Log
import android.widget.Toast
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.conversation.BaseQiChatExecutor
import com.softbankrobotics.peppercovidassistant.MainActivity
import com.softbankrobotics.peppercovidassistant.utils.ChatData
import java.util.*

class LanguageExecutor(qiContext: QiContext, private var mainActivity: MainActivity) : BaseQiChatExecutor(qiContext) {

    companion object {
        private const val TAG = "MSI_LanguageExecutor"
    }

    /**
     * @param params: List of action to execute, only one expected
     */
    override fun runWith(params: List<String>) {
        if (params.isEmpty())
            return
        val language = params[0]
        Log.d(TAG, "language :$language")
        when (language) {
            "switch_to_english" -> {
                mainActivity.checkForLocal("en")
            }
            "switch_to_french" -> {
                mainActivity.checkForLocal("fr")
            }
        }
    }

    override fun stop() {}

}