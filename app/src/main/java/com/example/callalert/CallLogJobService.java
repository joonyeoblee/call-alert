package com.example.callalert;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;
import android.provider.ContactsContract;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Date;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CallLogJobService extends JobService {

    private static final int CALL_LOG_JOB_ID = 1234;
    private static final String NOTIFICATION_CHANNEL_ID = "channel_id";
    private static final String NOTIFICATION_CHANNEL_NAME = "channel_name";
    private static final String PREFS_NAME = "prefs";
    private static final String LAST_CHECK_TIME = "last_check_time";
    private static final long INTERVAL_DAY = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

    @Override
    public boolean onStartJob(JobParameters params) {
        // 현재 시간을 구합니다.
        long currentTime = System.currentTimeMillis();

        // 이전에 확인한 시간을 가져옵니다.
        long lastCheckTime = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getLong(LAST_CHECK_TIME, 0);

        // 현재 시간과 이전에 확인한 시간을 비교합니다.
        if (currentTime - lastCheckTime > INTERVAL_DAY) {
            // 24시간 이내에 통화한 기록이 있는지 확인합니다.
            if (!hasRecentCallLogs()) {
                // 알림을 보냅니다.
                sendNotification();
            }

            // 이전에 확인한 시간을 현재 시간으로 업데이트합니다.
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putLong(LAST_CHECK_TIME, currentTime)
                    .apply();
        }

        // 작업이 끝나면 false를 반환합니다.
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // 작업이 중지될 때 호출됩니다.
        return true;
    }

    private boolean hasRecentCallLogs() {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = null;
        try {
            // CallLog.Calls.CONTENT_URI로부터 24시간 이내에 통화한 기록을 가져옵니다.
            cursor = resolver.query(CallLog.Calls.CONTENT_URI, null, CallLog.Calls.DATE + ">?",
                    new String[]{Long.toString(new Date().getTime() - INTERVAL_DAY)}, null);
            return cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void sendNotification() {
        // NotificationManager를 가져옵니다.
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // NotificationChannel을 만듭니다. Android 8.0 (API 레벨 26) 이상에서는 필수입니다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // NotificationCompat.Builder를 사용하여 알림을 만듭니다.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("24시간 이내에 통화한 기록 없음")
                .setContentText("최근 24시간 이내에 통화한 기록이 없습니다. 연락해 보시는 건 어떨까요?")
                .setAutoCancel(true);

        // NotificationManager를 사용하여 알림을 보냅니다.
        notificationManager.notify(0, builder.build());
    }
}