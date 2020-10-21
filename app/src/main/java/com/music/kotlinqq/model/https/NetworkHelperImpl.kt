package com.music.kotlinqq.model.https

import com.music.kotlinqq.bean.*
import com.music.kotlinqq.model.https.api.RetrofitService
import io.reactivex.Observable

/**
 * @author cyl
 * @date 2020/9/18
 */
class NetworkHelperImpl(private val mRetrofitService: RetrofitService) : NetworkHelper{

    override fun getAlbumSong(id: String): Observable<AlbumSong> {
        return mRetrofitService.getAlbumSong(id)
    }

    override fun search(seek: String, offset: Int): Observable<SearchSong> {
        return mRetrofitService.search(seek, offset)
    }

    override fun searchAlbum(seek: String, offset: Int): Observable<Album> {
        return mRetrofitService.searchAlbum(seek, offset)
    }

    override fun getLrc(seek: String): Observable<SongLrc> {
        return mRetrofitService.getLrc(seek)
    }

    override fun getOnlineSongLrc(songId: String): Observable<OnlineSongLrc> {
        return mRetrofitService.getOnlineSongLrc(songId)
    }

    override fun getSingerImg(singer: String): Observable<SingerImg> {
        return mRetrofitService.getSingerImg(singer)
    }

    override fun getSongUrl(data: String): Observable<SongUrl> {
        return mRetrofitService.getSongUrl(data)
    }

}