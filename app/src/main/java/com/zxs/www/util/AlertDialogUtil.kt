package com.zxs.www.util

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.text.TextUtils

import com.zxs.www.service.DownloadService
import com.zxs.www.service.ICallbackResult


class AlertDialogUtil private constructor() {

    private val h5Dialog: PopupDialog? = null

    private var apkDialog: AlertDialog? = null

    /**
     * 弹框
     * @param mContext
     * @param description  描述
     * @param download_url  下载地址
     * @param isForceUpdate  是否强制更新
     * @param appIcon  app图标
     */
    fun alertVersion(mContext: Activity, description: String, download_url: String,
                     isForceUpdate: Boolean, appIcon: Int, application_id: String): AlertDialog {
        if (apkDialog != null && apkDialog!!.isShowing) {
            apkDialog!!.dismiss()
        }
        var natigave = "取消"
        if (isForceUpdate) {
            natigave = "退出"
        }
        val builder = AlertDialog.Builder(mContext)
        builder.setMessage("您有新的版本,请下载更新!\n更新内容：\n$description")
        builder.setTitle("提示")
        builder.setPositiveButton("更新") { dialog, which ->
            dialog.dismiss()
            if (!TextUtils.isEmpty(download_url)) {
                openDownLoadService(mContext, download_url, appIcon, "中...", application_id)
            }
        }
        builder.setNegativeButton(natigave) { dialog, which ->
            dialog.dismiss()
            if (isForceUpdate) {
                apkDialog = null
                System.exit(0)
            } else {

            }
        }
        apkDialog = builder.create()
        apkDialog!!.setCancelable(false)
        apkDialog!!.setCanceledOnTouchOutside(false)
        apkDialog!!.show()
        return apkDialog as AlertDialog
    }

    companion object {

        private var instance: AlertDialogUtil? = null

        @Synchronized
        fun getInstance(): AlertDialogUtil {
            if (instance == null) {
                instance = AlertDialogUtil()
            }
            return instance as AlertDialogUtil
        }
    }

    /**
     * 打开升级
     */
    fun openDownLoadService(context: Context, downurl: String, appIcon: Int, tilte: String, application_id: String) {
        val callback = object : ICallbackResult {
            public override fun OnBackResult(s: Any) {}
        }

        val conn = object : ServiceConnection {
            public override fun onServiceDisconnected(name: ComponentName) {}

            public override fun onServiceConnected(name: ComponentName, service: IBinder) {
                val binder = service as DownloadService.DownloadBinder
                binder.addCallback(callback)
                binder.start()
            }
        }
        val intent = Intent(context, DownloadService::class.java)
        intent.putExtra(DownloadService.BUNDLE_KEY_DOWNLOAD_URL, downurl)
        intent.putExtra(DownloadService.BUNDLE_KEY_DOWNLOAD_APPLICATION_ID, application_id)
        intent.putExtra(DownloadService.BUNDLE_KEY_DOWNLOAD_ICON, appIcon)
        intent.putExtra(DownloadService.BUNDLE_KEY_TITLE, tilte)
        context.startService(intent)
        context.bindService(intent, conn, Context.BIND_AUTO_CREATE)
    }
}