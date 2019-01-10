package com.zxs.www.myapplication

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View

import com.zxs.www.util.AlertDialogUtil

class ExampleActivity : Activity() {

    private val name ="";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
    }

    public fun update(view:View) {
        //下载一个手电筒app
        val url = "https://imtt.dd.qq.com/16891/4EBBAFB877A11794A1CEF4E495B73705.apk?fsname=com.devuni.flashlight_10.10.6_20181226.apk"
        val isForceUpdate = false
        val description = "1、修复已知bug\n2、添加XXX功能\n3、\n4、\n5、\n6、\n7"
        AlertDialogUtil.getInstance().alertVersion(this, description, url, isForceUpdate, R.mipmap.ic_launcher, BuildConfig.APPLICATION_ID)
    }

    public fun foceUpdate(view:View) {
        //下载一个手电筒app
        val url = "https://imtt.dd.qq.com/16891/4EBBAFB877A11794A1CEF4E495B73705.apk?fsname=com.devuni.flashlight_10.10.6_20181226.apk"
        val isForceUpdate = true
        val description = "1、修复已知bug\n2、添加XXX功能\n3、强制更新"
        AlertDialogUtil.getInstance().alertVersion(this, description, url, isForceUpdate, R.mipmap.ic_launcher, BuildConfig.APPLICATION_ID)
    }
}
