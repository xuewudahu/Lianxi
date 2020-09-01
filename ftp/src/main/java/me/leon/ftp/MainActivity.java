package me.leon.ftp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv = findViewById(R.id.tv);

        findViewById(R.id.btnStart).setOnClickListener(v -> {
            try {
                FtpServerlet.getInstance().start();
                tv.setText("在\"我的电脑\" 地址栏输入:\nftp://" + getLocalInetAddress().getHostAddress() + ":2121/");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        findViewById(R.id.btnStop).setOnClickListener(v -> {
            FtpServerlet.getInstance().stop();
            tv.setText(null);
        });
    }

    /**
     * 获取移动设备本地IP
     *
     * @return
     */
    public InetAddress getLocalInetAddress() {
        InetAddress ip = null;
        try {
            Enumeration<NetworkInterface> enNetinterface = NetworkInterface.getNetworkInterfaces();
            while (enNetinterface.hasMoreElements()) {
                NetworkInterface ni = enNetinterface.nextElement();
                Enumeration<InetAddress> enIp = ni.getInetAddresses();
                while (enIp.hasMoreElements()) {
                    ip = enIp.nextElement();
                    if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(':') == -1) {
                        break;
                    } else {
                        ip = null;
                    }
                }

                if (ip != null) {
                    break;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ip;
    }
}
