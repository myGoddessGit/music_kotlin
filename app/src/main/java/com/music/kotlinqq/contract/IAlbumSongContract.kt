package com.music.kotlinqq.contract

import com.music.kotlinqq.base.presenter.IPresenter
import com.music.kotlinqq.base.view.BaseView
import com.music.kotlinqq.bean.AlbumSong

/**
 * @author cyl
 * @date 2020/9/21
 */
interface IAlbumSongContract  {

    interface View : BaseView {
        fun setAlbumSongList(dataBean: MutableList<AlbumSong.DataBean.ListBean>) //成功获取专辑歌曲后填充列表
        fun showAlbumSongError() // 获取专辑失败
        fun showAlbumMessage(name : String, language: String, company : String, albumType : String, desc : String) // 展示专辑详情
        override fun showLoading() // 显示进度
        fun hideLoading()   // 隐藏进度
        fun showNetError()  // 显示错误
    }

    interface Presenter : IPresenter<View> {
        fun getAlbumDetail(id : String, type : Int)  // 获取专辑的更多信息
        fun insertAllAlbumSong(dataBean : MutableList<AlbumSong.DataBean.ListBean>)  //将专辑歌曲添加到数据库
    }
}