package corecloud.xtc.share;

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.xtc.shareapi.share.communication.BaseResponse;
import com.xtc.shareapi.share.communication.SendMessageToXTC;
import com.xtc.shareapi.share.manager.ShareMessageManager;
import com.xtc.shareapi.share.shareobject.XTCAppExtendObject;
import com.xtc.shareapi.share.shareobject.XTCShareMessage;
import com.xtc.shareapi.share.sharescene.Chat;
import com.xtc.shareapi.share.sharescene.Moment;

public class shareapp {

    public static ShareMessageManager shareMessageManager;

    public static void setupShareEngine(final AppCompatActivity activity, final View view) {
        if (view == null) return;

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareToXtcPlatform(activity, 2);
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                shareToXtcPlatform(activity, 1);
                return true;
            }
        });
    }

    private static void shareToXtcPlatform(AppCompatActivity activity, int sceneType) {
        if (shareMessageManager == null) {
            shareMessageManager = new ShareMessageManager(activity);
        }
        if (!checkShareEnvironment(activity, sceneType)) {
            return;
        }
        try {
            SendMessageToXTC.Request request = new SendMessageToXTC.Request();
            XTCShareMessage shareMsg = new XTCShareMessage();

            XTCAppExtendObject appObj = new XTCAppExtendObject();
            appObj.setStartActivity(activity.getClass().getName());
            appObj.setExtInfo(activity.getPackageName());
            shareMsg.setShareObject(appObj);

            shareMsg.setThumbImage(BitmapFactory.decodeResource(activity.getResources(), R.drawable.corecloud));
            shareMsg.setDescription(activity.getString(R.string.app_name));
            request.setMessage(shareMsg);

            if (sceneType == 1) {
                Chat chatScene = new Chat();
                chatScene.setSelectActionMode(2);
                chatScene.setFriendType(4369);
                request.setScene(chatScene);
                request.setFlag(1);
                request.setSendMode(1);
            } else if (sceneType == 2) {
                request.setFlag(0);
                request.setScene(new Moment());
            }

            Bitmap appIcon = BitmapFactory.decodeResource(activity.getResources(), R.drawable.index);

            shareMessageManager.setAppIcon(appIcon);
            shareMessageManager.setAppName(activity.getString(R.string.app_name));
            shareMessageManager.sendRequestToXTC(request, "123456");
        } catch (Exception ignored) {}
    }

    private static boolean checkShareEnvironment(AppCompatActivity activity, int sceneType) {
        try {
            boolean isInstalled = false;
            if (sceneType == 1) {
                isInstalled = isAppInstalled(activity, "com.xtc.weichat") || isAppInstalled(activity, "com.xtc.im");
            } else if (sceneType == 2) {
                isInstalled = isAppInstalled(activity, "com.xtc.moment") || isAppInstalled(activity, "com.xtc.im.mainservice");
            }

            if (!isInstalled) {
                Toast.makeText(activity, activity.getString(R.string.error_no_target_app), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (!shareMessageManager.checkBaseVersion(sceneType)) {
                Toast.makeText(activity, activity.getString(R.string.error_unsupported_version), Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static boolean isAppInstalled(AppCompatActivity activity, String packageName) {
        try {
            activity.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static int handleIdentifierInject(String name, String defType) {
        try {
            if ("layout".equals(defType) && "chat_moment_sence".equals(name)) {
                return R.layout.chat_moment_sence;
            }
            if ("id".equals(defType)) {
                if ("rl_scene".equals(name)) return R.id.rl_scene;
                if ("ll_chat_scene".equals(name)) return R.id.ll_chat_scene;
                if ("iv_chat_scene".equals(name)) return R.id.iv_chat_scene;
                if ("ll_moment_scene".equals(name)) return R.id.ll_moment_scene;
                if ("iv_moment_scene".equals(name)) return R.id.iv_moment_scene;
                if ("bt_cancel_share".equals(name)) return R.id.bt_cancel_share;
            }
        } catch (Exception ignored) {}
        return 0;
    }

    public static Drawable handleDrawableInject(Resources baseRes, int id, @Nullable Resources.Theme theme) {
        if (id == 0x7f060067) {
            int resId = R.drawable.index;
            if (theme != null) {
                return baseRes.getDrawable(resId, theme);
            } else {
                return baseRes.getDrawable(resId);
            }
        }
        return null;
    }

    public static String handlePackageNameInject(String currentPkg) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int limit = Math.min(stackTrace.length, 15);
        for (int i = 0; i < limit; i++) {
            StackTraceElement element = stackTrace[i];
            String className = element.getClassName();
            if (className.contains("com.xtc.shareapi") || className.contains("com.xtc.opendataapi")) {
                String methodName = element.getMethodName();
                if (methodName.contains("PackageInfo")
                        || methodName.contains("getApplicationIcon")
                        || methodName.contains("getPackageInfo")
                        || methodName.contains("getAppName")) {
                    return currentPkg;
                }
                return "com.xxtc.openapi";
            }
            if (className.startsWith("android.view.")
                    || className.startsWith("android.os.")
                    || className.startsWith("android.app.")) {
                continue;
            }
            if (className.contains("PopWindowManager") || className.contains("WindowManager")) {
                return currentPkg;
            }
        }
        return currentPkg;
    }

    public static void handleResourcesInject(Resources baseRes, int id) throws Resources.NotFoundException {
        if (id == 0x7f090024) {
            throw new Resources.NotFoundException();
        }
    }

    public static void handleShareResponse(AppCompatActivity activity, BaseResponse var1) {
        int code = var1.getCode();
        if (code == 1) {
            Toast.makeText(activity, activity.getString(R.string.share_success), Toast.LENGTH_SHORT).show();
        } else if (code == 2) {
            Toast.makeText(activity, activity.getString(R.string.share_canceled), Toast.LENGTH_SHORT).show();
        } else if (code == 8) {
            Toast.makeText(activity, activity.getString(R.string.share_network_error), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, activity.getString(R.string.share_failed_code) + code, Toast.LENGTH_SHORT).show();
        }
    }
}