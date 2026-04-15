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

    public double calculateBMI() {
        if (height <= 0) return 0;
        double hm = height / 100.0;
        return Math.round((weight / (hm * hm)) * 10.0) / 10.0;
    }

    public static String getBMICategory(double b) {
        if (b <= 0) return "Chua tinh";
        if (b < 18.5) return "Thieu can";
        if (b < 25) return "Binh thuong";
        if (b < 30) return "Thua can";
        return "Beo phi";
    }

    public static String getSuggestion(double b) {
        if (b <= 0) return "Cap nhat ho so";
        if (b < 18.5) return "Tap tang co, bo sung dinh duong";
        if (b < 25) return "Duy tri the luc, tap deu dan";
        if (b < 30) return "Tang cardio giam mo";
        return "Bat dau nhe, tang dan";
    }

    public static int[] getDefaultTargets(double b) {
        if (b < 18.5) return new int[]{15, 20, 1000};
        if (b < 25) return new int[]{30, 40, 2000};
        if (b < 30) return new int[]{20, 30, 3000};
        return new int[]{10, 15, 1500};
    }

    // Tinh calo uoc tinh:
    public static double estimateCalories(String type, int count, long elapsedSeconds, double weight) {
        double met = 1.0;

        // Gán chỉ số MET chuẩn cho từng loại bài tập
        switch (type) {
            case "running":
                met = 9.8; // Chạy bộ tốc độ ~10km/h
                break;
            case "pushup":
                met = 8.0; // Chống đẩy cường độ cao
                break;
            case "situp":
                met = 3.8; // Gập bụng vừa phải
                break;
        }

        // Chuyển giây sang phút
        double minutes = elapsedSeconds / 60.0;

        // Công thức tính calo: (MET * 3.5 * cân nặng) / 200 * số phút
        return (met * 3.5 * weight / 200.0) * minutes;
    }

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

    // Getters & Setters
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
