package com.fitness.fittrack.models;
import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class WorkoutSession {
    private String id, userId, type;
    private int count, targetCount;
    private double distance, calories;
    private long duration;
    private Timestamp date;

    public WorkoutSession() {}

    public WorkoutSession(String userId, String type, int count, int targetCount,
                          double distance, double calories, long duration) {
        this.userId = userId; this.type = type; this.count = count;
        this.targetCount = targetCount; this.distance = distance;
        this.calories = calories; this.duration = duration;
        this.date = Timestamp.now();
    }

    public String getFormattedDuration() {
        long m = duration/60, s = duration%60;
        return String.format("%02d:%02d", m, s);
    }

    public String getTypeName() {
        if(type==null) return "";
        switch(type){case "pushup":return "Chống đẩy(Pushup)";case "situp":return "Gập bụng(Situp)";default:return "Chạy bộ(Run)";}
    }

    public Map<String,Object> toMap() {
        Map<String,Object> m = new HashMap<>();
        m.put("userId",userId); m.put("type",type); m.put("count",count);
        m.put("targetCount",targetCount); m.put("distance",distance);
        m.put("calories",calories); m.put("duration",duration);
        m.put("date",date);
        return m;
    }

    // Getters & Setters
    public String getId(){return id;} public void setId(String i){id=i;}
    public String getUserId(){return userId;} public void setUserId(String u){userId=u;}
    public String getType(){return type;} public void setType(String t){type=t;}
    public int getCount(){return count;} public void setCount(int c){count=c;}
    public int getTargetCount(){return targetCount;} public void setTargetCount(int t){targetCount=t;}
    public double getDistance(){return distance;} public void setDistance(double d){distance=d;}
    public double getCalories(){return calories;} public void setCalories(double c){calories=c;}
    public long getDuration(){return duration;} public void setDuration(long d){duration=d;}
    public Timestamp getDate(){return date;} public void setDate(Timestamp d){date=d;}
}
