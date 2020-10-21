package com.music.kotlinqq.model.db

import com.music.kotlinqq.bean.AlbumSong
import com.music.kotlinqq.bean.LocalSong
import com.music.kotlinqq.bean.Song

/**
 * @author cyl
 * @date 2020/9/17
 */
interface DbHelper {

    /**
     * 将搜索专辑列表中的所有歌曲否保存到网络歌曲数据库中
     * @param songList 专辑列表
     */
    fun insertAllAlbumSong(songList : List<AlbumSong.DataBean.ListBean>)
    fun getLocalMp3Info() : List<LocalSong>  // 得到本地列表
    fun saveSong(localSongs: List<LocalSong>) : Boolean  // 将本地音乐放在数据库中

    fun queryLove(songId : String) : Boolean  // 从数据库中查找是否为收藏歌曲
    fun saveToLove(song : Song) : Boolean    // 收藏歌曲
    fun deleteFromLove(songId : String) : Boolean  // 取消收藏歌曲
}