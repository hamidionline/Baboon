package com.givekesh.baboon.Utils;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.givekesh.baboon.R;
import com.givekesh.baboon.Utils.Comments.POJOS.Comment;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;


public class Utils {

    private static final long MILLIS_JULIAN_EPOCH = -210866803200000L;
    private static final long MILLIS_OF_A_DAY = 86400000L;
    private final Context mContext;

    public Utils(Context context) {
        mContext = context;
    }

    public Drawable getMaterialIcon(MaterialDrawableBuilder.IconValue iconValue, int color) {
        return MaterialDrawableBuilder.with(mContext)
                .setIcon(iconValue)
                .setColor(color)
                .build();
    }

    public String getPersianDate(String input) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss", Locale.ENGLISH).parse(input);
            long julianDate = ((long) Math.floor((date.getTime() - MILLIS_JULIAN_EPOCH)) / MILLIS_OF_A_DAY);
            long PersianRowDate = PersianCalendarUtils.julianToPersian(julianDate);
            long year = PersianRowDate >> 16;
            int month = (int) (PersianRowDate & 0xff00) >> 8;
            int day = (int) (PersianRowDate & 0xff);
            int persianYear = (int) (year > 0 ? year : year - 1);
            String[] persianMonth = mContext.getResources().getStringArray(R.array.month);

            return String.valueOf(day + " " + persianMonth[month] + " " + persianYear);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return input;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void openTelegram() {
        mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://telegram.me/baboon_ir")));
    }

    public void openMailChooser() {
        String[] mails = new String[]{"info@baboon.ir"};
        String PACKAGE_GMAIL = "android.gm";
        String PACKAGE_EMAIL = "android.email";
        String INTENT_TYPE_MSG = "message/rfc822";
        String INTENT_TYPE_TEXT = "text/plain";

        Intent mailIntent = new Intent();
        mailIntent.setAction(Intent.ACTION_SEND);
        mailIntent.putExtra(Intent.EXTRA_EMAIL, mails);
        mailIntent.setType(INTENT_TYPE_MSG);

        PackageManager pm = mContext.getPackageManager();
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType(INTENT_TYPE_TEXT);

        Intent openInChooser = Intent.createChooser(mailIntent, mContext.getString(R.string.email_chooser));

        List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
        List<LabeledIntent> intentList = new ArrayList<>();
        for (ResolveInfo ri : resInfo) {
            String packageName = ri.activityInfo.packageName;
            if (packageName.contains(PACKAGE_EMAIL)) {
                mailIntent.setPackage(packageName);
            } else if (packageName.contains(PACKAGE_GMAIL)) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intent.setAction(Intent.ACTION_SEND);
                intent.setType(INTENT_TYPE_TEXT);

                intent.putExtra(Intent.EXTRA_EMAIL, mails);
                intent.setType(INTENT_TYPE_MSG);

                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            }
        }

        LabeledIntent[] extraIntents = intentList.toArray(new LabeledIntent[intentList.size()]);

        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
        mContext.startActivity(openInChooser);
    }

    public void openInstagram() {
        Uri uri = Uri.parse(mContext.getString(R.string.instagram_intent_url));
        Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

        likeIng.setPackage(mContext.getString(R.string.instagram_package));

        try {
            mContext.startActivity(likeIng);
        } catch (ActivityNotFoundException e) {
            mContext.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(mContext.getString(R.string.instagram_url))));
        }
    }

    public Intent getBazaarIntent() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (isBazaarInstalled()) {
            intent.setData(Uri.parse("bazaar://details?id=com.givekesh.baboon"));
            intent.setPackage("com.farsitel.bazaar");
        } else
            intent.setData(Uri.parse("https://cafebazaar.ir/app/com.givekesh.baboon/?l=fa"));
        return intent;
    }

    private boolean isBazaarInstalled() {
        PackageManager packageManager = mContext.getPackageManager();
        try {
            packageManager.getPackageInfo("com.farsitel.bazaar", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public ArrayList<Comment> sortComments(ArrayList<Comment> data) {
        ArrayList<Comment> result = new ArrayList<>();
        HashSet<Integer> map = new HashSet<>();

        for (int i = 0; i < data.size(); i++) {
            if (result.isEmpty() || !map.contains(data.get(i).getId())) {
                result.add(data.get(i));
                map.add(data.get(i).getId());
            }
            for (Comment comment : data)
                if (data.get(i).getId() == comment.getParent_id() && !map.contains(comment.getId())) {
                    result.add(comment);
                    map.add(comment.getId());
                }
        }
        return result;
    }
}
