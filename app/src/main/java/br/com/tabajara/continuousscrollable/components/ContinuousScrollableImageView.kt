package br.com.tabajara.continuousscrollable.components

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.IntDef
import br.com.tabajara.continuousscrollable.R

class ContinuousScrollableImageView : LinearLayout {
    @IntDef(UP, RIGHT, DOWN, LEFT)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    internal annotation class Directions

    @Directions
    var DEFAULT_DIRECTION = LEFT

    @IntDef(HORIZONTAL, VERTICAL)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    internal annotation class Asymptote

    @Asymptote
    var DEFAULT_ASYMPTOTE = HORIZONTAL

    @IntDef(MATRIX, FIT_XY, FIT_START, FIT_CENTER, FIT_END, CENTER, CENTER_CROP, CENTER_INSIDE)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    internal annotation class ScaleType

    @ScaleType
    var DEFAULT_SCALE_TYPE = FIT_CENTER

    //</editor-fold>
    private val DEFAULT_RESOURCE_ID = -1
    private val DEFAULT_DURATION = 3000
    private var DIRECTION_MULTIPLIER = -1
    private var duration = 0
    private var resourceId = 0
    private var direction = 0
    private var scaleType = 0
    private var animator: ValueAnimator? = null
    private var firstImage: ImageView? = null
    private var secondImage: ImageView? = null
    private var isBuilt = false

    constructor(context: Context) : super(context) {
        init(context)
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        setViewAttributes(context, attrs, defStyleAttr)
        init(context)
    }

    private fun setViewAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.ContinuousScrollableImageView, defStyleAttr, 0)
        resourceId = array.getResourceId(R.styleable.ContinuousScrollableImageView_imageSrc, DEFAULT_RESOURCE_ID)
        direction = array.getInt(R.styleable.ContinuousScrollableImageView_direction_continous, DEFAULT_DIRECTION)
        duration = array.getInt(R.styleable.ContinuousScrollableImageView_duration_continous, DEFAULT_DURATION)
        scaleType = array.getInt(R.styleable.ContinuousScrollableImageView_scaleType, DEFAULT_SCALE_TYPE)
        setDirectionFlags(direction)
        array.recycle()
    }

    private fun setDirectionFlags(direction: Int) {
        when (direction) {
            UP -> {
                DIRECTION_MULTIPLIER = 1
                DEFAULT_ASYMPTOTE = VERTICAL
            }
            RIGHT -> {
                DIRECTION_MULTIPLIER = -1
                DEFAULT_ASYMPTOTE = HORIZONTAL
            }
            DOWN -> {
                DIRECTION_MULTIPLIER = -1
                DEFAULT_ASYMPTOTE = VERTICAL
            }
            LEFT -> {
                DIRECTION_MULTIPLIER = 1
                DEFAULT_ASYMPTOTE = HORIZONTAL
            }
        }
    }

    /**
     * @param context
     */
    private fun init(context: Context) {
        View.inflate(context, R.layout.continuos_scrollable_layout, this)
        build()
    }

    /**
     * Set duration in ms
     *
     * @param duration
     */
    fun setDuration(duration: Int) {
        this.duration = duration
        isBuilt = false
        build()
    }

    /**
     * set scrolling direction
     *
     * @param direction
     */
    fun setDirection(@Directions direction: Int) {
        this.direction = direction
        isBuilt = false
        setDirectionFlags(direction)
        build()
    }

    /**
     * @param resourceId
     */
    fun setResourceId(resourceId: Int) {
        this.resourceId = resourceId
        firstImage!!.setImageResource(this.resourceId)
        secondImage!!.setImageResource(this.resourceId)
    }

    /**
     * set image scale type
     *
     * @param scaleType
     */
    fun setScaleType(@ScaleType scaleType: Int) {
        if (firstImage == null || secondImage == null) {
            throw NullPointerException()
        }
        var type = ImageView.ScaleType.CENTER
        when (scaleType) {
            MATRIX -> type = ImageView.ScaleType.MATRIX
            FIT_XY -> type = ImageView.ScaleType.FIT_XY
            FIT_START -> type = ImageView.ScaleType.FIT_START
            FIT_CENTER -> type = ImageView.ScaleType.FIT_CENTER
            FIT_END -> type = ImageView.ScaleType.FIT_END
            CENTER -> type = ImageView.ScaleType.CENTER
            CENTER_CROP -> type = ImageView.ScaleType.CENTER_CROP
            CENTER_INSIDE -> type = ImageView.ScaleType.CENTER_INSIDE
        }
        this.scaleType = scaleType
        firstImage!!.scaleType = type
        secondImage!!.scaleType = type
    }

    private fun build() {
        if (isBuilt) return
        isBuilt = true
        setImages()
        if (animator != null) animator!!.removeAllUpdateListeners()
        animator = ValueAnimator.ofFloat(0.0f, DIRECTION_MULTIPLIER * -1.0f)
        animator?.repeatCount = ValueAnimator.INFINITE
        animator?.interpolator = LinearInterpolator()
        animator?.duration = duration.toLong()
        when (DEFAULT_ASYMPTOTE) {
            HORIZONTAL -> animator?.addUpdateListener(object : AnimatorUpdateListener {
                override fun onAnimationUpdate(animation: ValueAnimator) {
                    run {
                        val progress = DIRECTION_MULTIPLIER * -(animation.animatedValue as Float)
                        val width = DIRECTION_MULTIPLIER * (-firstImage!!.width).toFloat()
                        val translationX = width * progress
                        firstImage!!.translationX = translationX
                        secondImage!!.translationX = translationX - width
                    }
                }
            })
            VERTICAL -> animator?.addUpdateListener(object : AnimatorUpdateListener {
                override fun onAnimationUpdate(animation: ValueAnimator) {
                    run {
                        val progress = DIRECTION_MULTIPLIER * -(animation.animatedValue as Float)
                        val height = DIRECTION_MULTIPLIER * (-firstImage!!.height).toFloat()
                        val translationY = height * progress
                        firstImage!!.translationY = translationY
                        secondImage!!.translationY = translationY - height
                    }
                }
            })
        }
        animator?.start()
    }

    private fun setImages() {
        if (resourceId == -1) {
            Log.e(TAG, "image must be initialized before it can be used. You can use in XML like this: (app:imageSrc=\"@drawable/yourImage\") ")
            return
        }
        firstImage = findViewById(R.id.first_image)
        secondImage = findViewById(R.id.second_image)
        firstImage?.setImageResource(resourceId)
        secondImage?.setImageResource(resourceId)
        setScaleType(scaleType)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (animator != null) animator!!.cancel()
    }

    class Builder(activity: Activity) {
        private val scrollableImageView: ContinuousScrollableImageView = ContinuousScrollableImageView(activity)
        fun setDuration(duration: Int): Builder {
            scrollableImageView.setDuration(duration)
            return this
        }

        fun setResourceId(resourceId: Int): Builder {
            scrollableImageView.setResourceId(resourceId)
            return this
        }

        fun setDirection(@Directions direction: Int): Builder {
            scrollableImageView.setDirection(direction)
            return this
        }

        fun setScaleType(@ScaleType scaleType: Int): Builder {
            scrollableImageView.setScaleType(scaleType)
            return this
        }

        fun build(): ContinuousScrollableImageView {
            return scrollableImageView
        }

    }

    companion object {
        //<editor-fold desc="DEFAULT_DIRECTION = LEFT">
        const val UP = 0
        const val RIGHT = 1
        const val DOWN = 2
        const val LEFT = 3

        //</editor-fold>
        //<editor-fold desc="DEFAULT_ASYMPTOTE = HORIZONTAL">
        const val HORIZONTAL = 0
        const val VERTICAL = 1

        //</editor-fold>
        //<editor-fold desc="DEFAULT_SCALE_TYPE = CENTER">
        const val MATRIX = 0
        const val FIT_XY = 1
        const val FIT_START = 2
        const val FIT_CENTER = 3
        const val FIT_END = 4
        const val CENTER = 5
        const val CENTER_CROP = 6
        const val CENTER_INSIDE = 7
        val TAG = ContinuousScrollableImageView::class.java.simpleName
    }
}