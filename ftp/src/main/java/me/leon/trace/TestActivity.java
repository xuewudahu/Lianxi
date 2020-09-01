package me.leon.trace;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.leon.PrinterShareMgr;
import me.leon.ftp.R;
import me.leon.telnet.TelnetUtils;

public class TestActivity extends Activity {


    // 最大的ttl跳转 可以自己设定
    private final int MAX_TTL = 30;

    // 都是一些字符串 用于parse 用的
    private static final String PING = "PING";
    private static final String FROM_PING = "From";
    private static final String SMALL_FROM_PING = "from";
    private static final String PARENTHESE_OPEN_PING = "(";
    private static final String PARENTHESE_CLOSE_PING = ")";
    private static final String TIME_PING = "time=";
    private static final String EXCEED_PING = "exceed";
    private static final String UNREACHABLE_PING = "100%";

    // 初始化默认ttl 为1
    private int ttl = 1;
    private String ipToPing;
    // ping耗时
    private float elapsedTime;

    // 存放结果集的tarces
    private List<TracerouteContainer> traces = new ArrayList();
    private TelnetUtils telnetUtils;
    ;

    /**
     * file开头的uri转换为content开头的uri
     *
     * @param uri 任意uri
     * @return content开头的uri
     */
    private Uri file2Content(Uri uri) {
        if ("file".equals(uri.getScheme())) {
            String path = uri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                ContentResolver cr = this.getContentResolver();
                StringBuffer buff = new StringBuffer();
                buff.append("(")
                        .append(MediaStore.Images.ImageColumns.DATA)
                        .append("=")
                        .append("'" + path + "'")
                        .append(")");
                Cursor cur = cr.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Images.ImageColumns._ID},
                        buff.toString(), null, null);
                int index = 0;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                    index = cur.getInt(index);
                }
                if (index == 0) {
                    //do nothing
                } else {
                    Uri uri_temp = Uri
                            .parse("content://media/external/images/media/"
                                    + index);
                    if (uri_temp != null) {
                        uri = uri_temp;
                    }
                }
            }
        }
        return uri;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        findViewById(R.id.btnShare).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
//            intent.setType("*/*");
            if (Build.VERSION.SDK_INT > 23) {
                Uri uriForFile = FileProvider.getUriForFile(this, getPackageName(), new File("/sdcard/p1.jpg"));
                Log.d("dddddd", uriForFile.toString());
                intent.setData(uriForFile);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setComponent(new ComponentName("com.hp.android.printservice", "com.hp.android.printservice.core.SingleFileReceiver"));
            } else {
                intent.setData(Uri.fromFile(new File(
                        "/sdcard/p1.jpg")));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(new ComponentName("com.hp.android.printservice", "com.hp.android.printservice.common.TermsActivity"));
            }

//            intent.putExtra(Intent.EXTRA_TEXT, "hello ... \nhaha \n hehe");
//            TestActivity.this.startActivity(Intent.createChooser(intent, "请选择"));
            TestActivity.this.startActivity(intent);

        });
        findViewById(R.id.btnPrintDoc).setOnClickListener(v -> {
            PrinterShareMgr.getInstance().printFile(TestActivity.this, "/sdcard/print/1.doc");
        });
        findViewById(R.id.btnPrintImg).setOnClickListener(v -> {
            PrinterShareMgr.getInstance().printFile(TestActivity.this, "/sdcard/print/1.jpg");
        });
        findViewById(R.id.btnPrintPdf).setOnClickListener(v -> {
            PrinterShareMgr.getInstance().printFile(TestActivity.this, "/sdcard/print/1.pdf");
        });
        findViewById(R.id.btnPrintHtml).setOnClickListener(v -> {
            PrinterShareMgr.getInstance().printFile(TestActivity.this, "/sdcard/print/1.html");
        });
        findViewById(R.id.tv).setOnClickListener(v -> {
            PrinterShareMgr.getInstance().printFileHp(TestActivity.this,null);
        });
//        telnetUtils = new TelnetUtils();
//        executorService.execute(() -> {
//            String result = telnetUtils.connect("baidu.com", 80);
//            Log.v("ccc", result);
//        });
//        new ExecuteTracerouteAsyncTask(MAX_TTL, "baidu.com")
//                .execute();

    }

    private void showResultInLog() {
        for (TracerouteContainer container : traces) {
            Log.v("ccc", container.toString());

        }
        ttl = 1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (telnetUtils != null) {
            telnetUtils.disconnect();
        }
    }

    private class ExecuteTracerouteAsyncTask extends AsyncTask<Void, String, String> {

        private int maxTtl;

        private String url;

        public ExecuteTracerouteAsyncTask(int maxTtl, String url) {
            this.maxTtl = maxTtl;
            this.url = url;
        }


        @Override
        protected String doInBackground(Void... params) {
            Log.v("ccc", "doInBackground");
            String res = "";
            try {
                res = launchPing(url);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            TracerouteContainer trace;

            if (res.contains(UNREACHABLE_PING) && !res.contains(EXCEED_PING)) {
                trace = new TracerouteContainer("", parseIpFromPing(res),
                        elapsedTime);
            } else {
                trace = new TracerouteContainer("", parseIpFromPing(res),
                        ttl == maxTtl ? Float
                                .parseFloat(parseTimeFromPing(res))
                                : elapsedTime);
            }

            InetAddress inetAddr;
            try {
                inetAddr = InetAddress.getByName(trace.getIp());
                String hostname = inetAddr.getHostName();
                trace.setHostname(hostname);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            traces.add(trace);
            return res;
        }

        private String launchPing(String url) throws IOException {
            Process p;
            String command = "";

// 这个实际上就是我们的命令第一封装 注意ttl的值的变化 第一次调用的时候 ttl的值为1
            String format = "ping -c 1 -w 4 -t %d ";
            command = String.format(format, ttl);

            long startTime = System.nanoTime();
// 实际调用命令时 后面要跟上url地址
            Log.d("ccc", command);
            p = Runtime.getRuntime().exec(command + url);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));

            String s;
            String res = "";
            while ((s = stdInput.readLine()) != null) {
                res += s + "\n";
// 这个地方这么做的原因是 有的手机 返回的from 有的手机返回的是From所以要
// 这么去判定 请求结束的事件 算一下 延时
                if (s.contains(FROM_PING) || s.contains(SMALL_FROM_PING)) {
                    elapsedTime = (System.nanoTime() - startTime) / 1000000.0f;
                }
            }
            Log.v("zzz", res);

// 调用结束的时候 销毁这个资源
//            p.destroy();

            if ("".equals(res)) {
                return "无法ping 通";
//                throw new IllegalArgumentException();
            }
// 第一次调用ping命令的时候 记得把取得的最终的ip地址 赋给外面的ipToPing
// 后面要依据这个ipToPing的值来判断是否到达ip数据报的 终点
            if (ttl == 1) {
                ipToPing = parseIpToPingFromPing(res);
            }
            return res;
        }


        @Override
        protected void onPostExecute(String result) {
// 如果为空的话就截止吧 过程完毕
            if (TextUtils.isEmpty(result)) {
                return;
            }

// 如果这一跳的ip地址与最终的地址 一致的话 就说明 ping到了终点
            if (traces.get(traces.size() - 1).getIp().equals(ipToPing)) {
                if (ttl < maxTtl) {
                    ttl = maxTtl;
                    traces.remove(traces.size() - 1);
                    new ExecuteTracerouteAsyncTask(maxTtl, url).execute();
                } else {
// 如果ttl ==maxTtl的话 当然就结束了 我们就要打印出最终的结果
                    showResultInLog();
                }
            } else {
// 如果比较的ip 不相等 哪就说明还没有ping到最后一跳。我们就需要继续ping
// 继续ping的时候 记得ttl的值要加1
                if (ttl < maxTtl) {
                    ttl++;
                    new ExecuteTracerouteAsyncTask(maxTtl, url).execute();
                }
            }
            super.onPostExecute(result);
        }

    }


    private String parseIpFromPing(String ping) {
        String ip = "";
        if (ping.contains(FROM_PING)) {
            int index = ping.indexOf(FROM_PING);

            ip = ping.substring(index + 5);
            if (ip.contains(PARENTHESE_OPEN_PING)) {
                int indexOpen = ip.indexOf(PARENTHESE_OPEN_PING);
                int indexClose = ip.indexOf(PARENTHESE_CLOSE_PING);

                ip = ip.substring(indexOpen + 1, indexClose);
            } else {
                ip = ip.substring(0, ip.indexOf("\n"));
                if (ip.contains(":")) {
                    index = ip.indexOf(":");
                } else {
                    index = ip.indexOf(" ");
                }

                ip = ip.substring(0, index);
            }
        } else {
            int indexOpen = ping.indexOf(PARENTHESE_OPEN_PING);
            int indexClose = ping.indexOf(PARENTHESE_CLOSE_PING);

            ip = ping.substring(indexOpen + 1, indexClose);
        }

        return ip;
    }


    private String parseIpToPingFromPing(String ping) {
        String ip = "";
        if (ping.contains(PING)) {
            int indexOpen = ping.indexOf(PARENTHESE_OPEN_PING);
            int indexClose = ping.indexOf(PARENTHESE_CLOSE_PING);

            ip = ping.substring(indexOpen + 1, indexClose);
        }

        return ip;
    }


    private String parseTimeFromPing(String ping) {
        String time = "";
        if (ping.contains(TIME_PING)) {
            int index = ping.indexOf(TIME_PING);

            time = ping.substring(index + 5);
            index = time.indexOf(" ");
            time = time.substring(0, index);
        }

        return time;
    }

}