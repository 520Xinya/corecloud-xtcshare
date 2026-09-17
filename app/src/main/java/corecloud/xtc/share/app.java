package corecloud.xtc.share;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.xtc.shareapi.share.communication.SendMessageToXTC;
import com.xtc.shareapi.share.manager.ShareMessageManager;
import com.xtc.shareapi.share.shareobject.XTCAppExtendObject;
import com.xtc.shareapi.share.shareobject.XTCShareMessage;
import com.xtc.shareapi.share.sharescene.Chat;
import com.xtc.shareapi.share.sharescene.Moment;

public class app {

    private static ShareMessageManager shareMessageManager;

    public static void setupAppEngine(final AppCompatActivity activity, final View parentView) {
        if (activity == null || parentView == null) return;

        View app7View = parentView.findViewById(R.id.app7);
        if (app7View != null) {
            app7View.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareToXtcPlatform(activity, parentView, 1);
                }
            });
        }

        View app12View = parentView.findViewById(R.id.app12);
        if (app12View != null) {
            app12View.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shareToXtcPlatform(activity, parentView, 2);
                }
            });
        }
    }

    private static void shareToXtcPlatform(AppCompatActivity activity, View parentView, int sceneType) {
        EditText app6EditText = parentView.findViewById(R.id.app6);
        if (app6EditText == null) return;

        String customShareText = app6EditText.getText().toString().trim();

        if (TextUtils.isEmpty(customShareText)) {
            Toast.makeText(activity, "请输入分享文本", Toast.LENGTH_SHORT).show();
            return;
        }

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
            appObj.setExtInfo("");
            shareMsg.setShareObject(appObj);

            shareMsg.setThumbImage(BitmapFactory.decodeResource(activity.getResources(), R.drawable.corecloud));
            shareMsg.setDescription(customShareText);
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

    private static boolean checkShareEnvironment(Context context, int sceneType) {
        try {
            boolean isInstalled = false;
            if (sceneType == 1) {
                isInstalled = isAppInstalled(context, "com.xtc.weichat") || isAppInstalled(context, "com.xtc.im");
            } else if (sceneType == 2) {
                isInstalled = isAppInstalled(context, "com.xtc.moment") || isAppInstalled(context, "com.xtc.im.mainservice");
            }

            if (!isInstalled) {
                Toast.makeText(context, context.getString(R.string.error_no_target_app), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (!shareMessageManager.checkBaseVersion(sceneType)) {
                Toast.makeText(context, context.getString(R.string.error_unsupported_version), Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}