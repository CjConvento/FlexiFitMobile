// app/src/main/java/com/example/flexifitapp/custom/WaterGlassView.kt
package com.example.flexifitapp.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class WaterGlassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var current = 3
    private var max = 8

    // Softer but more visible on light backgrounds
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2.5f)
        color = Color.parseColor("#B8C2D3")
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val waterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#7B61FF")
        alpha = 150
    }

    private val shinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(3f)
        color = Color.WHITE
        alpha = 90
        strokeCap = Paint.Cap.ROUND
    }

    private val glassPath = Path()
    private val rect = RectF()

    init {
        // Show something in XML preview
        if (isInEditMode) {
            max = 8
            current = 5
        }
        setWillNotDraw(false)
    }

    fun setMaxGlasses(value: Int) {
        max = value.coerceAtLeast(1)
        // keep current in bounds
        current = current.coerceIn(0, max)
        invalidate()
    }

    fun setCurrentGlasses(value: Int) {
        current = value.coerceIn(0, max)
        invalidate()
    }

    fun getCurrentGlasses(): Int = current

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Nice default size if wrap_content
        val desiredW = dp(74f).toInt()
        val desiredH = dp(92f).toInt()
        val w = resolveSize(desiredW, widthMeasureSpec)
        val h = resolveSize(desiredH, heightMeasureSpec)
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val padding = dp(6f)

        val left = padding
        val top = padding
        val right = w - padding
        val bottom = h - padding

        val radius = dp(14f)

        rect.set(left, top, right, bottom)

        // Build rounded glass path
        glassPath.reset()
        glassPath.addRoundRect(rect, radius, radius, Path.Direction.CW)

        // Water ratio
        val ratio = (current.toFloat() / max.toFloat()).coerceIn(0f, 1f)

        // Water fill rect inside glass
        val inset = dp(7f)
        val innerLeft = left + inset
        val innerRight = right - inset
        val innerBottom = bottom - inset
        val innerTop = top + inset
        val innerHeight = (innerBottom - innerTop).coerceAtLeast(1f)

        val waterTop = innerTop + (1f - ratio) * innerHeight
        val waterRect = RectF(innerLeft, waterTop, innerRight, innerBottom)

        // Clip to glass shape and draw water
        canvas.save()
        canvas.clipPath(glassPath)
        canvas.drawRect(waterRect, waterPaint)
        canvas.restore()

        // Glass outline
        canvas.drawRoundRect(rect, radius, radius, outlinePaint)

        // Soft shine
        val shineX = left + dp(14f)
        canvas.drawLine(
            shineX,
            top + dp(18f),
            shineX,
            bottom - dp(24f),
            shinePaint
        )
    }

    private fun dp(v: Float): Float = v * resources.displayMetrics.density

}