package com.music.kotlinqq.view.main.download

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.apkfuns.logutils.LogUtils
import com.example.SpeedDialog.dialog.SpeedDialog
import com.music.kotlinqq.R
import com.music.kotlinqq.adapter.DownloadingAdapter
import com.music.kotlinqq.app.Constant
import com.music.kotlinqq.base.fragment.AttachFragment
import com.music.kotlinqq.bean.DownloadInfo
import com.music.kotlinqq.callback.OnDeleteClickListener
import com.music.kotlinqq.callback.OnItemClickListener
import com.music.kotlinqq.event.DownloadEvent
import com.music.kotlinqq.service.DownloadService
import kotlinx.android.synthetic.main.fragment_download_ing.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.litepal.LitePal
import java.util.*

/**
 * @author cyl
 * @date 2020/9/24
 */
class DownloadingFragment : AttachFragment(){

    private var mDownloadInfoList : MutableList<DownloadInfo>? = null // 下载队列
    private val mAdapter by lazy { DownloadingAdapter(mDownloadInfoList!!) }
    private var mDownloadBinder : DownloadService.DownloadBinder? = null
    private val mDownloadConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mDownloadBinder = service as DownloadService.DownloadBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_download_ing, container, false)
        EventBus.getDefault().register(this)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        initRecycler()
        val downIntent = Intent(attachActivity, DownloadService::class.java)
        attachActivity.bindService(downIntent, mDownloadConnection, Context.BIND_AUTO_CREATE)
        super.onActivityCreated(savedInstanceState)
    }

    private fun initRecycler(){
        mDownloadInfoList = LinkedList()
        mDownloadInfoList?.addAll(LitePal.findAll(DownloadInfo::class.java, true))
        songDowningRecycle.itemAnimator = null
        songDowningRecycle.layoutManager = layoutManager
        songDowningRecycle.adapter = mAdapter
        // 暂停
        mAdapter.setOnItemClickListener(object : OnItemClickListener{
            override fun onClick(position: Int) {
                val downloadInfo = mDownloadInfoList!![position]
                LogUtils.d("DownloadingFragment", mDownloadInfoList!![position].status)
                if (downloadInfo.status == Constant.TYPE_DOWNLOAD_PAUSED) {
                    mDownloadBinder?.startDownload(downloadInfo)
                } else {
                    mDownloadBinder?.pauseDownload(downloadInfo.songId)
                }
            }
        })
        // 取消下载
        mAdapter.setOnDeleteClickListener(object : OnDeleteClickListener{
            override fun onClick(position: Int) {
                val speedDialog = SpeedDialog(attachActivity, SpeedDialog.SELECT_TYPE)
                speedDialog.setTitle("取消下载")
                    .setMessage("确定不再下载吗")
                    .setSureText("删除")
                    .setSureClickListener {
                        mDownloadBinder?.cancelDownload(mDownloadInfoList!![position])
                    }
                    .show()
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public fun onDownloadingMessage(event : DownloadEvent){
        when (event.getDownloadStatus()) {
            Constant.TYPE_DOWNLOADING, Constant.TYPE_DOWNLOAD_PAUSED ->{
                val downloadInfo = event.getDownloadInfo()
                mDownloadInfoList?.removeAt(downloadInfo.position)
                mDownloadInfoList?.add(downloadInfo.position, downloadInfo)
                mAdapter.notifyItemChanged(downloadInfo.position)
            }
            Constant.TYPE_DOWNLOAD_SUCCESS -> {
                resetDownloadingInfoList()
                mAdapter.notifyDataSetChanged()
            }
            Constant.TYPE_DOWNLOAD_CANCELED -> {
                resetDownloadingInfoList()
                mAdapter.notifyDataSetChanged()
            }
            Constant.TYPE_DOWNLOAD_ADD -> {
                resetDownloadingInfoList()
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * 重新从数据库中读取需要下载的歌曲
     */
    private fun resetDownloadingInfoList(){
        mDownloadInfoList?.clear()
        val temp = LitePal.findAll(DownloadInfo::class.java, true)
        if (temp.size != 0) {
            mDownloadInfoList?.addAll(temp)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }
}