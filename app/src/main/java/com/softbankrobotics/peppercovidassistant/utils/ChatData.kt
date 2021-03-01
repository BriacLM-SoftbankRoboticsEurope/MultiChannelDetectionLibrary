package com.softbankrobotics.peppercovidassistant.utils

import android.app.Activity
import android.util.Log
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.conversation.*
import com.aldebaran.qi.sdk.`object`.locale.Language
import com.aldebaran.qi.sdk.`object`.locale.Region
import com.aldebaran.qi.sdk.builder.ListenBuilder
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder
import com.aldebaran.qi.sdk.builder.TopicBuilder
import com.aldebaran.qi.sdk.conversationalcontentlibrary.base.AbstractConversationalContent
import com.aldebaran.qi.sdk.conversationalcontentlibrary.base.ConversationalContentChatBuilder
import com.softbankrobotics.peppercovidassistant.R
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.HashMap


/**
 * Builds the main components of a QiChat, including a Chat, qiChatBot, the topics statues
 * and sets up for the Bookmarks.
 *
 * @param activity   the main activity where ChatData is built
 * @param qiContext  the activity qiContext
 * @param locale     the android locale you want to build the chat in
 * @param topicNames the names of the .top file you want to use in this chat bot
 * @param conversationContents : the conversationalContents
 * @param buildChat  if you are running the chat in the same activity that builds this object
 *
 * this should be set to true, otherwise set it to false and use startChat
 */

class ChatData(
    private val activity: Activity,
    private val qiContext: QiContext,
    private val locale: Locale,
    private val topicNames: List<String>,
    private val conversationContents: List<AbstractConversationalContent> = listOf(),
    private val buildChat: Boolean?
)
{
    private val topics: MutableMap<String, Topic>
    val qiChatbot: QiChatbot
    lateinit var chat: Chat
    private val bookmarks: MutableMap<String, Map<String, Bookmark>>
    private val qiLocale: com.aldebaran.qi.sdk.`object`.locale.Locale
    private var currentGotoBookmarkFuture: Future<Void>? = null

    init {
        // Change locale for the time being, in order to get the right resource
        val res = activity.resources
        val config = res.configuration
        var previousLocale: Locale? = null // needed if several languages are used.

        topics = mutableMapOf()

        if (config.locale !== locale) {
            previousLocale = config.locale
            config.setLocale(locale)
            res.updateConfiguration(config, res.displayMetrics)
        }

        for (topicName in topicNames) {
            Log.d(TAG, "adding $topicName to topic list")
            topics[topicName]= TopicBuilder.with(qiContext).withResource(getResId(topicName, R.raw::class.java)).build()
        }
        qiLocale = getQiLocale(locale)

        qiChatbot = QiChatbotBuilder.with(qiContext)
            .withTopics(topics.values.toList())
            .withLocale(qiLocale)
            .build()

        this.topics.values.forEach { topic ->
            this.qiChatbot.topicStatus(topic).enabled=true
        }

        if (buildChat!!) {
            setupChat(qiContext)
        }

        bookmarks = HashMap()
        for (t in qiChatbot.topics) {
            bookmarks[t.name] = t.bookmarks
        }

        if (previousLocale != null) {
            config.setLocale(previousLocale)
            res.updateConfiguration(config, res.displayMetrics)
        }
    }

    /**
     * sets up the chat, this function is separated from the constructor since you might want to
     * build ChatData in an activity that will not run the chat,
     * you can then build the chat in the activity where it is needed.
     *
     * @param qiContext the qiContext of the activity that is going to run the chat
     */

    fun setupChat(qiContext: QiContext) {
        //ChatBuilder.with(qiContext)
        chat = ConversationalContentChatBuilder.with(qiContext)
            .withChatbot(qiChatbot)
            .withLocale(qiLocale)
            .withConversationalContents(this.conversationContents)
            .build()
    }

    /**
     * Adds a listener catching the event occurred when the chat starts
     *
     * @param onStartedListener : the listener
     */

    fun setupOnStartedListener(onStartedListener: Chat.OnStartedListener?){
        if(onStartedListener!=null) {
            this.chat.addOnStartedListener(onStartedListener)
        }
    }

    /**
     * Adds a listener catching the event occurred when a bookmark is reached
     *
     * @param onBookmarkReachedListener : the listener
     */

    fun setupOnBookmarkReachedListener(onBookmarkReachedListener: QiChatbot.OnBookmarkReachedListener?){
        if(onBookmarkReachedListener!=null) {
            this.qiChatbot.addOnBookmarkReachedListener(onBookmarkReachedListener)
        }
    }

    /**
     * sets up the QiChatExecutors for this chat
     *
     * @param executors a map containing the names of the executors and the classes it is used with
     */
    fun setupExecutors(executors: Map<String, QiChatExecutor>) {
        qiChatbot.executors = executors
    }

    /**
     * Goes to the specified bookmark in the specified topic already enabled)
     *
     * @param bookmark the name of the bookmark
     * @param topic    the name of the topic
     */
    fun goToBookmark(bookmark: String, topic: String) {
        if (bookmark.isEmpty()) {
            Log.w(TAG, "bookmark cannot be empty")
            return
        }

        val tmp = bookmarks[topic]
        if (tmp == null) {
            Log.w(TAG, String.format("Could not find topic %s", topic))
            return
        }

        Executors.newSingleThreadExecutor().execute {

            if (topic != this.qiChatbot.focusedTopic?.name?:"") {
                if (this.qiChatbot.focusedTopic != null) {
                    Log.d(TAG, "Disables topic ${this.qiChatbot.focusedTopic.name}")
                    this.qiChatbot.topicStatus(this.qiChatbot.focusedTopic).enabled = true
                }
                Log.d(TAG, "Enables topic $topic")
                this.qiChatbot.topicStatus(this.topics[topic]).enabled = true
            }else{
                Log.d(TAG, "Enables topic $topic")
                this.qiChatbot.topicStatus(this.topics[topic]).enabled = true
            }

            Log.d(TAG, "going to bookmark $bookmark in topic : $topic")
            cancelCurrentGotoBookmarkFuture().thenConsume { uselessFuture ->
                if(uselessFuture.hasError()) Log.d(TAG, uselessFuture.errorMessage)
                currentGotoBookmarkFuture = qiChatbot.async().goToBookmark(
                    tmp[bookmark],
                    AutonomousReactionImportance.HIGH,
                    AutonomousReactionValidity.IMMEDIATE
                )
            }
        }
    }

    fun cancelCurrentGotoBookmarkFuture(): Future<Void> {
        if (currentGotoBookmarkFuture == null) return Future.of(null)
        //currentGotoBookmarkFuture!!.cancel(true)
        currentGotoBookmarkFuture!!.requestCancellation()
        return currentGotoBookmarkFuture!!
    }

    fun onRobotFocusLost() {
        try {
            qiChatbot.removeAllOnEndedListeners()
        } catch (ex: Exception) {
            Log.d(TAG, "onRobotFocusLost: $ex")
        }

        try {
            chat.removeAllOnStartedListeners()
        } catch (ex: Exception) {
            Log.d(TAG, "onRobotFocusLost: $ex")
        }

        try {
            qiChatbot.removeAllOnBookmarkReachedListeners()
        } catch (ex: Exception) {
            Log.d(TAG, "onRobotFocusLost: $ex")
        }

        try {
            chat.removeAllOnNormalReplyFoundForListeners()
        } catch (ex: Exception) {
            Log.d(TAG, "onRobotFocusLost: $ex")
        }
    }

    fun enableListeningAnimation(enableListeningAnimation: Boolean) {
        if (enableListeningAnimation)
            chat.async().setListeningBodyLanguage(BodyLanguageOption.NEUTRAL)
        else
            chat.async().setListeningBodyLanguage(BodyLanguageOption.DISABLED)
    }

    fun runChat(): Future<Void> {
        return chat.async().run()
    }

    companion object {

        private const val TAG = "ChatData"

        /**
         * gets the resource ID thanks to its name and Class
         *
         * @param resName the resource name, as it appears in android studio
         * @param c       the class where this resource is located, for example if your file is a drawable
         * this should be R.drawable.class
         * @return the resource ID
         */
        private fun getResId(resName: String, c: Class<*>): Int {
            return try {
                val idField = c.getDeclaredField(resName)
                idField.getInt(idField)
            } catch (e: Exception) {
                e.printStackTrace()
                -1
            }

        }

        /**
         * Translates the android Locale to a qiLocale if available, otherwise will return en_US
         *
         * @param locale the android locale
         * @return the qiLocale
         */
        private fun getQiLocale(locale: Locale): com.aldebaran.qi.sdk.`object`.locale.Locale {
            val qiLocale: com.aldebaran.qi.sdk.`object`.locale.Locale
            val strLocale = locale.toString()
            Log.d(TAG, "Lang : $strLocale")
            when {
                strLocale.contains("fr") -> {
                    qiLocale = com.aldebaran.qi.sdk.`object`.locale.Locale(Language.FRENCH, Region.FRANCE)
                }
                strLocale.contains("zh") -> {
                    qiLocale = if (strLocale == "zh_CN") {
                        com.aldebaran.qi.sdk.`object`.locale.Locale(Language.CHINESE, Region.CHINA)
                    } else {
                        com.aldebaran.qi.sdk.`object`.locale.Locale(Language.CHINESE, Region.TAIWAN)
                    }
                }
                strLocale.contains("en") -> {
                    qiLocale = com.aldebaran.qi.sdk.`object`.locale.Locale(Language.ENGLISH, Region.UNITED_STATES)
                }
                strLocale.contains("ar") -> {
                    qiLocale = com.aldebaran.qi.sdk.`object`.locale.Locale(Language.ARABIC, Region.EGYPT)
                }
                strLocale.contains("da") -> {
                    qiLocale = com.aldebaran.qi.sdk.`object`.locale.Locale(Language.DANISH, Region.DENMARK)
                }
                strLocale.contains("nl") -> {
                    qiLocale = com.aldebaran.qi.sdk.`object`.locale.Locale(Language.DUTCH, Region.NETHERLANDS)
                }
                strLocale.contains("fi") -> {
                    qiLocale = com.aldebaran.qi.sdk.`object`.locale.Locale(Language.FINNISH, Region.FINLAND)
                }
                strLocale.contains("de") -> {
                    qiLocale = com.aldebaran.qi.sdk.`object`.locale.Locale(Language.GERMAN, Region.GERMANY)
                }
                strLocale.contains("it") -> {
                    qiLocale = com.aldebaran.qi.sdk.`object`.locale.Locale(Language.ITALIAN, Region.ITALY)
                }
                strLocale.contains("ja") -> {
                    qiLocale = com.aldebaran.qi.sdk.`object`.locale.Locale(Language.JAPANESE, Region.JAPAN)
                }
                strLocale.contains("nb") -> {
                    qiLocale = com.aldebaran.qi.sdk.`object`.locale.Locale(Language.NORWEGIAN_BOKMAL, Region.NORWAY)
                }
                strLocale.contains("es") -> {
                    qiLocale = com.aldebaran.qi.sdk.`object`.locale.Locale(Language.SPANISH, Region.SPAIN)
                }
                strLocale.contains("sv") -> {
                    qiLocale = com.aldebaran.qi.sdk.`object`.locale.Locale(Language.SWEDISH, Region.SWEDEN)
                }
                strLocale.contains("tr") -> {
                    qiLocale = com.aldebaran.qi.sdk.`object`.locale.Locale(Language.TURKISH, Region.TURKEY)
                }
                strLocale.contains("cs") -> {
                    qiLocale = com.aldebaran.qi.sdk.`object`.locale.Locale(Language.CZECH, Region.CZECH_REPUBLIC)
                }
                strLocale.contains("pl") -> {
                    qiLocale = com.aldebaran.qi.sdk.`object`.locale.Locale(Language.POLISH, Region.POLAND)
                }
                strLocale.contains("sk") -> {
                    qiLocale = com.aldebaran.qi.sdk.`object`.locale.Locale(Language.SLOVAK, Region.SLOVAKIA)
                }
                strLocale.contains("el") -> {
                    qiLocale = com.aldebaran.qi.sdk.`object`.locale.Locale(Language.GREEK, Region.GREECE)
                }
                strLocale.contains("ko") -> {
                    qiLocale = com.aldebaran.qi.sdk.`object`.locale.Locale(Language.KOREAN, Region.REPUBLIC_OF_KOREA)
                }
                strLocale.contains("hu") -> {
                    qiLocale = com.aldebaran.qi.sdk.`object`.locale.Locale(Language.HUNGARIAN, Region.HUNGARY)
                }
                else -> {
                    qiLocale = com.aldebaran.qi.sdk.`object`.locale.Locale(Language.ENGLISH, Region.UNITED_STATES)
                }
            }
            return qiLocale
        }

        /**
         * Checks that a language is available on the robot
         * @param qiContext : the qiContext
         * @param locale : the locale to be checked
         * @return true if the language is available, false otherwise
         */

        fun isLanguageAvailable(qiContext:QiContext, locale: Locale):Boolean{
            val qiLocale= getQiLocale(locale)
            return try {
                val phraseSet = PhraseSetBuilder.with(qiContext).withTexts("1", "2").build()
                ListenBuilder.with(qiContext).withLocale(qiLocale).withPhraseSet(phraseSet).build()
                true
            } catch (e: Exception) {
                Log.w(TAG, String.format("Could not build LISTEN in %s: %s", locale, e))
                false
            }
        }
    }
}