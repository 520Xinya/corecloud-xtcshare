package corecloud.xtc.share;

import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class deving {

    private static final int[] DEVING_IDS = new int[]{
            R.id.set18,
            R.id.set45,
            R.id.set53,
            R.id.set80,
            R.id.set89
    };

    public static void setupDevingEngine(final AppCompatActivity activity, final View parentView) {
        if (activity == null || parentView == null) return;

        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity, activity.getString(R.string.deving), Toast.LENGTH_SHORT).show();
            }
        };

        for (int id : DEVING_IDS) {
            View targetView = parentView.findViewById(id);
            if (targetView != null) {
                targetView.setOnClickListener(clickListener);
            }
        }
    }
}