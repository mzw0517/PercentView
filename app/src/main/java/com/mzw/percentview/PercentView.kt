package com.mzw.percentview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class PercentView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet,
    defStyleAttr: Int = 0
) : View(context, attributeSet, defStyleAttr) {

    private var mCircleColor: Int = 0
    private var mArcColor: Int = 0
    private var mArcWidth: Int = 0
    private var mPercentTextColor: Int = 0
    private var mPercentTextSize: Int = 0
    private var mRadius: Int = 0
    private var mCurPercent = 0.0f

    private val mCirclePaint: Paint
    private val mArcPaint: Paint
    private val mPercentTextPaint: Paint
    private val mTextBound: Rect
    private val mArcRectF: RectF

    private var mOnClickListener: View.OnClickListener? = null

    init {
        if (attributeSet != null) {
            val ta = context.obtainStyledAttributes(attributeSet, R.styleable.PercentView, defStyleAttr, 0)
            mCircleColor = ta.getColor(R.styleable.PercentView_circleBg, -0x71d606)
            mArcColor = ta.getColor(R.styleable.PercentView_arcColor, -0x1200)
            mArcWidth = ta.getDimensionPixelSize(
                R.styleable.PercentView_arcWidth,
                DensityUtils.dp2px(context, 16f)
            )
            mPercentTextColor = ta.getColor(R.styleable.PercentView_arcColor, -0x1200)
            mPercentTextSize = ta.getDimensionPixelSize(
                R.styleable.PercentView_percentTextSize,
                DensityUtils.sp2px(context, 16f)
            )
            mRadius = ta.getDimensionPixelSize(
                R.styleable.PercentView_radius,
                DensityUtils.dp2px(context, 100f)
            )
            ta.recycle()
        }

        mCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mCirclePaint.setStyle(Paint.Style.FILL)
        mCirclePaint.setColor(mCircleColor)

        mArcPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mArcPaint.setStyle(Paint.Style.STROKE)
        mArcPaint.setStrokeWidth(mArcWidth.toFloat())
        mArcPaint.setColor(mArcColor)
        mArcPaint.setStrokeCap(Paint.Cap.ROUND)//使圆弧两头圆滑

        mPercentTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPercentTextPaint.setStyle(Paint.Style.STROKE)
        mPercentTextPaint.setColor(mPercentTextColor)
        mPercentTextPaint.setTextSize(mPercentTextSize.toFloat())

        mArcRectF = RectF()//圆弧的外接矩形

        mTextBound = Rect()//文本的范围矩形

        setOnClickListener {
            if (mOnClickListener != null) {
                mOnClickListener!!.onClick(this@PercentView)
            }
        }
    }

    fun setCurPercent(curPercent: Float) {
        val anim = ValueAnimator.ofFloat(mCurPercent, curPercent)
        anim.duration = (Math.abs(mCurPercent - curPercent) * 20).toLong()
        anim.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            mCurPercent = Math.round(value * 10).toFloat() / 10//四舍五入保留到小数点后两位
            invalidate()
        }
        anim.start()
    }

    fun setOnCircleClickListener(onClickListener: View.OnClickListener) {
        this.mOnClickListener = onClickListener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            measureDimension(widthMeasureSpec),
            measureDimension(heightMeasureSpec)
        )
    }

    private fun measureDimension(measureSpec: Int): Int {
        var result: Int
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = 2 * mRadius
            if (specMode == View.MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize)
            }
        }
        return result
    }

    override fun onDraw(canvas: Canvas) {
        //画圆
        canvas.drawCircle(
            (width / 2).toFloat(),
            (height / 2).toFloat(),
            mRadius.toFloat(),
            mCirclePaint
        )

        //画圆弧
        mArcRectF.set(
            (width / 2 - mRadius + mArcWidth / 2).toFloat(),
            (height / 2 - mRadius + mArcWidth / 2).toFloat(),
            (width / 2 + mRadius - mArcWidth / 2).toFloat(),
            (height / 2 + mRadius - mArcWidth / 2).toFloat()
        )
        canvas.drawArc(mArcRectF, 270f, 360 * mCurPercent / 100, false, mArcPaint)

        val text = "$mCurPercent%"
        //计算文本宽高
        mPercentTextPaint.getTextBounds(text, 0, text.length, mTextBound)
        //画百分比文本
        canvas.drawText(
            text,
            (width / 2 - mTextBound.width() / 2).toFloat(),
            (height / 2 + mTextBound.height() / 2).toFloat(),
            mPercentTextPaint
        )
    }

}