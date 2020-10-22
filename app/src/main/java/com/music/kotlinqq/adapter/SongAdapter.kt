package com.music.kotlinqq.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.andexert.library.RippleView
import com.music.kotlinqq.R
import com.music.kotlinqq.app.App
import com.music.kotlinqq.bean.LocalSong
import com.music.kotlinqq.util.FileUtil
import kotlinx.android.synthetic.main.footer_local_songs_item.view.*
import kotlinx.android.synthetic.main.recycler_song_item.view.*

/**
 * @author cyl
 * @date 2020/9/23
 */
class SongAdapter(private val mMp3InfoList : MutableList<LocalSong>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var footerViewType = 1
    private var itemViewType = 0
    private var mLastPosition = -1
    private var onItemClickListener : ((tag : Int) -> Unit)? = null

    fun setOnItemClickListener(onItemClickListener: (tag : Int) -> Unit){
        this.onItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            itemViewType -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_song_item, parent, false)
                ViewHolder(view)
            }
            else -> {
                val footerView = LayoutInflater.from(parent.context).inflate(R.layout.footer_local_songs_item, parent, false)
                FooterHolder(footerView)
            }
        }
    }

    override fun getItemCount(): Int {
        return mMp3InfoList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position + 1 == itemCount) footerViewType else itemViewType
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (viewHolder is ViewHolder){
            val holder = viewHolder as ViewHolder
            val mp3Info = mMp3InfoList[position]
            holder.songNameTv.text = mp3Info.name
            holder.artistTv.text = mp3Info.singer
            val songId = FileUtil.getSong()!!.songId
            if (mp3Info.songId == songId){
                holder.songNameTv.setTextColor(App.getContext().resources.getColor(R.color.musicStyle_low))
                holder.artistTv.setTextColor(App.getContext().resources.getColor(R.color.musicStyle_low))
                holder.playingIv.visibility = View.VISIBLE
                mLastPosition = position
            } else {
                holder.songNameTv.setTextColor(App.getContext().resources.getColor(R.color.white))
                holder.artistTv.setTextColor(App.getContext().resources.getColor(R.color.white))
                holder.playingIv.visibility = View.GONE
            }
            holder.songView.setOnRippleCompleteListener {
                onItemClickListener!!.invoke(position)
                equalPosition(position)
            }
        } else {
            val footerHolder = viewHolder as FooterHolder
            footerHolder.numTv.text = "共${mMp3InfoList.size}首音乐"
        }
    }

    internal inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var songNameTv : TextView = itemView.tv_song_name
        var artistTv : TextView = itemView.tv_artist
        var playingIv : ImageView = itemView.iv_playing
        var songView : RippleView = itemView.ripple
    }

    internal inner class FooterHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var numTv : TextView = itemView.tv_song_num
    }

    private fun equalPosition(position: Int){
        if (position != mLastPosition){
            notifyItemChanged(mLastPosition)
            mLastPosition = position
        }
        notifyItemChanged(position)
    }
}