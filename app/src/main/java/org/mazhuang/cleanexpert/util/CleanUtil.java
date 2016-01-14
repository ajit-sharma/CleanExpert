package org.mazhuang.cleanexpert.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

import org.mazhuang.cleanexpert.R;
import org.mazhuang.cleanexpert.ui.JunkCleanActivity;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by mazhuang on 16/1/14.
 */
public class CleanUtil {
    public static String formatShortFileSize(Context context, long number) {
        if (context == null) {
            return "";
        }

        float result = number;
        int suffix = R.string.byteShort;
        if (result > 900) {
            suffix = R.string.kilobyteShort;
            result = result / 1024;
        }
        if (result > 900) {
            suffix = R.string.megabyteShort;
            result = result / 1024;
        }
        if (result > 900) {
            suffix = R.string.gigabyteShort;
            result = result / 1024;
        }
        if (result > 900) {
            suffix = R.string.terabyteShort;
            result = result / 1024;
        }
        if (result > 900) {
            suffix = R.string.petabyteShort;
            result = result / 1024;
        }
        String value;
        if (result < 1) {
            value = String.format("%.2f", result);
        } else if (result < 10) {
            value = String.format("%.2f", result);
        } else if (result < 100) {
            value = String.format("%.1f", result);
        } else {
            value = String.format("%.0f", result);
        }
        return context.getResources().
                getString(R.string.clean_file_size_suffix,
                        value, context.getString(suffix));
    }

    public static void freeAllAppsCache(final Handler handler) {

        Context context = ContextUtil.applicationContext;

        File externalDir = context.getExternalCacheDir();
        if (externalDir == null) {
            return;
        }

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> installedPackages = pm.getInstalledApplications(PackageManager.GET_GIDS);
        for (ApplicationInfo info : installedPackages) {
            String externalCacheDir = externalDir.getAbsolutePath()
                    .replace(context.getPackageName(), info.packageName);
            File externalCache = new File(externalCacheDir);
            if (externalCache.exists() && externalCache.isDirectory()) {
                deleteFile(externalCache);
            }
        }

        try {
            Method freeStorageAndNotify = pm.getClass()
                    .getMethod("freeStorageAndNotify", long.class, IPackageDataObserver.class);
            long freeStorageSize = Long.MAX_VALUE;

            freeStorageAndNotify.invoke(pm, freeStorageSize, new IPackageDataObserver.Stub() {
                @Override
                public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
                    Message msg = handler.obtainMessage(JunkCleanActivity.MSG_SYS_CACHE_CLEAN_FINISH);
                    msg.sendToTarget();
                }
            });
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteFile(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (String name : children) {
                boolean suc = deleteFile(new File(file, name));
                if (!suc) {
                    return false;
                }
            }
        }
        return file.delete();
    }
}
