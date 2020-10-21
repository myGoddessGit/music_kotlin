package com.music.kotlinqq.model.https

import com.music.kotlinqq.bean.*
import io.reactivex.Observable

/**
 * @author cyl
 * @date 2020/9/17
 */
interface NetworkHelper {

    fun getAlbumSong(id : String) : Observable<AlbumSong> // 得到专辑

    fun search(seek: String, offset : Int) : Observable<SearchSong> // 搜索歌曲

    fun searchAlbum(seek: String, offset : Int) : Observable<Album> // 搜索照片

    fun getLrc(seek : String) : Observable<SongLrc> // 获取歌词

    fun getOnlineSongLrc(songId : String) : Observable<OnlineSongLrc> // 获取网络歌曲的歌词

    fun getSingerImg(singer : String) : Observable<SingerImg> // 获取歌手头像

    fun getSongUrl(data : String) : Observable<SongUrl> // 获取播放地址

}