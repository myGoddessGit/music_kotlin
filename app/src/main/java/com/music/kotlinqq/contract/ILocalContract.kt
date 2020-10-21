package com.music.kotlinqq.contract

import com.music.kotlinqq.base.presenter.IPresenter
import com.music.kotlinqq.base.view.BaseView
import com.music.kotlinqq.bean.LocalSong

/**
 * @author cyl
 * @date 2020/9/22
 */
interface ILocalContract  {

    interface View : BaseView {
        fun showMusic(mp3InfoList : MutableList<LocalSong>) // 显示本地音乐
    }

    interface Presenter: IPresenter<View> {
        fun getLocalMp3Info() // 得到本地音乐列表
        fun saveSong(localSongs : MutableList<LocalSong>) //将本地音乐放到数据库中
    }

}