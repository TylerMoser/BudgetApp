package me.tylermoser.budget.android.views

import android.content.Context
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageButton

/**
 * An Android [ImageButton] intended for easily rotating the image on the button
 *
 * @author Tyler Moser
 */
class RotatableImageButton(context: Context? = null, attributeSet: AttributeSet? = null)
    : ImageButton(context, attributeSet)
{

    /**
     * Starts rotating the UI element indefinitely
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
     * Stops rotating the UI element
     */
    fun stopRotation() {
        animation?.repeatCount = 0
    }

}
