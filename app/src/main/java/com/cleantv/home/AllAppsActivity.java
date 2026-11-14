package com.cleantv.home;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

/** Grid of every TV app installed on the device. */
public class AllAppsActivity extends Activity {

    private static final int COLUMNS = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF04060D, 0xFF0D1830, 0xFF222850}));
        scroll.setFillViewport(true);
        scroll.setClipChildren(false);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(64), dp(40), dp(64), dp(48));
        content.setClipChildren(false);
        content.setClipToPadding(false);
        scroll.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(this);
        title.setText("כל האפליקציות");
        title.setTextSize(34);
        title.setTextColor(Color.WHITE);
        title.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        content.addView(title);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(COLUMNS);
        grid.setClipChildren(false);
        grid.setClipToPadding(false);
        LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        glp.topMargin = dp(28);
        content.addView(grid, glp);

        PackageManager pm = getPackageManager();
        Intent query = new Intent(Intent.ACTION_MAIN);
        query.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<ResolveInfo> apps = new ArrayList<>(pm.queryIntentActivities(query, 0));
        apps.removeIf(ri -> ri.activityInfo.packageName.equals(getPackageName()));

        Collator collator = Collator.getInstance();
        apps.sort((a, b) -> collator.compare(
                String.valueOf(a.loadLabel(pm)), String.valueOf(b.loadLabel(pm))));

        boolean first = true;
        for (ResolveInfo ri : apps) {
            final ActivityInfo ai = ri.activityInfo;
            String label = String.valueOf(ri.loadLabel(pm));
            Drawable banner = ai.loadBanner(pm);
            if (banner == null) banner = ai.applicationInfo.loadBanner(pm);
            Drawable icon = banner == null ? ri.loadIcon(pm) : null;

            Tiles.Tile t = Tiles.create(this, label, banner, icon, null, 200, 112, v -> {
                Intent launch = new Intent(Intent.ACTION_MAIN);
                launch.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
                launch.setClassName(ai.packageName, ai.name);
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(launch);
                } catch (ActivityNotFoundException | SecurityException e) {
                    Toast.makeText(this, "לא ניתן לפתוח את " + label, Toast.LENGTH_SHORT).show();
                }
            });

            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            lp.setMargins(dp(14), dp(14), dp(14), dp(14));
            grid.addView(t.root, lp);

            if (first) {
                t.card.post(t.card::requestFocus);
                first = false;
            }
        }

        if (apps.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("לא נמצאו אפליקציות");
            empty.setTextColor(0xB3FFFFFF);
            empty.setTextSize(20);
            content.addView(empty);
        }

        setContentView(scroll);
    }

    private int dp(float v) {
        return Tiles.dp(this, v);
    }
}
