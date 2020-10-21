package com.music.kotlinqq.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.music.kotlinqq.R
import com.music.kotlinqq.util.CommonUtil

/**
 * @author cyl
 * @date 2020/9/26
 */
class WelComeActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CommonUtil.hideStatusBar(this, true)
        window.setBackgroundDrawable(null)
        setContentView(R.layout.activity_welcome)

        // 申请权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        } else {
            Handler().postDelayed({getHome()}, 2000)
        }
    }
    private fun getHome(){
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Handler().postDelayed({getHome()}, 2000)
            } else {
                CommonUtil.showToast(this, "拒绝该权限无法使用该程序")
                finish()
            }
        }
    }
}