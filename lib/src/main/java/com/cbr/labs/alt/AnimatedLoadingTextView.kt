package com.cbr.labs.alt

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin

class AnimatedLoadingTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes), TiltListener {

    private var strokeAnimator: ValueAnimator? = null

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.style = Paint.Style.STROKE
    }

    var textLayout: StaticLayout
    lateinit var textBitmap: Bitmap

    private var strokeGap = 0f
    private var strokeWidth = 0f

    private var maxRadius = 0f
    private var center = PointF(0f, 0f)
    private var initialRadius = 0f

    private var strokeRadiusOffset = 0f
        set(value) {
            field = value
            postInvalidateOnAnimation()
        }


    private var text: String

    private val tiltSensor: TiltSensor = TiltSensorImpl(context).apply {
        addListener(this@AnimatedLoadingTextView)
    }

    companion object {
        const val DEFAULT_STROKE_WIDTH = 20f
        const val DEFAULT_STROKE_GAP = 10f
        const val DEFAULT_TEXT_SIZE = 30f
    }

    init {

        setLayerType(LAYER_TYPE_SOFTWARE, null)

        val attrsTypedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.AnimatedLoadingTextView,
            defStyleAttr,
            defStyleRes
        )

        text = attrsTypedArray.getString(R.styleable.AnimatedLoadingTextView_alv_text) ?: ""
        strokeGap = attrsTypedArray.getDimension(
            R.styleable.AnimatedLoadingTextView_alv_strokeGap,
            DEFAULT_STROKE_GAP
        )
        strokeWidth = attrsTypedArray.getDimension(
            R.styleable.AnimatedLoadingTextView_alv_strokeWidth,
            DEFAULT_STROKE_WIDTH
        )

        textPaint.apply {
            color =
                attrsTypedArray.getColor(
                    R.styleable.AnimatedLoadingTextView_alv_textColor,
                    Color.BLACK
                )
            textSize = attrsTypedArray.getDimension(
                R.styleable.AnimatedLoadingTextView_alv_textSize,
                DEFAULT_TEXT_SIZE
            )
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_ATOP)
        }

        strokePaint.apply {
            color =
                attrsTypedArray.getColor(
                    R.styleable.AnimatedLoadingTextView_alv_strokeColor,
                    Color.WHITE
                )
            strokeWidth = attrsTypedArray.getDimension(
                R.styleable.AnimatedLoadingTextView_alv_strokeWidth,
                DEFAULT_STROKE_WIDTH
            )
        }

        strokeGap =
            attrsTypedArray.getDimension(
                R.styleable.AnimatedLoadingTextView_alv_strokeGap,
                DEFAULT_STROKE_GAP
            )

        attrsTypedArray.recycle()

        textLayout = createLayout(text)

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = textLayout.width
        val h = textLayout.height
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        textBitmap = textToBitmap(text)

        center.set(0f, 0f)
        maxRadius = hypot(w.toDouble(), h.toDouble()).toFloat()
        initialRadius = 10f
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        strokeAnimator = ValueAnimator.ofFloat(0f, strokeGap + strokeWidth).apply {
            addUpdateListener {
                strokeRadiusOffset = it.animatedValue as Float
            }
            duration = 660L
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
        tiltSensor.addListener(this)
        tiltSensor.register()
    }

    override fun onDetachedFromWindow() {
        strokeAnimator?.cancel()
        tiltSensor.unregister()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {

        var currentRadius = initialRadius + strokeRadiusOffset

        while (currentRadius < maxRadius) {
            canvas.drawArc(
                center.x - currentRadius,
                center.y - currentRadius,
                center.x + currentRadius,
                center.y + currentRadius,
                0f, 360f, false, strokePaint
            )
            currentRadius += (strokeGap + strokeWidth)
        }

        canvas.drawBitmap(textBitmap, 0f, 0f, textPaint)
    }

    private fun createLayout(text: String): StaticLayout {
        return text.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(
                    it,
                    0,
                    it.length,
                    textPaint,
                    textPaint.measureText(it).toInt()
                ).build()
            } else {
                StaticLayout(
                    text,
                    textPaint,
                    textPaint.measureText(it).toInt(),
                    Layout.Alignment.ALIGN_CENTER,
                    1f,
                    0f,
                    true
                )
            }
        }
    }

    private fun textToBitmap(text: String): Bitmap {
        val baseline = -textPaint.ascent()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawText(text, 0f, baseline, textPaint)
        return bitmap
    }

    override fun onTilt(pitchRollRad: Pair<Double, Double>) {
        val pitchRad = pitchRollRad.first
        val rollRad = pitchRollRad.second

        // Use half view height/width to calculate offset instead of full view/device measurement
        val maxYOffset = this.height / 2
        val maxXOffset = this.width / 2

        val yOffset = (sin(pitchRad) * maxYOffset)
        val xOffset = (sin(rollRad) * maxXOffset)

        center.set(max(xOffset.toFloat(), 0f), max(yOffset.toFloat(), 0f))

    }
}