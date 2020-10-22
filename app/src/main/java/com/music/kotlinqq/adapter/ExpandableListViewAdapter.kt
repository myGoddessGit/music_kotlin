package com.music.kotlinqq.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.andexert.library.RippleView
import com.music.kotlinqq.R
import com.music.kotlinqq.bean.AlbumCollection
import com.music.kotlinqq.util.CommonUtil
import kotlinx.android.synthetic.main.item_first.view.*
import kotlinx.android.synthetic.main.item_second.view.*

/**
 * @author cyl
 * @date 2020/9/23
 * 二级菜单适配类
 */
class ExpandableListViewAdapter(private val mContext : Context, private val mGroupStrings : Array<String>, private val mAlbumCollectionList : MutableList<MutableList<AlbumCollection>>) : BaseExpandableListAdapter() {

    /**
     * 函数可以作为参数
     */
    private var mChildClickListener : ((tag1 : Int, tag2 : Int) -> Unit)? = null

    fun setOnChildItemClickListener(onChildItemClickListener: (tag1 : Int, tag2 : Int) -> Unit){
        this.mChildClickListener = onChildItemClickListener
    }
    override fun getGroup(groupPosition: Int): Any {
        return mGroupStrings[groupPosition]
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    // 绘制一级列表
    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view : View
        val groupViewHolder : GroupViewHolder
        if (convertView == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.item_first, parent, false)
            groupViewHolder = GroupViewHolder()
            groupViewHolder.groupTextView = view.tv_new_song
            groupViewHolder.pointIv = view.iv_point
            view.tag  = groupViewHolder
        } else {
            view = convertView
            groupViewHolder = view.tag as GroupViewHolder
        }
        groupViewHolder.groupTextView!!.text = mGroupStrings[groupPosition]
        if (isExpanded){
            groupViewHolder.pointIv!!.setImageResource(R.drawable.up)
        } else {
            groupViewHolder.pointIv!!.setImageResource(R.drawable.down)
        }
        return view
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return mAlbumCollectionList[groupPosition].size
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return mAlbumCollectionList[groupPosition][childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
       return groupPosition.toLong()
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val view : View
        val childViewHolder: ChildViewHolder
        if (convertView == null){
            childViewHolder = ChildViewHolder()
            view = LayoutInflater.from(mContext).inflate(R.layout.item_second, parent, false)
            childViewHolder.albumNameTv = view.tv_album_name
            childViewHolder.faceIv = view.iv_album
            childViewHolder.authorTv = view.tv_author
            childViewHolder.childView = view.ripple
            view.tag = childViewHolder
        } else {
            view = convertView
            childViewHolder = view.tag as ChildViewHolder
        }
        view.setBackgroundResource(R.color.translucent)
        childViewHolder.albumNameTv!!.text = mAlbumCollectionList[groupPosition][childPosition].albumName
        childViewHolder.authorTv!!.text = mAlbumCollectionList[groupPosition][childPosition].singerName
        CommonUtil.setImgWithGlide(mContext, mAlbumCollectionList[groupPosition][childPosition].albumPic, childViewHolder.faceIv!!)
        childViewHolder.childView!!.setOnRippleCompleteListener {
            mChildClickListener!!.invoke(groupPosition, childPosition)
        }
        return view
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupCount(): Int {
       return mGroupStrings.size
    }

    inner class GroupViewHolder {
        var groupTextView : TextView? = null
        var pointIv : ImageView? = null
    }

    inner class ChildViewHolder {
        var albumNameTv : TextView? = null
        var faceIv : ImageView? = null
        var authorTv : TextView? = null
        var childView : RippleView? = null
    }
}