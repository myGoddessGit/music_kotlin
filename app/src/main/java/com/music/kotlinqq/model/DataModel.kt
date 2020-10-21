package com.music.kotlinqq.model

import com.music.kotlinqq.bean.*
import com.music.kotlinqq.model.db.DbHelper
import com.music.kotlinqq.model.db.DbHelperImpl
import com.music.kotlinqq.model.https.NetworkHelper
import com.music.kotlinqq.model.https.NetworkHelperImpl
import com.music.kotlinqq.model.prefs.PreferencesHelper
import com.music.kotlinqq.model.prefs.PreferencesHelperImpl
import io.reactivex.Observable

/**
 * @author cyl
 * @date 2020/9/18
 */
class DataModel(private val mNetworkHelper: NetworkHelperImpl, private val mDbHelper: DbHelperImpl, private val mPreferencesHelper: PreferencesHelperImpl)
    : NetworkHelper, DbHelper, PreferencesHelper {

    override fun getAlbumSong(id: String): Observable<AlbumSong> {
        return mNetworkHelper.getAlbumSong(id)
    }

    override fun search(seek: String, offset: Int): Observable<SearchSong> {
       return mNetworkHelper.search(seek, offset)
    }

    override fun searchAlbum(seek: String, offset: Int): Observable<Album> {
        return mNetworkHelper.searchAlbum(seek, offset)
    }

    override fun getLrc(seek: String): Observable<SongLrc> {
        return mNetworkHelper.getLrc(seek)
    }

    override fun getOnlineSongLrc(songId: String): Observable<OnlineSongLrc> {
        return mNetworkHelper.getOnlineSongLrc(songId)
    }

    override fun getSingerImg(singer: String): Observable<SingerImg> {
        return mNetworkHelper.getSingerImg(singer)
    }

    override fun getSongUrl(data: String): Observable<SongUrl> {
        return mNetworkHelper.getSongUrl(data)
    }

    override fun insertAllAlbumSong(songList: List<AlbumSong.DataBean.ListBean>) {
        mDbHelper.insertAllAlbumSong(songList)
    }

    override fun getLocalMp3Info(): List<LocalSong> {
       return mDbHelper.getLocalMp3Info()
    }

    override fun saveSong(localSongs: List<LocalSong>): Boolean {
       return mDbHelper.saveSong(localSongs)
    }

    override fun queryLove(songId: String): Boolean {
        return mDbHelper.queryLove(songId)
    }

    override fun saveToLove(song: Song): Boolean {
        return mDbHelper.saveToLove(song)
    }

    override fun deleteFromLove(songId: String): Boolean {
        return mDbHelper.deleteFromLove(songId)
    }

    override fun setPlayMode(mode: Int) {
        return mPreferencesHelper.setPlayMode(mode)
    }

    override fun getPlayMode(): Int {
        return mPreferencesHelper.getPlayMode()
    }

}