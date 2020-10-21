package com.music.kotlinqq.kotlin_extend

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.text.Html
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.music.kotlinqq.R
import com.music.kotlinqq.app.Api
import com.music.kotlinqq.app.App
import com.music.kotlinqq.bean.Song
import retrofit2.http.POST
import java.io.*

/**
 * @author cyl
 * @date 2020/9/26
 */
    /**
     * 屏幕截图
     */
fun Activity.screenShot(activity: Activity, isDeleteStatusBar: Boolean = true) : Bitmap {
    val decorView = activity.window.decorView
    decorView.isDrawingCacheEnabled = true
    decorView.buildDrawingCache()
    val bmp = decorView.drawingCache
    val dm = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(dm)
    var ret : Bitmap? = null
    ret = if (isDeleteStatusBar) {
        val res = activity.resources
        val resId = res.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight = res.getDimensionPixelSize(resId)
        Bitmap.createBitmap(bmp, 0, statusBarHeight, dm.widthPixels, dm.heightPixels - statusBarHeight)
    } else {
        Bitmap.createBitmap(bmp, 0, 0, dm.widthPixels, dm.heightPixels)
    }
    decorView.destroyDrawingCache()
    return ret!!
}

/**
 * 是否竖屏
 */
fun Activity.isProtrait() : Boolean {
    return resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
}

/**
 * 是否横屏
 */
fun Activity.isLandscape() : Boolean {
    return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

/**
 * 设置横屏
 */
fun Activity.setLandscape(){
    this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
}

/**
 * 设置全屏
 */
fun Activity.setFullScreen(){
    this.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
}

/**
 * 显示软键盘
 */
fun Activity.showKeyboard(){
    val imm : InputMethodManager = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?: return
    var view = this.currentFocus
    if (view == null){
        view = View(this)
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.requestFocus()
    }
    imm.showSoftInput(view, InputMethodManager.SHOW_FORCED)
}

/**
 * 隐藏软键盘
 */
fun Activity.hideKeyboard(){
    val imm : InputMethodManager = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?: return
    var view = this.currentFocus
    if (view == null){
        view = View(this)
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.requestFocus()
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

/**
 * bitmap缩放
 */
fun Bitmap.scale(newWidth : Int, newHeight : Int, recycler : Boolean = false) : Bitmap {
    val ret = Bitmap.createScaledBitmap(this, newWidth, newHeight, true)
    if (recycler && !isRecycled) this.recycle()
    return ret
}

/**
 * toast
 */
fun Context.toast(text : CharSequence, duration : Int = Toast.LENGTH_SHORT){
    Toast.makeText(this, text, duration).show()
}

fun Context.toast(resId : Int, duration : Int = Toast.LENGTH_SHORT){
    Toast.makeText(this, resId, duration).show()
}

fun Context.centetToast(resId : Int, duration : Int = Toast.LENGTH_SHORT){
    val t = Toast.makeText(this, resId, duration)
    t.setGravity(Gravity.CENTER, 0, 0)
    t.show()
}

/**
 * 尺寸转换
 */
// dp -> px
fun Context.dp2px(dpValue : Float) : Int {
    val scale = resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

// px -> dp
fun Context.px2dp(pxValue : Float) : Int {
    val scale = resources.displayMetrics.density
    return (pxValue / scale + 0.5f).toInt()
}

// sp -> px
fun Context.sp2px(spValue : Float) : Int {
    val scale = resources.displayMetrics.scaledDensity
    return (spValue * scale + 0.5f).toInt()
}

// px -> sp
fun Context.px2sp(pxValue: Float) : Int {
    val scale = resources.displayMetrics.scaledDensity
    return (pxValue / scale + 0.5f).toInt()
}

/********  屏幕尺寸  *********/
@SuppressLint("ObsoleteSdkInt")
fun Context.getScreenWidth() : Int {
    val wm : WindowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager?: return  resources.displayMetrics.widthPixels
    val point = Point()
    if (Build.VERSION.SDK_INT  >= Build.VERSION_CODES.JELLY_BEAN_MR1){
        wm.defaultDisplay.getRealSize(point)
    } else {
        wm.defaultDisplay.getSize(point)
    }
    return point.x
}

@SuppressLint("ObsoleteSdkInt")
fun Context.getScreenHeight() : Int {
    val wm : WindowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager?: return resources.displayMetrics.heightPixels
    val point = Point()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
        wm.defaultDisplay.getRealSize(point)
    } else {
        wm.defaultDisplay.getSize(point)
    }
    return point.y
}

// ------- NetWork ------
/**
 * 打开网络设置
 */
fun Context.openWirelessSettings(){
    startActivity(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

/**
 * 网络是否连接
 */
fun Context.isConnected(): Boolean {
    val info = this.getActiveNetworkInfo()
    return info.isConnected
}

/**
 * 是否为移动数据
 */
fun Context.isMobileData() : Boolean {
    val info = this.getActiveNetworkInfo()
    return (info.isAvailable && info.type == ConnectivityManager.TYPE_MOBILE)
}

/**
 * 返回桌面
 */
fun Context.startHomeActivty(){
    val homeIntent = Intent(Intent.ACTION_MAIN)
    homeIntent.addCategory(Intent.CATEGORY_HOME)
    startActivity(homeIntent)
}

@SuppressLint("MissingPermission")
fun Context.getActiveNetworkInfo() : NetworkInfo {
    val manager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return manager.activeNetworkInfo
}





