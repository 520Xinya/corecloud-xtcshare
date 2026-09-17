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
import com.xtc.shareapi.share.shareobject.XTCShareMessage;
import com.xtc.shareapi.share.shareobject.XTCTextObject;
import com.xtc.shareapi.share.sharescene.Chat;

public class text {

    private static ShareMessageManager shareMessageManager;

    public static void setupTextEngine(final AppCompatActivity activity, final View targetView, final View parentView) {
        if (activity == null || targetView == null || parentView == null) return;

        targetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText text7View = parentView.findViewById(R.id.text7);
                if (text7View == null) return;

                String shareContent = text7View.getText().toString().trim();
                if (TextUtils.isEmpty(shareContent)) {
                    Toast.makeText(activity, "请输入分享内容", Toast.LENGTH_SHORT).show();
                    return;
                }

                executeShareText(activity, shareContent);
            }
        });
    }

    private static void executeShareText(AppCompatActivity activity, String shareContent) {
        if (shareMessageManager == null) {
            shareMessageManager = new ShareMessageManager(activity);
        }

        if (!checkShareEnvironment(activity, 1)) {
            return;
        }

        try {
            Chat chat = new Chat();
            chat.setSelectActionMode(2);
            chat.setFriendType(4369);

            XTCTextObject xtcTextObject = new XTCTextObject();
            xtcTextObject.setText(shareContent);

            XTCShareMessage xtcShareMessage = new XTCShareMessage();
            xtcShareMessage.setShareObject(xtcTextObject);

            SendMessageToXTC.Request request = new SendMessageToXTC.Request();
            request.setScene(chat);
            request.setMessage(xtcShareMessage);
            request.setFlag(1);
            request.setSendMode(2);

            Bitmap appIcon = BitmapFactory.decodeResource(activity.getResources(), R.drawable.index);
            shareMessageManager.setAppIcon(appIcon);
            shareMessageManager.setAppName(activity.getString(R.string.app_name));

            shareMessageManager.sendRequestToXTC(request, "");
        } catch (Exception ignored) {}
    }

    private static boolean checkShareEnvironment(Context context, int sceneType) {
        try {
            boolean isInstalled = false;
            if (sceneType == 1) {
                isInstalled = isAppInstalled(context, "com.xtc.weichat") || isAppInstalled(context, "com.xtc.im");
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