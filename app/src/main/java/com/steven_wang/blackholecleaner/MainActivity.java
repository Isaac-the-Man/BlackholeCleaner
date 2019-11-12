package com.steven_wang.blackholecleaner;

import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.File;
import java.util.Random;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity {

//    Constants / Parameters
    final int MIN_DURATION = 15;
    final int MAX_DURATION = 25;
    final int PROGRESS_FPS = 20;

//    Get Objects
    ImageView blackhole, halo, blackholeHalo, whiteScreen, seed, seedHalo;
    TextView deploy, status, stats, restart;
    ConstraintLayout layout;
    ProgressBar progressBar;
    AdView bannerAd;
    RequestConfiguration requestConfiguration;

//    Common attributes
    int height, width;
    int duration;

//    Flags
    boolean doBlackholeHaloAnimation = false;
    boolean doNebulaAnimation = false;
    int progress;

//    Status List
    String[] statusList = {
        "Launching Blackhole",
        "Searching Cache",
        "Searching Redundancy",
        "Collecting Garbage",
        "Clearing Cache",
        "Optimizing Performance",
        "Optimizing Memory",
        "Clearing Garbage"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Get Objects
        blackhole = findViewById(R.id.blackhole);
        halo = findViewById(R.id.halo);
        deploy = findViewById(R.id.deploy);
        layout = findViewById(R.id.layout);
        blackholeHalo = findViewById(R.id.blackholeHalo);
        whiteScreen = findViewById(R.id.flash);
        progressBar = findViewById(R.id.progressBar);
        status = findViewById(R.id.status);
        seed = findViewById(R.id.seed);
        seedHalo = findViewById(R.id.nebulaHalo);
        stats = findViewById(R.id.stats);
        restart = findViewById(R.id.restart);
        bannerAd = findViewById(R.id.adView);

//        configure halo size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;
        int radius = (int)Math.sqrt(Math.pow(height, 2) + Math.pow(width, 2))*2;
        halo.getLayoutParams().height = radius;
        halo.getLayoutParams().width = radius;
        halo.requestLayout();

//        Hide status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

//        Initiazlize Ads
        MobileAds.initialize(getApplicationContext(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });
        requestConfiguration = MobileAds.getRequestConfiguration().toBuilder()
                .setTagForChildDirectedTreatment(RequestConfiguration
                        .TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                .build();

//        kicks off halo animation cycle
        haloAnimation();

//        Do animation when clicked
        deploy.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

//                        Randomly select run duration (10 ~ 30 sec)
                        duration = getDuration();

//                        clear cache directory
                        Handler cacheHandler = new Handler();
                        cacheHandler.post(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        clearCache();
                                    }
                                }
                        );

//                        Animation
                        absorbAnimation(duration);
                        runProgress();
                        for (int i = 0; i<500; i++) {
                            createDebris();
                        }

//                        Explode Blackhole
                        Handler handler = new Handler();
                        handler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        doBlackholeHaloAnimation = false;
                                        explodeAnimation();
                                    }
                                }
                        , duration*1000);
                    }
                }
        );

//        reset to restart
        restart.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reset();
                    }
                }
        );

    }

//    load ad on start
    @Override
    protected void onStart() {
        super.onStart();
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAd.loadAd(adRequest);
    }

    //    delete cache
    public void clearCache() {
        File cacheDir = getCacheDir();
        deleteRecursive(cacheDir);
    }

//    delete every file in directory
    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        fileOrDirectory.delete();
    }

//    reset for restart
    public void reset() {
//        Flash screen
        Animation flashShow = new AlphaAnimation(0f, 1f);
        flashShow.setDuration(100);
        flashShow.setStartOffset(0);
        flashShow.setFillAfter(true);
        Animation flashDim = new AlphaAnimation(1f, 0f);
        flashDim.setDuration(2000);
        flashDim.setStartOffset(200);
        flashDim.setFillAfter(true);
        AnimationSet flashSet = new AnimationSet(true);
        flashSet.addAnimation(flashShow);
        flashSet.addAnimation(flashDim);
        flashSet.setFillAfter(true);
        whiteScreen.startAnimation(flashSet);

//        reset components when white out
        flashShow.setAnimationListener(
                new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        doNebulaAnimation = false;
                        doBlackholeHaloAnimation = false;
                        blackhole.clearAnimation();
                        seedHalo.clearAnimation();
                        blackhole.setAlpha(1f);
                        blackholeHalo.setAlpha(1f);
                        deploy.setVisibility(View.VISIBLE);
                        restart.setVisibility(View.INVISIBLE);
                        seed.setVisibility(View.INVISIBLE);
                        seedHalo.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        status.setVisibility(View.INVISIBLE);
                        stats.setVisibility(View.INVISIBLE);
                        haloAnimation();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                }
        );

//        refresh ad
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAd.loadAd(adRequest);
    }

//    Show ending scene
    public void endingScene() {
        progressBar.setVisibility(View.INVISIBLE);
        status.setVisibility(View.INVISIBLE);
        seed.setVisibility(View.VISIBLE);
        seedHalo.setVisibility(View.VISIBLE);
        restart.setVisibility(View.VISIBLE);
        showStats();
        nebulaAnimation();
    }

//    Calculate ending stats
    public void showStats() {
//        Randomly generate stats between 60 to 98
        Random random = new Random();
        int stat = 60 + random.nextInt(39);
        stats.setText(stat + "% Optimized");
        stats.setVisibility(View.VISIBLE);
    }

//    run progressbar & updating status
    public void runProgress() {
//        Distribute Status time, distribute equally excpet for the last element
        final int statusInterval = duration / statusList.length;

        progress = 0;
        progressBar.setMax(duration*PROGRESS_FPS);
        progressBar.setProgress(progress);

        progressBar.setVisibility(View.VISIBLE);
        status.setVisibility(View.VISIBLE);
        deploy.setVisibility(View.GONE);

        final Handler handler = new Handler();
        handler.post(
                new Runnable() {
                    @Override
                    public void run() {
//                        update status
                        int pointer = (int) (((float)progress / PROGRESS_FPS) / statusInterval);
                        if (pointer < statusList.length) {
                            status.setText(statusList[pointer]);
                        }

//                        update progressbar
                        progressBar.setProgress(progress);
                        progress++;
                        if (progress <= duration*PROGRESS_FPS) {
                            handler.postDelayed(this, 1000/PROGRESS_FPS);
                        }
                    }
                }
        );
    }

//    blackhole explode animation
    public void explodeAnimation() {
//        Rapid Expand Blackhole & its Halo
        Animation drasticExpand = new ScaleAnimation(1f, 3f, 1f, 3f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        drasticExpand.setDuration(1500);
        drasticExpand.setFillAfter(true);

//        Rapid Compress
        Animation drasticShrink = new ScaleAnimation(1f, 0.01f, 1f, 0.01f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        drasticShrink.setDuration(500);
        drasticShrink.setStartOffset(1500);
        drasticShrink.setFillAfter(true);

//        Dim Blackhole
        Animation dimBlackhole = new AlphaAnimation(1f, 0f);
        dimBlackhole.setDuration(500);
        dimBlackhole.setStartOffset(2000);
        dimBlackhole.setFillAfter(true);

//        Rapid Expand Halo
        Animation drasticExpandHalo = new ScaleAnimation(1f, 10f, 1f, 10f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        drasticExpandHalo.setDuration(500);
        drasticExpandHalo.setStartOffset(1500);
        drasticExpandHalo.setFillAfter(true);

//        Dim Halo
        Animation dimHalo = new AlphaAnimation(1f, 0f);
        dimHalo.setDuration(500);
        dimHalo.setStartOffset(1500);
        dimHalo.setFillAfter(true);

//        Flash screen
        Animation flashShow = new AlphaAnimation(0f, 1f);
        flashShow.setDuration(100);
        flashShow.setStartOffset(1800);
        flashShow.setFillAfter(true);
        Animation flashDim = new AlphaAnimation(1f, 0f);
        flashDim.setDuration(2000);
        flashDim.setStartOffset(2000);
        flashDim.setFillAfter(true);

        AnimationSet explodeSet = new AnimationSet(true);
        explodeSet.addAnimation(drasticExpand);
        explodeSet.addAnimation(drasticShrink);
        explodeSet.addAnimation(dimBlackhole);
        explodeSet.setFillAfter(true);

        AnimationSet haloSet = new AnimationSet(true);
        haloSet.addAnimation(drasticExpandHalo);
        haloSet.addAnimation(dimHalo);
        haloSet.setFillAfter(true);

        AnimationSet flash = new AnimationSet(true);
        flash.addAnimation(flashShow);
        flash.addAnimation(flashDim);
        flash.setFillAfter(true);

//        show ending scene when flashed
        flashShow.setAnimationListener(
                new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        endingScene();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                }
        );

        blackhole.startAnimation(explodeSet);
        blackholeHalo.startAnimation(haloSet);
        whiteScreen.setAnimation(flash);
    }

//    start blackhole halo animation
    public void haloAnimation() {
        doBlackholeHaloAnimation = true;
        //        Blackhole Halo expand & shrink
        Animation blackholeHaloExpand = new ScaleAnimation(1f, 1.25f, 1f, 1.25f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        blackholeHaloExpand.setDuration(1000);
        blackholeHaloExpand.setFillAfter(true);
        Animation blackholeHaloShrink = new ScaleAnimation(1f, 0.8f, 1f, 0.8f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        blackholeHaloShrink.setDuration(1000);
        blackholeHaloShrink.setStartOffset(1000);
        blackholeHaloShrink.setFillAfter(true);
        final AnimationSet blackholeSet = new AnimationSet(true);
        blackholeSet.addAnimation(blackholeHaloExpand);
        blackholeSet.addAnimation(blackholeHaloShrink);

        blackholeSet.setAnimationListener(
                new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (doBlackholeHaloAnimation) {
                            blackholeHalo.startAnimation(blackholeSet);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                }
        );
        blackholeHalo.startAnimation(blackholeSet);
    }

    //    start blackhole halo animation
    public void nebulaAnimation() {
        doNebulaAnimation = true;
        //        Nebula Halo expand & shrink
        Animation nebulaHaloExpand = new ScaleAnimation(1f, 1.25f, 1f, 1.25f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        nebulaHaloExpand.setDuration(1000);
        nebulaHaloExpand.setFillAfter(true);
        Animation nebulaHaloShrink = new ScaleAnimation(1f, 0.8f, 1f, 0.8f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        nebulaHaloShrink.setDuration(1000);
        nebulaHaloShrink.setStartOffset(1000);
        nebulaHaloShrink.setFillAfter(true);
        final AnimationSet nebulaSet = new AnimationSet(true);
        nebulaSet.addAnimation(nebulaHaloExpand);
        nebulaSet.addAnimation(nebulaHaloShrink);

        nebulaSet.setAnimationListener(
                new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (doNebulaAnimation) {
                            seedHalo.startAnimation(nebulaSet);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                }
        );
        seedHalo.startAnimation(nebulaSet);
    }

    //    Do N round of blackhole and halo animation
    public void absorbAnimation(int repeatCount) {
//        Halo absorb
        Animation haloShrink = new ScaleAnimation(1f, 0.05f, 1f, 0.05f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        haloShrink.setDuration(1000);
        haloShrink.setRepeatCount(repeatCount);

        halo.startAnimation(haloShrink);
    }

//    Randomly generate a debris , and set the animation
    public void createDebris() {
//        Create Instance
        final ImageView debris = new ImageView(getApplicationContext());
        debris.setBackground(getDrawable(R.drawable.debris_tiny));
        layout.addView(debris);
        debris.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
//                        remove listener immediately
                        debris.getViewTreeObserver().removeOnGlobalLayoutListener(this);

//                        Randomly Generate location 0-L, 1-R, 2-T, 3-B
                        Random random = new Random();
                        int side = random.nextInt(4);
                        int debris_x = 0;
                        int debris_y = 0;
                        switch (side){
                            case 0:
                                debris_x = 0 - debris.getWidth();
                                debris_y = random.nextInt(height);
                                break;
                            case 1:
                                debris_x = width;
                                debris_y = random.nextInt(height);
                                break;
                            case 2:
                                debris_x = random.nextInt(width);
                                debris_y = 0 - debris.getHeight();
                                break;
                            case 3:
                                debris_x = random.nextInt(width);
                                debris_y = height;
                                break;
                        }
                        debris.setX(debris_x);
                        debris.setY(debris_y);

                        //        set acceleration motion
                        Animation translate = new TranslateAnimation(0f
                                , blackhole.getX() + blackhole.getWidth()/2 - debris.getX() - debris.getWidth()/2, 0f
                                , blackhole.getY() + blackhole.getHeight()/2 - debris.getY() - debris.getHeight()/2);
                        translate.setInterpolator(new AccelerateInterpolator(2f));
                        translate.setDuration(1000);
                        translate.setStartOffset(random.nextInt(duration*1000));
                        translate.setFillAfter(true);
                        translate.setAnimationListener(
                                new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        layout.removeView(debris);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                }
                        );
                        debris.startAnimation(translate);
                    }
                }
        );
    }

//    Duration Generator
    public  int getDuration() {
        Random random = new Random();
        return MIN_DURATION + random.nextInt(MAX_DURATION - MIN_DURATION);
    }

}
