package com.zxs.www.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;

import com.zxs.www.service.DownloadService;
import com.zxs.www.service.ICallbackResult;


public class AlertDialogUtil {

    private static AlertDialogUtil instance;

    private PopupDialog h5Dialog;

    private AlertDialog apkDialog;

    private AlertDialogUtil() {
    }

    public static synchronized AlertDialogUtil getInstance() {
        if (instance == null) {
            instance = new AlertDialogUtil();
        }
        return instance;
    }

    /**
     * 弹框
     * @param mContext
     * @param description  描述
     * @param download_url  下载地址
     * @param isForceUpdate  是否强制更新
     * @param appIcon  app图标
     */
    public AlertDialog alertVersion(final Activity mContext, final String description, final String download_url, final boolean isForceUpdate,final int appIcon) {
        if (apkDialog != null && apkDialog.isShowing()) {
            apkDialog.dismiss();
        }
        String natigave = "取消";
        if (isForceUpdate) {
            natigave = "退出";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("您有新的版本,请下载更新!\n更新内容：\n"+description);
        builder.setTitle("提示");
        builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (!TextUtils.isEmpty(download_url)) {
                    openDownLoadService(mContext, download_url,appIcon, "中...");
                }
            }
        });
        builder.setNegativeButton(natigave, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (isForceUpdate) {
                    apkDialog = null;
                    System.exit(0);
                } else {

                }
            }
        });
        apkDialog = builder.create();
        apkDialog.setCancelable(false);
        apkDialog.setCanceledOnTouchOutside(false);
        apkDialog.show();
        return apkDialog;
    }

    /**

    /**
     * 打开升级
     *
     * @param context
     * @param downurl
     * @param tilte
     */
    public static void openDownLoadService(Context context, String downurl,int appIcon, String tilte) {
        final ICallbackResult callback = new ICallbackResult() {
            @Override
            public void OnBackResult(Object s) {
            }
        };

        ServiceConnection conn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                DownloadService.DownloadBinder binder = (DownloadService.DownloadBinder) service;
                binder.addCallback(callback);
                binder.start();
            }
        };
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(DownloadService.BUNDLE_KEY_DOWNLOAD_URL, downurl);
        intent.putExtra(DownloadService.BUNDLE_KEY_DOWNLOAD_ICON, appIcon);
        intent.putExtra(DownloadService.BUNDLE_KEY_TITLE, tilte);
        context.startService(intent);
        context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

}
