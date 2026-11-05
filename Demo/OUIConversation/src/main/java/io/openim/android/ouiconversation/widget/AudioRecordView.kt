package io.openim.android.ouiconversation.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.os.SystemClock
import android.view.View
import io.openim.android.ouiconversation.R
import io.openim.android.ouicore.R as CoreR
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/** 聊天语音消息展示；播放状态由外部播放器控制。 */
open class AudioRecordView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var durationSeconds = 0L
    private var durationText = "0″"
    private var visibleText = durationText
    private var outgoing = false
    private var playing = false
    private var phase = 0f
    private var animationStartTime = 0L

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 15f * resources.displayMetrics.scaledDensity
    }
    private val soundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1.5f)
        strokeCap = Paint.Cap.BUTT
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val arcBounds = RectF()
    private val backgroundBounds = RectF()
    private val maxWidthPx = resources.getDimension(CoreR.dimen.msg_max_width)

    init {
        val attributes = context.obtainStyledAttributes(
            attrs, R.styleable.AudioRecordView, defStyleAttr, 0
        )
        try {
            outgoing = attributes.getBoolean(R.styleable.AudioRecordView_audioRecordOutgoing, false)
        } finally {
            attributes.recycle()
        }
        updateColors()
        updateContentDescription()
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    fun setDurationSeconds(seconds: Long) {
        val newDuration = seconds.coerceAtLeast(0L)
        if (durationSeconds == newDuration) return
        durationSeconds = newDuration
        durationText = "$newDuration″"
        updateContentDescription()
        updateVisibleText()
        requestLayout()
        invalidate()
    }

    fun setOutgoing(outgoing: Boolean) {
        if (this.outgoing == outgoing) return
        this.outgoing = outgoing
        updateColors()
        invalidate()
    }

    fun setPlaying(playing: Boolean) {
        if (this.playing == playing) return
        this.playing = playing
        animationStartTime = if (playing) SystemClock.uptimeMillis() else 0L
        phase = 0f
        invalidate()
    }

    fun isPlaying(): Boolean = playing

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val contentWidth = dp(12f * 2 + 10f + 4f) + textPaint.measureText(durationText)
        val durationWidth = dp(40f + durationSeconds.toFloat() * 5f)
        val desiredWidth = min(
            maxWidthPx,
            max(contentWidth, durationWidth) + paddingLeft + paddingRight
        )
        val metrics = textPaint.fontMetrics
        val desiredHeight = max(dp(38f), metrics.descent - metrics.ascent) + paddingTop + paddingBottom
        setMeasuredDimension(
            resolveSize(max(suggestedMinimumWidth, ceil(desiredWidth).toInt()), widthMeasureSpec),
            resolveSize(max(suggestedMinimumHeight, ceil(desiredHeight).toInt()), heightMeasureSpec)
        )
        updateVisibleText()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        backgroundBounds.set(0f, 0f, w.toFloat(), h.toFloat())
        updateVisibleText()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = min(dp(8f), min(width, height) / 2f)
        canvas.drawRoundRect(backgroundBounds, radius, radius, fillPaint)

        val left = paddingLeft + dp(12f)
        val right = width - paddingRight - dp(12f)
        if (right <= left) return
        val centerY = paddingTop + (height - paddingTop - paddingBottom) / 2f
        val iconWidth = dp(10f)
        val iconLeft = if (outgoing) right - iconWidth else left
        val textLeft = if (outgoing) {
            iconLeft - dp(4f) - textPaint.measureText(visibleText)
        } else {
            iconLeft + iconWidth + dp(4f)
        }
        val checkpoint = canvas.save()
        canvas.clipRect(left, paddingTop.toFloat(), right, (height - paddingBottom).toFloat())
        val metrics = textPaint.fontMetrics
        canvas.drawText(visibleText, textLeft, centerY - (metrics.ascent + metrics.descent) / 2f, textPaint)
        drawSoundIcon(canvas, iconLeft, centerY)
        canvas.restoreToCount(checkpoint)
        if (playing && isAttachedToWindow && isShown && windowVisibility == VISIBLE) {
            postInvalidateDelayed(32L)
        }
    }

    private fun drawSoundIcon(canvas: Canvas, left: Float, centerY: Float) {
        val checkpoint = canvas.save()
        if (outgoing) canvas.scale(-1f, 1f, left + dp(5f), centerY)
        val centerX = left + dp(1.5f)
        canvas.drawCircle(centerX, centerY, dp(1.5f), dotPaint)
        if (playing) {
            val elapsed = (SystemClock.uptimeMillis() - animationStartTime).coerceAtLeast(0L)
            phase = (elapsed % 1050L) / 1050f
        }
        val animationStep = if (playing) (phase * 3f).toInt() else 2
        for (wave in 0..1) {
            if (animationStep <= wave) continue
            val radius = dp(3.75f + wave * 3f)
            arcBounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
            canvas.drawArc(arcBounds, -55f, 110f, false, soundPaint)
        }
        canvas.restoreToCount(checkpoint)
    }

    private fun updateVisibleText() {
        val available = (measuredWidth - paddingLeft - paddingRight - dp(38f)).coerceAtLeast(0f)
        visibleText = TextUtils.ellipsize(durationText, textPaint, available, TextUtils.TruncateAt.END).toString()
    }

    private fun updateContentDescription() {
        contentDescription = "${context.getString(CoreR.string.voice)} $durationText"
    }

    private fun updateColors() {
        fillPaint.color = context.getColor(if (outgoing) CoreR.color.theme_selected else CoreR.color.gray_f4)
        val foreground = context.getColor(if (outgoing) CoreR.color.txt_theme else CoreR.color.txt_main)
        textPaint.color = foreground
        soundPaint.color = foreground
        dotPaint.color = foreground
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (playing) invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (playing && visibility == VISIBLE) invalidate()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (playing && visibility == VISIBLE) invalidate()
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density
}
