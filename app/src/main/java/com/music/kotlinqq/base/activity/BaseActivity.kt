package com.music.kotlinqq.base.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.music.kotlinqq.base.view.BaseView
import com.music.kotlinqq.util.CommonUtil

/**
 * @author cyl
 * @date 2020/9/21
 */
abstract class BaseActivity : AppCompatActivity(), BaseView {

    protected abstract fun getLayoutId() : Int // 布局id
    protected abstract fun initView() // 初始化布局
    protected abstract fun initData() // 初始化数据
    protected abstract fun onClick() // 点击事件

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())
        initView()
        initData()
        onClick()
    }

    override fun showToast(message: String) {
        CommonUtil.showToast(this, message)
    }

    override fun showNormalView() {

    }

    override fun showErrorView() {

    }

    override fun showLoading() {

    }

    override fun reload() {

    }

}