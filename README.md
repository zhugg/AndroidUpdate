# AndroidUpdate
用于app更新  一行代码集成下载加安装

示例：
AlertDialogUtil.getInstance().alertVersion(this, description, url, isForceUpdate, R.mipmap.ic_launcher,BuildConfig.APPLICATION_ID);



gradle文件添加：

    repositories {

        jcenter()

        maven { url"https://jitpack.io" }

    }

    dependencies {

     implementation 'com.github.zhugg:AndroidUpdate:1.1.0'

    }
    

AndroidManifest.xml文件添加：

        <!--7.0文件-->
        <provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="${applicationId}.fileProvider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
        

代码中动态申请权限：

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }


