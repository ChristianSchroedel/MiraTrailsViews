package org.example.miratrail.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import org.example.miratrail.R

class StageProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var stages = listOf("Start", "Mitte", "Ziel")
    private var completed = 0
    private val density = resources.displayMetrics.density
    private val compact = context.obtainStyledAttributes(attrs, R.styleable.StageProgressView)
        .let { attributes ->
            try {
                attributes.getBoolean(R.styleable.StageProgressView_compact, false)
            } finally {
                attributes.recycle()
            }
        }
    private val check = ContextCompat.getDrawable(context, R.drawable.figma_check)
    private val open = ContextCompat.getDrawable(context, R.drawable.figma_stage_open)

    init {
        updateAccessibility()
    }

    fun show(stages: List<String>, completedStages: Int) {
        this.stages = stages.ifEmpty { listOf("Start") }
        completed = completedStages.coerceIn(0, this.stages.size)
        updateAccessibility()
        invalidate()
    }

    private fun updateAccessibility() {
        contentDescription =
            "Etappen: $completed von ${stages.size} abgeschlossen. ${stages.joinToString(", ")}."
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (compact) {
            drawSegments(canvas)
            return
        }
        val left = 41f * density
        val right = width - 41f * density
        val centerY = 15f * density
        val step = if (stages.size == 1) 0f else (right - left) / (stages.size - 1)
        paint.strokeWidth = 2f * density
        paint.color = ContextCompat.getColor(context, R.color.sage)
        canvas.drawLine(left, centerY, right, centerY, paint)
        stages.forEachIndexed { index, name ->
            val x = left + step * index
            paint.color = ContextCompat.getColor(
                context,
                if (index < completed) R.color.coral else R.color.sage
            )
            paint.style = Paint.Style.FILL
            canvas.drawCircle(x, centerY, 15f * density, paint)
            val icon = if (index < completed) check else open
            val halfSize = 8f * density
            icon?.setBounds(
                (x - halfSize).toInt(), (centerY - halfSize).toInt(),
                (x + halfSize).toInt(), (centerY + halfSize).toInt()
            )
            icon?.draw(canvas)
            paint.style = Paint.Style.FILL
            paint.color = ContextCompat.getColor(context, R.color.ink)
            paint.textSize = 12f * resources.displayMetrics.scaledDensity
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(name, x, 52f * density, paint)
        }
    }

    private fun drawSegments(canvas: Canvas) {
        val gap = 5f * density
        val segmentWidth = ((width - paddingLeft - paddingRight) -
            gap * (stages.size - 1)) / stages.size
        paint.style = Paint.Style.FILL
        stages.indices.forEach { index ->
            paint.color = ContextCompat.getColor(
                context, if (index < completed) R.color.coral else R.color.sage
            )
            val left = paddingLeft + index * (segmentWidth + gap)
            canvas.drawRoundRect(
                left, 0f, left + segmentWidth, 4f * density,
                2f * density, 2f * density, paint
            )
        }
    }
}
