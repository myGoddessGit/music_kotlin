package com.music.kotlinqq.base.fragment

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.music.kotlinqq.R
import com.music.kotlinqq.base.presenter.IPresenter
import com.wang.avi.AVLoadingIndicatorView
import com.music.kotlinqq.app.Constant.*
import java.lang.IllegalStateException

/**
 * @author cyl
 * @date 2020/9/21
 */
abstract class BaseLoadingFragment<T : IPresenter<*>> : BaseMvpFragment<T>() {

    private var mNormalView : View? = null
    private var mErrorView : View? = null
    private var mLoadingView : View? = null
    private var avLoadingView : AVLoadingIndicatorView? = null
    private var mCurrentState = NORMAL_STATE  // 当前布局状态

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (getView() == null) return
        mNormalView = view.findViewById(R.id.normalView)
        if (mNormalView == null) {
            throw IllegalStateException("BaseLoadingFragment异常1")
        }
        if (this.mNormalView!!.parent !is ViewGroup){
            throw IllegalStateException("BaseLoadingFragment异常2")
        }

        val parentPanel = mNormalView!!.parent as ViewGroup
        View.inflate(mActivity, R.layout.error_view, parentPanel) // 加载错误布局
        View.inflate(mActivity, R.layout.loading_view, parentPanel) // 加载loading布局
        mLoadingView = parentPanel.findViewById(R.id.loadingView)
        avLoadingView = parentPanel.findViewById(R.id.avLoading)
        mErrorView = parentPanel.findViewById(R.id.errorView)
        val reloadBtn = parentPanel.findViewById<TextView>(R.id.reloadBtn)
        reloadBtn.setOnClickListener { reload() } // 重新加载

        mNormalView!!.visibility = View.VISIBLE
        mErrorView!!.visibility = View.GONE
        mLoadingView!!.visibility = View.GONE
    }

    override fun showNormalView() {
        super.showNormalView()
        if (mCurrentState == NORMAL_STATE) return
        hideViewByState(mCurrentState)
        mCurrentState = NORMAL_STATE
        showViewByState(mCurrentState)
    }

    override fun showErrorView() {
        super.showErrorView()
        if (mCurrentState == ERROR_STATE) return
        hideViewByState(mCurrentState)
        mCurrentState = ERROR_STATE
        showViewByState(mCurrentState)
    }

    override fun showLoading() {
        super.showLoading()
        if (mCurrentState == LOADING_STATE) return
        hideViewByState(mCurrentState)
        mCurrentState = LOADING_STATE
        showViewByState(mCurrentState)
    }
    // 隐藏布局
    private fun hideViewByState(state : Int){
        when (state){
            NORMAL_STATE -> {
                if (mNormalView == null) return
                mNormalView!!.visibility = View.GONE
            }
            LOADING_STATE -> {
                if (mLoadingView == null || avLoadingView == null)return
                mLoadingView!!.visibility = View.GONE
            }
            else -> {
                if (mErrorView == null) return
                mErrorView!!.visibility = View.GONE
            }
        }
    }

    // 显示布局
    private fun showViewByState(state : Int){
        when (state) {
            NORMAL_STATE -> {
                if (mNormalView == null) return
                mNormalView!!.visibility = View.VISIBLE
            }
            LOADING_STATE -> {
                if (mLoadingView == null || avLoadingView == null) return
                mLoadingView!!.visibility = View.VISIBLE
                avLoadingView!!.show()
            }
            else -> {
                if (mErrorView == null) return
                mErrorView!!.visibility = View.VISIBLE
            }
        }
    }

    override fun initOtherView() {

    }

}