package com.softbankrobotics.peppercovidassistant

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.QiSDK
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks
import com.aldebaran.qi.sdk.`object`.conversation.Bookmark
import com.aldebaran.qi.sdk.`object`.conversation.Chat
import com.aldebaran.qi.sdk.`object`.conversation.QiChatExecutor
import com.aldebaran.qi.sdk.`object`.conversation.QiChatbot
import com.aldebaran.qi.sdk.`object`.human.Human
import com.aldebaran.qi.sdk.conversationalcontentlibrary.askrobotname.AskRobotNameConversationalContent
import com.aldebaran.qi.sdk.conversationalcontentlibrary.greetings.GreetingsConversationalContent
import com.aldebaran.qi.sdk.conversationalcontentlibrary.repeat.RepeatConversationalContent
import com.aldebaran.qi.sdk.conversationalcontentlibrary.robotabilities.RobotAbilitiesConversationalContent
import com.aldebaran.qi.sdk.conversationalcontentlibrary.volumecontrol.VolumeControlConversationalContent
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayPosition
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy
import com.softbankrobotics.multichanneldetectionlibrary.MultiChannelDetection
import com.softbankrobotics.multichanneldetectionlibrary.MultiChannelDetectionCallbacks
import com.softbankrobotics.peppercovidassistant.executors.ActionExecutor
import com.softbankrobotics.peppercovidassistant.executors.CovidExecutor
import com.softbankrobotics.peppercovidassistant.executors.LanguageExecutor
import com.softbankrobotics.peppercovidassistant.fragments.BaseRobotFragment
import com.softbankrobotics.peppercovidassistant.fragments.MainFragment
import com.softbankrobotics.peppercovidassistant.fragments.SplashFragment
import com.softbankrobotics.peppercovidassistant.fragments.dialog.InformationDialog
import com.softbankrobotics.peppercovidassistant.utils.ChatData
import com.softbankrobotics.peppercovidassistant.utils.CountDownNoInteraction
import java.util.*
import kotlin.collections.HashMap

// RobotActivity : Pepper App
// RobotLifecycleCallbacks : Pepper App
// Chat.OnStartedListener : Dynamic chat and conversation
// QiChatbot.OnBookmarkReachedListener : Bookmark message to say
// HumanAwarenessUtils : Multi Human Detection*
class MainActivity : RobotActivity(), RobotLifecycleCallbacks, Chat.OnStartedListener, QiChatbot.OnBookmarkReachedListener, MultiChannelDetectionCallbacks {

    companion object {
        private const val TAG = "MSI_MAIN_ACTIVITY"
        const val ID_FRAGMENT_SPLASH = 1
        const val ID_FRAGMENT_MAIN = 2
        private const val KEY_REQUEST_PERMISSION=102
    }

    /***********************************UI components*******************************************/
    var currentFragmentId = -1      //The current fragment id to display
    var fragment: BaseRobotFragment?=null           //The current displayed fragment

    /************************************Robot items********************************************/
    private var itemBuilt : Boolean = false
    var qiContext:QiContext?=null
    private var chatDataList:HashMap<String, ChatData> = hashMapOf()    //HashMap with chat data (key : the locale string, entry : the chat data)
    var currentChatData: ChatData?=null                                 //The current running chat data
    private var runningChat: Future<Void>?=null                         //The current running chat
    private val executors: MutableMap<String, QiChatExecutor> = HashMap()

    /**********************************HumanAwareness********************************************/
    override var context: RobotActivity = this
    var multiChannelDetection: MultiChannelDetection? = null
    var informationDialog : InformationDialog? = null

    /**********************************Countdown*************************************************/
    private val countDownNoInteraction = CountDownNoInteraction(this, 120000, 10000) //10000, 1000

    /**********************************LANG******************************************************/
    var res : Resources? = null
    var config : Configuration? = null

    /**********************************READY*****************************************************/
    var chatIsReady = false
    private var message : HashMap<String, Boolean> = HashMap()

    /************************************Activity life cycle*************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        this.multiChannelDetection = MultiChannelDetection(this)

        super.onCreate(savedInstanceState)

        res = resources
        config = res?.configuration

        // Initialize message
        message[getString(R.string.message_mapping)] = false
        message[getString(R.string.message_localizing)] = false
        message[getString(R.string.message_build_chat)] = false
        message[getString(R.string.loading_message)] = false

        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE)
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.BOTTOM)
        hideStatusBar()

        setContentView(R.layout.activity_main)

        lang()

        requestCamAndWritePermission()
        QiSDK.register(this, this)
        showFragment(ID_FRAGMENT_SPLASH)
    }

    public override fun onResume() {
        super.onResume()

        if (this.multiChannelDetection != null) {
            this.multiChannelDetection?.onResume(this)
            this.multiChannelDetection?.isRobotReady = false
            this.chatIsReady = false
        }

        res = resources
        config = res?.configuration

        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE)
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.BOTTOM)

        hideStatusBar()
        showFragment(ID_FRAGMENT_SPLASH)
    }

    override fun onPause() {
        this.countDownNoInteraction.cancel()
        super.onPause()
    }

    override fun onDestroy() {
        this.countDownNoInteraction.cancel()
        QiSDK.unregister(this, this)
        super.onDestroy()
    }

    /**
     * Go to specific bookmark
     * @param bookmark
     * @param topic
     */
    fun goToBookmark(bookmark: String, topic: String) {
        this.currentChatData?.goToBookmark(bookmark, topic)
    }

    override fun onBookmarkReached(bookmark: Bookmark?) {}

    /**
     * Shows a fragment
     * @param fragmentId : the id of the fragment to display
     */
    fun showFragment(fragmentId:Int) {
        when(fragmentId){
            ID_FRAGMENT_SPLASH -> this.fragment = SplashFragment()
            ID_FRAGMENT_MAIN -> this.fragment = MainFragment()
            else -> this.fragment = SplashFragment()
        }
        this.fragment?.let { currentFragment ->
            this.currentFragmentId = fragmentId
            supportFragmentManager.beginTransaction().replace(R.id.activity_main_fragment, currentFragment).commitAllowingStateLoss()
        }
    }

    /**
     * Hide status bar and bottom bar
     */
    private fun hideStatusBar() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        actionBar?.hide()
    }

    /******************************User interaction**********************************************/
    override fun onUserInteraction() {
        if (this.chatIsReady && this.multiChannelDetection != null && this.multiChannelDetection?.isRobotReady!!)
            this.countDownNoInteraction.reset()
    }

    /************************************Robot life cycle***************************************/
    override fun onRobotFocusGained(qiContext: QiContext?) {
        Log.d(TAG, "Robot focus gained")
        this.qiContext=qiContext

        // INIT HUMAN AWARENESS
        if (qiContext != null) {
            // INIT CHAT
            defineCurrentLocale()
            buildChat()

            if (this.multiChannelDetection == null)
                this.multiChannelDetection = MultiChannelDetection(this)
            // USE HEAD CAMERA INSTEAD OF TABLET
            this.multiChannelDetection?.useHeadCamera = true
            // HOLD BASE IF NOT ENGAGE
            this.multiChannelDetection?.holdBase = true
            // TURN TO INITIAL POSITION
            this.multiChannelDetection?.turnToInitialPosition = true
            // START
            this.multiChannelDetection?.onRobotFocusGained(qiContext)
        }
    }

    override fun onRobotFocusLost() {
        Log.d(TAG, "Robot focus lost")
        if (this.multiChannelDetection != null)
            this.multiChannelDetection?.onRobotFocusLost()
        try {
            currentChatData!!.chat.removeAllOnStartedListeners()
            currentChatData!!.chat.removeAllOnNormalReplyFoundForListeners()
            currentChatData!!.onRobotFocusLost()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        qiContext = null
    }

    override fun onRobotFocusRefused(reason: String?) {
        Log.d(TAG, "Robot focus refused : $reason")
    }

    /********************************Human Detection******************************************/
    /**
     * Human Detected -> Go to the main fragment
     */
    private fun awake() {
        if (chatIsReady && this.multiChannelDetection != null && this.multiChannelDetection?.isRobotReady!!) {
          if (currentFragmentId == ID_FRAGMENT_SPLASH)
              showFragment(ID_FRAGMENT_MAIN)
          this.countDownNoInteraction.reset()
        }
    }

    /**
     * Robot is Ready to engage : see HumanDetection
     * READY WHEN PEPPER LOCALIZE, MAP AND CHAT DONE
     */
    override fun onRobotReady(isRobotReady: Boolean) {
        if (isRobotReady) {
            if (currentFragmentId == ID_FRAGMENT_SPLASH) {
                runOnUiThread {
                    (fragment as SplashFragment).setReady(multiChannelDetection?.isRobotReady!! && chatIsReady)
                    this.countDownNoInteraction.start()
                }
                currentChatData?.enableListeningAnimation(false)
            }
        } else {
            this.showFragment(ID_FRAGMENT_SPLASH)
        }
    }

    /**
     * @param step: Index of the step
     * @param finished: State of the step
     */
    override fun onStepReach(step: Int, finished: Boolean) {
        if (this.currentFragmentId == ID_FRAGMENT_SPLASH) {
            message[message.keys.elementAt(step)] = finished
            if (finished) {
                message.forEach { (key, value) ->
                    if (!value) {
                        (this.fragment as SplashFragment).setLoadingMessage(key)
                        return
                    }
                }
            } else
                (this.fragment as SplashFragment).setLoadingMessage(message.keys.elementAt(step))
        }
    }

    /**
     * Listener Charging Flap State Changed
     * @param open: Boolean -> state Open/Close
     */
    override fun onChargingFlapStateChanged(open: Boolean) {
        if (open && qiContext != null) {
            showFragment(ID_FRAGMENT_SPLASH)
            informationDialog = InformationDialog()
            informationDialog?.setText(context.getString(R.string.close_charging_flap))
            informationDialog?.setOnClickListener {
                this.multiChannelDetection?.cancelMappingAndLocalize()
                this.multiChannelDetection?.ready()
                informationDialog?.dismiss()
            }
            if (!informationDialog?.isVisible!!)
                informationDialog?.show(context.fragmentManager, context.getString(R.string.warning))
        } else if (!open && qiContext != null && informationDialog != null && informationDialog?.isVisible!!)
            informationDialog?.dismiss()
    }

    /**
     * @param human: Nullable -> Human detected by the normal function of the robot
     * @param faces: Nullable -> Human detected by the library FaceMaskDetection
     */
    override fun onHumanDetected(human: Human?, faces: List<MultiChannelDetection.FaceDetected>?) {
        if (human != null)
            awake()
        if (faces != null){
            if (faces.isNotEmpty())
                awake()
            if (currentFragmentId == ID_FRAGMENT_MAIN)
                runOnUiThread { (fragment as MainFragment).maskDetector(faces) }
        }
    }

    /********************************Chat management*********************************************/
    /**
     * Builds the chat with the current locale
     */
    private fun buildChat(){
        if (!itemBuilt) {
            Log.d(TAG, "Build chat")
            onStepReach(2, false)

            val topicsNamesFR = listOf("topic_small_talk", "concepts", "topic_covid")
            val topicsNames = listOf("concepts", "topic_covid")

            val conversationalContents = listOf(
                    GreetingsConversationalContent(),
                    AskRobotNameConversationalContent(),
                    RobotAbilitiesConversationalContent(),
                    RepeatConversationalContent(),
                    VolumeControlConversationalContent()
            )

            if (qiContext != null) {
                executors["ActionExecutor"] = ActionExecutor(qiContext!!, this)
                executors["LanguageExecutor"] = LanguageExecutor(qiContext!!, this)
                executors["CovidExecutor"] = CovidExecutor(qiContext!!, this)
            }

            this.chatDataList["en"] = ChatData(this, this.qiContext!!, Locale.ENGLISH, topicsNames, conversationalContents, true)
            this.chatDataList["fr"] = ChatData(this, this.qiContext!!, Locale.FRENCH, topicsNamesFR, conversationalContents, true)
            itemBuilt = true
        } else {
            if (qiContext != null) {
                this.chatDataList["en"]?.setupChat(qiContext!!)
                this.chatDataList["fr"]?.setupChat(qiContext!!)
            }
        }
        defineCurrentLocale()
        selectChatData()

        this.runningChat = this.currentChatData?.runChat()
        onStepReach(2, true)
    }

    /**
     * Defines the current locale to set
     */
    private fun defineCurrentLocale(){
        if (this.qiContext != null && !ChatData.isLanguageAvailable(this.qiContext!!, config?.locale!!)) {
            resources.configuration.setLocale(Locale.ENGLISH)
            resources.updateConfiguration(resources.configuration, resources.displayMetrics)
        }
    }

    /**
     * Selects the chat data matching the correct locale
     */
    private fun selectChatData(){
        when (config?.locale?.language) {
            "en" -> this.currentChatData = this.chatDataList["en"]
            "fr" -> this.currentChatData = this.chatDataList["fr"]
            else -> this.currentChatData = this.chatDataList["en"]
        }
        this.currentChatData?.setupOnStartedListener(this)
        this.currentChatData?.setupOnBookmarkReachedListener(this)
        this.currentChatData!!.chat.async().addOnNormalReplyFoundForListener {
            awake()
        }
        this.currentChatData?.setupExecutors(executors)
    }

    /**
     * On chat started
     */
    override fun onStarted() {
        chatIsReady = true
        onRobotReady(true)
    }

    /******************************Permissions management****************************************/

    private fun requestCamAndWritePermission() {
        if (!permissionAlreadyGranted())
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), KEY_REQUEST_PERMISSION)
    }

    private fun permissionAlreadyGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when(requestCode){
            KEY_REQUEST_PERMISSION -> if (grantResults.isNotEmpty()) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, R.string.message_camera_permission, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /*************************************** LANG **************************************/
    /**
     * Update the lang of both Dialog and UI
     * @param lang: Lang
     */
    fun setLocale(lang: String) {
        if (config?.locale?.country == lang)
            return
        runOnUiThread { this.showFragment(ID_FRAGMENT_SPLASH) }
        setUIWithLocale(lang)
        chatIsReady = false
        if (runningChat != null) {
            runningChat?.requestCancellation()
            runningChat?.thenConsume {
                currentChatData!!.chat.async().removeAllOnStartedListeners()
                currentChatData!!.chat.async().removeAllOnNormalReplyFoundForListeners()
                currentChatData!!.qiChatbot.removeAllOnBookmarkReachedListeners()
                currentChatData!!.cancelCurrentGotoBookmarkFuture()
                selectChatData()
                this.runningChat = this.currentChatData?.runChat()
            }
        } else
            this.runningChat = this.currentChatData?.runChat()
        lang()
    }

    /**
     * Update the current UI With new local
     * @param strLocale: Lang
     */
    private fun setUIWithLocale(strLocale: String) {
        val locale = Locale(strLocale)
        config!!.setLocale(locale)
        res!!.updateConfiguration(config, res!!.displayMetrics)
    }

    /**
     * Initialize the interaction to update lang
     */
    private fun lang() {
        val languageContainer = findViewById<View>(R.id.view_lang_container)
        languageContainer.visibility = View.INVISIBLE
        languageContainer.isClickable = false

        val buttonLanguage = findViewById<ImageButton>(R.id.button_language)
        val buttonLang1 = findViewById<ImageButton>(R.id.button_lang_1)
        val buttonLang2 = findViewById<ImageButton>(R.id.button_lang_2)

        buttonLanguage.setOnClickListener { v: View? ->
            if (languageContainer.isClickable) {
                languageContainer.visibility = View.INVISIBLE
                languageContainer.isClickable = false
            } else {
                languageContainer.visibility = View.VISIBLE
                languageContainer.isClickable = true
            }
        }
        buttonLang1.setOnClickListener { v: View? ->
            if (itemBuilt)
                setLocale("fr")
        }
        buttonLang2.setOnClickListener { v: View? ->
            if (itemBuilt)
                setLocale("en")
        }
        chosenLang()
    }

    private fun chosenLang() {
        val languageContainer = findViewById<View>(R.id.view_lang_container)

        findViewById<ImageButton>(R.id.button_language).setImageResource(if (config?.locale?.language == "fr") R.drawable.lang_fr else R.drawable.lang_uk)
        languageContainer.visibility = View.INVISIBLE
        languageContainer.isClickable = false
    }
}