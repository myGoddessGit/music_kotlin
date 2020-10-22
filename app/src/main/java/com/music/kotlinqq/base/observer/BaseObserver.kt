package com.music.kotlinqq.base.observer

import android.os.Handler
import com.apkfuns.logutils.LogUtils
import com.google.gson.JsonParseException
import com.music.kotlinqq.base.view.BaseView
import io.reactivex.observers.ResourceObserver
import org.json.JSONException
import retrofit2.HttpException
import java.net.UnknownHostException
import java.text.ParseException

/**
 * @author cyl
 * @date 2020/9/21
 */
open class BaseObserver<T> : ResourceObserver<T> {

    private var isShowLoadingView = true

    private var isShowErrorView = true

    private var baseView : BaseView

    companion object {
        const val TAG = "BASE_TAG_ERROR"
    }

    @JvmOverloads
    constructor(baseView: BaseView, isShowLoadingView : Boolean = false, isShowErrorView : Boolean = false){
        this.baseView = baseView
        this.isShowLoadingView = isShowLoadingView
        this.isShowErrorView = isShowErrorView
    }

    override fun onStart() {
        if (isShowLoadingView) baseView.showLoading()
    }

    override fun onComplete() {

    }

    override fun onNext(value: T) {
        Handler().postDelayed({
            baseView.showNormalView()
        }, 500)
    }

    override fun onError(e: Throwable) {
        Handler().postDelayed({
            if (isShowErrorView) baseView.showErrorView()
        }, 500)
        e.printStackTrace()
        if (e is UnknownHostException){
            LogUtils.e(TAG, "netWorkError:  ${e.message}")
            netWorkError()
        } else if (e is InterruptedException){
            LogUtils.e(TAG, "timeOut:  ${e.message}")
            timeoutError()
        } else if (e is HttpException) {
            LogUtils.e(TAG, "http错误:  ${e.message}")
            httpError()
        } else if (e is JsonParseException || e is JSONException || e is ParseException){
            LogUtils.e(TAG, "解析错误:  ${e.message}")
            parseKnown()
        } else {
            LogUtils.e(TAG, "未知错误:  ${e.message}")
            unKnown()
        }
    }

    private fun unKnown(){
        baseView.showToast("未知错误")
    }

    private fun parseKnown(){
        baseView.showToast("解析错误")
    }

    private fun httpError(){
        baseView.showToast("网络错误")
    }

    private fun timeoutError(){
        baseView.showToast("连接超时, 请重试")
    }

    private fun netWorkError(){
        baseView.showToast("当前网络不可用, 请检查网络连接")
    }

}