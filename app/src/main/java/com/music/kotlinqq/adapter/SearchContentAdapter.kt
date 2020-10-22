package com.music.kotlinqq.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.andexert.library.RippleView
import com.apkfuns.logutils.LogUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.music.kotlinqq.R
import com.music.kotlinqq.app.Constant
import com.music.kotlinqq.bean.Album
import com.music.kotlinqq.bean.SearchSong
import com.music.kotlinqq.util.CommonUtil
import com.music.kotlinqq.util.FileUtil
import kotlinx.android.synthetic.main.recycler_album_item.view.*
import kotlinx.android.synthetic.main.recycler_song_search_item.view.*
import kotlinx.android.synthetic.main.recycler_song_search_item.view.ripple
import kotlinx.android.synthetic.main.recycler_album_item.view.ripple as ripple1

/**
 * @author cyl
 * @date 2020/9/23
 */
class SearchContentAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private var mSongListBeans : ArrayList<SearchSong.DataBean.SongBean.ListBean>? = null
    private var mAlbumList : MutableList<Album.DataBean.AlbumBean.ListBean>? = null
    private var mSeek : String
    private var mContext : Context
    private var mLastPosition = -1
    private var mType : Int

    companion object{
        private var mItemClick : ((tag : Int) -> Unit)? = null
        private var mAlbumClick :((tag : Int) -> Unit)? = null
        fun setItemClick(itemClick : (tag : Int) -> Unit){
            mItemClick = itemClick
        }
        fun setAlbumClick(albumClick : (tag : Int) -> Unit){
            mAlbumClick = albumClick
        }
    }
    constructor(dataBeans : MutableList<Album.DataBean.AlbumBean.ListBean>, seek : String, context : Context, type : Int){
        mContext = context
        mSeek = seek
        mAlbumList = dataBeans
        mType = type
    }
    constructor(songListBeans: ArrayList<SearchSong.DataBean.SongBean.ListBean>, seek: String, context: Context, type: Int){
        mContext = context
        mSeek = seek
        mSongListBeans = songListBeans
        mType = type
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view : View
        return when(viewType){
            Constant.TYPE_SONG -> {
                view = LayoutInflater.from(mContext).inflate(R.layout.recycler_song_search_item, parent, false)
                ViewHolder(view)
            }
            Constant.TYPE_ALBUM -> {
                view = LayoutInflater.from(mContext).inflate(R.layout.recycler_album_item, parent, false)
                AlbumHolder(view)
            }
            else -> {
                null!!
            }
        }
    }

    override fun getItemCount(): Int {
        return when(mType){
            Constant.TYPE_SONG -> {
                mSongListBeans!!.size
            }
            Constant.TYPE_ALBUM -> {
                mAlbumList!!.size
            }
            else -> {
                0
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return mType
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder){
            val songHolder = holder as ViewHolder
            val songListBean = mSongListBeans!![position]
            // 设置歌手 因为歌手可能有两个
            var singer = songListBean.singer[0].name
            for (i in 1 until songListBean.singer.size){
                singer += "、${songListBean.singer[i].name}"
            }
            songHolder.artistTv.text = singer
            CommonUtil.showStringColor(mSeek, singer, songHolder.artistTv)
            songHolder.titleTv.text = songListBean.songname
            CommonUtil.showStringColor(mSeek, songListBean.songname, songHolder.titleTv)

            // 根据点击显示
            if (songListBean.songmid == FileUtil.getSong()!!.songId){
                songHolder.playLine.visibility = View.VISIBLE
                mLastPosition = position
                songHolder.mItemView.setBackgroundResource(R.color.translucent)
            } else {
                songHolder.playLine.visibility = View.INVISIBLE
                songHolder.mItemView.setBackgroundResource(R.color.transparent)
            }
            songHolder.mItemView.setOnRippleCompleteListener {
                mItemClick!!.invoke(position)
                equalPosition(position)
            }
        } else {
            Log.i("thisQq", "thisQq")
            LogUtils.d("SearchContentAdapterQQ", mAlbumList)
            val albumHolder = holder as AlbumHolder
            val albumList = mAlbumList!![position]
            Glide.with(mContext).load(albumList.albumPic).apply(RequestOptions.errorOf(R.drawable.background)).into(albumHolder.albumIv)
            albumHolder.albumName.text = albumList.albumName
            albumHolder.singerName.text = albumList.singerName
            albumHolder.publicTime.text = albumList.publicTime
            CommonUtil.showStringColor(mSeek, albumList.albumName, albumHolder.albumName)
            CommonUtil.showStringColor(mSeek, albumList.singerName, albumHolder.singerName)
            CommonUtil.showStringColor(mSeek, albumList.publicTime, albumHolder.publicTime)
            albumHolder.item.setOnRippleCompleteListener {
                mAlbumClick!!.invoke(position)
            }
        }
    }

    internal inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var titleTv : TextView = itemView.tv_title
        var artistTv : TextView = itemView.tv_artist
        var playLine : View = itemView.line_play
        var mItemView : RippleView = itemView.ripple
    }

    internal inner class AlbumHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var albumIv : ImageView = itemView.iv_album
        var singerName : TextView = itemView.tv_singer_name
        var albumName : TextView = itemView.tv_album_name
        var publicTime : TextView = itemView.tv_public_time
        var item : RippleView = itemView.ripple1
    }

    private fun equalPosition(position: Int){
        if (position != mLastPosition){
            if (mLastPosition != -1) notifyItemChanged(mLastPosition)
            mLastPosition = position
        }
        notifyItemChanged(position)
    }
}