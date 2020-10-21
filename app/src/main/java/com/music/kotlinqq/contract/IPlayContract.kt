package com.music.kotlinqq.contract

import com.music.kotlinqq.base.presenter.IPresenter
import com.music.kotlinqq.base.view.BaseView
import com.music.kotlinqq.bean.Song

/**
 * @author cyl
 * @date 2020/9/22
 */
interface IPlayContract {
    interface View : BaseView {
        fun getSingerName(): String //得到歌手的姓名
        fun getSingerAndLrc() //按钮点击事件，获取封面和歌词
        fun setSingerImg(imgUrl: String)  //将图片设置成背景
        fun showLove(love: Boolean)  //判断是否显示我喜欢的图标
        fun showLoveAnim()  //喜欢的动画
        fun saveToLoveSuccess() //保存到我喜欢数据库成功
        fun sendUpdateCollection()  //发送广播更新收藏列表
        fun showLrc(lrc: String) //显示歌词
        fun getLrcError(content: String?) //获取不到歌词
        fun setLocalSongId(songId: String)  //设置本地音乐的songId
        fun getSongIdSuccess(songId: String) //成功获取到该音乐的id
        fun saveLrc(lrc: String) //保存歌词
    }

    interface Presenter : IPresenter<View> {

        //保存播放状态
        fun getSingerImg(singer: String, song: String, duration: Long)

        fun getLrc(songId: String, type: Int) //获取歌词
        fun getSongId(song: String, duration: Long) //获取歌曲在qq音乐中的id
        fun setPlayMode(mode : Int)
        fun getPlayMode() : Int
        fun queryLove(songId: String) //查询我喜欢的数据库中有没这首歌
        fun saveToLove(song: Song)  //添加到我喜欢的表
        fun deleteFromLove(songId: String)  //从我喜欢的表中移除
    }
}