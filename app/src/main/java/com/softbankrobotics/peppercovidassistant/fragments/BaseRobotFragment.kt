package com.softbankrobotics.peppercovidassistant.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.softbankrobotics.peppercovidassistant.MainActivity

/*************************************************************************************************
 * Base for all fragments
 ************************************************************************************************/

abstract class BaseRobotFragment:Fragment() {

        /**********************************Activity reference****************************************/

    protected lateinit var mainActivity: MainActivity       //Reference to the MainActivity

    /**********************************UI components*********************************************/

    protected lateinit var layout: View                     //Reference to the fragment's layout

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

        getFirstBookmark()?.let { bookmark ->
            getTopic()?.let { topic ->
                this.mainActivity.goToBookmark(bookmark, topic)
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