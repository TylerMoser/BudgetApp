package me.tylermoser.budget.android.views

import android.content.Context
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView

/**
 * An Android [ImageView] intended for easily rotating the image being displayed
 *
 * @author Tyler Moser
 */
class RotatableImageView(context: Context? = null, attributeSet: AttributeSet? = null)
    : ImageView(context, attributeSet) {

    /**
     * Starts rotating the image indefinitely
     */
    fun startRotation() {
        animation = RotateAnimation(
                0.0f,
                360.0f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
        ).apply {
            repeatCount = -1
            duration = 600
        }
    }

    /**
     * Stops rotating the image
     */
    fun stopRotation() {
        animation?.repeatCount = 0
    }

}