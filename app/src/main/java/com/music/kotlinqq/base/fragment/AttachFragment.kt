package com.music.kotlinqq.base.fragment

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager

/**
 * @author cyl
 * @date 2020/9/24
 */
open class AttachFragment : Fragment(){

    protected var attachActivity = FragmentActivity()
    protected val layoutManager = LinearLayoutManager(attachActivity)
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        attachActivity = context as FragmentActivity
    }
}