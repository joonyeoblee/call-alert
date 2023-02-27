package com.example.callalert;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CallLog;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;

public class NumberLog {

    private long getLastCallTime(Context context, String number) {
        long lastCallTime = 0;

        if (number == null || number.isEmpty()) {
            return lastCallTime;
        }

        // 권한 확인
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없는 경우
            return lastCallTime;
        }

        // 콘텐트 프로바이더를 사용하여 전화 기록 가져오기
        String[] projection = new String[] { CallLog.Calls.DATE };
        String selection = CallLog.Calls.NUMBER + " = ?";
        String[] selectionArgs = new String[] { number };
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
}
