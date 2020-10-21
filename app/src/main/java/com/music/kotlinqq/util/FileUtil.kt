package com.music.kotlinqq.util

import android.graphics.Bitmap
import android.util.Log
import com.music.kotlinqq.app.Api
import com.music.kotlinqq.app.App
import com.music.kotlinqq.app.Constant
import com.music.kotlinqq.bean.Song
import java.io.*
import java.lang.StringBuilder

/**
 * @author cyl
 * @date 2020/9/16
 */
class FileUtil {
    companion object {
        private const val TAG = "FileUtil"
        fun saveSong(song: Song){
            try {
                val file = File(App.getContext().getExternalFilesDir("yearling")?.absolutePath)
                if (!file.exists()){
                    file.mkdirs()
                }
                // 写对象流的对象
                val userFile = File(file, "song.txt")
                val oos = ObjectOutputStream(FileOutputStream(userFile))
                oos.writeObject(song)
                oos.close()
            } catch (e : FileNotFoundException){
                e.printStackTrace()
            } catch (e : IOException){
                e.printStackTrace()
            }
        }

        fun getSong(): Song? {
            try {
                val ois = ObjectInputStream(FileInputStream(App.getContext().getExternalFilesDir("")?.toString() + "/yearling/song.txt"))
                return ois.readObject() as Song
            } catch (e : FileNotFoundException){
                e.printStackTrace()
                return Song()
            } catch (e : IOException){
                e.printStackTrace()
            } catch (e : ClassNotFoundException){
                e.printStackTrace()
            }
            return null
        }

        fun saveImgToNative(bitmap: Bitmap, singer: String){
            val file = File(Api.STORAGE_IMG_FILE)
            if (!file.exists()){
                file.mkdirs()
            }
            val singerImgFile = File(file, "$singer.jpg")
            var fos : FileOutputStream? = null
            try {
                fos = FileOutputStream(singerImgFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.flush()
            } catch (e : FileNotFoundException){
                e.printStackTrace()
            } catch (e : IOException){
                e.printStackTrace()
            } finally {
                try {
                    fos?.close()
                } catch (e: IOException){
                    e.printStackTrace()
                }
            }
        }
        // 保存歌词到本地
        fun saveLrcToNative(lrc : String, songName: String){
            Thread {
                val file = File(Api.STORAGE_LRC_FILE)
                if (!file.exists()){
                    file.mkdirs()
                }
                val lrcFile = File(file, songName + Constant.LRC)
                try {
                    val fileWriter = FileWriter(lrcFile)
                    fileWriter.write(lrc)
                    fileWriter.close()
                } catch (e : IOException){
                    e.printStackTrace()
                }
            }.start()
        }

        /**
         * 本地获取歌词
         */
        fun getLrcFromNative(songName: String) : String? {
            try {
                val fileReader = FileReader(Api.STORAGE_LRC_FILE + songName + Constant.LRC)
                val bufferedReader = BufferedReader(fileReader)
                val lrc = StringBuilder()
                while (true){
                    val s = bufferedReader.readLine() ?: break
                    lrc.append(s).append("\n")
                }
                fileReader.close()
                Log.e(TAG, "getLrcFromNative: $lrc")
                return lrc.toString()
            } catch (e : IOException){
                e.printStackTrace()
            }
            return null
        }
    }
}