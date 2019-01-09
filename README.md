# AndroidUpdate
用于app更新  一行代码集成下载加安装

示例：
AlertDialogUtil.getInstance().alertVersion(this, description, url, isForceUpdate, R.drawable.icon);


gradle文件添加：
    repositories {

        jcenter()

        maven { url"https://jitpack.io" }

    }

    dependencies {

        compile 'com.github.zhugg:AndroidUpdate:V1.0'

    }
