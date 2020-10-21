package com.music.kotlinqq.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.music.kotlinqq.R
import com.music.kotlinqq.app.Api
import com.music.kotlinqq.app.App
import java.net.URL
import java.util.*

/**
 * @author cyl
 * @date 2020/9/15
 */
 class CommonUtil {

    companion object {
        var toast: Toast? = null
        fun hideStatusBar(activity: Activity, isHide: Boolean){
            val decorView = activity.window.decorView
            if (isHide){
                if (Build.VERSION.SDK_INT >= 22){
                    decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
                    activity.window.statusBarColor = Color.TRANSPARENT
                }
            } else {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                activity.window.statusBarColor = App.getContext().resources.getColor(R.color.actionBarColor)
            }
        }

        @SuppressLint("ShowToast")
        fun showToast(context: Context, message: String){
            if (toast == null){
                toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
            } else {
                toast?.setText(message)
            }
            toast?.show()
        }

        /**
         * 得到屏幕的宽度
         */
        fun getScreenWidth(context: Context?): Int{
            if (null == context){
                return 0
            }
            return context.resources.displayMetrics.widthPixels
        }

        /**
         * 得到屏幕的高度
         */
        fun getScreenHeight(context: Context?) : Int {
            if (null == context){
                return 0
            }
            return context.resources.displayMetrics.heightPixels
        }

        /**
         * EditText 获得焦点弹出软键盘
         */
        fun showKeyboard(editText: EditText, context: Context){
            // 设置可以获取焦点
            editText.isFocusable  = true
            editText.isFocusableInTouchMode = true
            // 请求获得焦点
            editText.requestFocus()
            // 调用系统的输入法
            val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
        }

        /**
         * 关闭软键盘
         */
        fun closeKeyboard(editText: EditText, context: Context){
            editText.clearFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
        }

        fun showStringColor(appointStr : String, originalStr: String, textView: TextView){
            val str = originalStr.replace(appointStr, "<font color='#FFC66D'>$appointStr</font>")
            textView.text = Html.fromHtml(str)
        }

        /**
         * 获取状态栏高度
         */
        fun getStatusHeightPx(act: Activity) : Int{
            var height: Int = 0
            val resourceId = act.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0){
                height = act.resources.getDimensionPixelSize(resourceId)
            }
            return height
        }

        fun getForegroundDrawable(bitmap: Bitmap) : Drawable {
            // 得到屏幕的宽高比 -> 按比例切割图片的一部分
            val widthHeightSize = (DisplayUtil.getScreenHeight(App.getContext()) * 1.0 / DisplayUtil.getScreenHeight(App.getContext()) * 1.0).toFloat()
            val cropBitmapWidth = (widthHeightSize * bitmap.height).toInt()
            val cropBitmapWidthX = ((bitmap.width - cropBitmapWidth) / 2.0).toInt()
            // 切割图片部分
            val cropBitmap = Bitmap.createBitmap(bitmap, cropBitmapWidthX, 0, cropBitmapWidth, bitmap.height)
            // 缩小图片
            val scaleBitmap = Bitmap.createScaledBitmap(cropBitmap, bitmap.width / 50, bitmap.height / 50, false)
            // 模糊化
            val blurBitmap = FastBlurUtil.doBlur(scaleBitmap, 3, true)
            // 加入灰色遮罩层 避免图片过亮影响其他控件
            return BitmapDrawable(blurBitmap)
        }

        /**
         * Glide 加载图片
         */
        fun setImgWithGlide(context: Context, imgUrl: String?, view: ImageView){
            Glide.with(context)
                .load(imgUrl)
                .apply(RequestOptions.placeholderOf(R.drawable.welcome))
                .apply(RequestOptions.errorOf(R.drawable.love))
                .into(view)
        }

        /**
         * 加载歌手图片
         */
        fun setSingerImg(context: Context, singer: String, view: ImageView){
            var str = ""
            if (singer.contains("/")){
                val s = singer.split("/")
                str = s[0]
            }
            str = str.trim()
            val imgUrl = Api.STORAGE_IMG_FILE + str + ".jpg"
            Glide.with(context)
                .load(imgUrl)
                .apply(RequestOptions.placeholderOf(R.drawable.welcome))
                .apply(RequestOptions.errorOf(R.drawable.welcome))
                .into(view)
        }

        fun getBJTime(){
            Log.i("thisTime", "thisTime")
            val url = URL("http://www.bjtime.cn")
            val uc = url.openConnection()
            uc.connect()
            val ld = uc.date
            val date = Date(ld)
            Log.i("BJTime", "${date.hours}时${date.minutes}分${date.seconds}秒")
        }
    }
}