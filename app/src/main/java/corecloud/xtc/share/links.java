package corecloud.xtc.share;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xtc.shareapi.share.bean.SerializableMap;
import com.xtc.shareapi.share.communication.SendMessageToXTC;
import com.xtc.shareapi.share.manager.ShareMessageManager;
import com.xtc.shareapi.share.shareobject.XTCShareMessage;
import com.xtc.shareapi.share.shareobject.XTCWebObject;
import com.xtc.shareapi.share.sharescene.Chat;
import com.xtc.shareapi.share.sharescene.Moment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class links {

    public static class LinkBean {
        public String name;
        public String url;

        public LinkBean(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }

    public static void setupLinkEngine(final Activity activity, final View root) {
        if (activity == null || root == null) return;

        final View targetContainer = findTargetView(root, R.id.set4);
        final View scope = targetContainer != null ? targetContainer : root;

        final EditText link17 = (EditText) findTargetView(scope, R.id.link17);
        final EditText link20 = (EditText) findTargetView(scope, R.id.link20);
        final View link7 = findTargetView(scope, R.id.link7);
        final View link12 = findTargetView(scope, R.id.link12);
        final View link21 = findTargetView(scope, R.id.link21);

        if (link7 != null) {
            link7.setClickable(true);
            link7.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (validateFields(activity, link17, link20)) {
                        performXtcShare(activity, link17, link20, 1);
                    }
                }
            });
        }

        if (link12 != null) {
            link12.setClickable(true);
            link12.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (validateFields(activity, link17, link20)) {
                        performXtcShare(activity, link17, link20, 2);
                    }
                }
            });
        }

        if (link21 != null) {
            link21.setClickable(true);
            link21.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLinkListDialog(activity, link17, link20);
                }
            });
        }
    }

    private static void showLinkListDialog(final Activity activity, final EditText link17, final EditText link20) {
        List<LinkBean> list = loadJsonFromAssets(activity);
        if (list.isEmpty()) {
            Toast.makeText(activity, "未找到有效的链接配置", Toast.LENGTH_SHORT).show();
            return;
        }

        final FullScreenDialog dialog = new FullScreenDialog(activity, list, new OnItemClickListener() {
            @Override
            public void onItemClick(LinkBean item) {
                if (link17 != null) link17.setText(item.name);
                if (link20 != null) link20.setText(item.url);
            }
        });
        dialog.show();
    }

    private interface OnItemClickListener {
        void onItemClick(LinkBean item);
    }

    private static class FullScreenDialog extends Dialog {

        private final List<LinkBean> list;
        private final OnItemClickListener listener;
        private GestureDetector gestureDetector;

        public FullScreenDialog(@NonNull Activity activity, List<LinkBean> list, OnItemClickListener listener) {
            super(activity, android.R.style.Theme_NoTitleBar);
            this.list = list;
            this.listener = listener;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_link_list, null);
            setContentView(view);

            Window window = getWindow();
            if (window != null) {
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                window.setGravity(Gravity.CENTER);
            }

            applyBackground(view);
            initViews(view);
            initGesture(view);
        }

        private void applyBackground(View rootView) {
            File jpgFile = new File(getContext().getFilesDir(), "index.jpg");
            File pngFile = new File(getContext().getFilesDir(), "index.png");
            File bgFile = jpgFile.exists() ? jpgFile : (pngFile.exists() ? pngFile : null);

            if (bgFile != null && bgFile.exists()) {
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(bgFile.getAbsolutePath());
                    if (bitmap != null) {
                        rootView.setBackground(new BitmapDrawable(getContext().getResources(), bitmap));
                        return;
                    }
                } catch (Throwable ignored) {
                }
            }
            rootView.setBackgroundColor(Color.BLACK);
        }

        private void initViews(View view) {
            RecyclerView recyclerView = view.findViewById(R.id.rv_links);
            if (recyclerView != null) {
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(new RecyclerView.Adapter<LinkViewHolder>() {
                    @NonNull
                    @Override
                    public LinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_links, parent, false);
                        return new LinkViewHolder(itemView);
                    }

                    @Override
                    public void onBindViewHolder(@NonNull LinkViewHolder holder, int position) {
                        if (position == 0) {
                            holder.tvName.setText("返回");
                            holder.tvUrl.setText("");
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dismiss();
                                }
                            });
                        } else {
                            final LinkBean item = list.get(position - 1);
                            holder.tvName.setText(item.name);
                            holder.tvUrl.setText(item.url);
                            holder.tvName.setSelected(true);
                            holder.tvUrl.setSelected(true);
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (listener != null) listener.onItemClick(item);
                                    dismiss();
                                }
                            });
                        }
                    }

                    @Override
                    public int getItemCount() {
                        return list.size() + 1;
                    }
                });
            }
        }

        private void initGesture(View view) {
            gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    if (e1 != null && e2 != null) {
                        float deltaX = e2.getX() - e1.getX();
                        float deltaY = Math.abs(e2.getY() - e1.getY());
                        if (deltaX > 120 && Math.abs(velocityX) > 200 && deltaY < 150) {
                            dismiss();
                            return true;
                        }
                    }
                    return false;
                }
            });

            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            if (gestureDetector != null && gestureDetector.onTouchEvent(ev)) return true;
            return super.dispatchTouchEvent(ev);
        }
    }

    private static class LinkViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvUrl;

        public LinkViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.item_name);
            tvUrl = itemView.findViewById(R.id.item_url);
        }
    }

    private static List<LinkBean> loadJsonFromAssets(Activity activity) {
        List<LinkBean> list = new ArrayList<>();
        try {
            InputStream is = activity.getAssets().open("index.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            is.close();
            String jsonStr = sb.toString().trim();
            if (jsonStr.contains("[")) jsonStr = jsonStr.substring(jsonStr.indexOf("["));
            if (jsonStr.contains("]")) jsonStr = jsonStr.substring(0, jsonStr.lastIndexOf("]") + 1);
            JSONArray array = new JSONArray(jsonStr);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.optJSONObject(i);
                if (obj != null) {
                    String name = obj.optString("name", "");
                    String url = obj.optString("url", "");
                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(url)) list.add(new LinkBean(name, url));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private static View findTargetView(View parent, int id) {
        if (parent == null) return null;
        if (parent.getId() == id) return parent;
        if (parent instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) parent;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                View result = findTargetView(child, id);
                if (result != null) return result;
            }
        }
        return null;
    }

    private static boolean validateFields(Activity activity, EditText link17, EditText link20) {
        if (link17 == null || link20 == null) return false;
        String content = link17.getText().toString().trim();
        String url = link20.getText().toString().trim();
        return !TextUtils.isEmpty(content) && !TextUtils.isEmpty(url) && (url.startsWith("http://") || url.startsWith("https://"));
    }

    private static void performXtcShare(final Activity activity, final EditText link17, final EditText link20, final int sceneType) {
        activity.getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                String content = link17.getText().toString().trim();
                String url = link20.getText().toString().trim();
                String appName = activity.getApplicationInfo().loadLabel(activity.getPackageManager()).toString();
                SendMessageToXTC.Request request = new SendMessageToXTC.Request();
                XTCShareMessage xtcShareMessage = new XTCShareMessage();
                Bitmap fyBitmap = loadAssetsImage(activity, "index.png");
                Bitmap logoBitmap = loadAssetsImage(activity, "corecloud.png");
                try {
                    ShareMessageManager shareMessageManager = new ShareMessageManager(activity);
                    shareMessageManager.setAppName(appName);
                    shareMessageManager.setAppIcon(fyBitmap);
                    XTCWebObject xTCWebObject = new XTCWebObject();
                    SerializableMap serializableMap = new SerializableMap();
                    serializableMap.setMap(new HashMap<>());
                    xTCWebObject.setExtMap(serializableMap);
                    xTCWebObject.setExtInfo("");
                    xTCWebObject.setUrl(url);
                    xtcShareMessage.setShareObject(xTCWebObject);
                    xtcShareMessage.setThumbImage(logoBitmap);
                    xtcShareMessage.setDescription(content);
                    if (sceneType == 1) {
                        Chat chat = new Chat();
                        chat.setSelectActionMode(2);
                        chat.setFriendType(4369);
                        request.setScene(chat);
                        request.setFlag(1);
                        request.setSendMode(2);
                    } else if (sceneType == 2) {
                        Moment moment = new Moment();
                        request.setScene(moment);
                    }
                    request.setMessage(xtcShareMessage);
                    shareMessageManager.sendRequestToXTC(request, "123456");
                } catch (Exception ignored) {
                }
            }
        });
    }

    private static Bitmap loadAssetsImage(Activity activity, String fileName) {
        try {
            InputStream is = activity.getAssets().open(fileName);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            is.close();
            return bitmap;
        } catch (Exception e) {
            Bitmap fallback = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
            android.graphics.Canvas canvas = new android.graphics.Canvas(fallback);
            canvas.drawColor(0xFF008577);
            return fallback;
        }
    }
}