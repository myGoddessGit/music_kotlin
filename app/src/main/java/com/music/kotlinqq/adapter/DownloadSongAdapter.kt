package com.music.kotlinqq.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.andexert.library.RippleView
import com.music.kotlinqq.R
import com.music.kotlinqq.app.App
import com.music.kotlinqq.bean.DownloadSong
import com.music.kotlinqq.util.FileUtil
import kotlinx.android.synthetic.main.footer_local_songs_item.view.*
import kotlinx.android.synthetic.main.recycler_song_search_item.view.*

/**
 * @author cyl
 * @date 2020/9/23
 * 已下载歌曲的适配器
 */
class DownloadSongAdapter(private val mDownloadSongList : MutableList<DownloadSong>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val footerViewType = 1
    private val itemViewType = 0
    private var mLastPosition = -1

    private var onItemClickListener : ((tag : Int) -> Unit)? = null

    fun setOnItemClickListener(onItemClickListener: (tag : Int) -> Unit){
        this.onItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType){
            itemViewType -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_song_search_item, parent, false)
                ViewHolder(view)
            }
            else -> {
                val footerView = LayoutInflater.from(parent.context).inflate(R.layout.footer_local_songs_item, parent, false)
                FooterHolder(footerView)
            }
        }
    }

    override fun getItemCount(): Int {
        return mDownloadSongList.size + 1
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (viewHolder is ViewHolder){
            val holder = viewHolder as ViewHolder
            val downloadSong = mDownloadSongList[position]
            holder.songNameTv.text = downloadSong.name
            holder.singerTv.text = downloadSong.singer
            // 根据点击显示
            if (downloadSong.songId == FileUtil.getSong()!!.songId){
                holder.playLine.visibility = View.VISIBLE
                mLastPosition = position
                holder.songNameTv.setTextColor(App.getContext().resources.getColor(R.color.yellow))
                holder.singerTv.setTextColor(App.getContext().resources.getColor(R.color.yellow))
            } else {
                holder.playLine.visibility = View.INVISIBLE
                holder.songNameTv.setTextColor(App.getContext().resources.getColor(R.color.white))
                holder.singerTv.setTextColor(App.getContext().resources.getColor(R.color.white_blue))
            }
            holder.item.setOnRippleCompleteListener {
                if (onItemClickListener != null){
                    onItemClickListener!!.invoke(position)
                }
                equalPosition(position)
            }
        } else {
            val footerHolder = viewHolder as FooterHolder
            footerHolder.numTv.text = "共${mDownloadSongList.size}首音乐"
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position + 1 == itemCount) footerViewType else itemViewType
    }

    internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var songNameTv : TextView = itemView.tv_title
        var singerTv : TextView = itemView.tv_artist
        var playLine : View = itemView.line_play
        var item : RippleView = itemView.ripple
    }

    internal inner class FooterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var numTv : TextView = itemView.tv_song_num
    }

    // 判断点击的是否为上一个点击的项目
    private fun equalPosition(position: Int){
        if (position != mLastPosition){
            notifyItemChanged(mLastPosition)
            mLastPosition = position
        }
        notifyItemChanged(position)
    }
}