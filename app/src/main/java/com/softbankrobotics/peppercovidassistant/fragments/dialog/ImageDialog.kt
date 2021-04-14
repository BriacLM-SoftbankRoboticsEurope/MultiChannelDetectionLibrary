package com.softbankrobotics.peppercovidassistant.fragments.dialog

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.softbankrobotics.peppercovidassistant.R

class ImageDialog : AbstractDialog() {

    private var clickListener: View.OnClickListener? = null // Listener On Button Click
    private var content: View? = null

    private var backgroundDrawable: Drawable? = null
    private var imageDrawable: Drawable? = null
    private var imageBitmap: Bitmap? = null
    private var text = ""

    var handler = Handler()
    var runnable : Runnable? = null

    companion object {
        const val TIME : Long = 5000
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)
        content = inflater.inflate(R.layout.dialog_image, container, false)

        if (imageBitmap != null)
            content?.findViewById<ImageView>(R.id.image)?.setImageBitmap(imageBitmap)
        if (imageDrawable != null)
            content?.findViewById<ImageView>(R.id.image)?.setImageDrawable(imageDrawable)
        if (backgroundDrawable != null)
            content?.findViewById<FrameLayout>(R.id.image_background)?.background = backgroundDrawable
        content?.findViewById<TextView>(R.id.text)?.text = text
        setContentLayout(content!!)
        setSkip(skipDialog, clickListener)
        return v
    }

    override fun dismiss() {
        if (runnable != null)
            handler.removeCallbacks(runnable!!)
        super.dismiss()
    }

    fun defaultDelayed() {
        runnable = Runnable {
            dismiss()
        }
        handler.postDelayed(runnable!!, TIME)
    }

    /**
     * @param positiveOnClickListener: Action to do on click
     */
    fun setOnClickListener(positiveOnClickListener: View.OnClickListener) {
        this.clickListener = positiveOnClickListener
    }

    fun setBackgroundDrawable(drawable: Drawable) {
        backgroundDrawable = drawable
        if (content != null)
            content?.findViewById<FrameLayout>(R.id.image_background)?.background = backgroundDrawable
    }

    fun setText(newText: String) {
        text = newText
        if (content != null)
            content?.findViewById<TextView>(R.id.text)?.text = text
    }

    fun setImage(img: Bitmap) {
        imageBitmap = img
        if (content != null)
            content?.findViewById<ImageView>(R.id.image)?.setImageBitmap(imageBitmap)
    }

    fun setImage(img: Drawable) {
        imageDrawable = img
        if (content != null)
            content?.findViewById<ImageView>(R.id.image)?.setImageDrawable(imageDrawable)
    }
}
