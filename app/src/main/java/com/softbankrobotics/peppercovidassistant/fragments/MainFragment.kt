package com.softbankrobotics.peppercovidassistant.fragments

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.aldebaran.qi.sdk.util.FutureUtils
import com.softbankrobotics.peppercovidassistant.MainActivity
import com.softbankrobotics.peppercovidassistant.R
import com.softbankrobotics.peppercovidassistant.fragments.dialog.ImageDialog
import java.lang.Exception

class MainFragment : BaseRobotFragment() {

    companion object {
        private const val TAG = "MSI_MAIN_FRAGMENT"
    }

    private var animIsLaunch = false
    private var stopAnim = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity.currentChatData?.enableListeningAnimation(true)

        view.findViewById<FrameLayout>(R.id.protective_measure).setOnClickListener {
            showNoTouchDialog(false)
            noTouchDialog?.handler?.postDelayed({
                noTouchDialog?.dismiss()
                mainActivity.showFragment(MainActivity.ID_FRAGMENT_MESSAGE, "0")
            }, ImageDialog.TIME)
        }
        view.findViewById<FrameLayout>(R.id.wash_hand).setOnClickListener {
            showNoTouchDialog(false)
            noTouchDialog?.handler?.postDelayed({
                noTouchDialog?.dismiss()
                mainActivity.showFragment(MainActivity.ID_FRAGMENT_MESSAGE, "1")
            }, ImageDialog.TIME)
        }
        view.findViewById<FrameLayout>(R.id.mask).setOnClickListener {
            showNoTouchDialog(false)
            noTouchDialog?.handler?.postDelayed({
                noTouchDialog?.dismiss()
                mainActivity.showFragment(MainActivity.ID_FRAGMENT_MESSAGE, "2")
            }, ImageDialog.TIME)
        }
        view.findViewById<FrameLayout>(R.id.symptom).setOnClickListener {
            showNoTouchDialog(false)
            noTouchDialog?.handler?.postDelayed({
                noTouchDialog?.dismiss()
                mainActivity.showFragment(MainActivity.ID_FRAGMENT_MESSAGE, "3")
            }, ImageDialog.TIME)
        }
        mainActivity.goToBookmark("HELLO_" + (1..4).random(), "topic_covid")
        FutureUtils.futureOf {
            mainActivity.currentChatData?.chat?.addOnHearingChangedListener { hearing ->
                stopAnim = hearing.not()
                if (!animIsLaunch)
                    animationScaleUpDown(view.findViewById<ImageView>(R.id.micro), 2F).run()
            }
        }
    }

    private fun animationScaleUpDown(view : View, value : Float) : Runnable {
        animIsLaunch = true
        return Runnable {
            try {
            if (!stopAnim)
                view.animate().scaleX(value).scaleY(value).setDuration(500).withEndAction(animationScaleUpDown(view, if (value == 1F) 2F else 1F))
            else {
                if (value == 1F)
                    view.animate().scaleX(1F).scaleY(1F).setDuration(500)
                animIsLaunch = false
                }
            } catch (e: Exception) {
                animIsLaunch = false
                view.animate().scaleX(1F).scaleY(1F).setDuration(500)
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FutureUtils.futureOf {
            mainActivity.currentChatData?.chat?.removeAllOnHearingChangedListeners()
        }
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

}