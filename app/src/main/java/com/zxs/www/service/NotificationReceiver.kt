package com.zxs.www.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


/**
 * description:
 * Date: 2016/11/16 13:23
 * User: Administrator
 */
class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 打开启动页
        //        Intent mainIntent = new Intent(context, AppStartActivity.class);
        //        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //        // 进入特殊的页面
        //        Intent clickIntent = new Intent(context, HomeActivity.class);
        //        Intent[] intents = new Intent[]{mainIntent, clickIntent};
        //        context.startActivities(intents);
    }

    companion object {

        private val TAG = "GETUI"
    }
}