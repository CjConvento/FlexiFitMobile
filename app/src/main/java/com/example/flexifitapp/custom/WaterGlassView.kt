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
        color = Color.parseColor("#E0E7FF") // Light bluish-gray border
        strokeCap = Paint.Cap.ROUND
    }

    // Kulay ng tubig sa loob
    private val waterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#7B61FF") // Yung purple-blue color sa image mo
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
        // Gawin nating square-ish yung view para maganda yung shape ng drop
        val desiredSize = dp(80f).toInt()
        val size = resolveSize(desiredSize, widthMeasureSpec)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val centerX = w / 2f
        val centerY = h / 2f
        val size = minOf(w, h) * 0.8f // Para may padding sa paligid

        // 1. I-calculate ang Drop Path (Yung hugis patak)
        dropPath.reset()
        // Magsisimula sa taas (yung matulis na part)
        dropPath.moveTo(centerX, h * 0.1f)
        // Gagawa ng curves para sa bilog na bottom
        dropPath.cubicTo(
            centerX + size / 1.5f, h * 0.5f, // Control point 1
            centerX + size / 2f, h * 0.95f,  // Control point 2
            centerX, h * 0.95f               // Bottom center
        )
        dropPath.cubicTo(
            centerX - size / 2f, h * 0.95f,
            centerX - size / 1.5f, h * 0.5f,
            centerX, h * 0.1f
        )
        dropPath.close()

        // 2. I-draw muna yung gray outline/background ng drop
        canvas.drawPath(dropPath, outlinePaint)

        // 3. I-calculate yung fill level
        val ratio = (current.toFloat() / max.toFloat()).coerceIn(0f, 1f)
        val fillHeight = h * 0.95f - (ratio * (h * 0.85f))

        // 4. Clip at Draw Water
        canvas.save()
        canvas.clipPath(dropPath) // Siguraduhin na sa loob lang ng drop ang kulay

        // Eto yung rectangle na tumataas base sa baso na nainom
        canvas.drawRect(0f, fillHeight, w, h, waterPaint)
        canvas.restore()
    }

    private fun dp(v: Float): Float = v * resources.displayMetrics.density
}