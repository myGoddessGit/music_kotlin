package com.music.kotlinqq.util

/**
 * @author cyl
 * @date 2020/9/16
 */
class MediaUtil {
    companion object {

        fun formatTime(time: Long) : String{
            val min = (time / 60).toString()
            var sec = (time % 60).toString()
            if (sec.length < 2){
                sec = "0$sec"
            }
            return "$min:$sec"
        }

        fun formatSinger(singer: String): String {
            var str : String = ""
            if (singer.contains("/")){
                val s = singer.split("/")
                str = s[0]
            }
            return str.trim()
        }

        fun formatSize(size: Long): String {
            val d = (size / 1024 / 1024).toDouble()
            return String.format("%.1f", d)
        }
    }
}

