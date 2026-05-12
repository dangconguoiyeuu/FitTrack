package com.fitness.fittrack;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.fitness.fittrack.utils.UsageLogTracker;

public class FitTrackApplication extends Application {
    private static FitTrackApplication instance;

    // Hàm khởi tạo app và đăng ký bộ theo dõi vòng đời Activity để tự động ghi lịch sử sử dụng.
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            // Hàm được gọi khi Activity được tạo; dùng để lưu một bản ghi mở màn hình vào SQLite.
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                UsageLogTracker.trackActivityOpened(FitTrackApplication.this, activity);
            }

            // Hàm bắt buộc của ActivityLifecycleCallbacks; hiện không cần xử lý khi Activity start.
            @Override public void onActivityStarted(Activity activity) {}

            // Hàm bắt buộc của ActivityLifecycleCallbacks; hiện không cần xử lý khi Activity resume.
            @Override public void onActivityResumed(Activity activity) {}

            // Hàm bắt buộc của ActivityLifecycleCallbacks; hiện không cần xử lý khi Activity pause.
            @Override public void onActivityPaused(Activity activity) {}

            // Hàm bắt buộc của ActivityLifecycleCallbacks; hiện không cần xử lý khi Activity stop.
            @Override public void onActivityStopped(Activity activity) {}

            // Hàm bắt buộc của ActivityLifecycleCallbacks; hiện không cần xử lý khi lưu state.
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

            // Hàm bắt buộc của ActivityLifecycleCallbacks; hiện không cần xử lý khi Activity destroy.
            @Override public void onActivityDestroyed(Activity activity) {}
        });
    }

    // Ham tra ve application context de cac helper co the doc phien dang nhap offline ma khong can Activity.
    public static Context getAppContext() {
        return instance != null ? instance.getApplicationContext() : null;
    }
}
