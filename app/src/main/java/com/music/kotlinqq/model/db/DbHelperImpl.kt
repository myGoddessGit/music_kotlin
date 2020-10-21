package com.music.kotlinqq.model.db

import android.provider.MediaStore
import android.util.Log
import com.music.kotlinqq.app.Api
import com.music.kotlinqq.app.App
import com.music.kotlinqq.bean.*
import org.litepal.LitePal
import java.io.File

/**
 * @author cyl
 * @date 2020/9/17
 */
class DbHelperImpl : DbHelper{

    override fun insertAllAlbumSong(songList: List<AlbumSong.DataBean.ListBean>) {
        LitePal.deleteAll(OnlineSong::class.java)
        for (i in 0 until songList.size){
            val song = songList[i]
            val onlineSong = OnlineSong()
            onlineSong.id = i + 1
            onlineSong.name = song.songname
            onlineSong.singer = song.singer[0].name
            onlineSong.songId = song.songmid
            onlineSong.duration = song.interval.toLong()
            onlineSong.pic = Api.ALBUM_PIC + song.albummid + Api.JPG
            onlineSong.url = null
            onlineSong.lrc = null
            onlineSong.save()
        }
    }

    override fun getLocalMp3Info(): List<LocalSong> {
        val mp3InfoList = ArrayList<LocalSong>()
        getFromDownloadFile(mp3InfoList) // 从下载列表中读取歌曲文件
        val cursor = App.getContext().contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
            MediaStore.Audio.Media.DEFAULT_SORT_ORDER)
        for (i in 0 until cursor!!.count){
            cursor.moveToNext()
            val mp3Info = LocalSong()
            var title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))!! // 音乐标题
            var artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))!!// 艺术家
            val duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)) // 时长
            val size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)) // 文件大小
            val url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))!! // 文件路径
            val isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC)) // 是否为音乐
            if (isMusic != 0){  // 只将音乐添加到集合中
                if (size > 1000 * 800){
                    if (title.contains("-")){
                        val str = title.split("-")
                        artist = str[0]
                        title = str[1]
                    }
                    Log.i("DBDuration", (duration / 1000).toString())
                    mp3Info.name = title.trim()
                    mp3Info.singer = artist
                    mp3Info.duration = duration / 1000
                    mp3Info.url = url
                    mp3Info.songId = i.toString()
                    mp3InfoList.add(mp3Info)
                }
            }
        }
        cursor.close()
        return mp3InfoList
    }

    override fun saveSong(localSongs: List<LocalSong>): Boolean {
        LitePal.deleteAll(LocalSong::class.java)
        for (localSong in localSongs){
            val song = LocalSong()
            song.name = localSong.name
            song.singer = localSong.singer
            song.url = localSong.url
            song.songId = localSong.songId
            song.duration = localSong.duration
            if (!song.save()) return false
        }
        return true
    }

    override fun queryLove(songId: String): Boolean {
        val love = LitePal.where("songId=?", songId).find(Love::class.java)
        return love.size != 0
    }

    override fun saveToLove(song: Song): Boolean {
        val love = Love()
        love.name = song.songName
        love.singer = song.singer
        love.url = song.url
        love.pic = song.imgUrl
        love.duration = song.duration
        love.songId = song.songId
        love.isOnline = song.isOnline
        love.qqId = song.qqId
        love.mediaId = song.mediaId
        love.isDownload = song.isDownload
        return love.save()
    }

    override fun deleteFromLove(songId: String): Boolean {
        return LitePal.deleteAll(Love::class.java, "songId=?",songId) != 0
    }

    /**
     * 从下载列表中读取文件
     */
    private fun getFromDownloadFile(songList: MutableList<LocalSong>){
        val file = File(Api.STORAGE_SONG_FILE)
        if (!file.exists()){
            file.mkdirs()
            return
        }
        val subFile = file.listFiles()
        for (value in subFile){
            val songFileName = value.name
            val songFile = songFileName.substring(0, songFileName.lastIndexOf("."))
            val songValue = songFile.split("-")
            val size = songValue[4].toLong()
            //如果文件的大小不等于实际大小，则表示该歌曲还未下载完成，被人为暂停，故跳过该歌曲，不加入到已下载集合
            if (size != value.length()) continue
            val song = LocalSong()
            song.singer = songValue[0]
            song.name = songValue[1]
            song.duration = songValue[2].toLong()
            song.songId = songValue[3]
            song.url = Api.STORAGE_SONG_FILE + songFileName
            songList.add(song)
        }
    }
}