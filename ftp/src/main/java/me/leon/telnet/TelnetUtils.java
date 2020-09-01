package me.leon.telnet;

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.Pair;

import org.apache.commons.net.telnet.TelnetClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Telnet操作类
 * Created by fblife on 2017/3/28.
 */

public class TelnetUtils {
    private static final String TAG = TelnetUtils.class.getSimpleName();
    private TelnetClient client;
    public TelnetUtils() {
        client = new TelnetClient();
        client.setConnectTimeout(30000);
    }

    /**
     * 连接及登录
     *
     * @param ip   目标主机IP
     * @param port 端口号（Telnet 默认 23）
     */
    public String connect(String ip, int port) {
        try {
            Log.d(TAG, "connecting : " + ip + " port: " + port);
            client.connect(ip, port);
            return "success";
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            return "err: " + e.getMessage();
        } finally {
            disconnect();
        }
    }


    /**
     * 关闭连接
     */
    public void disconnect() {
        try {
            if (client != null) {
                client.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("CheckResult")
    public static void telnet() {

        List<Pair<String, Integer>> urls = new ArrayList<>();
        urls.add(new Pair<>("shiduai.com", 9091));
        urls.add(new Pair<>("baidu.com", 80));
        urls.add(new Pair<>("npmjs.org", 80));
        urls.add(new Pair<>("aliyun.com", 80));
        urls.add(new Pair<>("aliyuncs.com", 80));
        urls.add(new Pair<>("agora.io", 443));
        urls.add(new Pair<>("agora.io", 8001));
        urls.add(new Pair<>("agoraio.cn", 80));
        urls.add(new Pair<>("api.jpush.cn", 443));
        urls.add(new Pair<>("qiniu.com", 80));
        urls.add(new Pair<>("zjfzol.com.cn", 80));

//        Flowable.fromIterable(urls)
//                .delay(10, TimeUnit.SECONDS)
//                .map(url -> new Pair<>(url, new TelnetUtils().connect(url.first, url.second)))
//                .parallel()
//                .runOn(Schedulers.io())
//                .sequential()
//                .toList()
//                .subscribe(result -> {
//                    for (Pair<Pair<String, Integer>, String> item : result) {
//                        Log.w("Telnet", item.first.first + "  " + item.second);
//                    }
//                }, throwable -> {
//                    Log.w("Telnet", throwable.getMessage());
//                    throwable.printStackTrace();
//                });
    }
}
