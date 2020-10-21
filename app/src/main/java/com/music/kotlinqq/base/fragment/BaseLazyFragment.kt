package com.music.kotlinqq.base.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.apkfuns.logutils.LogUtils

/**
 * @author cyl
 * @date 2020/9/21
 */
abstract class BaseLazyFragment : Fragment(){

    private var isViewCreated = false  // 布局是否被创建
    private var isLoadData = false     // 数据是否被加载
    private var isFirstVisible = true   // 是否第一次可见
    protected var attachActivity = FragmentActivity()

    protected abstract fun lazyLoadData() // 加载数据

    companion object {
        const val TAG = "BaseLazyFragment"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isViewCreated = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (isFragmentVisible(this)){
            if (this.parentFragment == null || isFragmentVisible(this.parentFragment!!)){
                LogUtils.d(TAG, "onActivityCreated:  加载数据")
                lazyLoadData()
                isLoadData = true
                if (isFirstVisible) isFirstVisible = false
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isFragmentVisible(this) && !isLoadData && isViewCreated){
            lazyLoadData()
            isLoadData = true
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && !this.isResumed){
            return
        }
        if (!hidden && isFirstVisible){
            LogUtils.d(TAG, "onHiddenChanged : 加载数据")
            lazyLoadData()
            isFirstVisible = false
        }
    }

    private fun isFragmentVisible(fragment: Fragment) : Boolean {
        return fragment.userVisibleHint && !fragment.isHidden
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isFirstVisible = true
        isLoadData = false
        isViewCreated = false
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        attachActivity = context as FragmentActivity
    }
}