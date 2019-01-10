package com.zxs.www.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.support.v4.content.FileProvider
import android.text.TextUtils


import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.util.ArrayList
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


/**
 * 文件操作工具包
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
class FileUtil {

    /**
     * 获取目录文件个数
     *
     * @return
     */
    fun getFileList(dir: File): Long {
        var count: Long = 0
        val files = dir.listFiles()
        count = files.size.toLong()
        for (file in files) {
            if (file.isDirectory) {
                count = count + getFileList(file)// 递归
                count--
            }
        }
        return count
    }

    enum class PathStatus {
        SUCCESS, EXITS, ERROR
    }

    companion object {

        protected var updateDir: File? = null

        protected var updateFile: File? = null

        /**
         * 分隔符.
         */
        val FILE_EXTENSION_SEPARATOR = "."

        /**
         * "/"
         */
        val SEP = File.separator


        /**
         * 写文本文件 在Android系统中，文件保存在 /data/data/PACKAGE_NAME/files 目录下
         *
         * @param context
         */
        fun write(context: Context, fileName: String, content: String?) {
            var content = content
            if (content == null)
                content = ""
            try {
                val fos = context.openFileOutput(fileName,
                        Context.MODE_PRIVATE)
                fos.write(content.toByteArray(charset("utf-8")))
                fos.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        /**
         * 读取文本文件
         *
         * @param context
         * @param fileName
         * @return
         */
        fun read(context: Context, fileName: String): String? {
            try {
                val `in` = context.openFileInput(fileName)
                return readInStream(`in`)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return ""
        }

        fun readInStream(inStream: InputStream): String? {
            try {
                val outStream = ByteArrayOutputStream()
                val buffer = ByteArray(512)
                var length :Int
                do {
                    length = inStream.read(buffer)
                    if (length != -1) {
                        outStream.write(buffer, 0, length)
                    }else{
                        break
                    }
                } while (true)

                outStream.close()
                inStream.close()
                return outStream.toString()
            } catch (e: IOException) {
            }

            return null
        }

        fun createFile(folderPath: String, fileName: String): File {
            val destDir = File(folderPath)
            if (!destDir.exists()) {
                destDir.mkdirs()
            }
            return File(folderPath, fileName + fileName)
        }

        /**
         * 向手机写图片
         *
         * @param buffer
         * @param folder
         * @param fileName
         * @return
         */
        fun writeFile(buffer: ByteArray, folder: String,
                      fileName: String): Boolean {
            var writeSucc = false

            val sdCardExist = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

            var folderPath = ""
            if (sdCardExist) {
                folderPath = (Environment.getExternalStorageDirectory().toString()
                        + File.separator + folder + File.separator)
            } else {
                writeSucc = false
            }

            val fileDir = File(folderPath)
            if (!fileDir.exists()) {
                fileDir.mkdirs()
            }

            val file = File(folderPath + fileName)
            var out: FileOutputStream? = null
            try {
                out = FileOutputStream(file)
                out.write(buffer)
                writeSucc = true
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    out!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            return writeSucc
        }

        /**
         * 根据文件绝对路径获取文件名
         *
         * @param filePath
         * @return
         */
        fun getFileName(filePath: String): String {
            return if (TextUtils.isEmpty(filePath)) "" else filePath.substring(filePath.lastIndexOf(File.separator) + 1)
        }

        /**
         * 根据文件的绝对路径获取文件名但不包含扩展名
         *
         * @param filePath
         * @return
         */
        fun getFileNameNoFormat(filePath: String): String {
            if (TextUtils.isEmpty(filePath)) {
                return ""
            }
            val point = filePath.lastIndexOf('.')
            return filePath.substring(filePath.lastIndexOf(File.separator) + 1,
                    point)
        }

        /**
         * 获取文件扩展名
         *
         * @param fileName
         * @return
         */
        fun getFileFormat(fileName: String): String {
            if (TextUtils.isEmpty(fileName))
                return ""

            val point = fileName.lastIndexOf('.')
            return fileName.substring(point + 1)
        }

        /**
         * 获取文件大小
         *
         * @param filePath
         * @return
         */
        fun getFileSize(filePath: String): Long {
            var size: Long = 0

            val file = File(filePath)
            if (file != null && file.exists()) {
                size = file.length()
            }
            return size
        }

        /**
         * 获取文件大小
         *
         * @param size 字节
         * @return
         */
        fun getFileSize(size: Long): String {
            if (size <= 0)
                return "0"
            val df = java.text.DecimalFormat("##.##")
            val temp = size.toFloat() / 1024
            return if (temp >= 1024) {
                df.format((temp / 1024).toDouble()) + "M"
            } else {
                df.format(temp.toDouble()) + "K"
            }
        }

        /**
         * 转换文件大小
         *
         * @param fileS
         * @return B/KB/MB/GB
         */
        fun formatFileSize(fileS: Long): String {
            val df = java.text.DecimalFormat("#.00")
            var fileSizeString = ""
            if (fileS < 1024) {
                fileSizeString = df.format(fileS.toDouble()) + "B"
            } else if (fileS < 1048576) {
                fileSizeString = df.format(fileS.toDouble() / 1024) + "KB"
            } else if (fileS < 1073741824) {
                fileSizeString = df.format(fileS.toDouble() / 1048576) + "MB"
            } else {
                fileSizeString = df.format(fileS.toDouble() / 1073741824) + "G"
            }
            return fileSizeString
        }

        /**
         * 获取目录文件大小
         *
         * @param dir
         * @return
         */
        fun getDirSize(dir: File?): Long {
            if (dir == null) {
                return 0
            }
            if (!dir.isDirectory) {
                return 0
            }
            var dirSize: Long = 0
            val files = dir.listFiles()
            if (files != null) {

                for (file in files) {
                    if (file.isFile) {
                        dirSize += file.length()
                    } else if (file.isDirectory) {
                        dirSize += file.length()
                        dirSize += getDirSize(file) // 递归调用继续统计
                    }
                }
            }
            return dirSize
        }

        @Throws(IOException::class)
        fun toBytes(`in`: InputStream): ByteArray {
            val out = ByteArrayOutputStream()
            var ch: Int
            do {
                ch = `in`.read()
                if (ch != -1) {
                    out.write(ch)
                }else{
                    break
                }
            } while (true)
            val buffer = out.toByteArray()
            out.close()
            return buffer
        }

        /**
         * 检查文件是否存在
         *
         * @param name
         * @return
         */
        fun checkFileExists(name: String): Boolean {
            val status: Boolean
            if (name != "") {
                val path = Environment.getExternalStorageDirectory()
                val newPath = File(path.toString() + name)
                status = newPath.exists()
            } else {
                status = false
            }
            return status
        }

        /**
         * 检查路径是否存在
         *
         * @param path
         * @return
         */
        fun checkFilePathExists(path: String): Boolean {
            return File(path).exists()
        }

        /**
         * 计算SD卡的剩余空间
         *
         * @return 返回-1，说明没有安装sd卡
         */
        val freeDiskSpace: Long
            get() {
                val status = Environment.getExternalStorageState()
                var freeSpace: Long = 0
                if (status == Environment.MEDIA_MOUNTED) {
                    try {
                        val path = Environment.getExternalStorageDirectory()
                        val stat = StatFs(path.path)
                        val blockSize = stat.blockSize.toLong()
                        val availableBlocks = stat.availableBlocks.toLong()
                        freeSpace = availableBlocks * blockSize / 1024
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                } else {
                    return -1
                }
                return freeSpace
            }

        /**
         * 新建目录
         *
         * @param directoryName
         * @return
         */
        fun createDirectory(directoryName: String): Boolean {
            var status: Boolean
            if (directoryName != "") {
                val path = Environment.getExternalStorageDirectory()
                val newPath = File(path.toString() + directoryName)
                status = newPath.mkdir()
                status = true
            } else
                status = false
            return status
        }

        /**
         * 检查是否安装SD卡
         *
         * @return
         */
        fun checkSaveLocationExists(): Boolean {
            val sDCardStatus = Environment.getExternalStorageState()
            val status: Boolean
            if (sDCardStatus == Environment.MEDIA_MOUNTED) {
                status = true
            } else
                status = false
            return status
        }

        /**
         * 检查是否安装外置的SD卡
         *
         * @return
         */
        fun checkExternalSDExists(): Boolean {

            val evn = System.getenv()
            return evn.containsKey("SECONDARY_STORAGE")
        }

        /**
         * 删除目录(包括：目录里的所有文件)
         *
         * @param fileName
         * @return
         */
        fun deleteDirectory(fileName: String): Boolean {
            var status: Boolean
            val checker = SecurityManager()

            if (fileName != "") {

                val path = Environment.getExternalStorageDirectory()
                val newPath = File(path.toString() + fileName)
                checker.checkDelete(newPath.toString())
                if (newPath.isDirectory) {
                    val listfile = newPath.list()
                    try {
                        for (i in listfile.indices) {
                            val deletedFile = File(newPath.toString() + "/"
                                    + listfile[i].toString())
                            deletedFile.delete()
                        }
                        newPath.delete()
                        status = true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        status = false
                    }

                } else
                    status = false
            } else
                status = false
            return status
        }

        /**
         * 删除文件
         *
         * @param fileName
         * @return
         */
        fun deleteFile(fileName: String): Boolean {
            var status: Boolean
            val checker = SecurityManager()

            if (fileName != "") {

                val path = Environment.getExternalStorageDirectory()
                val newPath = File(path.toString() + fileName)
                checker.checkDelete(newPath.toString())
                if (newPath.isFile) {
                    try {
                        newPath.delete()
                        status = true
                    } catch (se: SecurityException) {
                        se.printStackTrace()
                        status = false
                    }

                } else
                    status = false
            } else
                status = false
            return status
        }

        /**
         * 删除空目录
         *
         *
         * 返回 0代表成功 ,1 代表没有删除权限, 2代表不是空目录,3 代表未知错误
         *
         * @return
         */
        fun deleteBlankPath(path: String): Int {
            val f = File(path)
            if (!f.canWrite()) {
                return 1
            }
            if (f.list() != null && f.list().size > 0) {
                return 2
            }
            return if (f.delete()) {
                0
            } else 3
        }

        /**
         * 重命名
         *
         * @param oldName
         * @param newName
         * @return
         */
        fun reNamePath(oldName: String, newName: String): Boolean {
            val f = File(oldName)
            return f.renameTo(File(newName))
        }

        /**
         * 删除文件
         *
         * @param filePath
         */
        fun deleteFileWithPath(filePath: String): Boolean {
            val checker = SecurityManager()
            val f = File(filePath)
            checker.checkDelete(filePath)
            if (f.isFile) {
                f.delete()
                return true
            }
            return false
        }

        /**
         * 清空一个文件夹
         */
        fun clearFileWithPath(filePath: String) {
            val files = FileUtil.listPathFiles(filePath)
            if (files.isEmpty()) {
                return
            }
            for (f in files) {
                if (f.isDirectory) {
                    clearFileWithPath(f.absolutePath)
                } else {
                    f.delete()
                }
            }
        }

        /**
         * 获取SD卡的根目录
         *
         * @return
         */
        val sdRoot: String
            get() = Environment.getExternalStorageDirectory().absolutePath

        /**
         * 获取手机外置SD卡的根目录
         *
         * @return
         */
        val externalSDRoot: String
            get() {

                val evn = System.getenv()

                return evn["SECONDARY_STORAGE"].toString()
            }

        /**
         * 列出root目录下所有子目录
         *
         * @return 绝对路径
         */
        fun listPath(root: String): List<String> {
            val allDir = ArrayList<String>()
            val checker = SecurityManager()
            val path = File(root)
            checker.checkRead(root)
            // 过滤掉以.开始的文件夹
            if (path.isDirectory) {
                for (f in path.listFiles()) {
                    if (f.isDirectory && !f.name.startsWith(".")) {
                        allDir.add(f.absolutePath)
                    }
                }
            }
            return allDir
        }

        /**
         * 获取一个文件夹下的所有文件
         *
         * @param root
         * @return
         */
        fun listPathFiles(root: String): List<File> {
            val allDir = ArrayList<File>()
            val checker = SecurityManager()
            val path = File(root)
            checker.checkRead(root)
            val files = path.listFiles()
            for (f in files) {
                if (f.isFile)
                    allDir.add(f)
                else
                    listPath(f.absolutePath)
            }
            return allDir
        }

        /**
         * 创建目录
         */
        fun createPath(newPath: String): PathStatus {
            val path = File(newPath)
            if (path.exists()) {
                return PathStatus.EXITS
            }
            return if (path.mkdir()) {
                PathStatus.SUCCESS
            } else {
                PathStatus.ERROR
            }
        }

        /**
         * 截取路径名
         *
         * @return
         */
        fun getPathName(absolutePath: String): String {
            val start = absolutePath.lastIndexOf(File.separator) + 1
            val end = absolutePath.length
            return absolutePath.substring(start, end)
        }

        /**
         * 获取应用程序缓存文件夹下的指定目录
         *
         * @param context
         * @param dir
         * @return
         */
        fun getAppCache(context: Context, dir: String): String {
            val savePath = context.cacheDir.absolutePath + "/" + dir + "/"
            var savedir: File? = File(savePath)
            if (!savedir!!.exists()) {
                savedir.mkdirs()
            }
            savedir = null
            return savePath
        }


        /**
         * 得到SD卡根目录.
         */
        // 取得sdcard文件路径
        val rootPath: File?
            get() {
                var path: File? = null
                if (FileUtil.sdCardIsAvailable()) {
                    path = Environment.getExternalStorageDirectory()
                } else {
                    path = Environment.getDataDirectory()
                }
                return path
            }

        /**
         * SD卡是否可用.
         */
        fun sdCardIsAvailable(): Boolean {
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val sd = File(Environment.getExternalStorageDirectory().path)
                return sd.canWrite()
            } else
                return false
        }

        /**
         * 文件或者文件夹是否存在.
         */
        fun fileExists(filePath: String): Boolean {
            val file = File(filePath)
            return file.exists()
        }

        fun deleleFile(path: String) {
            deleteFile(File(path))
        }

        fun deleteFile(file: File?) {
            if (file != null && file.exists())
                file.delete()
        }

        /**
         * 删除指定文件夹下所有文件, 保留文件夹.
         */
        fun delAllFile(path: String): Boolean {
            val flag = false
            val file = File(path)
            if (!file.exists()) {
                return flag
            }
            if (file.isFile) {
                file.delete()
                return true
            }
            val files = file.listFiles()
            for (i in files.indices) {
                val exeFile = files[i]
                if (exeFile.isDirectory) {
                    delAllFile(exeFile.absolutePath)
                } else {
                    exeFile.delete()
                }
            }
            return flag
        }

        /**
         * 文件复制.
         */
        fun copy(srcFile: String, destFile: String): Boolean {
            try {
                val `in` = FileInputStream(srcFile)
                val out = FileOutputStream(destFile)
                val bytes = ByteArray(1024)
                var c: Int = 0
                var read:Int
                do {
                    read = `in`.read(bytes)
                    if (read != -1) {
                        out.write(bytes, 0, c)
                    }else{
                        break
                    }
                } while (true)
                `in`.close()
                out.flush()
                out.close()
            } catch (e: Exception) {
                return false
            }

            return true
        }

        /**
         * 复制整个文件夹内.
         *
         * @param oldPath string 原文件路径如：c:/fqf.
         * @param newPath string 复制后路径如：f:/fqf/ff.
         */
        fun copyFolder(oldPath: String, newPath: String) {
            try {
                File(newPath).mkdirs() // 如果文件夹不存在 则建立新文件夹
                val a = File(oldPath)
                val file = a.list()
                var temp: File? = null
                for (i in file.indices) {
                    if (oldPath.endsWith(File.separator)) {
                        temp = File(oldPath + file[i])
                    } else {
                        temp = File(oldPath + File.separator + file[i])
                    }

                    if (temp.isFile) {
                        val input = FileInputStream(temp)
                        val output = FileOutputStream(newPath + "/" + temp.name.toString())
                        val b = ByteArray(1024 * 5)
                        var len: Int
                        do {
                            len = input.read(b)
                            if (len != -1) {
                                output.write(b, 0, len)
                            }else{
                                break
                            }
                        } while (true)
                        output.flush()
                        output.close()
                        input.close()
                    }
                    if (temp.isDirectory) {// 如果是子文件夹
                        copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i])
                    }
                }
            } catch (e: NullPointerException) {
            } catch (e: Exception) {
            }

        }

        /**
         * 重命名文件.
         */
        fun renameFile(resFilePath: String, newFilePath: String): Boolean {
            val resFile = File(resFilePath)
            val newFile = File(newFilePath)
            return resFile.renameTo(newFile)
        }

        /**
         * 获取磁盘可用空间.
         */
        val sdCardAvailaleSize: Long
            @SuppressLint("NewApi")
            get() {
                val path = rootPath
                val stat = StatFs(path!!.path)
                val blockSize: Long
                val availableBlocks: Long
                if (Build.VERSION.SDK_INT >= 18) {
                    blockSize = stat.blockSizeLong
                    availableBlocks = stat.availableBlocksLong
                } else {
                    blockSize = stat.blockSize.toLong()
                    availableBlocks = stat.availableBlocks.toLong()
                }
                return availableBlocks * blockSize
            }

        /**
         * 获取某个目录可用大小.
         */
        @SuppressLint("NewApi")
        fun getDirSize(path: String): Long {
            val stat = StatFs(path)
            val blockSize: Long
            val availableBlocks: Long
            if (Build.VERSION.SDK_INT >= 18) {
                blockSize = stat.blockSizeLong
                availableBlocks = stat.availableBlocksLong
            } else {
                blockSize = stat.blockSize.toLong()
                availableBlocks = stat.availableBlocks.toLong()
            }
            return availableBlocks * blockSize
        }

        /**
         * 获取文件或者文件夹大小.
         */
        fun getFileAllSize(path: String): Long {
            val file = File(path)
            if (file.exists()) {
                if (file.isDirectory) {
                    val childrens = file.listFiles()
                    var size: Long = 0
                    for (f in childrens) {
                        size += getFileAllSize(f.path)
                    }
                    return size
                } else {
                    return file.length()
                }
            } else {
                return 0
            }
        }

        /**
         * 创建一个文件.
         */
        fun initFile(path: String): Boolean {
            var result = false
            try {
                val file = File(path)
                if (!file.exists()) {
                    result = file.createNewFile()
                } else if (file.isDirectory) {
                    file.delete()
                    result = file.createNewFile()
                } else if (file.exists()) {
                    result = true
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return result
        }

        /**
         * 创建一个文件夹.
         */
        fun initDirectory(path: String): Boolean {
            var result = false
            val file = File(path)
            if (!file.exists()) {
                result = file.mkdir()
            } else if (!file.isDirectory) {
                file.delete()
                result = file.mkdir()
            } else if (file.exists()) {
                result = true
            }
            return result
        }

        /**
         * 复制文件.
         */
        @Throws(IOException::class)
        fun copyFile(from: File, to: File) {
            if (!from.exists()) {
                throw IOException("The source file not exist: " + from.absolutePath)
            }
            val fis = FileInputStream(from)
            try {
                copyFile(fis, to)
            } finally {
                fis.close()
            }
        }

        /**
         * 复制文件.
         */
        @Throws(IOException::class)
        fun copyFile(from: InputStream, to: File): Long {
            var totalBytes: Long = 0
            val fos = FileOutputStream(to, false)
            try {
                val data = ByteArray(1024)
                var len: Int = from.read(data)
                do {
                    if (len > -1) {
                        fos.write(data, 0, len)
                        totalBytes += len.toLong()
                    }else{
                        break
                    }
                } while (true)
                fos.flush()
            } finally {
                fos.close()
            }
            return totalBytes
        }

        /**
         * 保存流到文件.
         */
        fun saveFile(inputStream: InputStream, filePath: String) {
            try {
                val outputStream = FileOutputStream(File(filePath), false)
                val buffer = ByteArray(1024)
                var len: Int
                do {
                    len = inputStream.read(buffer)
                    if (len != -1) {
                        outputStream.write(buffer, 0, len)
                    }else{
                        break
                    }
                } while (true)
                outputStream.flush()
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        /**
         * 用UTF8保存一个文件.
         */
        @Throws(IOException::class)
        fun saveFileUTF8(path: String, content: String, append: Boolean?) {
            val fos = FileOutputStream(path, append!!)
            val out = OutputStreamWriter(fos, "UTF-8")
            out.write(content)
            out.flush()
            out.close()
            fos.flush()
            fos.close()
        }

        /**
         * 用UTF8读取一个文件.
         */
        fun getFileUTF8(path: String): String {
            var result = ""
            var fin: InputStream? = null
            try {
                fin = FileInputStream(path)
                val length = fin.available()
                val buffer = ByteArray(length)
                var type: Charset = Charset.forName("UTF-8")
                fin.read(buffer)
                fin.close()
                result = String(buffer, type)
            } catch (e: Exception) {
            }

            return result
        }

        /**
         * 得到一个文件Intent.
         */
        fun getFileIntent(path: String, mimeType: String): Intent {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.fromFile(File(path)), mimeType)
            return intent
        }

        /**
         * .zip文件解压缩
         * DeCompress the ZIP to the path
         *
         * @param zipFileString name of ZIP
         * @param outPathString path to be unZIP
         * @throws Exception
         */
        fun unZipFolder(zipFileString: String, outPathString: String) {
            var inZip: ZipInputStream? = null
            try {
                inZip = ZipInputStream(FileInputStream(zipFileString))
                var zipEntry: ZipEntry
                var szName = ""
                do {
                    zipEntry = inZip.nextEntry
                    if (zipEntry != null) {
                        szName = zipEntry.name
                        if (zipEntry.isDirectory) {
                            // get the folder name of the widget
                            szName = szName.substring(0, szName.length - 1)
                            val folder = File(outPathString + File.separator + szName)
                            folder.mkdirs()
                        } else {
                            val file = File(outPathString + File.separator + szName)
                            file.createNewFile()
                            // get the output stream of the file
                            val out = FileOutputStream(file)
                            val buffer = ByteArray(1024)
                            var len: Int
                            // read (len) bytes into buffer

                            do {
                                len = inZip.read(buffer)
                                if (len != -1) {
                                    out.write(buffer, 0, len)
                                    out.flush()
                                }else{
                                    break
                                }
                                // write (len) byte from buffer at the position 0
                            } while (true)
                            out.close()
                        }
                    }else{
                        break
                    }
                } while (true)
                inZip.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        /**
         * 判断SD卡是否可用
         *
         * @return SD卡可用返回true
         */
        fun hasSdcard(): Boolean {
            val status = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == status
        }

        /*
     * public static void copyAssetFileToFiles(Context context, String filename)
	 * throws IOException { InputStream is = context.getAssets().open(filename);
	 * byte[] buffer = new byte[is.available()]; is.read(buffer); is.close();
	 *
	 * File of = new File(context.getFilesDir() + "/" + filename);
	 * of.createNewFile(); FileOutputStream os = new FileOutputStream(of);
	 * os.write(buffer); os.close(); }
	 */

        /**
         * 安装app
         *
         * @param context
         * @param file
         */
        fun installAPK(context: Context, file: File?, application_id: String?) {
            if (file == null || !file.exists()) {
                return
            }
            val intent = Intent(Intent.ACTION_VIEW)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                val contentUri = FileProvider.getUriForFile(context, "$application_id.fileProvider", file)
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
            } else {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
            }
            context.startActivity(intent)
        }
    }

}