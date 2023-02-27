package com.example.callalert;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ContactAdapter extends ArrayAdapter<String> {

    private ArrayList<Boolean> mToggleStates;
    private Map<Integer, Long> mLastCallTimeMap = new HashMap<>(); // 이전에 전화한 시간을 저장하는 Map
    private static final long TIME_INTERVAL = 24 * 60 * 60 * 1000; // 시간 간격 (24시간)
    private static final int REQUEST_CODE_READ_CALL_LOG = 1;
    long installationTime = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).firstInstallTime;

    public ContactAdapter(Context context, ArrayList<Contact> contacts, long installationTime) throws PackageManager.NameNotFoundException {
        super(context, 0, contacts);
        mToggleStates = new ArrayList<>();
        for (int i = 0; i < contacts.size(); i++) {
            mToggleStates.add(false);
            mLastCallTimeMap.put(i, installationTime);
            Log.d("installTime",String.valueOf(installationTime));
        }
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list, parent, false);
        }

        TextView nameTextView = convertView.findViewById(R.id.contact_name);
        Switch toggleButton = convertView.findViewById(R.id.toggle_button);

        String name = getItem(position);
        nameTextView.setText(name);

        // 리스트뷰가 다시 그려질 때 이전에 설정한 토글 버튼의 상태를 설정합니다.
        toggleButton.setChecked(mToggleStates.get(position));

        // 토글 버튼 클릭 이벤트 리스너 설정
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mToggleStates.set(position, isChecked);
            if (isChecked) {
                // 버튼이 켜진 경우
                Toast.makeText(getContext(), name + " button on", Toast.LENGTH_SHORT).show();

//                // 이전에 전화를 건 시간 저장
//                mLastCallTimeMap.put(position, getLastCallTime(getContext(), name));
//
//                Log.d("Time",String.valueOf(mLastCallTimeMap.get(position)));

            } else {
                // 버튼이 꺼진 경우
                Toast.makeText(getContext(), name + " button off", Toast.LENGTH_SHORT).show();

//                // 이전에 전화를 건 시간 가져오기
//                long lastCallTime = mLastCallTimeMap.get(position);
//
//                // 현재 시간과 비교하여 시간 간격 이내인지 확인
//                if (System.currentTimeMillis() - lastCallTime > TIME_INTERVAL) {
//                    // 24시간 이내에 전화를 걸지 않았으므로 알림 보내기
//                    showNotification(getContext(), position);
//                } else {
//                    // 24시간 이내에 전화를 걸었으므로 시간 갱신
//                    mLastCallTimeMap.put(position, System.currentTimeMillis() - TIME_INTERVAL);
//                }
            }
        });

        Log.d("ContactAdapter", "mToggleStates: " + mToggleStates.toString());
        return convertView;
    }


    private long getLastCallTime(Context context, String targetNumber) {
        long lastCallTime = 0;

        if (targetNumber == null || targetNumber.isEmpty()) {
            return lastCallTime;
        }

        // 권한 확인
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
            return lastCallTime;
        }

        // 콘텐트 프로바이더를 사용하여 전화 기록 가져오기
        String[] projection = new String[] { CallLog.Calls.DATE };
        String selection = CallLog.Calls.NUMBER + " = ?";
        String[] selectionArgs = new String[] { targetNumber };
        String sortOrder = CallLog.Calls.DATE + " DESC";

        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                projection, selection, selectionArgs, null);

// 전화 기록을 가져와서 리스트에 저장
        ArrayList<Long> callTimes = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long callTime = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                callTimes.add(callTime);
            } while (cursor.moveToNext());
            cursor.close();
        }

// 리스트를 내림차순으로 정렬
        Collections.sort(callTimes, Collections.reverseOrder());

// 가장 최근의 전화 기록 반환
        if (!callTimes.isEmpty()) {
            lastCallTime = callTimes.get(0);
        }


        return lastCallTime;
    }
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) getContext(),
                Manifest.permission.READ_CALL_LOG)) {
            // 이전에 권한이 거부되었을 때 설명을 보여줍니다.
            Toast.makeText(getContext(), "Call Log permission is needed to show call alerts.", Toast.LENGTH_LONG).show();
        }

        // 권한 요청
        ActivityCompat.requestPermissions((Activity) getContext(),
                new String[] { Manifest.permission.READ_CALL_LOG },
                REQUEST_CODE_READ_CALL_LOG);
    }

    // 알림을 보내는 메소드
    private void showNotification(Context context, int position) {
        String name = getItem(position);
        // 알림 채널 생성 (API 레벨 26 이상에서는 필수)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "call_alert";
            String channelName = name;
            String channelDescription = "24 hours have passed since the last call.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        // 알림 생성
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "call_alert")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Call Alert" + name)
                .setContentText(name + "24 hours have passed since the last call.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(position, builder.build());
    }
}

