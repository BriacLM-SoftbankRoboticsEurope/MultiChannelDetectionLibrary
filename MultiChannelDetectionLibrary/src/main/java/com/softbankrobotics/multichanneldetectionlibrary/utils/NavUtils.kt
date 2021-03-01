package com.softbankrobotics.multichanneldetectionlibrary.utils

import com.aldebaran.qi.Future
import com.aldebaran.qi.sdk.QiContext
import com.aldebaran.qi.sdk.`object`.actuation.Animate
import com.aldebaran.qi.sdk.`object`.actuation.Frame
import com.aldebaran.qi.sdk.`object`.geometry.Quaternion
import com.aldebaran.qi.sdk.`object`.geometry.Transform
import com.aldebaran.qi.sdk.builder.AnimateBuilder
import com.aldebaran.qi.sdk.builder.AnimationBuilder
import java.util.*
import java.util.concurrent.ExecutionException

class NavUtils {

    companion object {
        /**
         * Gets the "yaw" (or "theta") angle from a quaternion (the only angle relevant for navigation)
         */
        fun getYawFromQuaternion(q: Quaternion): Double {
            // yaw (z-axis rotation)
            val x: Double = q.getX()
            val y: Double = q.getY()
            val z: Double = q.getZ()
            val w: Double = q.getW()
            val sinYaw = 2.0 * (w * z + x * y)
            val cosYaw = 1.0 - 2.0 * (y * y + z * z)
            return Math.atan2(sinYaw, cosYaw)
        }

        /**
         * Tries to directly go to given pos and angle, in straight line. Returns a future.
         */
        fun goStraightToPos(qiContext: QiContext?, theta: Double): Future<Void?>? {
            val animationString = String.format(
                Locale.ENGLISH,
                "[\"Holonomic\", [\"Line\", [%f, %f]], %f, 3.0]",
                0.0,
                0.0,
                theta
            )
            val animation = AnimationBuilder.with(qiContext).withTexts(animationString).buildAsync()
            val animate: Future<Animate>
            try {
                animate = AnimateBuilder.with(qiContext).withAnimation(animation.get()).buildAsync()
                return animate.get().async().run()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
            return null
        }

        /**
         * Turn to align with a given frame. Returns a future.
         */
        fun alignWithFrame(qiContext: QiContext, frame: Frame): Future<Void?>? {
            val deltaTransform: Transform
            try {
                deltaTransform =
                    frame.async().computeTransform(qiContext.actuation.async().robotFrame().get())
                        .get().transform
                val quaternion: Quaternion = deltaTransform.rotation
                val theta = getYawFromQuaternion(quaternion)
                return goStraightToPos(qiContext, theta)
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
            return null
        }
    }
}