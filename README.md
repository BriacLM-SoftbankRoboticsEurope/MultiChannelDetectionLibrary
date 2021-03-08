# Pepper Covid Assistant

## Library

The module HumanDetection use both the native detection with the QiSDK and the library pepper-mask-detection to improve the detection of masked user.

## How it work

Add JitPack repository to your build file:
Add it in your root build.gradle at the end of repositories:

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency on the face detection lib to your app build.gradle in the dependencies section:

```
dependencies {
	implementation 'com.github.BriacLM-SoftbankRoboticsEurope:MultiChannelDetectionLibrary:1.6'
}
```

To use this library you first need to implement the interface HumanDetectionCallbacks to your MainActivity with two members variable context and humanDetection

```kotlin
class MainActivity : RobotActivity(), MultiChannelDetectionCallbacks {

    override var context: RobotActivity = this
    var multiChannelDetection: MultiChannelDetection? = null

    ---
}
```

Then initialize the multiChannelDetection variable before the call to the super

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    this.multiChannelDetection = MultiChannelDetection(this)

    super.onCreate(savedInstanceState)
}
```

Add the onResume function

```kotlin
override fun onResume() {
    super.onResume()

    if (this.multiChannelDetection != null) {
        this.multiChannelDetection?.onResume(this)
        this.multiChannelDetection?.isRobotReady = false
    }
}
```

Initialize the options in the onRobotFocusGained function and call this.multiChannelDetection?.onRobotFocusGained(qiContext)

```kotlin
override fun onRobotFocusGained(qiContext: QiContext?) {
    if (this.multiChannelDetection == null) {
        this.multiChannelDetection = MultiChannelDetection(this)
    }
    this.multiChannelDetection?.useHeadCamera = true
    this.multiChannelDetection?.holdBase = true
    this.multiChannelDetection?.turnToInitialPosition = true
    this.multiChannelDetection?.onRobotFocusGained(qiContext)
}

override fun onRobotFocusLost() {
    if (this.multiChannelDetection != null) {
        this.multiChannelDetection?.onRobotFocusLost()
    }
}
```

List of options

```kotlin
    // Directory where store the map (Map and localize)
    var filesDirectoryPath: String? = null
    // Save intial orientation
    var saveInitialPosition = true
    // Use the head camera (true) or the tablet camera (false) to use the mask detection
    this.multiChannelDetection?.useHeadCamera = true
    // Hold pepper base when he is not engaged with a user
    this.multiChannelDetection?.holdBase = true
    // Turn pepper to the initial orientation when he is localized
    this.multiChannelDetection?.turnToInitialPosition = true
    // Charging flap state change detection
    this.multiChannelDetection?.useChargingFlapDetection = true
    // Use the HumanAwarness from the QiSDK
    this.multiChannelDetection?.useHumanDetection = true
    // Use the FaceMask Detection from the library
    this.multiChannelDetection?.useFaceMaskDetection = true
    // Use the onEngagedHumanChangedListener from HumanAwarness
    this.multiChannelDetection?.useEngagedHumanChangedListener = true
    // Use the onRecommendedHumanToEngageChangedListener from HumanAwarness
    this.multiChannelDetection?.useRecommendedHumanToEngageChangedListener = true
    // Use the onHumansAroundChangedListener from HumanAwarness
    this.multiChannelDetection?.useHumansAroundChangedListener = true
    // Map the surrounding Environement and localize into it
    this.multiChannelDetection?.hasToLocalizeAndMap = true
```

## Application

Pepper Covid Assistant is a application for Pepper to prevent and inform about the Covid19 and show a use case of the mask detection library

## Features

The following features are shown with this application :

* Chat with Pepper FR/EN
* Human awareness
* Face Mask Detection
* Charging Flap State Detection

## Compatibility

Tested running on pepper 1.8.

### Dependencies

QiSDK

* 'com.aldebaran:qisdk:1.7.5'
* 'com.aldebaran:qisdk-design:1.7.5'

Conversational Content

* 'com.aldebaran:qisdk-conversationalcontent:0.19.1-experimental-05'
* 'com.aldebaran:qisdk-conversationalcontent-greetings:0.19.1-experimental-05'
* 'com.aldebaran:qisdk-conversationalcontent-askrobotname:0.19.1-experimental-05'
* 'com.aldebaran:qisdk-conversationalcontent-robotabilities:0.19.1-experimental-05'
* 'com.aldebaran:qisdk-conversationalcontent-notunderstood:0.19.1-experimental-05'
* 'com.aldebaran:qisdk-conversationalcontent-repeat:0.19.1-experimental-05'
* 'com.aldebaran:qisdk-conversationalcontent-volumecontrol:0.19.1-experimental-05'

Face Mask Detection

* 'com.github.softbankrobotics-labs:pepper-mask-detection:master-SNAPSHOT'

## Authors

Softbank robotics : FAE
Contributors names and contact info

* FAE - [@FAE](fae-emea@softbankrobotics.com)

## Version History

* 0.2
    * Various bug fixes and optimizations
    * See [commit change]() or See [release history]()
* 0.1
    * Initial Release

## Screens

#### Splash Screen Loading
![Alt text](/Screens/screen_splash_loading.png?raw=true "Splash Screen Loading")
---
#### Splash Screen Welcome
![Alt text](/Screens/screen_splash_welcome.png?raw=true "Splash Screen Welcome")
---
#### Main Screen
![Alt text](/Screens/screen_main.png?raw=true "Main Screen")
---
#### Main Screen Message
![Alt text](/Screens/screen_main_message.png?raw=true "Main Screen Message")
---
#### Main Screen Lang
![Alt text](/Screens/screen_main_lang.png?raw=true "Main Screen Lang")
---
#### Main Screen  Mask Warning Dialog
![Alt text](/Screens/screen_main_mask_dialog.png?raw=true "Main Screen Mask Warning Dialog")
---
#### Splash Screen Charging Flap Dialog
![Alt text](/Screens/screen_splash_charging_flap.png?raw=true "Splash Screen Charging Flap Dialog")
---