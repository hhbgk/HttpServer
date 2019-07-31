package com.hhbgk.http.server;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private HttpServerImpl mHttpServerImpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHttpServerImpl = new HttpServerImpl(8080);

        try {
            mHttpServerImpl.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextView textView = findViewById(R.id.tv_url);
        String url = "url:http://" + getLocalIpStr(getApplicationContext()) + ":8080";
        Log.i("test", "Url=" + url);
        textView.setText(url);
    }

    private String getLocalIpStr(Context context) {
        WifiManager wifiManager=(WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = null;
        if (wifiManager != null) {
            wifiInfo = wifiManager.getConnectionInfo();
        }
        if (wifiInfo != null) {
            return intToIpAddr(wifiInfo.getIpAddress());
        }
        return null;
    }

    private static String intToIpAddr(int ip) {
        return (ip & 0xff) + "." + ((ip>>8)&0xff) + "." + ((ip>>16)&0xff) + "." + ((ip>>24)&0xff);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mHttpServerImpl != null) {
            mHttpServerImpl.stop();
        }
    }
}
