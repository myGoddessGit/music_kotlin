package com.music.kotlinqq.model.prefs

import android.content.Context
import android.content.SharedPreferences
import com.music.kotlinqq.app.App
import com.music.kotlinqq.app.Constant

/**
 * @author cyl
 * @date 2020/9/18
 */
class PreferencesHelperImpl : PreferencesHelper {

    private val mPreferences : SharedPreferences =
        App.getContext().getSharedPreferences(Constant.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
    override fun setPlayMode(mode: Int) {
        mPreferences.edit().putInt(Constant.PREFS_PLAY_MODE, mode).apply()
    }

    override fun getPlayMode(): Int {
        return mPreferences.getInt(Constant.PREFS_PLAY_MODE, 0)
    }
}