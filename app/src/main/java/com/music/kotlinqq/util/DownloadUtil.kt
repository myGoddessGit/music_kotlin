package com.music.kotlinqq.util

import com.music.kotlinqq.app.Api
import com.music.kotlinqq.bean.DownloadSong
import java.io.File

/**
 * @author cyl
 * @date 2020/9/16
 */
class DownloadUtil {

   companion object {
       fun getSongFromFile(fileName: String): List<DownloadSong>{
           val res = ArrayList<DownloadSong>()
           val file = File(fileName)
           if (!file.exists()){
               file.mkdirs()
               return res
           }
           val subFile = file.listFiles()
           for (value in subFile){
               val songFileName = value.name
               val songFile = songFileName.substring(0, songFileName.lastIndexOf("."))
               val songValue = songFile.split("-")
               val size = songValue[4].toLong()
               if (size != value.length()) continue
               val downloadSong = DownloadSong()
               downloadSong.singer = songValue[0]
               downloadSong.name = songValue[1]
               downloadSong.duration = songValue[2].toLong()
               downloadSong.songId = songValue[3]
               downloadSong.url = fileName + songFileName
               res.add(downloadSong)
           }
           return res
       }

       fun isExistOfDownloadSong(songId: String) : Boolean{
           val file = File(Api.STORAGE_SONG_FILE)
           if (!file.exists()){
               file.mkdirs()
               return false
           }
           val subFile = file.listFiles()
           for (value in subFile){
               val songFileName = value.name
               val songFile = songFileName.substring(0, songFileName.lastIndexOf("."))
               val songValue = songFile.split("-")
               //如果文件的大小不等于实际大小，则表示该歌曲还未下载完成，被人为暂停，故跳过该歌曲，不加入到已下载集合
               if (songValue[3] == songId){
                   val size = songValue[4].toLong()
                   return size == value.length()
               }
           }
           return false
       }
       // 组装下载歌曲的文件名
       fun getSaveSongFile(singer: String, songName: String, duration: Long, songId: String, size : Long) : String{
           return "$singer-$songName-$duration-$songId-$size.m4a"
       }
   }
}