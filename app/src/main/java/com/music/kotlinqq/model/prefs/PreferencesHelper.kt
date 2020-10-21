package com.music.kotlinqq.model.prefs

/**
 * @author cyl
 * @date 2020/9/18
 */
interface PreferencesHelper {

    fun setPlayMode(mode : Int) // 保存播放状态

    fun getPlayMode() : Int  // 获取播放状态
}