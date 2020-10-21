package com.music.kotlinqq.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.andexert.library.RippleView
import com.music.kotlinqq.R
import com.music.kotlinqq.app.Constant
import com.music.kotlinqq.bean.HistorySong
import com.music.kotlinqq.callback.OnItemClickListener
import com.music.kotlinqq.util.FileUtil
import kotlinx.android.synthetic.main.footer_local_songs_item.view.*
import kotlinx.android.synthetic.main.recycler_song_search_item.view.*

/**
 * @author cyl
 * @date 2020/9/23
 */
class HistoryAdapter(private val mHistoryList : MutableList<HistorySong>, private val mContext : Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val footerViewType = 1
    private val itemViewType = 0
    private var mLastPosition = -1
    private var onItemClickListener : ((tag : Int) -> Unit)? = null

    fun setOnItemClickListener(onItemClick: (tag : Int) -> Unit){
        this.onItemClickListener = onItemClick
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       return when (viewType){
            itemViewType -> {
                val view = LayoutInflater.from(mContext).inflate(R.layout.recycler_song_search_item, parent, false)
                ViewHolder(view)
            }
            else -> {
                val footerView = LayoutInflater.from(mContext).inflate(R.layout.footer_local_songs_item, parent, false)
                FooterHolder(footerView)
            }
        }
    }

    override fun getItemCount(): Int {
        return mHistoryList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position + 1 == itemCount) footerViewType else itemViewType
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (viewHolder is ViewHolder){
            val holder = viewHolder as ViewHolder
            val history = mHistoryList[position]
            holder.songNameTv.text = history.name
            holder.singerTv.text = history.singer

            if (history.songId == FileUtil.getSong()!!.songId){
                holder.playLine.visibility = View.VISIBLE
                mLastPosition = position
                holder.songNameTv.setTextColor(mContext.resources.getColor(R.color.yellow))
                holder.singerTv.setTextColor(mContext.resources.getColor(R.color.yellow))
            } else {
                holder.playLine.visibility = View.INVISIBLE
                holder.songNameTv.setTextColor(mContext.resources.getColor(R.color.white))
                holder.singerTv.setTextColor(mContext.resources.getColor(R.color.white_blue))
            }
            holder.mItemView.setOnRippleCompleteListener {
                onItemClickListener!!.invoke(position)
                equalPosition(position)
            }
        } else {
            val footerHolder = viewHolder as FooterHolder
            footerHolder.numTv.text = "这里会记录你最近播放的${Constant.HISTORY_MAX_SIZE}首歌"
        }
    }

    internal inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var songNameTv : TextView = itemView.tv_title
        var singerTv : TextView = itemView.tv_artist
        var mItemView : RippleView = itemView.ripple
        var playLine : View = itemView.line_play
    }

    internal inner class FooterHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        var numTv : TextView = itemView.tv_song_num
    }

    // 判断点击的是否为上一个点击的项目
    private fun equalPosition(position : Int){
        if (position != mLastPosition){
            notifyItemChanged(mLastPosition)
            mLastPosition = position
        }
        notifyItemChanged(position)
    }
}