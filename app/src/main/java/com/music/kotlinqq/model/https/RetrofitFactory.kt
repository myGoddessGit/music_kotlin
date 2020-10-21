package com.music.kotlinqq.model.https

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.music.kotlinqq.app.Api
import com.music.kotlinqq.model.https.api.RetrofitService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * @author cyl
 * @date 2020/9/17
 */
object RetrofitFactory {

    private var sOkHttpClient : OkHttpClient? = null
    private var sRetrofit : Retrofit? = null
    private var songUrlRetrofit : Retrofit? = null
    private var sSingerPicRetrofit : Retrofit? = null

    /**
     * 配置Retrofit
     */
    private val retrofit: Retrofit
        @Synchronized get(){
            if (sRetrofit == null) {
                sRetrofit = Retrofit.Builder()
                    .baseUrl(Api.FIDDLER_BASE_QQ_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
            }
            return sRetrofit!!
        }
    /**
     * 获取歌手照片
     */
    private val retrofitOfSinger : Retrofit
        @Synchronized get() {
            if (sSingerPicRetrofit == null){
                sSingerPicRetrofit = Retrofit.Builder()
                    .baseUrl(Api.SINGER_PIC_BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
            }
            return sSingerPicRetrofit!!
        }
    /**
     * 得到播放地址
     */

    private val retrofitOfSongUrl : Retrofit
        @Synchronized get() {
            if (songUrlRetrofit == null) {
                songUrlRetrofit = Retrofit.Builder()
                    .baseUrl(Api.FIDDLER_BASE_SONG_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
            }
            return songUrlRetrofit!!
    }

    private val okHttpClient : OkHttpClient
        @Synchronized get(){
            if (sOkHttpClient == null) {
                val loggingInterceptor = HttpLoggingInterceptor{
                    message -> Log.i("RetrofitLog", "retrofitBack = $message")
                }
                loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                sOkHttpClient = OkHttpClient.Builder()
                    .connectTimeout(100, TimeUnit.SECONDS)
                    .readTimeout(100, TimeUnit.SECONDS)
                    .writeTimeout(100, TimeUnit.SECONDS)
                    .build()
            }
            return sOkHttpClient!!
        }

    fun createRequest() : RetrofitService {
        return retrofit.create(RetrofitService::class.java)
    }

    fun createRequestOfSinger() : RetrofitService {
        return retrofitOfSinger.create(RetrofitService::class.java)
    }

    fun createRequestOfSongUrl() : RetrofitService {
        return retrofitOfSongUrl.create(RetrofitService::class.java)
    }
}