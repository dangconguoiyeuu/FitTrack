package com.fitness.fittrack.models;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String uid;
    private String email, name, gender, fitnessLevel;
    private int age;
    private double height, weight, bmi;
    private int targetPushup, targetSitup, targetSteps;

    public User() {}

    // --- Tính toán BMI dựa trên chiều cao (cm) và cân nặng (kg) ---
    public double calculateBMI() {
        if (height <= 0) return 0;
        double hm = height / 100.0;
        return Math.round((weight / (hm * hm)) * 10.0) / 10.0;
    }

    // --- Phân loại thể trạng dựa trên chỉ số BMI ---
    public static String getBMICategory(double b) {
        if (b <= 0) return "Chưa tính";
        if (b < 18.5) return "Thiếu cân";
        if (b < 23) return "Bình thường";
        if (b < 25) return "Thừa cân"; // Tiền béo phì
        if (b < 30) return "Béo phì độ I";
        return "Béo phì độ II"; // Đây là mức 30.5 của bạn trong ảnh
    }

    // --- Đưa ra lời khuyên luyện tập dựa trên chỉ số BMI ---
    public static String getSuggestion(double b) {
        if (b <= 0) return "Vui lòng cập nhật hồ sơ để nhận tư vấn";

        if (b < 18.5)
            return "Tập trung sức mạnh nhẹ nhàng và tăng dinh dưỡng trong 10 ngày đầu. Cập nhật lại thông số sau 10 ngày để theo dõi sự cải thiện.";

        if (b < 23)
            return "Duy trì lối sống năng động, nâng dần mục tiêu sau mỗi 10 ngày. Đừng quên cập nhật lại chỉ số cơ thể định kỳ để giữ vững vóc dáng.";

        if (b < 25)
            return "Tăng cường Cardio đốt mỡ, duy trì cường độ ổn định trong 10 ngày. Hãy cập nhật lại thông số sau 10 ngày để điều chỉnh lộ trình.";

        if (b < 30)
            return "Kết hợp Cardio cường độ vừa phải trong chu kỳ 10 ngày đầu. Hãy cập nhật lại cân nặng sau 10 ngày để đo lường hiệu quả giảm mỡ.";

        // Đối với BMI >= 30 (Trường hợp của bạn trong ảnh)
        return "Khởi động nhẹ nhàng để bảo vệ khớp trong 10 ngày, sau đó tăng dần cường độ. Hãy cập nhật lại thông số cơ thể sau 10 ngày tập luyện.";
    }

    // --- Đề xuất mục tiêu số lần tập mặc định dựa trên thể trạng ---
    // --- Đề xuất mục tiêu hàng ngày dựa trên tiêu chuẩn thích nghi 10 ngày ---
    public static int[] getDefaultTargets(double b) {
        if (b <= 0) return new int[]{10, 10, 3, 2000};

        if (b < 18.5) {
            // Thiếu cân: 3 hiệp, mỗi hiệp ít để chú trọng kỹ thuật
            return new int[]{10, 12, 3, 3000};
        }
        if (b < 23) {
            // Bình thường: 3 hiệp, cường độ tiêu chuẩn
            return new int[]{15, 20, 3, 5000}; // Tổng 45 lần pushup, 60 lần situp
        }
        if (b < 25) {
            // Thừa cân: 3 hiệp, giảm nhịp để giữ sức
            return new int[]{12, 15, 3, 4500};
        }
        if (b < 30) {
            // Béo phì độ I: 2 hiệp để cơ thể thích nghi
            return new int[]{10, 12, 2, 4000};
        }
        // Béo phì độ II (BMI >= 30): 2 hiệp ngắn, bảo vệ khớp tuyệt đối
        return new int[]{8, 10, 2, 3000};
    }

    /**
     * Tính toán lượng Calo tiêu thụ ước tính dựa trên chỉ số MET (Metabolic Equivalent of Task)
     * Công thức: (MET * 3.5 * cân nặng) / 200 * số phút tập luyện
     */
    public static double estimateCalories(String type, int count, long elapsedSeconds, double weight) {
        double met = 1.0;

        switch (type) {
            case "running":
                met = 9.8; // Chạy bộ tốc độ trung bình (~10km/h)
                break;
            case "pushup":
                met = 8.0; // Chống đẩy cường độ cao
                break;
            case "situp":
                met = 3.8; // Gập bụng vừa phải
                break;
        }

        double minutes = elapsedSeconds / 60.0;
        return (met * 3.5 * weight / 200.0) * minutes;
    }

    // --- Chuyển đổi đối tượng sang Map để lưu trữ trên Firebase Firestore ---
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("uid", uid); m.put("email", email); m.put("name", name);
        m.put("age", age); m.put("height", height); m.put("weight", weight);
        m.put("gender", gender); m.put("fitnessLevel", fitnessLevel);
        m.put("bmi", bmi);
        m.put("targetPushup", targetPushup);
        m.put("targetSitup", targetSitup);
        m.put("targetSteps", targetSteps);
        return m;
    }

    // --- Hệ thống Getters & Setters ---
    public String getUid(){return uid;} public void setUid(String u){uid=u;}
    public String getEmail(){return email;} public void setEmail(String e){email=e;}
    public String getName(){return name;} public void setName(String n){name=n;}
    public int getAge(){return age;} public void setAge(int a){age=a;}
    public double getHeight(){return height;} public void setHeight(double h){height=h;}
    public double getWeight(){return weight;} public void setWeight(double w){weight=w;}
    public String getGender(){return gender;} public void setGender(String g){gender=g;}
    public String getFitnessLevel(){return fitnessLevel;} public void setFitnessLevel(String f){fitnessLevel=f;}
    public double getBmi(){return bmi;} public void setBmi(double b){bmi=b;}
    public int getTargetPushup(){return targetPushup;} public void setTargetPushup(int t){targetPushup=t;}
    public int getTargetSitup(){return targetSitup;} public void setTargetSitup(int t){targetSitup=t;}
    public int getTargetSteps(){return targetSteps;} public void setTargetSteps(int t){targetSteps=t;}
}