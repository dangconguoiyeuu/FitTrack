package com.fitness.fittrack.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fitness.fittrack.models.UsageLog;

import java.util.ArrayList;
import java.util.List;

public class UsageLogDatabase extends SQLiteOpenHelper {
    private static final String DB_NAME = "fittrack_usage_logs.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "usage_logs";

    private static UsageLogDatabase instance;

    // Hàm lấy singleton database để toàn app dùng chung một kết nối SQLite offline.
    public static synchronized UsageLogDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new UsageLogDatabase(context.getApplicationContext());
        }
        return instance;
    }

    // Hàm khởi tạo SQLiteOpenHelper với tên file database lưu trực tiếp trên thiết bị.
    private UsageLogDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Hàm tạo bảng usage_logs khi database chưa tồn tại trên thiết bị.
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "timestamp_millis INTEGER NOT NULL, " +
                "time_text TEXT NOT NULL, " +
                "user_id TEXT, " +
                "user_name TEXT, " +
                "screen_name TEXT NOT NULL, " +
                "feature_name TEXT NOT NULL, " +
                "activity_name TEXT NOT NULL" +
                ")");
    }

    // Hàm nâng cấp database khi DB_VERSION thay đổi; hiện tại chỉ tạo mới từ phiên bản đầu.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    // Hàm lưu một lần mở màn hình/chức năng vào SQLite, hoạt động cả khi không có mạng.
    public long insertLog(UsageLog log) {
        ContentValues values = new ContentValues();
        values.put("timestamp_millis", log.getTimestampMillis());
        values.put("time_text", log.getTimeText());
        values.put("user_id", log.getUserId());
        values.put("user_name", log.getUserName());
        values.put("screen_name", log.getScreenName());
        values.put("feature_name", log.getFeatureName());
        values.put("activity_name", log.getActivityName());
        return getWritableDatabase().insert(TABLE, null, values);
    }

    // Hàm lấy toàn bộ lịch sử sử dụng, sắp xếp bản ghi mới nhất lên đầu danh sách.
    public List<UsageLog> getAllLogs() {
        return queryLogs(null, null);
    }

    // Hàm tìm kiếm lịch sử theo chuỗi thời gian người dùng nhập, ví dụ 12/05/2026 hoặc 12/05/2026 09:30.
    public List<UsageLog> searchByTime(String timeKeyword) {
        String keyword = timeKeyword == null ? "" : timeKeyword.trim();
        if (keyword.isEmpty()) {
            return getAllLogs();
        }
        return queryLogs("time_text LIKE ?", new String[]{"%" + keyword + "%"});
    }

    // Hàm query chung để tái sử dụng cho xem tất cả và tìm kiếm theo thời gian.
    private List<UsageLog> queryLogs(String selection, String[] args) {
        List<UsageLog> logs = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(
                TABLE,
                null,
                selection,
                args,
                null,
                null,
                "timestamp_millis DESC"
        );
        try {
            while (cursor.moveToNext()) {
                logs.add(fromCursor(cursor));
            }
        } finally {
            cursor.close();
        }
        return logs;
    }

    // Hàm chuyển một dòng Cursor từ SQLite thành object UsageLog để adapter hiển thị.
    private UsageLog fromCursor(Cursor cursor) {
        UsageLog log = new UsageLog();
        log.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        log.setTimestampMillis(cursor.getLong(cursor.getColumnIndexOrThrow("timestamp_millis")));
        log.setTimeText(cursor.getString(cursor.getColumnIndexOrThrow("time_text")));
        log.setUserId(cursor.getString(cursor.getColumnIndexOrThrow("user_id")));
        log.setUserName(cursor.getString(cursor.getColumnIndexOrThrow("user_name")));
        log.setScreenName(cursor.getString(cursor.getColumnIndexOrThrow("screen_name")));
        log.setFeatureName(cursor.getString(cursor.getColumnIndexOrThrow("feature_name")));
        log.setActivityName(cursor.getString(cursor.getColumnIndexOrThrow("activity_name")));
        return log;
    }
}
