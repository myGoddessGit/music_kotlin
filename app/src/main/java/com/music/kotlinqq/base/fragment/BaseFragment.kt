package com.music.kotlinqq.base.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.music.kotlinqq.base.view.BaseView
import com.music.kotlinqq.util.CommonUtil

/**
 * @author cyl
 * @date 2020/9/21
 */
abstract class BaseFragment : BaseLazyFragment(), BaseView {

    protected var mActivity = Activity()
    protected abstract fun initView() // 初始化控件
    protected abstract fun initView(view: View)
    protected abstract fun loadData() // 加载数据
    protected abstract fun getLayoutId() : Int // 获取Fragment的布局id
    protected abstract fun initOtherView()
    protected var layoutManager = LinearLayoutManager(attachActivity)

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mActivity = context as Activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(getLayoutId(), container, false)
        initView(view)
        initView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initOtherView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun lazyLoadData() {
        loadData()
    }

    override fun showNormalView() {

    }

    override fun showErrorView() {

    }

    override fun showLoading() {

    }

    override fun reload() {

    }

    override fun showToast(message: String) {
        CommonUtil.showToast(mActivity, message)
    }

}