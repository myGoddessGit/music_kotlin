package com.music.kotlinqq.base.presenter

import com.music.kotlinqq.base.view.BaseView
import io.reactivex.disposables.Disposable

/**
 * @author cyl
 * @date 2020/9/21
 */
interface IPresenter<T : BaseView> {
    fun attachView(view: T) // 绑定布局

    fun isAttachView() : Boolean // 是否绑定了布局

    fun detachView()  // 解绑布局

    fun addRxSubscribe(disposable: Disposable) // 添加订阅者
}