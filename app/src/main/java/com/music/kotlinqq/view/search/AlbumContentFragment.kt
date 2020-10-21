package com.music.kotlinqq.view.search

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.andexert.library.RippleView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.github.florent37.materialviewpager.MaterialViewPager
import com.music.kotlinqq.R
import com.music.kotlinqq.base.fragment.AttachFragment
import com.music.kotlinqq.app.Constant.*
import com.music.kotlinqq.bean.AlbumCollection
import com.music.kotlinqq.bean.SearchHistory
import com.music.kotlinqq.event.AlbumCollectionEvent
import com.music.kotlinqq.util.CommonUtil
import kotlinx.android.synthetic.main.fragment_album_content.view.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import org.greenrobot.eventbus.EventBus
import org.litepal.LitePal

/**
 * @author cyl
 * @date 2020/9/25
 */
class AlbumContentFragment : AttachFragment() {

    private var mAlbumName: String? = null
    private var mSingerName: String? = null
    private var mAlbumPic: String? = null
    private var mPublicTime: String? = null
    private var mId: String? = null
    private var mViewPager: MaterialViewPager? = null
    private var toolBar: Toolbar? = null
    private var mAlbumBackground: RelativeLayout? = null
    private var mSingerNameTv: TextView? = null
    private var mPublicTimeTv: TextView? = null
    private var mAlbumPicIv: ImageView? = null
    private var mLoveBtn: MenuItem? = null
    private var mLove = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getBundle()
        val view = inflater.inflate(R.layout.fragment_album_content, container, false)
        mViewPager = view.materialViewPager
        toolBar = mViewPager?.toolbar
        mAlbumBackground = mViewPager?.headerBackgroundContainer?.findViewById(R.id.relative_album)
        mAlbumPicIv = mViewPager?.headerBackgroundContainer?.findViewById(R.id.iv_album)
        mSingerNameTv = mViewPager?.headerBackgroundContainer?.findViewById(R.id.tv_singer_name)
        mPublicTimeTv = mViewPager?.headerBackgroundContainer?.findViewById(R.id.tv_public_time)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.love, menu)
        mLoveBtn = menu.findItem(R.id.btn_love)
        showLove()
        super.onCreateOptionsMenu(menu, inflater)
    }

    @SuppressLint("SetTextI18n")
    private fun initView(){
        toolBar?.title = mAlbumName
        val target = object : SimpleTarget<Drawable>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL){
            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                val bitmap = (resource as BitmapDrawable).bitmap
                mAlbumBackground?.background = CommonUtil.getForegroundDrawable(bitmap)
                mAlbumPicIv?.setImageBitmap(bitmap)
            }
        }
        Glide.with(attachActivity)
            .load(mAlbumPic)
            .apply(RequestOptions.placeholderOf(R.drawable.welcome))
            .apply(RequestOptions.errorOf(R.drawable.welcome))
            .into(target)
        mSingerNameTv?.text = "歌手$mSingerName"
        mPublicTimeTv?.text = "发行时间$mPublicTime"
        toolBar?.setTitleTextColor(attachActivity.resources.getColor(R.color.white))
        if (toolBar != null){
            (attachActivity as AppCompatActivity).setSupportActionBar(toolBar)
            val actionBar = (attachActivity as AppCompatActivity).supportActionBar
            if (actionBar != null){
                actionBar.setDisplayHomeAsUpEnabled(true)
                actionBar.setDisplayShowHomeEnabled(true)
                actionBar.setDisplayShowTitleEnabled(true)
                actionBar.setDisplayUseLogoEnabled(false)
                actionBar.setHomeButtonEnabled(true)
            }
        }
        toolBar?.setNavigationOnClickListener {
            attachActivity.supportFragmentManager.popBackStack()
        }
        mViewPager?.viewPager?.adapter = object : FragmentStatePagerAdapter(attachActivity.supportFragmentManager){
            override fun getItem(position: Int): Fragment? {
                return when (position){
                    0 -> {
                        AlbumSongFragment.newInstance(AlbumSongFragment.ALBUM_SONG, mId!!, mPublicTime!!)
                    }
                    1 -> {
                        AlbumSongFragment.newInstance(AlbumSongFragment.ALBUM_INFORMATION, mId!!, mPublicTime!!)
                    }
                    else ->{
                        null
                    }
                }
            }

            override fun getCount(): Int {
                return 2
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return when (position){
                    0 -> {
                        "歌曲列表"
                    }
                    1 -> {
                        "专辑信息"
                    }
                    else -> {
                        ""
                    }
                }
            }
        }
        mViewPager?.pagerTitleStrip?.setViewPager(mViewPager?.viewPager)
        mViewPager?.pagerTitleStrip?.setIndicatorColorResource(R.color.yellow)
        mViewPager?.pagerTitleStrip?.tabBackground = R.color.tab
        mViewPager?.pagerTitleStrip?.setTextColorStateListResource(R.color.white)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.btn_love){
            if (mLove){
                LitePal.deleteAllAsync(AlbumCollection::class.java, "albumId=?", mId).listen {
                    mLoveBtn?.setIcon(R.drawable.favorites)
                    CommonUtil.showToast(attachActivity, "你已取消收藏该专辑")
                }
            } else {
                val albumCollection = AlbumCollection()
                albumCollection.albumId = mId
                albumCollection.albumName = mAlbumName
                albumCollection.albumPic = mAlbumPic
                albumCollection.publicTime = mPublicTime
                albumCollection.singerName = mSingerName
                albumCollection.saveAsync().listen {
                    mLoveBtn?.setIcon(R.drawable.favorites_selected)
                    CommonUtil.showToast(attachActivity, "收藏专辑成功")
                }
            }
            mLove = !mLove
            EventBus.getDefault().post(AlbumCollectionEvent())
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showLove(){
        if (LitePal.where("albumId=?", mId).find(AlbumCollection::class.java).size != 0){
            mLove = true
            mLoveBtn?.setIcon(R.drawable.favorites_selected)
        } else {
            mLove = false
            mLoveBtn?.setIcon(R.drawable.favorites)
        }
    }

    companion object {
        fun newInstance(id : String, albumName : String, albumPic : String, singerName : String, publicTime : String) : Fragment{
            val albumContentFragment = AlbumContentFragment()
            val bundle = Bundle()
            bundle.putString(ALBUM_ID_KEY, id)
            bundle.putString(ALBUM_NAME_KEY, albumName)
            bundle.putString(ALBUM_PIC_KEY, albumPic)
            bundle.putString(SINGER_NAME_KEY, singerName)
            bundle.putString(PUBLIC_TIME_KEY, publicTime)
            albumContentFragment.arguments = bundle
            return albumContentFragment
        }
    }

    private fun getBundle(){
        if (arguments != null){
            mId = arguments?.getString(ALBUM_ID_KEY)
            mAlbumName = arguments?.getString(ALBUM_NAME_KEY)
            mAlbumPic = arguments?.getString(ALBUM_PIC_KEY)
            mSingerName = arguments?.getString(SINGER_NAME_KEY)
            mPublicTime = arguments?.getString(PUBLIC_TIME_KEY)
        }
    }

    open class SearchFragment : AttachFragment(){
        private var mSeekEdit : EditText? = null
        private var mSeekTv : RippleView? = null
        private var mBackIv : RippleView? = null

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val view = inflater.inflate(R.layout.fragment_search, container, false)
            mSeekEdit = view.edit_seek
            mSeekTv = view.tv_search
            mBackIv = view.iv_back
            replaceFragment(SearchHistoryFragment())
            return view
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onActivityCreated(savedInstanceState: Bundle?) {
            super.onActivityCreated(savedInstanceState)
            CommonUtil.showKeyboard(mSeekEdit!!, attachActivity)
            mSeekTv?.setOnClickListener {
                CommonUtil.closeKeyboard(mSeekEdit!!, attachActivity)
                mSeekEdit?.isCursorVisible = false
                if (mSeekEdit?.text.toString().trim().isEmpty()){
                    mSeekEdit?.setText(mSeekEdit?.hint.toString().trim())
                }
                saveDatabase(mSeekEdit?.text.toString())
                replaceFragment(ContentFragment.newInstance(mSeekEdit?.text.toString()))
            }
            mSeekEdit?.setOnTouchListener { _, event ->
                if (MotionEvent.ACTION_DOWN == event.action){
                    mSeekEdit?.isCursorVisible = true
                }
                false
            }
            mBackIv?.setOnClickListener {
                CommonUtil.closeKeyboard(mSeekEdit!!, attachActivity)
                attachActivity.supportFragmentManager.popBackStack()
            }
        }

        private fun saveDatabase(seekHistory : String){
            val searchHistoryList = LitePal.where("history=?", seekHistory).find(SearchHistory::class.java)
            if (searchHistoryList.size == 1){
                LitePal.delete(SearchHistory::class.java, searchHistoryList[0].id.toLong())
            }
            val searchHistory = SearchHistory()
            searchHistory.history = seekHistory
            searchHistory.save()
        }

        public fun setSeekEdit(seek : String){
            mSeekEdit?.setText(seek)
            mSeekEdit?.isCursorVisible = false
            mSeekEdit?.setSelection(seek.length)
            CommonUtil.closeKeyboard(mSeekEdit!!, attachActivity)
            saveDatabase(seek)
            replaceFragment(ContentFragment.newInstance(mSeekEdit?.text.toString()))
        }

        // 搜索后的页面
        private fun replaceFragment(fragment: Fragment){
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment)
            transaction.commit()
        }
    }
}