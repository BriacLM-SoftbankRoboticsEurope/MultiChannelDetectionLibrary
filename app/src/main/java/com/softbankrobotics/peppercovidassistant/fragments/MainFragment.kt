package com.softbankrobotics.peppercovidassistant.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import com.softbankrobotics.peppercovidassistant.fragments.dialog.MaskDialog
import com.softbankrobotics.peppercovidassistant.R
import com.softbankrobotics.multichanneldetectionlibrary.MultiChannelDetection
import com.softbankrobotics.peppercovidassistant.models.*

class MainFragment : BaseRobotFragment() {

    companion object {
        private const val TAG = "MSI_MAIN_FRAGMENT"
        private const val PREFIX_MEASURE = "COVID_"
        private const val PREFIX_WASH = "WASH_"
        private const val PREFIX_MASK = "MASK_"
        private const val PREFIX_SYMPTOM = "SYMPTOM_"
    }

    private var message: Pair<String, Int>? = null

    /**********************************Loop handling*********************************************/
    private var currentMessageIndex = 0

    /**********************************CovidInfo*************************************************/
    private var messageData = messageDataFrenchProtectiveMeasures
    private lateinit var messagePlayers : List<Pair<String, Int>>
    private var prefix = PREFIX_MEASURE

    /**********************************InfoDialog*************************************************/
    var noMaskDialog : MaskDialog? = null
    var skipMask = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity.currentChatData?.enableListeningAnimation(true)

        view.findViewById<AppCompatButton>(R.id.protective_measure).setOnClickListener {
            protectiveMeasureMessage()
        }
        view.findViewById<AppCompatButton>(R.id.wash_hand).setOnClickListener {
            washHandsMessage()
        }
        view.findViewById<AppCompatButton>(R.id.mask).setOnClickListener {
            maskMessage()
        }
        view.findViewById<AppCompatButton>(R.id.symptom).setOnClickListener {
            symptomMessage()
        }
        mainActivity.goToBookmark("HELLO_" + (1..4).random(), "topic_covid")
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
    override fun getLayoutId(): Int = R.layout.fragment_main

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
        layout.findViewById<FrameLayout>(R.id.welcome_message).visibility = View.GONE
        layout.findViewById<LinearLayout>(R.id.loop_message).visibility = View.VISIBLE
        mainActivity.goToBookmark(prefix + "1", "topic_covid")
        showMessage(1)
    }

    /**
     * @param index: Int index of the current message Text+Image
     */
    fun showMessage(index: Int) {
        currentMessageIndex = index
        if (currentMessageIndex > messagePlayers.size)
            return
        this.message = messagePlayers[currentMessageIndex - 1]
        showCurrentMessage()
    }

    /**
     * Interaction at the end of the message
     */
    fun endMessage() {
        mainActivity.runOnUiThread {
            layout.findViewById<LinearLayout>(R.id.loop_message).visibility = View.GONE
            layout.findViewById<FrameLayout>(R.id.welcome_message).visibility = View.VISIBLE
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

    /***************************
     * MASK DETECTOR
     **************************/
    /**
     * @param faces: List of faces detected by the library FaceMaskDetection
     */
    fun maskDetector(faces: List<MultiChannelDetection.FaceDetected>) {
        if (faces.isNotEmpty() && !skipMask) {
            if (faces[0].hasMask) {
                if (noMaskDialog != null && noMaskDialog?.isVisible!!)
                    noMaskDialog?.dismiss()
            } else {
                if (noMaskDialog == null || !noMaskDialog?.isVisible!!) {
                    noMaskDialog = MaskDialog()
                    noMaskDialog?.setPicture(faces[0].picture)
                    noMaskDialog?.setOnClickListener {
                        skipMask = true
                        noMaskDialog?.dismiss()
                    }
                    noMaskDialog?.show(mainActivity.fragmentManager, mainActivity.getString(R.string.warning))
                } else if (noMaskDialog != null && noMaskDialog?.isVisible!!)
                    noMaskDialog?.setPicture(faces[0].picture)
            }
        }
    }

    override fun onDestroy() {
        if (noMaskDialog != null && noMaskDialog?.isVisible!!)
            noMaskDialog?.dismiss()
        super.onDestroy()
    }
}