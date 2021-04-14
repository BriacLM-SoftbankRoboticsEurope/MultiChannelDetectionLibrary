package com.softbankrobotics.peppercovidassistant.models

import com.aldebaran.qi.sdk.`object`.locale.Language
import com.aldebaran.qi.sdk.`object`.locale.Locale
import com.aldebaran.qi.sdk.`object`.locale.Region
import com.softbankrobotics.peppercovidassistant.R

/**
 * Data Class, message to show
 */
data class CovidInfoData (
    val locale: Locale,
    val messages: List<Pair<String, Int>>
)

/**
 * FR Message -> Protective Measure
 */
val messageDataFrenchProtectiveMeasures = CovidInfoData(
    locale = Locale(Language.FRENCH, Region.FRANCE),
    messages = listOf(
        Pair("Lavez-vous très régulièrement les mains", R.drawable.ic_wash_hands),
        Pair("Porter un masque", R.drawable.ic_mask),
        Pair("Toussez ou éternuez dans votre coude ou dans un mouchoir", R.drawable.ic_sneeze),
        Pair("Utilisez un mouchoir à usage unique et jetez-le", R.drawable.ic_tissues),
        Pair("Saluez sans se serrer la main, évitez les embrassades", R.drawable.ic_distance)
    )
)


/**
 * EN Message -> Protective Measure
 */
val messageDataEnglishProtectiveMeasures = CovidInfoData(
    locale = Locale(Language.ENGLISH, Region.UNITED_STATES),
    messages = listOf(
        Pair("Wash your hand often", R.drawable.ic_wash_hands),
        Pair("Wear a mask", R.drawable.ic_mask),
        Pair("Cough or sneeze in your elbow or in a tissue", R.drawable.ic_sneeze),
        Pair("Use a disposable tissue and then throw it in a trash bin", R.drawable.ic_tissues),
        Pair("Avoid handshake and hug", R.drawable.ic_distance)
    )
)


/**
 * FR Message -> Mask
 */
val messageDataFrenchMask = CovidInfoData(
        locale = Locale(Language.FRENCH, Region.FRANCE),
        messages = listOf(
                Pair("Bien se laver les mains", R.drawable.wash_hand_1),
                Pair("Mettre les élastiques derrières les oreilles", R.drawable.mask_put_on),
                Pair("Nouer les lacets derrières la tête et le cou", R.drawable.mask_tied),
                Pair("Pincer le bord rigide au niveau du nez, s’il y en a un, et abaisser le masque en dessous du menton", R.drawable.mask_set),
                Pair("Se laver les mains et enlever le masque en ne touchant que les lacets ou les élastiques", R.drawable.mask_remove),
                Pair("Après utilisation, le mettre dans un sac plastique et le jeter", R.drawable.mask_trow),
                Pair("S’il est en tissu, le laver à 60° pendant 30 min", R.drawable.wash_mask),
                Pair("Bien se laver les mains à nouveau", R.drawable.wash_hand_3)
        )
)


/**
 * EN Message -> Mask
 */
val messageDataEnglishMask = CovidInfoData(
        locale = Locale(Language.ENGLISH, Region.UNITED_STATES),
        messages = listOf(
                Pair("Wash your hands", R.drawable.wash_hand_1),
                Pair("Place a loop around each ear", R.drawable.mask_put_on),
                Pair("Tied your mask behind your head and neck", R.drawable.mask_tied),
                Pair("Pinch nose clips on your nose", R.drawable.mask_set),
                Pair("Wash your hands then hold both of the ear loops and lift and remove the mask", R.drawable.mask_remove),
                Pair("Throw the mask in the trash", R.drawable.mask_trow),
                Pair("If your mask is in tissu wash it at 60°C for 30 minutes", R.drawable.wash_mask),
                Pair("Wash your hands again", R.drawable.wash_hand_3)
        )
)


/**
 * FR Message -> Symptom
 */
val messageDataFrenchSymptom = CovidInfoData(
        locale = Locale(Language.FRENCH, Region.FRANCE),
        messages = listOf(
            Pair("Fièvre", R.drawable.symptom_fever),
            Pair("Toux", R.drawable.symptom_cought),
            Pair("Essouflement", R.drawable.symptom_breath),
            Pair("Perte du goût et de l'odorat", R.drawable.symptom_taste)
        )
)


/**
 * EN Message -> Symptom
 */
val messageDataEnglishSymptom = CovidInfoData(
        locale = Locale(Language.ENGLISH, Region.UNITED_STATES),
        messages = listOf(
            Pair("Fever", R.drawable.symptom_fever),
            Pair("Cought", R.drawable.symptom_cought),
            Pair("Shortness Of Breath", R.drawable.symptom_breath),
            Pair("Loss Of Smell", R.drawable.symptom_taste)
        )
)


/**
 * FR Message -> Wash Hand
 */
val messageDataFrenchWashHand = CovidInfoData(
        locale = Locale(Language.FRENCH, Region.FRANCE),
        messages = listOf(
                Pair("Se laver les mains pendant 30 secondes à l'eau et au savon", R.drawable.hand1),
                Pair("Frottez-vous les mains, paume contre paume", R.drawable.hand2),
                Pair("Lavez le dos des mains", R.drawable.hand3),
                Pair("Puis lavez entre les doigts ", R.drawable.hand4),
                Pair("Lavez au niveau des articulations", R.drawable.hand5),
                Pair("Lavez aussi à la base des pouces", R.drawable.hand6),
                Pair("Lavez le bout des doigts et des ongles", R.drawable.hand7),
                Pair("Enfin séchez-vous les mains avec une serviette propre ou à l'air libre", R.drawable.hand8)
        )
)


/**
 * EN Message -> Wash Hand
 */
val messageDataEnglishWashHand = CovidInfoData(
        locale = Locale(Language.ENGLISH, Region.UNITED_STATES),
        messages = listOf(
                Pair("Use soap", R.drawable.hand1),
                Pair("Palm to palm", R.drawable.hand2),
                Pair("Right palm over left dorsum with interlaced fingers and vice versa", R.drawable.hand3),
                Pair("Palm to palm with fingers interlaced", R.drawable.hand4),
                Pair("Back of fingers to opposing palms with fingers interlocked", R.drawable.hand5),
                Pair("Rotational rubbing of left thumb clasped in right palm and vice versa", R.drawable.hand6),
                Pair("Rotational rubbing, backward and forward with clasped fingers of right hand in left palm and vice versa", R.drawable.hand7),
                Pair("Dry your hands", R.drawable.hand8)
        )
)