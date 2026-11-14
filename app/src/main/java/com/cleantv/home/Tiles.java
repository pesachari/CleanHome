package com.cleantv.home;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Shared helpers: app tiles, buttons, launching apps. */
final class Tiles {

    private Tiles() {}

    /** A tile = the focusable card plus the label under it. */
    static final class Tile {
        final LinearLayout root;
        final FrameLayout card;

        Tile(LinearLayout root, FrameLayout card) {
            this.root = root;
            this.card = card;
        }
    }

    static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    /**
     * @param banner   wide TV banner of the app (preferred), or null
     * @param icon     square icon, used when there is no banner, or null
     * @param subtitle text shown inside the card when the app is not installed, or null
     */
    static Tile create(Context c, String label, Drawable banner, Drawable icon, String subtitle,
                       int widthDp, int heightDp, View.OnClickListener click) {
        LinearLayout col = new LinearLayout(c);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        col.setClipChildren(false);
        col.setClipToPadding(false);

        FrameLayout card = new FrameLayout(c);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(c, 18));
        bg.setColor(0x2EFFFFFF);
        card.setBackground(bg);
        card.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        card.setClipToOutline(true);
        card.setFocusable(true);
        card.setFocusableInTouchMode(true);
        card.setClickable(true);
        card.setOnClickListener(click);

        // White ring drawn on top when focused
        GradientDrawable ring = new GradientDrawable();
        ring.setCornerRadius(dp(c, 18));
        ring.setColor(Color.TRANSPARENT);
        ring.setStroke(dp(c, 3), Color.WHITE);
        ring.setAlpha(0);
        card.setForeground(ring);

        if (banner != null) {
            ImageView iv = new ImageView(c);
            iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            iv.setImageDrawable(banner);
            card.addView(iv, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else if (icon != null) {
            ImageView iv = new ImageView(c);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            iv.setImageDrawable(icon);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(c, 72), dp(c, 72));
            lp.gravity = Gravity.CENTER;
            card.addView(iv, lp);
        } else {
            LinearLayout texts = new LinearLayout(c);
            texts.setOrientation(LinearLayout.VERTICAL);
            texts.setGravity(Gravity.CENTER);
            TextView big = new TextView(c);
            big.setText(label);
            big.setTextColor(Color.WHITE);
            big.setTextSize(24);
            big.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            big.setGravity(Gravity.CENTER);
            texts.addView(big);
            if (subtitle != null) {
                TextView small = new TextView(c);
                small.setText(subtitle);
                small.setTextColor(0xCCFFFFFF);
                small.setTextSize(13);
                small.setGravity(Gravity.CENTER);
                small.setPadding(dp(c, 8), dp(c, 6), dp(c, 8), 0);
                texts.addView(small);
            }
            card.addView(texts, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        col.addView(card, new LinearLayout.LayoutParams(dp(c, widthDp), dp(c, heightDp)));

        TextView name = new TextView(c);
        name.setText(label);
        name.setTextColor(Color.WHITE);
        name.setTextSize(16);
        name.setSingleLine(true);
        name.setEllipsize(TextUtils.TruncateAt.END);
        name.setGravity(Gravity.CENTER);
        name.setAlpha(0.6f);
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(
                dp(c, widthDp), ViewGroup.LayoutParams.WRAP_CONTENT);
        nlp.topMargin = dp(c, 14);
        col.addView(name, nlp);

        final float lift = dp(c, 12);
        card.setOnFocusChangeListener((v, has) -> {
            float s = has ? 1.12f : 1f;
            v.animate().scaleX(s).scaleY(s).translationZ(has ? lift : 0f).setDuration(160).start();
            ring.setAlpha(has ? 255 : 0);
            v.invalidate();
            name.animate().alpha(has ? 1f : 0.6f).setDuration(160).start();
        });

        return new Tile(col, card);
    }

    /** Small rounded button ("All apps", "Settings"). */
    static TextView pill(Context c, String text, View.OnClickListener click) {
        TextView b = new TextView(c);
        b.setText(text);
        b.setTextSize(16);
        b.setTextColor(Color.WHITE);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(c, 22), dp(c, 10), dp(c, 22), dp(c, 10));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(c, 999));
        bg.setColor(0x2EFFFFFF);
        b.setBackground(bg);
        b.setFocusable(true);
        b.setFocusableInTouchMode(true);
        b.setClickable(true);
        b.setOnClickListener(click);
        b.setOnFocusChangeListener((v, has) -> {
            bg.setColor(has ? Color.WHITE : 0x2EFFFFFF);
            b.setTextColor(has ? Color.BLACK : Color.WHITE);
            v.animate().scaleX(has ? 1.08f : 1f).scaleY(has ? 1.08f : 1f).setDuration(140).start();
        });
        return b;
    }

    static boolean isInstalled(PackageManager pm, String pkg) {
        try {
            return pm.getApplicationInfo(pkg, 0).enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /** Wide TV banner of an installed app, or null. */
    static Drawable bannerOf(PackageManager pm, String pkg) {
        Drawable d = null;
        Intent li = pm.getLeanbackLaunchIntentForPackage(pkg);
        if (li != null) {
            try {
                d = pm.getActivityBanner(li);
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        if (d == null) {
            try {
                d = pm.getApplicationBanner(pkg);
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        return d;
    }

    static Drawable iconOf(PackageManager pm, String pkg) {
        try {
            return pm.getApplicationIcon(pkg);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /** Opens an app; if it is not installed, opens its Play Store page. */
    static void launch(Activity a, String pkg) {
        PackageManager pm = a.getPackageManager();
        Intent i = pm.getLeanbackLaunchIntentForPackage(pkg);
        if (i == null) i = pm.getLaunchIntentForPackage(pkg);
        if (i != null) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                a.startActivity(i);
                return;
            } catch (ActivityNotFoundException ignored) {
            }
        }
        openStore(a, pkg);
    }

    static void openStore(Activity a, String pkg) {
        Intent store = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg));
        store.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            a.startActivity(store);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(a, "לא ניתן לפתוח את חנות Play", Toast.LENGTH_LONG).show();
        }
    }
}
