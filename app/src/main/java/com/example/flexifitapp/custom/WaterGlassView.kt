package com.example.flexifitapp.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class WaterGlassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var current = 3
    private var max = 8

    // Kulay ng patak ng tubig (Border/Outline)
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(3f)
        color = Color.parseColor("#9eb9d4")
        strokeCap = Paint.Cap.ROUND
    }

    // Paint para sa tubig (Gagamitan natin ng shader mamaya)
    private val waterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // Paint para sa bubbles
    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
        alpha = 50
    }

    private val dropPath = Path()

    init {
        if (isInEditMode) {
            current = 5
        }
    }

    fun setMaxGlasses(value: Int) {
        max = value.coerceAtLeast(1)
        current = current.coerceIn(0, max)
        invalidate()
    }

    fun setCurrentGlasses(value: Int) {
        current = value.coerceIn(0, max)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredSize = dp(80f).toInt()
        val size = resolveSize(desiredSize, widthMeasureSpec)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val centerX = w / 2f
        val size = minOf(w, h) * 0.8f

        // 1. I-set ang Gradient (Vertical: Start Color sa taas, End Color sa baba)
        // Ginamit ko yung colors na binigay mo: #6082B6 at #9eb9d4
        waterPaint.shader = LinearGradient(
            0f, h * 0.1f, // Simula sa itaas ng drop
            0f, h * 0.95f, // Hanggang sa bottom ng drop
            Color.parseColor("#6082B6"),
            Color.parseColor("#9eb9d4"),
            Shader.TileMode.CLAMP
        )

        // 2. I-calculate ang Drop Path
        dropPath.reset()
        dropPath.moveTo(centerX, h * 0.1f)
        dropPath.cubicTo(
            centerX + size / 1.5f, h * 0.5f,
            centerX + size / 2f, h * 0.95f,
            centerX, h * 0.95f
        )
        dropPath.cubicTo(
            centerX - size / 2f, h * 0.95f,
            centerX - size / 1.5f, h * 0.5f,
            centerX, h * 0.1f
        )
        dropPath.close()

        // 3. I-draw ang outline
        canvas.drawPath(dropPath, outlinePaint)

        // 4. I-calculate ang fill level
        val ratio = (current.toFloat() / max.toFloat()).coerceIn(0f, 1f)
        val fillHeight = h * 0.95f - (ratio * (h * 0.85f))

        // 5. Clip at Draw Water with Gradient & Bubbles
        canvas.save()
        canvas.clipPath(dropPath)

        // Draw Water with Gradient
        canvas.drawRect(0f, fillHeight, w, h, waterPaint)

        // Draw Bubbles (para mas "pop" sila sa gradient background)
        if (current > 0) {
            canvas.drawCircle(centerX + dp(10f), h * 0.75f, dp(6f), bubblePaint)
            canvas.drawCircle(centerX - dp(12f), h * 0.85f, dp(3f), bubblePaint)
            canvas.drawCircle(centerX + dp(5f), h * 0.60f, dp(4f), bubblePaint)
        }

        canvas.restore()
    }

    private fun dp(v: Float): Float = v * resources.displayMetrics.density
}