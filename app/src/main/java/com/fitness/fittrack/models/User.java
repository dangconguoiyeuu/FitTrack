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
        if (b < 25) return "Bình thường";
        if (b < 30) return "Thừa cân";
        return "Béo phì";
    }

    // --- Đưa ra lời khuyên luyện tập dựa trên chỉ số BMI ---
    public static String getSuggestion(double b) {
        if (b <= 0) return "Cập nhật hồ sơ";
        if (b < 18.5) return "Tập tăng cơ, bổ sung dinh dưỡng";
        if (b < 25) return "Duy trì thể lực, tập đều đặn";
        if (b < 30) return "Tăng cardio giảm mỡ";
        return "Bắt đầu nhẹ nhàng, tăng dần cường độ";
    }

    // --- Đề xuất mục tiêu số lần tập mặc định dựa trên thể trạng ---
    public static int[] getDefaultTargets(double b) {
        if (b < 18.5) return new int[]{15, 20, 1000};
        if (b < 25) return new int[]{30, 40, 2000};
        if (b < 30) return new int[]{20, 30, 3000};
        return new int[]{10, 15, 1500};
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