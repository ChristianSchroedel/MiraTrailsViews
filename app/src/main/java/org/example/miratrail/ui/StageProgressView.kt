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

    init {
        setBackgroundColor(ContextCompat.getColor(context, R.color.cream))
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
        val left = 28f * density
        val right = width - 28f * density
        val centerY = 33f * density
        val step = if (stages.size == 1) 0f else (right - left) / (stages.size - 1)
        paint.strokeWidth = 4f * density
        paint.color = ContextCompat.getColor(context, R.color.outline)
        canvas.drawLine(left, centerY, right, centerY, paint)
        if (completed > 1) {
            paint.color = ContextCompat.getColor(context, R.color.pine)
            canvas.drawLine(left, centerY, left + step * (completed - 1), centerY, paint)
        }
        stages.forEachIndexed { index, name ->
            val x = left + step * index
            paint.color = ContextCompat.getColor(
                context,
                if (index < completed) R.color.pine else R.color.cream
            )
            paint.style = Paint.Style.FILL
            canvas.drawCircle(x, centerY, 11f * density, paint)
            paint.color = ContextCompat.getColor(context, R.color.pine)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f * density
            canvas.drawCircle(x, centerY, 11f * density, paint)
            paint.style = Paint.Style.FILL
            paint.color = ContextCompat.getColor(context, R.color.ink)
            paint.textSize = 12f * resources.displayMetrics.scaledDensity
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(name, x, centerY + 32f * density, paint)
        }
    }
}
