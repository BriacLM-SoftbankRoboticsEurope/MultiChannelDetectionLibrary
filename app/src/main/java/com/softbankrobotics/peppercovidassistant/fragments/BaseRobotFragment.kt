package com.softbankrobotics.peppercovidassistant.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.softbankrobotics.multichanneldetectionlibrary.MultiChannelDetection
import com.softbankrobotics.peppercovidassistant.MainActivity
import com.softbankrobotics.peppercovidassistant.R
import com.softbankrobotics.peppercovidassistant.fragments.dialog.AbstractDialog
import com.softbankrobotics.peppercovidassistant.fragments.dialog.ImageDialog
import kotlinx.android.synthetic.main.dialog_image.*

/*************************************************************************************************
 * Base for all fragments
 ************************************************************************************************/

abstract class BaseRobotFragment:Fragment() {

        /**********************************Activity reference****************************************/

    protected lateinit var mainActivity: MainActivity       //Reference to the MainActivity

    /**********************************UI components*********************************************/

    protected lateinit var layout: View                     //Reference to the fragment's layout

    /**********************************InfoDialog*************************************************/
    var noMaskDialog : ImageDialog? = null
    var noTouchDialog : ImageDialog? = null

    /**********************************Fragment life cycle***************************************/

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mainActivity=activity as MainActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.layout=inflater.inflate(getLayoutId(), container, false)
        return this.layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*As soon as the view is ready, calls the first bookmark if not null*/
        layout.setOnClickListener {
            showNoTouchDialog(true)
        }
        getFirstBookmark()?.let { bookmark ->
            getTopic()?.let { topic ->
                this.mainActivity.goToBookmark(bookmark, topic)
            }
        }
    }

    fun showNoTouchDialog(useDefaultDelayed: Boolean) {
        noTouchDialog = ImageDialog()
        noTouchDialog?.setBackgroundDrawable(resources.getDrawable(R.drawable.circle))
        noTouchDialog?.setImage(resources.getDrawable(R.drawable.ic_no_touch))
        noTouchDialog?.isClosable = false
        noTouchDialog?.setText(resources.getString(R.string.no_touch))
        noTouchDialog?.skipDialog = false
        noTouchDialog?.show(
            mainActivity.fragmentManager,
            mainActivity.getString(R.string.warning)
        )
        if (useDefaultDelayed)
            noTouchDialog?.defaultDelayed()
    }

    /***************************
     * MASK DETECTOR
     **************************/
    /**
     * @param faces: List of faces detected by the library FaceMaskDetection
     */
    fun maskDetector(faces: List<MultiChannelDetection.FaceDetected>) {
        if (faces.isNotEmpty() && !mainActivity.skipMaskDetection) {
            if (faces[0].hasMask) {
                if (noMaskDialog != null && noMaskDialog?.isVisible!!)
                    noMaskDialog?.dismiss()
            } else {
                if (noMaskDialog == null || !noMaskDialog?.isVisible!!) {
                    noMaskDialog = ImageDialog()
                    noMaskDialog?.setText(resources.getString(R.string.put_mask_on))
                    noMaskDialog?.setImage(faces[0].picture)
                    noMaskDialog?.setOnClickListener {
                        mainActivity.skipMaskDetection = true
                        noMaskDialog?.dismiss()
                    }
                    noMaskDialog?.show(mainActivity.fragmentManager, mainActivity.getString(R.string.warning))
                } else {
                    noMaskDialog?.setImage(faces[0].picture)
                }
            }
        }
    }

    /***********************************UI monitoring*******************************************/

    /**
     * @return Reference to the layout to display
     */
    abstract fun getLayoutId():Int

    /**********************************Chat monitoring******************************************/

    /**
     * @return String reference to the chat topic
     */
    abstract fun getTopic():String?

    /**
     * @return String reference to the first bookmark to each when the fragment starts
     */
    abstract fun getFirstBookmark():String?

    //abstract fun handleOnBookmarkReached(bookmark:Bookmark?)    //Defines what to do when a bookmark is reached
}