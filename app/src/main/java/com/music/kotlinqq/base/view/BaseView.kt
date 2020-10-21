package com.music.kotlinqq.base.view

/**
 * @author cyl
 * @date 2020/9/21
 */
interface BaseView {
    fun showNormalView()  // 正常布局

    fun showErrorView()  // 错误布局

    fun showLoading() //  加载布局

    fun reload() // 重新加载

    fun showToast(message : String) // 显示Toast
}