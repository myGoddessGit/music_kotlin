package com.music.kotlinqq.bean

import org.litepal.crud.LitePalSupport

/**
 * <pre>
 * desc   : 下载的歌曲
</pre> *
 */

class DownloadSong : LitePalSupport() {
    var id: Int = 0
    var songId: String? = null
    var mediaId: String? = null //下载标识符
    var name: String? = null
    var singer: String? = null
    var url: String? = null
    var pic: String? = null
    var duration: Long = 0
}
