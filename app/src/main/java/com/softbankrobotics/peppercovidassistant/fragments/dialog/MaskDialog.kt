package com.softbankrobotics.peppercovidassistant.fragments.dialog

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.softbankrobotics.peppercovidassistant.R

class MaskDialog : AbstractDialog() {

    private var clickListener: View.OnClickListener? = null // Listener On Button Click
    private var image: ImageView? = null // Image Content View
    private var bitmap: Bitmap? = null // Picture to display

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        val input = inflater.inflate(R.layout.dialog_mask, container, false)

        val text = input.findViewById(R.id.text) as TextView
        val ok = input.findViewById(R.id.ok) as AppCompatButton
        image = input.findViewById(R.id.mask_detector_img) as ImageView

        image?.setImageBitmap(bitmap)

        text.text = getString(R.string.put_mask_on)
        ok.setOnClickListener(if (clickListener != null) clickListener else defaultClickListener())
        setContentLayout(input)
        return v
    }

    /**
     * @param positiveOnClickListener: Action to do on click
     */
    fun setOnClickListener(positiveOnClickListener: View.OnClickListener) {
        this.clickListener = positiveOnClickListener
    }

    /**
     * Set Picture
     * @param bitmap: Picture to show
     */
    fun setPicture(bitmap: Bitmap) {
        if (image != null)
            image?.setImageBitmap(bitmap)
        else
            this.bitmap = bitmap
    }
}
