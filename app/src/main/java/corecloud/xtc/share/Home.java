package corecloud.xtc.share;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.xtc.shareapi.share.communication.BaseResponse;
import com.xtc.shareapi.share.communication.ShowMessageFromXTC;
import com.xtc.shareapi.share.interfaces.IResponseCallback;
import com.xtc.shareapi.share.interfaces.IXTCCallback;
import com.xtc.shareapi.share.manager.ShareMessageManager;
import com.xtc.shareapi.share.manager.XTCCallbackImpl;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class Home extends AppCompatActivity implements IResponseCallback {

    private ViewPager2 viewPager;
    private QuantumIndicatorView indicatorView;
    private static final int PAGE_COUNT = 6;

    private IXTCCallback callback;
    public ShareMessageManager shareMessageManager;
    private Resources mInjectResources;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        this.callback = new XTCCallbackImpl(this);
        this.shareMessageManager = new ShareMessageManager(this);

        viewPager = findViewById(R.id.viewPager);
        indicatorView = findViewById(R.id.indicator_container);

        if (indicatorView != null) {
            indicatorView.setDotCount(PAGE_COUNT);
        }

        HomeAdapter adapter = new HomeAdapter();
        viewPager.setAdapter(adapter);

        viewPager.setPageTransformer(new DepthPageTransformer());

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (indicatorView != null) {
                    indicatorView.onPageScrolled(position, positionOffset);
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (indicatorView != null) {
                    indicatorView.onPageSelected(position);
                }

                setupCurrentPageElements(position);
            }
        });

        background.applyBackgroundToH(this);
    }

    private void setupCurrentPageElements(int position) {
        View recyclerView = viewPager.getChildAt(0);
        if (recyclerView instanceof RecyclerView) {
            RecyclerView.ViewHolder viewHolder = ((RecyclerView) recyclerView).findViewHolderForAdapterPosition(position);
            if (viewHolder != null && viewHolder.itemView != null) {
                TextView top8View = viewHolder.itemView.findViewById(R.id.top8);
                if (top8View != null) {
                    top8View.setSelected(true);
                }

                View set36View = viewHolder.itemView.findViewById(R.id.set36);
                if (set36View != null) {
                    shareapp.setupShareEngine(this, set36View);
                }

                links.setupLinkEngine(this, viewHolder.itemView);

                View text8View = viewHolder.itemView.findViewById(R.id.text8);
                if (text8View != null) {
                    text.setupTextEngine(this, text8View, viewHolder.itemView);
                }

                app.setupAppEngine(this, viewHolder.itemView);
                deving.setupDevingEngine(this, viewHolder.itemView);
                background.setupBackgroundEngine(this, viewHolder.itemView);

                loadAppNameWithRxJava3(viewHolder.itemView);
            }
        }
    }

    private void loadAppNameWithRxJava3(View itemView) {
        TextView set52View = itemView.findViewById(R.id.set52);
        if (set52View == null) return;

        Disposable disposable = Single.fromCallable(() -> {
                    ApplicationInfo appInfo = getApplicationInfo();
                    return getPackageManager().getApplicationLabel(appInfo).toString();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        appName -> set52View.setText(appName),
                        throwable -> {
                        }
                );

        disposables.add(disposable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        background.handleActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        background.handlePermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public Resources getResources() {
        if (mInjectResources == null) {
            final Resources baseRes = super.getResources();
            mInjectResources = new Resources(baseRes.getAssets(), baseRes.getDisplayMetrics(), baseRes.getConfiguration()) {
                @Override
                public int getIdentifier(String name, String defType, String defPackage) {
                    int customId = shareapp.handleIdentifierInject(name, defType);
                    if (customId != 0) return customId;
                    return super.getIdentifier(name, defType, defPackage);
                }

                @Override
                public Drawable getDrawable(int id, @Nullable Theme theme) throws NotFoundException {
                    Drawable customDrawable = shareapp.handleDrawableInject(baseRes, id, theme);
                    if (customDrawable != null) return customDrawable;
                    return super.getDrawable(id, theme);
                }

                @Override
                public Drawable getDrawable(int id) throws NotFoundException {
                    Drawable customDrawable = shareapp.handleDrawableInject(baseRes, id, null);
                    if (customDrawable != null) return customDrawable;
                    return super.getDrawable(id);
                }

                @Override
                public String getResourceName(int resid) throws NotFoundException {
                    if (resid == 0x7f090024) {
                        return "res/layout/chat_moment_sence.xml";
                    }
                    return super.getResourceName(resid);
                }

                @Override
                public android.content.res.XmlResourceParser getLayout(int id) throws NotFoundException {
                    if (id == 0x7f090024) {
                        return baseRes.getLayout(R.layout.chat_moment_sence);
                    }
                    return super.getLayout(id);
                }
            };
        }
        return mInjectResources;
    }

    @Override
    public String getPackageName() {
        return shareapp.handlePackageNameInject(super.getPackageName());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (this.callback != null) {
            this.callback.handleIntent(intent, this);
        }
    }

    @Override
    public void onReq(ShowMessageFromXTC.Request var1) {}

    @Override
    public void onResp(boolean isSuccess, BaseResponse var1) {
        runOnUiThread(() -> shareapp.handleShareResponse(Home.this, var1));
    }
}