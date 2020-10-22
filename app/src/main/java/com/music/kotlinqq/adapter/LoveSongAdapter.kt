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
import com.music.kotlinqq.bean.Love
import com.music.kotlinqq.util.FileUtil
import kotlinx.android.synthetic.main.recycler_song_search_item.view.*

/**
 * @author cyl
 * @date 2020/9/23
 */
class LoveSongAdapter(private val mLoveList : MutableList<Love>, private val mContext : Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val footerViewType = 1
    private val itemViewType = 0
    private var mLastPosition = -1
    private var mOnItemClickListener : ((tag : Int) -> Unit)? = null

    fun setOnItemClickListener(onItemClickListener: (tag : Int) -> Unit){
        this.mOnItemClickListener = onItemClickListener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
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
        return mLoveList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position + 1 == itemCount) footerViewType else itemViewType
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        if (viewHolder is ViewHolder){
            val holder = viewHolder as ViewHolder
            val love = mLoveList[position]
            holder.songNameTv.text = love.name
            holder.singerTv.text = love.singer
            if (love.songId == FileUtil.getSong()!!.songId){
                holder.playLine.visibility = View.VISIBLE
                mLastPosition = position
                holder.songNameTv.setTextColor(mContext.resources.getColor(R.color.yellow))
                holder.singerTv.setTextColor(mContext.resources.getColor(R.color.yellow))
            } else {
                holder.playLine.visibility = View.INVISIBLE
                holder.songNameTv.setTextColor(mContext.resources.getColor(R.color.white))
                holder.singerTv.setTextColor(mContext.resources.getColor(R.color.white_blue))
            }
            holder.item.setOnRippleCompleteListener {
                if (mOnItemClickListener != null){
                    mOnItemClickListener!!.invoke(position)
                }
                equalPosition(position)
            }
        } else {
            val footerHolder = viewHolder as FooterHolder
            footerHolder.numTv.text = "共${mLoveList.size}首音乐"
        }
    }

    internal inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var songNameTv: TextView = itemView.tv_title
        var singerTv: TextView = itemView.tv_artist
        var playLine: View = itemView.line_play
        var item: RippleView = itemView.ripple
    }

    internal inner class FooterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var numTv: TextView = itemView.findViewById(R.id.tv_song_num)
    }

    private fun equalPosition(position : Int){
        if (position != mLastPosition){
            notifyItemChanged(mLastPosition)
            mLastPosition = position
        }
        notifyItemChanged(position)
    }
}