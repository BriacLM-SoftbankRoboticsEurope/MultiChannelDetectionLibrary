package com.softbankrobotics.peppercovidassistant.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.softbankrobotics.peppercovidassistant.R
import com.softbankrobotics.peppercovidassistant.MainActivity
import com.softbankrobotics.peppercovidassistant.models.*

class MessageFragment : BaseRobotFragment() {

    companion object {
        private const val TAG = "MSI_MAIN_FRAGMENT"
        private const val PREFIX_MEASURE = "COVID_"
        private const val PREFIX_WASH = "WASH_"
        private const val PREFIX_MASK = "MASK_"
        private const val PREFIX_SYMPTOM = "SYMPTOM_"

        const val MEASURE_MESSAGE_ID = 0
        const val WASH_MESSAGE_ID = 1
        const val MASK_MESSAGE_ID = 2
        const val SYMPTOM_MESSAGE_ID = 3
    }

    private var message: Pair<String, Int>? = null

    /**********************************Loop handling*********************************************/
    private var currentMessageIndex = 0

    /**********************************CovidInfo*************************************************/
    private var messageData = messageDataFrenchProtectiveMeasures
    private var messagePlayers : List<Pair<String, Int>>? = null
    private var prefix = PREFIX_MEASURE

    var MESSAGE_ID = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity.currentChatData?.enableListeningAnimation(true)

        when(MESSAGE_ID) {
            MEASURE_MESSAGE_ID -> protectiveMeasureMessage()
            WASH_MESSAGE_ID -> washHandsMessage()
            MASK_MESSAGE_ID -> maskMessage()
            SYMPTOM_MESSAGE_ID -> symptomMessage()
        }
    }

    /**
     * Start Message Protective Measure
     */
    fun protectiveMeasureMessage() {
        prefix = PREFIX_MEASURE
        Log.d(TAG, "Start Information Message : $prefix")
        startMessage(if (mainActivity.config?.locale?.language == "fr") messageDataFrenchProtectiveMeasures else messageDataEnglishProtectiveMeasures)
    }

    /**
     * Start Message Wash Hand
     */
    fun washHandsMessage() {
        prefix = PREFIX_WASH
        Log.d(TAG, "Start Information Message : $prefix")
        startMessage(if (mainActivity.config?.locale?.language == "fr") messageDataFrenchWashHand else messageDataEnglishWashHand)
    }


    /**
     * Start Message Mask
     */
    fun maskMessage() {
        prefix = PREFIX_MASK
        Log.d(TAG, "Start Information Message : $prefix")
        startMessage(if (mainActivity.config?.locale?.language == "fr") messageDataFrenchMask else messageDataEnglishMask)
    }


    /**
     * Start Message Symptom
     */
    fun symptomMessage() {
        prefix = PREFIX_SYMPTOM
        Log.d(TAG, "Start Information Message : $prefix")
        startMessage(if (mainActivity.config?.locale?.language == "fr") messageDataFrenchSymptom else messageDataEnglishSymptom)
    }

    /**
     * @return Layout ID reference R.layout.fragment_main
     */
    override fun getLayoutId(): Int = R.layout.fragment_message

    /**
     * @return Topic string reference, Null
     */
    override fun getTopic(): String? = null

    /**
     * @return first bookmark reference, Null
     */
    override fun getFirstBookmark(): String? = null

    /**
     * @param message: CovidInfoData : list of messages to show
     */
    private fun startMessage(message: CovidInfoData) {
        messageData = message
        if (mainActivity.qiContext != null)
            messagePlayers = messageData.messages.map { msg -> Pair(msg.first, msg.second) }
        currentMessageIndex = 0
        mainActivity.goToBookmark(prefix + "1", "topic_covid")
        showMessage(1)
    }

    /**
     * @param index: Int index of the current message Text+Image
     */
    fun showMessage(index: Int) {
        currentMessageIndex = index
        if (messagePlayers == null) {
            mainActivity.showFragment(MainActivity.ID_FRAGMENT_MAIN)
            return
        }
        if (currentMessageIndex > messagePlayers?.size!!)
            return
        this.message = messagePlayers!![currentMessageIndex - 1]
        showCurrentMessage()
    }

    /**
     * Interaction at the end of the message
     */
    fun endMessage() {
        mainActivity.runOnUiThread {
            mainActivity.showFragment(MainActivity.ID_FRAGMENT_MAIN)
        }
        mainActivity.goToBookmark("END_SLIDE", "topic_covid")
    }

    /**
     * Display the current message
     */
    private fun showCurrentMessage() {
        val mainImage = layout.findViewById<ImageView>(R.id.mainImage)
        val mainLabel = layout.findViewById<TextView>(R.id.mainLabel)

        mainActivity.runOnUiThread {
            if (message?.second == null) {
                mainImage?.visibility = View.GONE
            } else {
                mainImage?.setImageResource(message?.second!!)
                mainImage?.visibility = View.VISIBLE
            }
            mainLabel?.text = message?.first
            mainLabel?.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        if (noMaskDialog != null && noMaskDialog?.isVisible!!)
            noMaskDialog?.dismiss()
        super.onDestroy()
    }
}