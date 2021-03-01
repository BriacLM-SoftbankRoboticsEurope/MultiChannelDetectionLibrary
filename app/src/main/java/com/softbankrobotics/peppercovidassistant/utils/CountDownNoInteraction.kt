package com.softbankrobotics.peppercovidassistant.utils

import android.os.CountDownTimer
import android.util.Log
import com.softbankrobotics.peppercovidassistant.MainActivity

/*************************************************************************************************
 * This countdown orders the mainActivity
 * to display a default fragment when no interaction occurred for some time
 ************************************************************************************************/

class CountDownNoInteraction(private val mainActivity: MainActivity, millisUtilEnd: Long, countDownInterval: Long) : CountDownTimer(millisUtilEnd, countDownInterval)
{

    companion object {
        private const val TAG = "MSI_NoInteraction"
    }

    /**
     * On Tick (millis)
     * @param millisUntilFinished
     */
    override fun onTick(millisUntilFinished: Long) {
        //Nothing
    }

    /**
     * Timer has finished
     */
    override fun onFinish() {
        Log.d(TAG, "Timer Finished")
        this.mainActivity.showFragment(MainActivity.ID_FRAGMENT_SPLASH)
    }

    /**
     * Reset Timer
     */
    fun reset() {
        Log.d(TAG, "Timer Reset")
        cancel()
        start()
    }
}