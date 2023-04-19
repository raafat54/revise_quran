package com.raafat.revise

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatTextView
import kotlin.math.min


class PagedTextView : AppCompatTextView {

    private var needPaginate = false
    private var isPaginating = false
    private val pageList = arrayListOf<CharSequence>()
    private var pageIndex: Int = 0
    private var pageHeight: Int = 0
    private var originalText: CharSequence = ""

    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context!!, attrs, defStyleAttr)

    fun size(): Int = pageList.size

    fun next(index: Int) {
        pageIndex = index
        setPageText()
    }

    private fun setPageText() {
        isPaginating = true
        text = pageList[pageIndex]
        isPaginating = false
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        if (!isPaginating) {
            originalText = text ?: ""
        }
        super.setText(text, type)
    }

    override fun setTextSize(unit: Int, size: Float) {
        super.setTextSize(unit, size)
        needPaginate = true
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        needPaginate = true
    }

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        super.setPaddingRelative(start, top, end, bottom)
        needPaginate = true
    }

    override fun setTextScaleX(size: Float) {
        if (size != textScaleX) {
            needPaginate = true
        }
        super.setTextScaleX(size)
    }

    override fun setTypeface(tf: Typeface?) {
        if (typeface != null && tf != typeface) {
            needPaginate = true
        }
        super.setTypeface(tf)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun setLetterSpacing(letterSpacing: Float) {
        if (letterSpacing != this.letterSpacing) {
            needPaginate = true
        }
        super.setLetterSpacing(letterSpacing)
    }

    override fun setHorizontallyScrolling(whether: Boolean) {
        super.setHorizontallyScrolling(false)
    }

    override fun setLineSpacing(add: Float, mult: Float) {
        if (add != lineSpacingExtra || mult != lineSpacingMultiplier) {
            needPaginate = true
        }
        super.setLineSpacing(add, mult)
    }

    override fun setMaxLines(maxLines: Int) {
        if (maxLines != this.maxLines) {
            needPaginate = true
        }

        super.setMaxLines(maxLines)
    }

    override fun setLines(lines: Int) {
        super.setLines(lines)

        if (lines != this.lineCount) {
            needPaginate = true
        }
    }

    private class NonClippableCanvas(@NonNull val bitmap: Bitmap) : Canvas(bitmap) {
        override fun clipRect(left: Float, top: Float, right: Float, bottom: Float): Boolean {
            return true
        }
    }

    private var rttCanvas: NonClippableCanvas? = null


    override fun onSizeChanged(width: Int, height: Int,
                               oldwidth: Int, oldheight: Int) {
        if ((width != oldwidth || height != oldheight) && width > 0 && height > 0) {
            rttCanvas?.bitmap?.recycle()
            try {
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)?.let {
                    rttCanvas = NonClippableCanvas(it)
                }
            } catch (t: Throwable) {
                // If for some reasons the bitmap cannot be created, we fall back on default rendering (potentially cropping the text).
                rttCanvas?.bitmap?.recycle()
                rttCanvas = null
            }
        }

        super.onSizeChanged(width, height, oldwidth, oldheight)
        pageHeight = height
    }

    override fun onDraw(canvas: Canvas) {
        rttCanvas?.let {
            // Clear the RTT canvas from the previous font.
            it.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            // Draw on the RTT canvas (-> bitmap) that will use clipping on the NonClippableCanvas, resulting in no-clipping
            super.onDraw(it)

            // Finally draw the bitmap that contains the rendered text (no clipping used here, will display on top of padding)
            canvas.drawBitmap(it.bitmap, 0f, 0f, null)

        } ?: super.onDraw(canvas) // If rtt is not available, use default rendering process
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (changed && needPaginate) {
            paginate(text)
            setPageText()
            needPaginate = false
        }
    }

    fun paginate(text: CharSequence) {
        pageList.clear()

        val layout = from(layout)
        val lines = min(maxLines, layout.lineCount)
        var startOffset = 0
        val heightWithoutPaddings = pageHeight - paddingTop - paddingBottom - paddingBottom
        var height = heightWithoutPaddings

        for (i in 0 until lines) {

            if (height < layout.getLineBottom(i)) {
                pageList.add(
                    text.subSequence(startOffset, layout.getLineStart(i))
                )
                Log.i("TAG", "1 : ${pageList.size}")
                startOffset = layout.getLineStart(i)
                height = layout.getLineTop(i) + heightWithoutPaddings
            }

            if (i == lines - 1) {
                pageList.add(
                    text.subSequence(startOffset, layout.getLineEnd(i))
                )
                if (pageList[pageList.size - 1].toString().split(" ").size <= 2
                    && size() > 1) {
                    pageList[pageList.size - 2] = pageList[pageList.size - 2].toString()
                        .plus(pageList[pageList.size - 1])
                    pageList.removeAt(pageList.size - 1)
                }

            }
        }
    }

    private fun from(layout: Layout): Layout =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            @Suppress("DEPRECATION")
            StaticLayout(
                originalText,
                paint,
                layout.width,
                layout.alignment,
                lineSpacingMultiplier,
                lineSpacingExtra,
                includeFontPadding)
        } else {
            StaticLayout.Builder
                .obtain(originalText, 0, originalText.length, paint, layout.width)
                .setAlignment(layout.alignment)
                .setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
                .setIncludePad(includeFontPadding)
                .setUseLineSpacingFromFallbacks()
                .setBreakStrategy(breakStrategy)
                .setHyphenationFrequency(hyphenationFrequency)
                .setJustificationMode()
                .setMaxLines(maxLines)
                .build()
        }

    private fun StaticLayout.Builder.setUseLineSpacingFromFallbacks(): StaticLayout.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            this.setUseLineSpacingFromFallbacks(isFallbackLineSpacing)
        }

        return this
    }

    private fun StaticLayout.Builder.setJustificationMode(): StaticLayout.Builder {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.setJustificationMode(justificationMode)
        }

        return this
    }
}