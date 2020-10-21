package com.music.kotlinqq.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ExpandableListView

/**
 * @author cyl
 * @date 2020/9/15
 */
class MyListView(context: Context, attr: AttributeSet) : ExpandableListView(context, attr) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(Int.MAX_VALUE.shr(2), MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
    }
}