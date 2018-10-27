package com.docwei.imageupload_lib.permission;

import android.content.Context;

/**
 * Created by hupei on 2016/4/26.
 */
public class Acp {

    private static Acp mInstance;
    private AcpManager mAcpManager;

    private Acp(Context context) {
        mAcpManager = new AcpManager(context.getApplicationContext());
    }

    public static Acp getInstance(Context context) {
        if (mInstance == null)
            synchronized (Acp.class) {
                if (mInstance == null) {
                    mInstance = new Acp(context);
                }
            }
        return mInstance;
    }

    /**
     * 开始请求
     *
     * @param options
     * @param acpListener
     */
    public void request(AcpOptions options, AcpListener acpListener) {
        if (options == null) new NullPointerException("AcpOptions is null...");
        if (acpListener == null) new NullPointerException("AcpListener is null...");
        mAcpManager.request(options, acpListener);
    }

    AcpManager getAcpManager() {
        return mAcpManager;
    }
}
