package com.fitness.fittrack.models;

public class UsageLog {
    private long id;
    private long timestampMillis;
    private String timeText;
    private String userId;
    private String userName;
    private String screenName;
    private String featureName;
    private String activityName;

    // Hàm khởi tạo rỗng để các lớp khác có thể tạo object rồi set dữ liệu từng phần.
    public UsageLog() {}

    // Hàm khởi tạo đầy đủ để đóng gói một lần mở màn hình/chức năng trước khi lưu SQLite.
    public UsageLog(long timestampMillis, String timeText, String userId, String userName,
                    String screenName, String featureName, String activityName) {
        this.timestampMillis = timestampMillis;
        this.timeText = timeText;
        this.userId = userId;
        this.userName = userName;
        this.screenName = screenName;
        this.featureName = featureName;
        this.activityName = activityName;
    }

    // Hàm trả về id tự tăng của bản ghi trong SQLite.
    public long getId() { return id; }

    // Hàm gán id tự tăng sau khi đọc dữ liệu từ SQLite.
    public void setId(long id) { this.id = id; }

    // Hàm trả về thời gian dạng millis để sắp xếp chính xác.
    public long getTimestampMillis() { return timestampMillis; }

    // Hàm gán thời gian millis cho bản ghi lịch sử sử dụng.
    public void setTimestampMillis(long timestampMillis) { this.timestampMillis = timestampMillis; }

    // Hàm trả về thời gian đã format để hiển thị và tìm kiếm theo mốc thời gian.
    public String getTimeText() { return timeText; }

    // Hàm gán chuỗi thời gian đã format cho bản ghi.
    public void setTimeText(String timeText) { this.timeText = timeText; }

    // Hàm trả về uid Firebase của người dùng nếu đã đăng nhập.
    public String getUserId() { return userId; }

    // Hàm gán uid Firebase cho bản ghi lịch sử sử dụng.
    public void setUserId(String userId) { this.userId = userId; }

    // Hàm trả về tên/email người dùng được ghi nhận tại thời điểm mở màn hình.
    public String getUserName() { return userName; }

    // Hàm gán tên/email người dùng cho bản ghi.
    public void setUserName(String userName) { this.userName = userName; }

    // Hàm trả về tên màn hình đã được mở.
    public String getScreenName() { return screenName; }

    // Hàm gán tên màn hình đã được mở.
    public void setScreenName(String screenName) { this.screenName = screenName; }

    // Hàm trả về tên chức năng tương ứng với màn hình.
    public String getFeatureName() { return featureName; }

    // Hàm gán tên chức năng tương ứng với màn hình.
    public void setFeatureName(String featureName) { this.featureName = featureName; }

    // Hàm trả về tên class Activity để hỗ trợ kiểm tra kỹ thuật khi cần.
    public String getActivityName() { return activityName; }

    // Hàm gán tên class Activity cho bản ghi.
    public void setActivityName(String activityName) { this.activityName = activityName; }
}
