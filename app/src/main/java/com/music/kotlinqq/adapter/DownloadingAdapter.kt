package com.music.kotlinqq.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.music.kotlinqq.R
import com.music.kotlinqq.app.Constant
import com.music.kotlinqq.bean.DownloadInfo
import com.music.kotlinqq.util.MediaUtil
import kotlinx.android.synthetic.main.recycler_downing_item.view.*

/**
 * @author cyl
 * @date 2020/9/23
 * 正在下载歌曲适配器
 */
class DownloadingAdapter(private val downloadInfoList : MutableList<DownloadInfo>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onItemClickListener : ((tag : Int) -> Unit)? = null

    private var onDeleteClickListener : ((tag : Int) -> Unit)? = null

    fun setOnDeleteClickListener(onDeleteClickListener: (tag : Int) -> Unit){
        this.onDeleteClickListener = onDeleteClickListener
    }

    fun setOnItemClickListener(onItemClickListener : (tag : Int) -> Unit){
        this.onItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view  = LayoutInflater.from(parent.context).inflate(R.layout.recycler_downing_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return downloadInfoList.size
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {
        val holder = viewHolder as ViewHolder
        if (downloadInfoList.size == 0) return
        val downloadInfo = downloadInfoList[i]
        holder.songTv.text = downloadInfo.songName
        when {
            // 准备开始下载
            downloadInfo.status == Constant.DOWNLOAD_READY -> {
                holder.sizeTv.text = "正在获取歌曲大小"
                holder.seekBar.visibility = View.GONE
            }
            // 正在下载
            downloadInfo.status == Constant.DOWNLOAD_ING -> {
                holder.sizeTv.text = MediaUtil.formatSize(downloadInfo.currentSize) + "M" + " / " + MediaUtil.formatSize(downloadInfo.totalSize) + "M"
                holder.seekBar.visibility = View.VISIBLE
            }
            // 下载暂停
            downloadInfo.status == Constant.DOWNLOAD_PAUSED -> {
                holder.sizeTv.text = "已暂停, 点击继续下载"
                holder.seekBar.visibility = View.GONE
            }
            // 还未开始下载
            else -> {
                holder.sizeTv.text = downloadInfo.singer
                holder.seekBar.visibility = View.GONE
            }
        }
        // 消费该事件  让seekBar不能拖动和点击
        holder.seekBar.setOnTouchListener {
            _, _ -> true
        }
        holder.itemView.setOnClickListener {
            onItemClickListener!!.invoke(i)
        }
        // 取消
        holder.cancelTv.setOnClickListener {
            onDeleteClickListener!!.invoke(i)
        }
    }

    internal inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        var songTv : TextView = itemView.songTv
        var sizeTv : TextView = itemView.sizeTv
        var seekBar : SeekBar = itemView.seekBar
        var cancelTv : ImageView = itemView.cancelIv
    }
}