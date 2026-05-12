package com.fitness.fittrack.utils;

import android.app.Activity;
import android.content.Context;

import com.fitness.fittrack.activities.BmiResultActivity;
import com.fitness.fittrack.activities.ExerciseActivity;
import com.fitness.fittrack.activities.HistoryActivity;
import com.fitness.fittrack.activities.HomeActivity;
import com.fitness.fittrack.activities.LoginActivity;
import com.fitness.fittrack.activities.PostWorkoutStretchActivity;
import com.fitness.fittrack.activities.ProfileActivity;
import com.fitness.fittrack.activities.RegisterActivity;
import com.fitness.fittrack.activities.ReminderActivity;
import com.fitness.fittrack.activities.SetupActivity;
import com.fitness.fittrack.activities.SplashActivity;
import com.fitness.fittrack.activities.StatisticsActivity;
import com.fitness.fittrack.activities.UsageHistoryActivity;
import com.fitness.fittrack.models.UsageLog;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UsageLogTracker {
    private static final SimpleDateFormat DISPLAY_TIME_FORMAT =
            new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

    // Ham ghi tu dong moi lan Activity duoc mo, gom thoi gian, nguoi dung, ten man hinh va ten chuc nang.
    public static void trackActivityOpened(Context context, Activity activity) {
        long now = System.currentTimeMillis();
        String activityName = activity.getClass().getSimpleName();
        String screenName = getScreenName(activity);
        String featureName = getFeatureName(activity);

        FirebaseUser currentUser = FirebaseHelper.getInstance().getCurrentUser();
        OfflineAuthHelper offlineAuth = OfflineAuthHelper.getInstance(context);
        String userId = currentUser != null ? currentUser.getUid() :
                (offlineAuth.isLoggedIn() ? offlineAuth.getCurrentUid() : "");
        String userName = resolveUserName(context, currentUser);

        UsageLog log = new UsageLog(
                now,
                DISPLAY_TIME_FORMAT.format(new Date(now)),
                userId,
                userName,
                screenName,
                featureName,
                activityName
        );
        UsageLogDatabase.getInstance(context).insertLog(log);
    }

    // Ham xac dinh ten man hinh de doc tu Activity class va luu vao lich su su dung.
    private static String getScreenName(Activity activity) {
        if (activity instanceof SplashActivity) return "Màn hình khởi động";
        if (activity instanceof LoginActivity) return "Màn hình đăng nhập";
        if (activity instanceof RegisterActivity) return "Màn hình đăng ký";
        if (activity instanceof HomeActivity) return "Trang chủ";
        if (activity instanceof ProfileActivity) return "Hồ sơ cá nhân";
        if (activity instanceof BmiResultActivity) return "Kết quả BMI";
        if (activity instanceof SetupActivity) return "Thiết lập bài tập";
        if (activity instanceof ExerciseActivity) return "Màn hình tập luyện";
        if (activity instanceof PostWorkoutStretchActivity) return "Giãn cơ sau tập";
        if (activity instanceof HistoryActivity) return "Lịch sử tập luyện";
        if (activity instanceof StatisticsActivity) return "Thống kê tập luyện";
        if (activity instanceof ReminderActivity) return "Nhắc nhở tập luyện";
        if (activity instanceof UsageHistoryActivity) return "Lịch sử sử dụng";
        return activity.getClass().getSimpleName();
    }

    // Ham xac dinh ten chuc nang tu Activity de ban ghi lich su de hieu nguoi dung vua mo chuc nang nao.
    private static String getFeatureName(Activity activity) {
        if (activity instanceof LoginActivity) return "Đăng nhập";
        if (activity instanceof RegisterActivity) return "Đăng ký tài khoản";
        if (activity instanceof HomeActivity) return "Menu chính";
        if (activity instanceof ProfileActivity) return "Cập nhật hồ sơ";
        if (activity instanceof BmiResultActivity) return "Phân tích BMI";
        if (activity instanceof SetupActivity) return "Chọn bài tập và mục tiêu";
        if (activity instanceof ExerciseActivity) return "Theo dõi bài tập bằng cảm biến";
        if (activity instanceof PostWorkoutStretchActivity) return "Giãn cơ bổ trợ";
        if (activity instanceof HistoryActivity) return "Xem lịch sử tập luyện";
        if (activity instanceof StatisticsActivity) return "Xem thống kê tập luyện";
        if (activity instanceof ReminderActivity) return "Cấu hình nhắc nhở";
        if (activity instanceof UsageHistoryActivity) return "Xem và tìm lịch sử sử dụng";
        if (activity instanceof SplashActivity) return "Điều hướng mở app";
        return "Mở màn hình";
    }

    // Ham lay ten nguoi dung tu Firebase hoac tu phien offline; neu chua dang nhap thi ghi Khach.
    private static String resolveUserName(Context context, FirebaseUser currentUser) {
        if (currentUser == null) {
            OfflineAuthHelper offlineAuth = OfflineAuthHelper.getInstance(context);
            if (offlineAuth.isLoggedIn()) {
                String name = offlineAuth.getCurrentName();
                if (name != null && !name.trim().isEmpty()) return name;
                String email = offlineAuth.getCurrentEmail();
                if (email != null && !email.trim().isEmpty()) return email;
            }
            return "Khách";
        }
        if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().trim().isEmpty()) {
            return currentUser.getDisplayName();
        }
        if (currentUser.getEmail() != null && !currentUser.getEmail().trim().isEmpty()) {
            return currentUser.getEmail();
        }
        return currentUser.getUid();
    }
}
