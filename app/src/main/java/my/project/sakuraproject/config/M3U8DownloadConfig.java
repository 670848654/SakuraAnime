package my.project.sakuraproject.config;

import android.util.Log;

import com.arialyy.aria.core.download.M3U8Entity;
import com.arialyy.aria.core.processor.IBandWidthUrlConverter;
import com.arialyy.aria.core.processor.ITsMergeHandler;
import com.arialyy.aria.core.processor.IVodTsUrlConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.Nullable;
import my.project.sakuraproject.util.VideoUtils;

/**
 * M3U8下载配置类
 */
public class M3U8DownloadConfig {
    private static final Pattern REG = Pattern.compile("http(.*)/");
    /************************************************************ m3u8下载配置 START ************************************************************/
    public static class BandWidthUrlConverter implements IBandWidthUrlConverter {
        @Override
        public String convert(String m3u8Url, String bandWidthUrl) {
            if (bandWidthUrl.startsWith("http"))
                return bandWidthUrl;
            try {
                Matcher m = REG.matcher(m3u8Url);
                URL url = new URL(m3u8Url);
                if (m.find()) {
                    m3u8Url = m.group();
                }
                else
                    m3u8Url = m3u8Url.replace(url.getPath(), "");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Log.e("bandWidthUrl", m3u8Url + bandWidthUrl);
            return m3u8Url + bandWidthUrl;
        }
    }

    public static class VodTsUrlConverter implements IVodTsUrlConverter {
        @Override
        public List<String> convert(String m3u8Url, List<String> tsUrls) {
            // 转换ts文件的url地址
            try {
                Matcher m = REG.matcher(m3u8Url);
                URL url = new URL(m3u8Url);
                if (m.find()) {
                    m3u8Url = m.group();
                }
                else
                    m3u8Url = m3u8Url.replace(url.getPath(), "");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            List<String> newUrls = new ArrayList<>();
            for (String url : tsUrls) {
                String tsUrl = url.contains("http") ? url : m3u8Url + url;
                Log.e("tsUrl", tsUrl);
                newUrls.add(tsUrl);
            }
            return newUrls; // 返回有效的ts文件url集合
        }
    }

    public static class TsMergeHandler implements ITsMergeHandler {
        public boolean merge(@Nullable M3U8Entity m3U8Entity, List<String> tsPath) {
            Log.e("TsMergeHandler", "合并TS....");
            String tsKey = m3U8Entity.getKeyPath() == null ? "" : VideoUtils.readKeyInfo(new File(m3U8Entity.getKeyPath()));
            byte[] tsIv = m3U8Entity.getIv() == null ? new byte[16] : m3U8Entity.getIv().getBytes();
            OutputStream outputStream = null;
            InputStream inputStream = null;
            FileOutputStream fileOutputStream = null;
            List<File> finishedFiles = new ArrayList<>();
            for (String path : tsPath) {
                try {
                    File pathFile = new File(path);
                    if (!tsKey.isEmpty()) {
                        Log.e("TsMergeHandler", "存在加密");
                        // 存在加密
                        inputStream= new FileInputStream(pathFile);
                        byte[] bytes = new byte[inputStream.available()];
                        inputStream.read(bytes);
                        fileOutputStream = new FileOutputStream(pathFile);
                        // 解密ts片段
                        fileOutputStream.write(VideoUtils.decrypt(bytes, tsKey, tsIv));
                    }
                    finishedFiles.add(pathFile);
                }catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (outputStream != null) outputStream.close();
                        if (inputStream != null) inputStream.close();
                        if (fileOutputStream != null) fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return VideoUtils.merge(m3U8Entity.getFilePath(), finishedFiles);
        }
    }
    /************************************************************ m3u8下载配置 END ************************************************************/
}
