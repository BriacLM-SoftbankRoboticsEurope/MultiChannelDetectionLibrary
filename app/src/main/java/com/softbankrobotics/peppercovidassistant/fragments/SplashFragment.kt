package com.softbankrobotics.peppercovidassistant.fragments

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.softbankrobotics.peppercovidassistant.MainActivity
import com.softbankrobotics.peppercovidassistant.R
import com.softbankrobotics.peppercovidassistant.fragments.dialog.ImageDialog

class SplashFragment : BaseRobotFragment() {

    /*****************************Fragment life cycle********************************************/

    /**
     * On View Created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity.currentChatData?.enableListeningAnimation(false)

        layout.setOnClickListener {
            showNoTouchDialog(false)
            noTouchDialog?.handler?.postDelayed({
                noTouchDialog?.dismiss()
                if (mainActivity.multiChannelDetection != null && mainActivity.multiChannelDetection?.isRobotReady!! && mainActivity.chatIsReady)
                    mainActivity.showFragment(MainActivity.ID_FRAGMENT_MAIN)
            }, ImageDialog.TIME)
        }
        setReady(if (mainActivity.multiChannelDetection == null) false else mainActivity.multiChannelDetection?.isRobotReady!! && mainActivity.chatIsReady)
        if (mainActivity.multiChannelDetection != null && mainActivity.multiChannelDetection?.isRobotReady!!)
            mainActivity.multiChannelDetection?.turnToInitialPosition()
    }

    /**
     * Unlock or lock the possibility to show the main fragment
     * @param isReady: Boolean is the robot ready to move to the main fragment
     */
    fun setReady(isReady: Boolean) {
        val content = layout.findViewById<LinearLayout>(R.id.loading_content)

        if (isReady) {
            content.visibility = View.GONE
        } else {
            content.visibility = View.VISIBLE
        }
    }

    /**
     * Message to show while loading
     * @param text: String, message to show
     */
    fun setLoadingMessage(text: String) {
        mainActivity.runOnUiThread {
            layout.findViewById<TextView>(R.id.loading_message).text = text
        }
    }

    /**
     * @return Layout ID reference, R.layout.fragment_splash
     */
    override fun getLayoutId(): Int = R.layout.fragment_splash

    /**
     * @return Topic string reference, Null
     */
    override fun getTopic(): String? = null

    /**
     * @return First Bookmark string reference, Null
     */
    override fun getFirstBookmark(): String? = null
}