package com.music.kotlinqq.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.andexert.library.RippleView
import com.music.kotlinqq.R
import com.music.kotlinqq.app.App
import com.music.kotlinqq.bean.AlbumSong
import com.music.kotlinqq.util.FileUtil
import kotlinx.android.synthetic.main.recycler_song_search_item.view.*

/**
 * @author cyl
 * @date 2020/9/22
 */
class AlbumSongAdapter(private val songsBeanList: MutableList<AlbumSong.DataBean.ListBean>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var mLastPosition = -1
    private var mSongClick : ((tag : Int) -> Unit)? = null
    private val songType = 1
    private val footerType = 2

    fun setSongClick(songClick: (tag : Int) -> Unit){
        this.mSongClick = songClick
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            songType -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_song_search_item, parent, false)
                ViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.footer_view_player_height, parent, false)
                FooterHolder(view)
            }
        }
    }

    override fun getItemCount(): Int {
        return songsBeanList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position + 1 == itemCount) footerType else songType
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (viewHolder is ViewHolder){
            val holder = viewHolder as ViewHolder
            val songsBean = songsBeanList[position]
            val singer = StringBuilder(songsBean.singer[0].name)
            for (i in 1 until songsBean.singer.size){
                singer.append("、").append(songsBean.singer[i].name)
            }
            holder.artistTv.text = singer.toString()
            holder.titleTv.text = songsBean.songname
            holder.mItemView.setBackgroundResource(R.color.translucent)
            if (songsBean.songmid == FileUtil.getSong()!!.songId){
                holder.playLine.visibility = View.VISIBLE
                holder.titleTv.setTextColor(App.getContext().resources.getColor(R.color.yellow))
                holder.artistTv.setTextColor(App.getContext().resources.getColor(R.color.yellow))
                mLastPosition = position
            } else {
                holder.playLine.visibility = View.VISIBLE
                holder.titleTv.setTextColor(App.getContext().resources.getColor(R.color.white))
                holder.artistTv.setTextColor(App.getContext().resources.getColor(R.color.white_blue))
            }
            holder.mItemView.setOnRippleCompleteListener {
                mSongClick?.invoke(position)
                equalPosition(position)
            }
        }
    }

    internal inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleTv : TextView = itemView.tv_title
        var artistTv : TextView = itemView.tv_artist
        var mItemView: RippleView = itemView.ripple
        var playLine : View = itemView.line_play

    }

    internal inner class FooterHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private fun equalPosition(position : Int){
        if (position != mLastPosition){
            if (mLastPosition != -1) notifyItemChanged(mLastPosition)
            mLastPosition = position
        }
        notifyItemChanged(position)
    }
}