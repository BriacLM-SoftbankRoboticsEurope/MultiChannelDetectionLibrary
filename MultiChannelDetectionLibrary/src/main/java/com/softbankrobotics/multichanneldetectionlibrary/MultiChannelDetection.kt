package com.softbankrobotics.multichanneldetectionlibrary


import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat
import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.actuation.*
import com.aldebaran.qi.sdk.`object`.autonomousabilities.DegreeOfFreedom
import com.aldebaran.qi.sdk.`object`.holder.AutonomousAbilitiesType
import com.aldebaran.qi.sdk.`object`.holder.Holder
import com.aldebaran.qi.sdk.`object`.human.Human
import com.aldebaran.qi.sdk.`object`.humanawareness.HumanAwareness
import com.aldebaran.qi.sdk.`object`.streamablebuffer.StreamableBuffer
import com.aldebaran.qi.sdk.builder.*
import com.aldebaran.qi.sdk.design.activity.RobotActivity
import com.softbankrobotics.facemaskdetection.FaceMaskDetection
import com.softbankrobotics.facemaskdetection.capturer.BottomCameraCapturer
import com.softbankrobotics.facemaskdetection.capturer.TopCameraCapturer
import com.softbankrobotics.facemaskdetection.detector.AizooFaceMaskDetector
import com.softbankrobotics.facemaskdetection.utils.OpenCVUtils
import com.softbankrobotics.multichanneldetectionlibrary.utils.NavUtils
import com.softbankrobotics.multichanneldetectionlibrary.utils.SaveFileHelper

class MultiChannelDetection(activity: MultiChannelDetectionCallbacks) {

    companion object {
        private const val TAG = "MSI_HumanDetection"
        // Save Map
        const val mapFileName = "mapData.txt"
        // Localize
        const val TRY_TO_LOCALIZE_LIMIT = 4
        const val TRY_TO_LOCALIZE_EXISTING_MAP_LIMIT = 2
        // Step
        const val MAPPING = 0
        const val LOCALIZING = 1
    }

    public class FaceDetected (var hasMask: Boolean, var picture: Bitmap, var confidence: Double)

    // Context
    private var qiContext : QiContext? = null
    public var activity : MultiChannelDetectionCallbacks? = activity

    // Store the map.
    private var explorationMap: ExplorationMap? = null
    // Store the LocalizeAndMap execution.
    private var mapAndLocalizeFuture: Future<Void>? = null
    private var turnToInitialPositionFuture: Future<Void?>? = null
    private var faceMaskDetectionFuture: Future<Unit>? = null
    // Save Map
    private var saveFileHelper: SaveFileHelper? = null
    // Localize test try
    private var tryToLocalize : Int = 0
    private var tryToLocalizeOnExistingMap : Int = 0
    // Map Frame
    private var initialPositionMapFrame: Frame? = null

    //Human awareness
    public var humanAwareness: HumanAwareness?=null
    public var faceMaskDetection: FaceMaskDetection? = null

    // -------------------- Ready -----------------
    public var isRobotReady: Boolean = false // Is pepper fully ready
    public var isRobotLocalize: Boolean = false // Is pepper localize
    private var humanEngaged : Boolean = false // Is pepper engaged with someone used to change the sensitivity of the mask detection

    //-------------------- OPTION-------------------
    // Use the head camera (true) or the tablet camera (false) to use the mask detection
    public var useHeadCamera : Boolean = false
    // Directory where store the map (Map and localize)
    private var filesDirectoryPath: String? = null
    // Save intial orientation (true)
    public var saveInitialPosition = true
    // Turn pepper to the initial orientation when he is localized (true)
    public var turnToInitialPosition = true
    // Hold pepper base when he is not engaged with a user (true) or free the base (false)
    public var holdBase : Boolean = false
    // Charging flap state change detection event (true)
    public var useChargingFlapDetection = true
    // Use the HumanAwarness from the QiSDK (true)
    public var useHumanDetection = true
    // Use the FaceMask Detection from the library (true)
    public var useFaceMaskDetection = true
    // Use the onEngagedHumanChangedListener from HumanAwarness (true)
    public var useEngagedHumanChangedListener = true
    // Use the onRecommendedHumanToEngageChangedListener from HumanAwarness (true)
    public var useRecommendedHumanToEngageChangedListener = true
    // Use the onHumansAroundChangedListener from HumanAwarness (true)
    public var useHumansAroundChangedListener = false
    // Map the surrounding environment and localize into it (true)
    public var hasToLocalizeAndMap = true

    // Hold Abilities
    private var holder : Holder? = null

    /**
     * Start the human detection process
     * Call in onRobotFocusGained
     */
    fun onRobotFocusGained(qiContext: QiContext) {
        Log.d(TAG, "Start Human Detection")
        this.qiContext = qiContext
        filesDirectoryPath = "${Environment.getExternalStorageDirectory().absolutePath}/Maps"
        if (permissionAlreadyGranted())
            saveFileHelper = SaveFileHelper()
        if (saveInitialPosition)
            saveInitialPosition()
        if (useChargingFlapDetection)
            initializeChargingFlapDetection()
        if (hasToLocalizeAndMap) {
            if (!isChargingFlapOpen())
                startMapAndLocalizeWithExistingMap()
        }
        if (useHumanDetection)
            initializeHumanDetection()
        if (useFaceMaskDetection)
            initializeFaceMaskDetection()
        if (!hasToLocalizeAndMap)
            ready()
    }

    fun onRobotFocusLost() {
        try {
            cancelMappingAndLocalize()
            this.isRobotLocalize = false
            if (this.humanAwareness != null) {
                this.humanAwareness!!.removeAllOnEngagedHumanChangedListeners()
                this.humanAwareness!!.removeAllOnHumansAroundChangedListeners()
                this.humanAwareness!!.removeAllOnRecommendedHumanToEngageChangedListeners()
            }
            if (this.faceMaskDetectionFuture != null)
                this.faceMaskDetectionFuture?.requestCancellation()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onResume(context: RobotActivity) {
        OpenCVUtils.loadOpenCV(context)
    }
    /**
     * Human Detection is complete and ready
     */
    fun ready() {
        isRobotReady = true
        if (faceMaskDetection != null) {
            faceMaskDetectionFuture = faceMaskDetection?.start { faces ->
                val sortedFaces =
                    if (humanEngaged) faces.filter { (it.confidence > 0.5) }.sortedBy { -it.bb.left }
                    else faces.filter { (it.confidence > 0.7) }.sortedBy { -it.bb.left }
                if (isRobotReady) {
                    val sortedFacesFilter = arrayListOf<FaceDetected>()
                    sortedFaces.forEach {
                        sortedFacesFilter.add(FaceDetected(it.hasMask, it.picture, it.confidence))
                    }
                    if (holder != null)
                        holder?.async()?.release()
                    activity?.onHumanDetected(null, sortedFacesFilter)
                }

            }
        }
        activity?.onRobotReady(true)
        activity?.onStepReach(MAPPING, true)
        activity?.onStepReach(LOCALIZING, true)
    }

    /**
     * Initialize the charging flap listener
     */
    private fun initializeChargingFlapDetection() {
        if (qiContext == null)
            return
        chargingFlapStateChange(isChargingFlapOpen())
        qiContext?.power?.chargingFlap?.addOnStateChangedListener {
            chargingFlapStateChange(it.open)
            if (!it.open)
                restartMapping(true)
        }
    }

    /**
     * On charging flap state change
     * @param stateOpen: Charging flap state
     */
    private fun chargingFlapStateChange(stateOpen: Boolean) {
        if (stateOpen)
            cancelMappingAndLocalize()
        Log.d(TAG, "Charging Flap Is Open : $stateOpen")
        activity?.robotActivity?.runOnUiThread{
            activity?.onChargingFlapStateChanged(stateOpen)
        }
    }

    /**
     * Get the charging flap state
     * Default state -- False --
     * @return the charging flap state (Open : True /Close : False)
     */
    private fun isChargingFlapOpen() : Boolean {
        return if (qiContext != null) qiContext!!.power.async().chargingFlap.value.async().state.value.open else false
    }

    /**
     * Intialize Human Detection by starting the HumanAwareness member
     */
    private fun initializeHumanDetection(){
        this.humanAwareness= qiContext?.humanAwareness

        if (useEngagedHumanChangedListener) {
            this.humanAwareness?.async()?.addOnEngagedHumanChangedListener { human ->
                Log.d(TAG, "EngagedHumanChangedListener Event")
                humanToEngage(human)
            }
        }
        if (useRecommendedHumanToEngageChangedListener) {
            this.humanAwareness?.async()?.addOnRecommendedHumanToEngageChangedListener { human ->
                Log.d(TAG, "RecommendedHumanToEngageChangedListener Event")
                humanToEngage(human)
            }
        }
        if (useHumansAroundChangedListener) {
            this.humanAwareness?.async()
                ?.addOnHumansAroundChangedListener { humans: MutableList<Human>? ->
                    Log.d(TAG, "HumansAroundChangedListener Event")
                    if (humans == null) {
                        humanEngaged = false
                        return@addOnHumansAroundChangedListener
                    }
                    if (humans.size > 0)
                        humanToEngage(humans[0])
                }
        }
    }

    /**
     * Human detected by the core function of pepper
     * @param human: Human to engage
     */
    private fun humanToEngage(human: Human?) {
        Log.d(TAG, "humanToEngage : Human Detected")
        if(human!=null && isRobotReady) {
            if (holder != null)
                holder?.async()?.release()
            humanEngaged = true
            activity?.onHumanDetected(human, null)
        } else
            humanEngaged = false
    }

    /**
     * Save the initial orientation of pepper
     */
    private fun saveInitialPosition() {
        Log.d(TAG, "saveInitialPosition")
        var mapping : Mapping? = null
        if (qiContext != null)
            mapping = qiContext?.mapping
        if (mapping != null)
            mapping.async()?.mapFrame()?.thenConsume {
                initialPositionMapFrame = it.get()
            }
    }

    /**
     * Align pepper with the initial orientation
     */
    fun turnToInitialPosition() : Future<Void>? {
        Log.d(TAG, "turnToInitialPosition")
        if (!isRobotLocalize)
            return Future.of(null)
        if (initialPositionMapFrame != null && qiContext != null && !isChargingFlapOpen()) {
            turnToInitialPositionFuture = NavUtils.alignWithFrame(qiContext!!, initialPositionMapFrame!!)
            if (turnToInitialPositionFuture == null)
                return Future.of(null)
            return turnToInitialPositionFuture?.thenConsume {
                when {
                    turnToInitialPositionFuture!!.isSuccess -> {
                        if (holdBase) {
                            holder = HolderBuilder.with(qiContext!!)
                                .withDegreesOfFreedom(DegreeOfFreedom.ROBOT_FRAME_ROTATION)
                                .build()
                            holder?.async()?.hold()
                        }
                    }
                }
            }
        }
        return Future.of(null)
    }

    /**
     * Restart the mapping and localization of pepper
     * @param localizeFromExistingMap :: Boolean to check the origin of the call and the action to take
     */
    private fun restartMapping(localizeFromExistingMap : Boolean) {
        Log.d(TAG, "Restart Mapping")

        if (!hasToLocalizeAndMap) {
            ready()
            return
        }

        cancelMappingAndLocalize()?.thenConsume {
            if (localizeFromExistingMap && tryToLocalizeOnExistingMap < TRY_TO_LOCALIZE_EXISTING_MAP_LIMIT)
                startMapAndLocalizeWithExistingMap()
            else
                startMapping()
        }
    }

    /**
     * Stop and cancel the mapping and localization of pepper
     */
    fun cancelMappingAndLocalize() : Future<Void>? {
        if (mapAndLocalizeFuture == null)
            return Future.of(null)
        mapAndLocalizeFuture?.requestCancellation()
        Log.d(TAG, "mapAndLocalizeFuture is not Null : requestCancellation")
        return mapAndLocalizeFuture
    }

    /**
     * StartMapAndLocalizeWithExistingMap : check if there is a map to use and start mapping and localizing
     */
    private fun startMapAndLocalizeWithExistingMap() {
        val mapData: StreamableBuffer?
        if (!hasToLocalizeAndMap) {
            ready()
            return
        }
        if (permissionAlreadyGranted()) {
            mapData = saveFileHelper?.readStreamableBufferFromFile(filesDirectoryPath, mapFileName)
            if (mapData != null)
                useExistingMap(mapData)
            else
                startMapping()
        } else
            startMapping()
    }

    /**
     * Load an existing map
     * @param mapData: StreamableBuffer
     */
    private fun useExistingMap(mapData: StreamableBuffer?) {
        if (!hasToLocalizeAndMap) {
            ready()
            return
        }
        if (mapData != null) {
            ++tryToLocalizeOnExistingMap
            Log.d(TAG, "loadMap")
            Future.of(ExplorationMapBuilder.with(qiContext).withStreamableBuffer(mapData).buildAsync()).andThenConsume {
                explorationMap = it.value
                cancelMappingAndLocalize()?.thenConsume {
                    startLocalizing(true)
                }
            }
            return
        }
    }

    /**
     * Start the mapping of the environment
     */
    private fun startMapping() {
        if (!hasToLocalizeAndMap) {
            ready()
            return
        }
        Log.d(TAG, "startMapping")
        tryToLocalize++
        activity?.onStepReach(MAPPING, false)

        // Create a LocalizeAndMap action.
        LocalizeAndMapBuilder.with(qiContext).buildAsync().andThenConsume {
            // Add an on status changed listener on the LocalizeAndMap action for the robot to say when he is localized.
            it.addOnStatusChangedListener {
            if (it == LocalizationStatus.LOCALIZED) {
                Log.d(TAG,"localizeAndMap : Robot is Localized")
                animationToLookAround()?.thenConsume {
                    Log.i(TAG, "Robot has mapped his environment.")
                    releaseAbilities()
                    // Cancel the LocalizeAndMap action.
                    cancelMappingAndLocalize()
                }
            }
        }
            val localizeAndMap = it
            cancelMappingAndLocalize()?.thenConsume {

                holdAbilities()?.thenConsume {
                    mapAndLocalizeFuture = localizeAndMap.async().run()

                    // Add a lambda to the action execution.
                    mapAndLocalizeFuture?.thenConsume {
                        if (it.hasError()) {
                            Log.d(
                                TAG,
                                "Error while mapping and localize : $tryToLocalize / $TRY_TO_LOCALIZE_LIMIT"
                            )
                            if (tryToLocalize >= TRY_TO_LOCALIZE_LIMIT) {
                                ready()
                            } else {
                                Log.e(TAG, "LocalizeAndMap action finished with error.", it.error)
                                restartMapping(false)
                            }
                        } else if (it.isCancelled) {
                            // Dump the ExplorationMap.

                            releaseAbilities()
                            explorationMap = localizeAndMap.dumpMap()
                            if (permissionAlreadyGranted())
                                saveFileHelper!!.writeStreamableBufferToFile(
                                    filesDirectoryPath,
                                    mapFileName,
                                    explorationMap?.serializeAsStreamableBuffer()!!
                                )
                            startLocalizing(false)
                        }
                    }
                }
            }
        }

        /*
        // Add an on status changed listener on the LocalizeAndMap action for the robot to say when he is localized.
        localizeAndMap?.addOnStatusChangedListener {
            if (it == LocalizationStatus.LOCALIZED) {
                Log.d(TAG,"localizeAndMap : Robot is Localized")
                animationToLookAround()?.thenConsume {
                    Log.i(TAG, "Robot has mapped his environment.")
                    releaseAbilities()
                    // Cancel the LocalizeAndMap action.
                    cancelMappingAndLocalize()
                }
            }
        }*/
        // Execute the LocalizeAndMap action asynchronously.
        /*
        cancelMappingAndLocalize()?.thenConsume {

            holdAbilities()?.thenConsume {
                mapAndLocalizeFuture = localizeAndMap?.async()?.run()

                // Add a lambda to the action execution.
                mapAndLocalizeFuture?.thenConsume {
                    if (it.hasError()) {
                        Log.d(
                            TAG,
                            "Error while mapping and localize : $tryToLocalize / $TRY_TO_LOCALIZE_LIMIT"
                        )
                        if (tryToLocalize >= TRY_TO_LOCALIZE_LIMIT) {
                            ready()
                        } else {
                            Log.e(TAG, "LocalizeAndMap action finished with error.", it.error)
                            restartMapping(false)
                        }
                    } else if (it.isCancelled) {
                        // Dump the ExplorationMap.

                        releaseAbilities()
                        explorationMap = localizeAndMap.dumpMap()
                        if (permissionAlreadyGranted())
                            saveFileHelper!!.writeStreamableBufferToFile(
                                filesDirectoryPath,
                                mapFileName,
                                explorationMap?.serializeAsStreamableBuffer()!!
                            )
                        startLocalizing(false)
                    }
                }
            }
        }
         */
    }

    /**
     * Start the localization of pepper in the environment
     * @param localizeFromExistingMap : Boolean to check the origin of the call
     */
    private fun startLocalizing(localizeFromExistingMap : Boolean) {
        Log.d(TAG, "startLocalizing")
        activity?.onStepReach(LOCALIZING, false)

        val localize = LocalizeBuilder.with(qiContext)
            .withMap(explorationMap)
        .buildAsync().andThenConsume {
            it.addOnStatusChangedListener {
                if (it == LocalizationStatus.LOCALIZED) {
                    Log.d(TAG, "localize Robot is Localized")
                    isRobotLocalize = true
                    if (turnToInitialPosition) {
                        val turnFuture = turnToInitialPosition()
                        if (turnFuture != null)
                            turnFuture.thenConsume { ready() }
                        else
                            ready()
                    }
                    else
                        ready()
                }
            }
            // Add a lambda to the action execution.
            mapAndLocalizeFuture = it.async().run()
            mapAndLocalizeFuture?.thenConsume {
                if (it.hasError()) {
                    Log.e(TAG, "Localize action finished with error.", it.error)
                    Log.d(TAG, "Error while mapping and localize : $tryToLocalize / $TRY_TO_LOCALIZE_LIMIT")
                    if (tryToLocalize >= TRY_TO_LOCALIZE_LIMIT)
                        ready()
                    else if (!isChargingFlapOpen())
                        restartMapping(localizeFromExistingMap)
                }
            }
        }

        /*
        localize?.addOnStatusChangedListener {
            if (it == LocalizationStatus.LOCALIZED) {
                Log.d(TAG, "localize Robot is Localized")
                isRobotLocalize = true
                if (turnToInitialPosition) {
                    val turnFuture = turnToInitialPosition()
                    if (turnFuture != null)
                        turnFuture.thenConsume { ready() }
                    else
                        ready()
                }
                else
                    ready()
            }
        }
         */

        Log.i(TAG, "Localizing...")


    }

    /**
     * Animation to look slowly around
     *
     * @return Future of the animation to look around
     */
    private fun animationToLookAround(): Future<Void?>? {
        Log.d(TAG, "animationToLookAround: started")
        return AnimationBuilder.with(qiContext) // Create the builder with the context.
            .withResources(R.raw.full_turn_with_stop) // Set the animation resource.
            .buildAsync().andThenCompose { animation: Animation? ->
                AnimateBuilder.with(qiContext)
                    .withAnimation(animation)
                    .buildAsync()
                    .andThenCompose { animate: Animate ->
                        animate.async().run()
                    }
            }
    }

    /**
     * Hold Autonomous abilities, BasicAwareness and BackgroundMovement if needed
     *
     * @return Future of the Holder
     */
    private fun holdAbilities(): Future<Void>? {
        // Build the holder for the abilities.
        return releaseAbilities().thenCompose {
            Log.d(TAG, "Hold Abilities to focus on a human (BASIC_AWARENESS + BACKGROUND_MOVEMENT)")
            holder = HolderBuilder.with(qiContext).withAutonomousAbilities(
                AutonomousAbilitiesType.BACKGROUND_MOVEMENT,
                AutonomousAbilitiesType.BASIC_AWARENESS).build()
            return@thenCompose holder?.async()?.hold()
        }
    }

    /**
     * Release Autonomous abilities.
     *
     * @return Future of the Holder
     */
    private fun releaseAbilities(): Future<Void?> {
        // Release the holder asynchronously.
        Log.d(TAG, "Release All Abilities")
        return if (holder != null) {
            holder!!.async().release().andThenConsume {
                holder = null
            }
        } else
            Future.of(null)
    }

    /**
     *  MASK DETECTOR WITH FACE MASK DETECTOR AND OPENCV
     *  Detection of users with or without mask
     */
    private fun initializeFaceMaskDetection() {
        if (activity?.robotActivity == null || qiContext == null)
            return
        Log.d(TAG, "maskDetector : FaceMaskDetection started")
        faceMaskDetection = FaceMaskDetection(
            AizooFaceMaskDetector(activity?.robotActivity!!),
            if (useHeadCamera) TopCameraCapturer(qiContext!!)
            else BottomCameraCapturer(activity?.robotActivity!!, activity?.robotActivity!!)
        )
    }

    /**
     * Check the permission to access storage
     *
     * @return Boolean True or False if permission granted
     */
    private fun permissionAlreadyGranted(): Boolean {
        return ContextCompat.checkSelfPermission(activity?.robotActivity!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
}
