package com.music.kotlinqq.base.presenter


import com.music.kotlinqq.base.view.BaseView
import com.music.kotlinqq.model.DataModel
import com.music.kotlinqq.model.db.DbHelperImpl
import com.music.kotlinqq.model.https.NetworkHelperImpl
import com.music.kotlinqq.model.https.RetrofitFactory
import com.music.kotlinqq.model.prefs.PreferencesHelperImpl
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * @author cyl
 * @date 2020/9/21
 */
abstract class BasePresenter<T : BaseView> : IPresenter<T> {

    protected var mView : T? = null

    protected var mModel : DataModel? = null

    init {
        if (mModel == null) {
            mModel = DataModel(NetworkHelperImpl(RetrofitFactory.createRequest()), DbHelperImpl(), PreferencesHelperImpl())
        }
    }
    private var mCompositeDisposable : CompositeDisposable? = null
    override fun attachView(view: T) {
        mView = view
    }

    override fun isAttachView(): Boolean {
        return mView != null
    }

    // presenter 与 view解绑时清除订阅者
    override fun detachView() {
        mView = null
        // 清除
        if (mCompositeDisposable != null){
            mCompositeDisposable!!.clear()
        }
    }

    // 添加订阅者
    override fun addRxSubscribe(disposable: Disposable) {
        if (mCompositeDisposable == null){
            mCompositeDisposable = CompositeDisposable()
        }
        mCompositeDisposable!!.add(disposable)
    }


}