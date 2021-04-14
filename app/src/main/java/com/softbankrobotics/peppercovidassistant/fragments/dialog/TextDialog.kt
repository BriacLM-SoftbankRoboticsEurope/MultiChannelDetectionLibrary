package com.softbankrobotics.peppercovidassistant.fragments.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.softbankrobotics.peppercovidassistant.R

class TextDialog : AbstractDialog() {

    private var text = "DEFAULT VALUE" // Default information value
    private var clickListener: View.OnClickListener? = null // Listener On Button Click

    /**
     * Generate content
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        val input = inflater.inflate(R.layout.dialog_information, container, false)
        val text = input.findViewById(R.id.text) as TextView
        text.text = this.text
        setContentLayout(input)
        setSkip(skipDialog, clickListener)
        return v
    }

    /**
     * @param text: Information to show
     */
    fun setText(text: String) {
        this.text = text
    }

    /**
     * @param positiveOnClickListener: Action to do on click
     */
    fun setOnClickListener(positiveOnClickListener: View.OnClickListener) {
        this.clickListener = positiveOnClickListener
    }
}
