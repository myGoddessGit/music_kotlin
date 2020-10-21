package com.music.kotlinqq.util

import android.content.Context

/**
 * @author cyl
 * @date 2020/9/16
 */
class ScreenUtil {
   companion object {
       /**
        * dp 转 px
        */
       fun dip2px(context: Context, dpValue: Float): Int {
           val scale = context.resources.displayMetrics.density
           return (dpValue.times(scale) + 0.5F).toInt()
       }

       /**
        * px 转 dp
        */
       fun px2dip(context: Context, pxValue: Float) : Int {
           val scale = context.resources.displayMetrics.density
           return (pxValue / scale + 0.5F).toInt()
       }
   }
}