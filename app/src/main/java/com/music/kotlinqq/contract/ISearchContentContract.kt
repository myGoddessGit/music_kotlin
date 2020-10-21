package com.music.kotlinqq.contract

import com.music.kotlinqq.base.presenter.IPresenter
import com.music.kotlinqq.base.view.BaseView
import com.music.kotlinqq.bean.Album
import com.music.kotlinqq.bean.SearchSong
import com.music.kotlinqq.bean.Song
import java.util.ArrayList

/**
 * @author cyl
 * @date 2020/9/22
 */
interface ISearchContentContract {
    interface View : BaseView {
        fun setSongsList(songListBeans: ArrayList<SearchSong.DataBean.SongBean.ListBean>)  //显示歌曲列表
        fun searchMoreSuccess(songListBeans: ArrayList<SearchSong.DataBean.SongBean.ListBean>)  //搜索更多内容成功
        fun searchMoreError()  //搜索更多内容失败
        fun searchMore() //搜索更多
        fun showSearcherMoreNetworkError() //下拉刷新网络错误

        fun searchAlbumSuccess(albumList: List<Album.DataBean.AlbumBean.ListBean>)  //获取专辑成功
        fun searchAlbumMoreSuccess(songListBeans: List<Album.DataBean.AlbumBean.ListBean>)  //搜索更多内容成功
        fun searchAlbumError()  //获取专辑失败
        fun getSongUrlSuccess(song: Song, url: String) //成功获取歌曲url
    }

    interface Presenter : IPresenter<View> {
        fun search(seek: String, offset: Int)  //搜索
        fun searchMore(seek: String, offset: Int)  //搜索更多
        fun searchAlbum(seek: String, offset: Int)  //搜索专辑
        fun searchAlbumMore(seek: String, offset: Int) //搜索更多专辑
        fun getSongUrl(song: Song) //得到歌曲的播放url
    }
}