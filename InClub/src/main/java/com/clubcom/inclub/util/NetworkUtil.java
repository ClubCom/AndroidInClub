package com.clubcom.inclub.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.clubcom.ccframework.FrameworkApplication;
import com.clubcom.inclub.MainApplication;

/**
 * Created by adamwalter3 on 7/1/16.
 */
public class NetworkUtil {
    private final static String WIFI_PASSWORD = "!QAZxsw2#EDCvfr4";

    public static boolean isConnected(@NonNull Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static boolean isWifiConnected(@NonNull Context context) {
        return isConnected(context, ConnectivityManager.TYPE_WIFI);
    }

    public static boolean isMobileConnected(@NonNull Context context) {
        return isConnected(context, ConnectivityManager.TYPE_MOBILE);
    }

    public static boolean isNetworkAvailable(@NonNull Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private static boolean isConnected(@NonNull Context context, int type) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            NetworkInfo networkInfo = connMgr.getNetworkInfo(type);
            return networkInfo != null && networkInfo.isConnected();
        } else {
            return isConnected(connMgr, type);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static boolean isConnected(@NonNull ConnectivityManager connMgr, int type) {
        Network[] networks = connMgr.getAllNetworks();
        NetworkInfo networkInfo;
        for (Network mNetwork : networks) {
            networkInfo = connMgr.getNetworkInfo(mNetwork);
            if (networkInfo != null && networkInfo.getType() == type && networkInfo.isConnected()) {
                return true;
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static NetworkInfo getConnectedNetwork(@NonNull ConnectivityManager connMgr, int type) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            NetworkInfo[] networks = connMgr.getAllNetworkInfo();
            for (NetworkInfo networkInfo : networks) {
                if (networkInfo != null && networkInfo.getType() == type && networkInfo.isConnected()) {
                    return networkInfo;
                }
            }
        } else {
            Network[] networks = connMgr.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network network : networks) {
                networkInfo = connMgr.getNetworkInfo(network);
                if (networkInfo != null && networkInfo.getType() == type && networkInfo.isConnected()) {
                    return networkInfo;
                }
            }
        }


        return null;
    }

    public static boolean checkConnections(Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        int wiFiPermission = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_WIFI_STATE);
        int networkPermission = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_NETWORK_STATE);

        if (wiFiPermission == PackageManager.PERMISSION_GRANTED) {
            if (networkPermission == PackageManager.PERMISSION_GRANTED) {
                if (NetworkUtil.isNetworkAvailable(ctx)) {
                    FrameworkApplication.NETWORK_CONNECTED = true;
                    NetworkInfo networkInfo = NetworkUtil.getConnectedNetwork(connectivityManager, ConnectivityManager.TYPE_WIFI);

                    if (networkInfo != null) {
                        FrameworkApplication.CURRENT_NETWORK = networkInfo.getExtraInfo();
                        FrameworkApplication.WIFI_CONNECTED = true;
                        System.out.println("Connected to: " + FrameworkApplication.CURRENT_NETWORK);
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        String ssid = wifiInfo.getSSID();
                        if (ssid.toLowerCase().contains("clubcom")) {
                            FrameworkApplication.WIFI_CONNECTED_CLUBCOM = isClubcomWifi();
                            return true;
                        } else {
                            FrameworkApplication.WIFI_CONNECTED_CLUBCOM = false;
                            return true;
                        }
                    } else {
                        FrameworkApplication.WIFI_CONNECTED = false;
                        FrameworkApplication.WIFI_CONNECTED_CLUBCOM = false;
                        return true;
                    }
                } else {
                    FrameworkApplication.NETWORK_CONNECTED = false;
                    return true;
                }
            } else {
                FrameworkApplication.NETWORK_PERMISSION_ERROR = true;
                return false;
            }
        } else {
            FrameworkApplication.WIFI_PERMISSION_ERROR = true;
            return false;
        }
    }

    public static String connectToWifi(Context ctx, String ssid, String password) {
        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wc = new WifiConfiguration();
        wc.SSID = "\"" + ssid + "\"";
        wc.preSharedKey = "\"" + password + "\"";
        wc.status = WifiConfiguration.Status.ENABLED;
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        int id = wifiManager.addNetwork(wc);

        wifiManager.addNetwork(wc);
        wifiManager.enableNetwork(id, true);
        wifiManager.reconnect();

        SharedPreferenceLoader.savePreference(ctx, SharedPreferenceLoader.PREF_WIFI_NAME, ssid);
        return wc.SSID.replaceAll("\"", "");
    }

    public static String connectToWifi(Context ctx, String ssid) {
        return connectToWifi(ctx, ssid, WIFI_PASSWORD);
    }

    private static boolean isClubcomWifi() {
        return true;
    }
}
