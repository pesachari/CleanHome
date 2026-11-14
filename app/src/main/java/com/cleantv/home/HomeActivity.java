package com.cleantv.home;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** The home screen: big clock, day/night background, a row of favourite apps. */
public class HomeActivity extends Activity {

    /** label, package, text shown when the app is not installed yet */
    private static final String[][] APPS = {
            {"Netflix", "com.netflix.ninja", "לחץ להתקנה"},
            {"YouTube", "com.google.android.youtube.tv", "לחץ להתקנה"},
            {"Apple TV", "com.apple.atve.androidtv.appletv", "לחץ להתקנה"},
            {"Prime Video", "com.amazon.amazonvideo.livingroom", "לחץ להתקנה"},
            {"דפדפן", "com.phlox.tvwebbrowser", "לחץ להתקנת TV Bro"},
    };

    private View background;
    private TextView dateView;
    private TextView greetingView;
    private LinearLayout appRow;
    private TextView homeButtonPill;
    private final List<View> cards = new ArrayList<>();
    private int focusIndex = 0;
    private int phase = -1;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            updateTime();
            handler.postDelayed(this, 30_000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildUi());
    }

    @Override
    protected void onResume() {
        super.onResume();
        buildAppRow(); // refresh: apps may have been installed or removed
        homeButtonPill.setVisibility(HomeButtonService.isEnabled(this) ? View.GONE : View.VISIBLE);
        handler.removeCallbacks(tick);
        handler.post(tick);
        final int idx = Math.min(focusIndex, cards.size() - 1);
        appRow.post(() -> cards.get(idx).requestFocus());
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(tick);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Home pressed while already home: jump back to the first app
        focusIndex = 0;
        if (!cards.isEmpty()) cards.get(0).requestFocus();
    }

    @Override
    public void onBackPressed() {
        // This is the home screen; there is nothing to go back to.
    }

    // ---------------------------------------------------------------- UI

    private int dp(float v) {
        return Tiles.dp(this, v);
    }

    private View buildUi() {
        FrameLayout root = new FrameLayout(this);
        root.setClipChildren(false);

        background = new View(this);
        root.addView(background, matchParent());

        // Darken the bottom so the tiles stay readable on bright backgrounds
        View scrim = new View(this);
        scrim.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0x22000000, 0x00000000, 0x99000000}));
        root.addView(scrim, matchParent());

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(72), dp(44), dp(72), dp(36));
        content.setClipChildren(false);
        content.setClipToPadding(false);
        root.addView(content, matchParent());

        content.addView(new View(this), new LinearLayout.LayoutParams(0, 0, 1f));

        TextClock clock = new TextClock(this);
        clock.setFormat24Hour("HH:mm");
        clock.setFormat12Hour("HH:mm");
        clock.setTextSize(132);
        clock.setTextColor(Color.WHITE);
        clock.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
        clock.setShadowLayer(dp(12), 0, dp(2), 0x55000000);
        clock.setIncludeFontPadding(false);
        content.addView(clock, wrap());

        dateView = new TextView(this);
        dateView.setTextSize(26);
        dateView.setTextColor(0xE6FFFFFF);
        dateView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        dateView.setShadowLayer(dp(8), 0, dp(1), 0x55000000);
        content.addView(dateView, wrap());

        greetingView = new TextView(this);
        greetingView.setTextSize(20);
        greetingView.setTextColor(0xB3FFFFFF);
        LinearLayout.LayoutParams glp = wrap();
        glp.topMargin = dp(6);
        content.addView(greetingView, glp);

        content.addView(new View(this), new LinearLayout.LayoutParams(0, 0, 1.3f));

        appRow = new LinearLayout(this);
        appRow.setOrientation(LinearLayout.HORIZONTAL);
        appRow.setClipChildren(false);
        appRow.setClipToPadding(false);
        content.addView(appRow, wrap());

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setClipChildren(false);
        LinearLayout.LayoutParams blp = wrap();
        blp.topMargin = dp(26);
        content.addView(buttons, blp);

        buttons.addView(Tiles.pill(this, "כל האפליקציות",
                v -> startActivity(new Intent(this, AllAppsActivity.class))), wrap());
        LinearLayout.LayoutParams slp = wrap();
        slp.setMarginStart(dp(16));
        buttons.addView(Tiles.pill(this, "הגדרות",
                v -> startActivity(new Intent(Settings.ACTION_SETTINGS))), slp);

        // Shown only until the Home-button service is turned on
        homeButtonPill = Tiles.pill(this, "⚙ הפעלת כפתור הבית", v -> {
            try {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } catch (android.content.ActivityNotFoundException e) {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        });
        LinearLayout.LayoutParams hlp = wrap();
        hlp.setMarginStart(dp(16));
        buttons.addView(homeButtonPill, hlp);

        return root;
    }

    private void buildAppRow() {
        appRow.removeAllViews();
        cards.clear();
        PackageManager pm = getPackageManager();
        for (int i = 0; i < APPS.length; i++) {
            final String label = APPS[i][0];
            final String pkg = APPS[i][1];
            final int index = i;
            boolean installed = Tiles.isInstalled(pm, pkg);
            Drawable banner = installed ? Tiles.bannerOf(pm, pkg) : null;
            Drawable icon = (installed && banner == null) ? Tiles.iconOf(pm, pkg) : null;
            Tiles.Tile t = Tiles.create(this, label, banner, icon,
                    installed ? null : APPS[i][2], 224, 126,
                    v -> Tiles.launch(this, pkg));
            t.card.setOnFocusChangeListener(chain(t.card.getOnFocusChangeListener(), index));
            LinearLayout.LayoutParams lp = wrap();
            if (i > 0) lp.setMarginStart(dp(26));
            appRow.addView(t.root, lp);
            cards.add(t.card);
        }
    }

    /** Keeps the tile's own focus animation and also remembers which tile is focused. */
    private View.OnFocusChangeListener chain(View.OnFocusChangeListener inner, int index) {
        return (v, has) -> {
            if (inner != null) inner.onFocusChange(v, has);
            if (has) focusIndex = index;
        };
    }

    // ---------------------------------------------------------------- time & background

    private void updateTime() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        dateView.setText(new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(new Date()));
        greetingView.setText(greeting(hour));

        int p = phaseFor(hour);
        if (p != phase) {
            Drawable next = new GradientDrawable(GradientDrawable.Orientation.TL_BR, colorsFor(p));
            Drawable current = background.getBackground();
            if (current == null) {
                background.setBackground(next);
            } else {
                if (current instanceof TransitionDrawable) {
                    current = ((TransitionDrawable) current).getDrawable(1);
                }
                TransitionDrawable td = new TransitionDrawable(new Drawable[]{current, next});
                td.setCrossFadeEnabled(true);
                background.setBackground(td);
                td.startTransition(3000);
            }
            phase = p;
        }
    }

    private static String greeting(int h) {
        if (h >= 5 && h < 12) return "בוקר טוב";
        if (h >= 12 && h < 17) return "צהריים טובים";
        if (h >= 17 && h < 21) return "ערב טוב";
        return "לילה טוב";
    }

    /** 0 = night, 1 = sunrise, 2 = day, 3 = sunset */
    private static int phaseFor(int h) {
        if (h >= 5 && h < 8) return 1;
        if (h >= 8 && h < 17) return 2;
        if (h >= 17 && h < 20) return 3;
        return 0;
    }

    private static int[] colorsFor(int phase) {
        switch (phase) {
            case 1:  return new int[]{0xFF2B3A67, 0xFFC56C76, 0xFFF2B880};
            case 2:  return new int[]{0xFF17428A, 0xFF3F7FCB, 0xFF8CBBE8};
            case 3:  return new int[]{0xFF2D1B4E, 0xFF9E3A62, 0xFFF08A5D};
            default: return new int[]{0xFF04060D, 0xFF0D1830, 0xFF222850};
        }
    }

    // ---------------------------------------------------------------- layout helpers

    private static FrameLayout.LayoutParams matchParent() {
        return new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private static LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
