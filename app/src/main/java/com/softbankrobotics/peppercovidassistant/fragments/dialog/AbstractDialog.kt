package com.softbankrobotics.peppercovidassistant.fragments.dialog

import android.app.DialogFragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.softbankrobotics.peppercovidassistant.R

abstract class AbstractDialog : DialogFragment() {

    private var content: FrameLayout? = null // Dialog Content View
    private var dialogView: View? = null // Dialog Content View
    var isClosable : Boolean = true
    var skipDialog : Boolean = true

    /**
     * Set the dialog style
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.translucent)
    }

    /**
     * Set the dialog Flag
     */
    override fun onActivityCreated(arg0: Bundle?) {
        super.onActivityCreated(arg0)
        val window = dialog.window ?: return
        val windowParams = window.attributes
        windowParams.dimAmount = 0.90f
        windowParams.flags = windowParams.flags or WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW
        window.attributes = windowParams
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        activity?.actionBar?.hide()
    }

    /**
     * Initialize the dialog
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialogView = inflater.inflate(R.layout.dialog_fragment, container, false)
        content = dialogView?.findViewById(R.id.content)
        if (isClosable)
            dialogView?.findViewById<ImageView>(R.id.close)?.setOnClickListener(defaultClickListener())
        else
            dialogView?.findViewById<ImageView>(R.id.close)?.visibility = View.GONE
        return dialogView
    }

    fun setSkip(skip: Boolean, clickListener: View.OnClickListener?) {
        val skipText = dialogView?.findViewById<TextView>(R.id.skip)
        if (skip) {
            skipText?.visibility = View.VISIBLE
            skipText?.setOnClickListener(clickListener)
        } else {
            skipText?.visibility = View.GONE
        }
    }

    /**
     * Modify the content view
     * @param view: New view
     */
    internal fun setContentLayout(view: View) {
        content!!.removeAllViews()
        content!!.addView(view)
    }

    /**
     * Default Click listener to dismiss the dialog
     *
     * @return Default Listener to hide the dialog
     */
    fun defaultClickListener(): View.OnClickListener {
        return View.OnClickListener { dismiss() }
    }
}
