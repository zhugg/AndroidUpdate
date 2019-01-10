package com.zxs.www.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.text.TextUtils
import android.widget.RemoteViews


import com.zxs.www.R
import com.zxs.www.util.Constant
import com.zxs.www.util.FileUtil

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

import android.app.Notification.DEFAULT_VIBRATE
import android.app.NotificationManager.IMPORTANCE_DEFAULT


/**
 * download service
 */
class DownloadService : Service() {

    private var progress: Int = 0

    private var mNotificationManager: NotificationManager? = null

    private var canceled: Boolean = false

    private var downloadUrl: String? = null

    private var appIcon: Int = 0

    private var mTitle = "正在下载%s"

    //文件保存路径
    private var application_id: String? = null
    private var saveFileName: String? = null
    private val saveFileDir = (FileUtil.rootPath?.absolutePath
            + File.separator
            + "xiangfa"
            + File.separator
            + "download"
            + File.separator)


    private var callback: ICallbackResult? = null

    private var binder: DownloadBinder? = null

    private var serviceIsDestroy = false

    private val mContext = this

    private var downLoadThread: Thread? = null

    private var mNotification: Notification? = null

    private val mHandler = object : Handler() {

        override fun handleMessage(msg: Message) {
            // TODO Auto-generated method stub
            super.handleMessage(msg)
            when (msg.what) {
                0 -> {
                    // 下载完毕
                    mNotificationManager!!.cancel(NOTIFY_ID)
                    installApk()
                }
                2 -> {
                    // 取消通知
                    mNotificationManager!!.cancel(NOTIFY_ID)
                }
                1 -> {
                    val bundle = msg.data
                    val rate = bundle.getInt("downloadCount", 0)
                    if (rate < 100) {
                        val contentview = mNotification!!.contentView
                        contentview.setTextViewText(R.id.tv_download_state, "$mTitle($rate%)")
                        contentview.setProgressBar(R.id.pb_download, 100, rate, false)
                    } else {
                        // 下载完毕后变换通知形式
                        mNotification!!.flags = Notification.FLAG_AUTO_CANCEL
                        mNotification!!.contentView = null
                        val intent = Intent(mContext, application.javaClass)
                        // 告知已完成
                        intent.putExtra("completed", "yes")
                        // 更新参数,注意flags要使用FLAG_UPDATE_CURRENT
                        val contentIntent = PendingIntent.getActivity(
                                mContext, 0, intent,
                                PendingIntent.FLAG_UPDATE_CURRENT)
                        //					mNotification.setLatestEventInfo(mContext, "下载完成",
                        //							"文件已下载完毕", contentIntent);
                        serviceIsDestroy = true
                        stopSelf()// 停掉服务自身
                    }
                    mNotificationManager!!.notify(NOTIFY_ID, mNotification)
                }
                else -> {
                }
            }
        }
    }

    private val mdownApkRunnable = Runnable {
        val dir = File(saveFileDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val apkFile = saveFileName
        val saveFile = File(apkFile!!)
        try {
            if (!saveFile.parentFile.exists()) {
                saveFile.parentFile.mkdirs()
            }
            downloadUpdateFile(downloadUrl, saveFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        downloadUrl = intent.getStringExtra(BUNDLE_KEY_DOWNLOAD_URL)
        application_id = intent.getStringExtra(BUNDLE_KEY_DOWNLOAD_APPLICATION_ID)
        appIcon = intent.getIntExtra(BUNDLE_KEY_DOWNLOAD_ICON, 0)
        saveFileName = saveFileDir + Constant.APP_NAME
        mTitle = String.format(mTitle, intent.getStringExtra(BUNDLE_KEY_TITLE))
        return binder
    }

    private fun getSaveFileName(downloadUrl: String?): String {
        return if (downloadUrl == null || TextUtils.isEmpty(downloadUrl)) {
            ""
        } else downloadUrl.substring(downloadUrl.lastIndexOf("/"))
    }

    override fun onCreate() {
        super.onCreate()
        binder = DownloadBinder()
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        stopForeground(true)// 这个不确定是否有作用
    }

    private fun startDownload() {
        canceled = false
        downloadApk()
    }

    /**
     * 创建通知
     */
    private fun setUpNotification(icon: Int) {
        val tickerText = "下载中..."
        val `when` = System.currentTimeMillis()
        mNotification = Notification(icon, tickerText, `when`)

        // 放置在"正在运行"栏目中
        mNotification!!.flags = Notification.FLAG_ONLY_ALERT_ONCE
        mNotification!!.defaults = DEFAULT_VIBRATE
        mNotification!!.priority = IMPORTANCE_DEFAULT
        val contentView = RemoteViews(packageName,
                R.layout.download_notification_show)
        contentView.setImageViewResource(R.id.iv_image, icon)
        contentView.setTextViewText(R.id.tv_download_state, mTitle)

        // 指定个性化视图
        mNotification!!.contentView = contentView
        mNotification!!.fullScreenIntent = PendingIntent.getActivity(this, 1, // requestCode是0的时候三星手机点击通知栏通知不起作用
                Intent(), PendingIntent.FLAG_UPDATE_CURRENT)

        //        Intent intent = new Intent(this, HomeActivity.class);
        //        PendingIntent contentIntent = PendingIntent.getActivity(this, 1,
        //                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //
        //        // 指定内容意图
        //        mNotification.contentIntent = contentIntent;

        mNotificationManager!!.notify(NOTIFY_ID, mNotification)
    }

    private fun downloadApk() {
        downLoadThread = Thread(mdownApkRunnable)
        downLoadThread!!.start()
    }

    /**
     * 安装apk
     */
    private fun installApk() {
        val apkfile = File(saveFileName!!)
        if (!apkfile.exists()) {
            return
        }
        FileUtil.installAPK(mContext, apkfile, application_id)
    }

    @Throws(Exception::class)
    fun downloadUpdateFile(downloadUrl: String?, saveFile: File): Long {
        var downloadCount = 0
        val currentSize = 0
        var totalSize: Long = 0
        var updateTotalSize = 0

        var httpConnection: HttpURLConnection? = null
        var ips: InputStream? = null
        var fos: FileOutputStream? = null

        try {
            val url = URL(downloadUrl)
            httpConnection = url.openConnection() as HttpURLConnection
            httpConnection
                    .setRequestProperty("User-Agent", "PacificHttpClient")
            if (currentSize > 0) {
                httpConnection.setRequestProperty("RANGE", "bytes="
                        + currentSize + "-")
            }
            httpConnection.connectTimeout = 10000
            httpConnection.readTimeout = 15000
            updateTotalSize = httpConnection.contentLength
            if (httpConnection.responseCode == 404) {
                throw Exception("fail!")
            }
            ips = httpConnection.inputStream
            fos = FileOutputStream(saveFile, false)
            val buffer = ByteArray(1024)
            var readsize :Int
            do {
                readsize = ips.read(buffer)
                if (readsize > 0) {
                    fos.write(buffer, 0, readsize)
                    totalSize += readsize.toLong()
                    // 为了防止频繁的通知导致应用吃紧，百分比增加10才通知一次
                    if (downloadCount == 0 || (totalSize * 100 / updateTotalSize).toInt() - 4 > downloadCount) {
                        downloadCount += 4
                        // 更新进度
                        val msg = mHandler.obtainMessage()
                        msg.what = 1
                        //===
                        val bundle = Bundle()
                        bundle.putInt("downloadCount", downloadCount)
                        bundle.putLong("totalSize", totalSize)
                        bundle.putInt("updateTotalSize", updateTotalSize)
                        msg.data = bundle //mes利用Bundle传递数据
                        // msg.arg1 = downloadCount;
                        mHandler.sendMessage(msg)
                        if (callback != null)
                            callback!!.OnBackResult(progress)
                    }
                }else{
                    break
                }
            } while (true)

            // 下载完成通知安装
            mHandler.sendEmptyMessage(0)
            // 下载完了，cancelled也要设置
            canceled = true

        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect()
            }
            if (ips != null) {
                ips.close()
            }
            if (fos != null) {
                fos.close()
            }
        }
        return totalSize
    }

    inner class DownloadBinder : Binder() {

        var progress: Int = 0
            get() = progress

        val isCanceled: Boolean
            get() = canceled

        fun start() {
            if (downLoadThread == null || !downLoadThread!!.isAlive) {
                progress = 0
                setUpNotification(appIcon)
                object : Thread() {
                    override fun run() {
                        // 下载
                        startDownload()
                    }
                }.start()
            }
        }

        fun cancel() {
            canceled = true
        }

        fun serviceIsDestroy(): Boolean {
            return serviceIsDestroy
        }

        fun cancelNotification() {
            mHandler.sendEmptyMessage(2)
        }

        fun addCallback(callback: ICallbackResult) {
            this@DownloadService.callback = callback
        }
    }

    companion object {

        val BUNDLE_KEY_DOWNLOAD_URL = "download_url"
        val BUNDLE_KEY_DOWNLOAD_APPLICATION_ID = "download_application_id"
        val BUNDLE_KEY_DOWNLOAD_ICON = "download_icon"

        val BUNDLE_KEY_TITLE = "title"

        private val NOTIFY_ID = 0
    }
}
