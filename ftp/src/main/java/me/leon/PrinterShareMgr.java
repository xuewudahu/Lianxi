package me.leon;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;

/**
 * <p>description：</p>
 * <p>author：Leon</p>
 * <p>date：2019/7/18 0018</p>
 * <p>e-mail：deadogone@gmail.com</p>
 *
 * uiautomator dump && cat /storage/emulated/legacy/window_dump.xml|grep -o "android:id/message"
 * am force-stop com.dynamixsoftware.printershare
 */
public class PrinterShareMgr {
    private static PrinterShareMgr mgr = new PrinterShareMgr();

    public static PrinterShareMgr getInstance() {
        return mgr;
    }

    private PrinterShareMgr() {
    }

    /**
     * printershare 软件打印
     * @param context
     * @param filePath
     */
    public void printFile(Context context, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.fromFile(new File(filePath));
        ComponentName comp = null;
        if (filePath.endsWith("pdf")) {
            intent.setDataAndType(data,"application/pdf");
            comp = new ComponentName("com.dynamixsoftware.printershare", "com.dynamixsoftware.printershare.ActivityPrintPDF");
        } else if (filePath.endsWith("doc") || filePath.endsWith("docx")||filePath.endsWith("txt")){
            intent.setDataAndType(data,"application/doc");
            comp = new ComponentName("com.dynamixsoftware.printershare", "com.dynamixsoftware.printershare.ActivityPrintDocuments");
        }else if (filePath.endsWith("jpg") || filePath.endsWith("jpeg") || filePath.endsWith("gif") || filePath.endsWith("png")){
            intent.setDataAndType(data,"image/jpeg");
            comp = new ComponentName("com.dynamixsoftware.printershare", "com.dynamixsoftware.printershare.ActivityPrintPictures");
        }else if (filePath.endsWith("html")||filePath.endsWith("htm")){
            intent.setDataAndType(data,"text/html");
            comp = new ComponentName("com.dynamixsoftware.printershare", "com.dynamixsoftware.printershare.ActivityWeb");
        }

        intent.setComponent(comp);

        context.startActivity(intent);
    }

    /**
     * 惠普打印插件 打印
     * @param context
     * @param filePath
     */
    public void printFileHp(Context context, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            Intent intent = new Intent("com.hp.android.printservice.TESTPRINT");
            context.startActivity(intent);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        Uri data = Uri.fromFile(new File(filePath));
        ComponentName comp = null;
        if (filePath.endsWith("pdf")) {
            intent.setDataAndType(data, "application/pdf");
        }else if (filePath.endsWith("jpg")|| filePath.endsWith("jpeg")){
            intent.setDataAndType(data, "image/jpeg");
        }else if (filePath.endsWith("png")){
            intent.setDataAndType(data, "image/png");
        }
        comp = new ComponentName("com.hp.android.printservice", "com.hp.android.printservice.common.TermsActivity");
        intent.setComponent(comp);

        context.startActivity(intent);
    }

}
