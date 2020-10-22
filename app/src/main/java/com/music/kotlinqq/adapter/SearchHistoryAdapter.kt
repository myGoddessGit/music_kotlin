package com.music.kotlinqq.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.andexert.library.RippleView
import com.music.kotlinqq.R
import com.music.kotlinqq.bean.SearchHistory
import kotlinx.android.synthetic.main.recycler_seek_history_item.view.*

/**
 * @author cyl
 * @date 2020/9/23
 */
class SearchHistoryAdapter(private val mSearchHistoryList : MutableList<SearchHistory>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val mHistoryType = 0
    private val mFooterType = 1
    private var mOnItemClickListener : ((tag : Int) -> Unit)? = null
    private var mOnDeleteClickListener : ((tag : Int) -> Unit)? = null
    private var mFooterClickListener : (() -> Unit)? = null


    fun setOnItemClickListener(onItemClickListener : (tag : Int) -> Unit){
        mOnItemClickListener = onItemClickListener
    }

    fun setOnDeleteClickListener(onDeleteClickListener : (tag : Int) -> Unit){
        mOnDeleteClickListener = onDeleteClickListener
    }

    fun setFooterClickListener(onFooterClickListener: () -> Unit){
        mFooterClickListener = onFooterClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType){
            mHistoryType -> {
                val view  = LayoutInflater.from(parent.context).inflate(R.layout.recycler_seek_history_item, parent,false)
                HistoryHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.footer_delete_all_history_item, parent,false)
                FooterHolder(view)
            }
        }
    }

    override fun getItemCount(): Int {
       return mSearchHistoryList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position + 1 == itemCount) mFooterType else mHistoryType
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
       if (holder is HistoryHolder){
           val historyHolder = holder as HistoryHolder
           historyHolder.historyTv.text = mSearchHistoryList[position].history
           historyHolder.deleteTv.setOnClickListener {
               mOnDeleteClickListener!!.invoke(position)
           }
           historyHolder.mItemView.setOnRippleCompleteListener {
               mOnItemClickListener!!.invoke(position)
           }
       } else {
           val footerHolder = holder as FooterHolder
           footerHolder.itemView.setOnClickListener {
               mFooterClickListener!!.invoke()
           }
       }
    }

    internal inner class HistoryHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        var historyTv : TextView = itemView.tv_seek_history
        var deleteTv : ImageView = itemView.iv_history_delete
        var mItemView : RippleView = itemView.ripple
    }

    internal inner class FooterHolder(itemView : View) : RecyclerView.ViewHolder(itemView)

}