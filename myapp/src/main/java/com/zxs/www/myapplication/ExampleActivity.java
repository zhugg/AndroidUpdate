package com.zxs.www.myapplication;

import android.app.Activity;
import android.os.Bundle;

import com.zxs.www.util.AlertDialogUtil;

public class ExampleActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String url = "http://618bc512975a1d143f1d8d53bcf3ea55.apk";
        boolean isForceUpdate = false;
        String description = "1、修复已知bug\n2、添加XXX功能";
        AlertDialogUtil.getInstance().alertVersion(this, description, url, isForceUpdate, R.drawable.icon);

    }
}
