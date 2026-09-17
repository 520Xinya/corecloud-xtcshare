package corecloud.xtc.share;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

public class link {

    public static void setupLinkEngine(final Context context, final View view) {
        if (context == null || view == null) return;

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String url = context.getString(R.string.link);
                    if (url != null && !url.isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                } catch (Exception ignored) {}
            }
        });
    }
}