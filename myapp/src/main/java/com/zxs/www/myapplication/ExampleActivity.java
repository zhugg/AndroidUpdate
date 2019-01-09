package com.zxs.www.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.zxs.www.util.AlertDialogUtil;

public class ExampleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        //下载一个手电筒app
        String url = "https://imtt.dd.qq.com/16891/4EBBAFB877A11794A1CEF4E495B73705.apk?fsname=com.devuni.flashlight_10.10.6_20181226.apk";
        boolean isForceUpdate = false;
        String description = "1、修复已知bug\n2、添加XXX功能";
        AlertDialogUtil.getInstance().alertVersion(this, description, url, isForceUpdate, R.mipmap.ic_launcher,BuildConfig.APPLICATION_ID);

    }
}
