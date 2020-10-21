package com.music.kotlinqq.util

import android.app.ActivityManager
import android.content.Context

/**
 * @author cyl
 * @date 2020/9/16
 */
class ServiceUtil {
    companion object {
        fun isServiceRunning(context: Context, serviceName: String) :Boolean{
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val infos = am.getRunningServices(100)
            for (info in infos){
                val name = info.service.className
                if (name == serviceName){
                    return true
                }
            }
            return false
        }
    }
}