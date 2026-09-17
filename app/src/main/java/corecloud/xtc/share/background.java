package corecloud.xtc.share;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class background {

    public static final int REQUEST_CODE_PICK_XTC_IMAGE = 2001;
    public static final int REQUEST_CODE_PERMISSION = 2002;

    public static void setupBackgroundEngine(final Activity activity, final View root) {
        if (activity == null || root == null) return;

        View targetView = findTargetView(root, R.id.set18);
        if (targetView != null) {
            targetView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkPermissionAndOpenGallery(activity);
                }
            });
        }

        applyBackgroundToH(activity);
    }

    private static View findTargetView(View parent, int id) {
        if (parent == null) return null;
        if (parent.getId() == id) return parent;
        if (parent instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) parent;
            for (int i = 0; i < group.getChildCount(); i++) {
                View result = findTargetView(group.getChildAt(i), id);
                if (result != null) return result;
            }
        }
        return null;
    }

    public static void checkPermissionAndOpenGallery(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
                return;
            }
        }
        openXtcGallery(activity);
    }

    public static void openXtcGallery(Activity activity) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra("com.xtc.camera.LEFT_BUTTON_TEXT", "取消");
            intent.putExtra("com.xtc.camera.RIGHT_BUTTON_TEXT", "确定");
            activity.startActivityForResult(intent, REQUEST_CODE_PICK_XTC_IMAGE);
        } catch (Exception ignored) {
        }
    }

    public static void handleActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_XTC_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            String photoPath = null;
            Uri dataUri = data.getData();

            Bundle bundle = data.getExtras();
            if (bundle != null) {
                photoPath = bundle.getString(MediaStore.EXTRA_OUTPUT, null);
            }

            if (photoPath != null && !photoPath.isEmpty()) {
                copyPathToPrivateStorage(activity, photoPath);
            } else if (dataUri != null) {
                copyUriToPrivateStorage(activity, dataUri);
            }

            applyBackgroundToH(activity);
        }
    }

    public static void handlePermissionsResult(Activity activity, int requestCode, int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openXtcGallery(activity);
            }
        }
    }

    private static void copyPathToPrivateStorage(Activity activity, String srcPath) {
        try {
            String lowerPath = srcPath.toLowerCase();
            if (!lowerPath.endsWith(".jpg") && !lowerPath.endsWith(".jpeg") && !lowerPath.endsWith(".png")) {
                return;
            }

            String ext = (lowerPath.endsWith(".png")) ? ".png" : ".jpg";
            File srcFile = new File(srcPath);
            if (!srcFile.exists()) return;

            clearOldBackgroundFiles(activity);

            File destFile = new File(activity.getFilesDir(), "index" + ext);
            InputStream in = new FileInputStream(srcFile);
            OutputStream out = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            out.flush();
            out.close();
            in.close();
        } catch (Exception ignored) {
        }
    }

    private static void copyUriToPrivateStorage(Activity activity, Uri uri) {
        try {
            InputStream in = activity.getContentResolver().openInputStream(uri);
            if (in == null) return;

            clearOldBackgroundFiles(activity);

            File destFile = new File(activity.getFilesDir(), "index.jpg");
            OutputStream out = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            out.flush();
            out.close();
            in.close();
        } catch (Exception ignored) {
        }
    }

    private static void clearOldBackgroundFiles(Activity activity) {
        File jpgFile = new File(activity.getFilesDir(), "index.jpg");
        File pngFile = new File(activity.getFilesDir(), "index.png");
        if (jpgFile.exists()) jpgFile.delete();
        if (pngFile.exists()) pngFile.delete();
    }

    public static void applyBackgroundToH(Activity activity) {
        try {
            View targetH = activity.findViewById(R.id.h);
            if (targetH == null) return;

            File jpgFile = new File(activity.getFilesDir(), "index.jpg");
            File pngFile = new File(activity.getFilesDir(), "index.png");
            File bgFile = jpgFile.exists() ? jpgFile : (pngFile.exists() ? pngFile : null);

            if (bgFile != null && bgFile.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                Bitmap bitmap = BitmapFactory.decodeFile(bgFile.getAbsolutePath(), options);
                if (bitmap != null) {
                    targetH.setBackground(new BitmapDrawable(activity.getResources(), bitmap));
                    return;
                }
            }

            targetH.setBackgroundColor(Color.BLACK);
        } catch (Throwable ignored) {
            try {
                View targetH = activity.findViewById(R.id.h);
                if (targetH != null) targetH.setBackgroundColor(Color.BLACK);
            } catch (Throwable ignored2) {
            }
        }
    }
}