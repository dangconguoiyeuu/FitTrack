package com.fitness.fittrack.utils;

import android.content.Context;

import com.fitness.fittrack.FitTrackApplication;
import com.fitness.fittrack.models.User;
import com.fitness.fittrack.models.WorkoutSession;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Singleton quan ly Firebase Auth + Firestore.
 * Collections: "users/{uid}", "workouts/{docId}"
 */
public class FirebaseHelper {
    private static FirebaseHelper instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;

    private FirebaseHelper() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) instance = new FirebaseHelper();
        return instance;
    }

    public FirebaseAuth getAuth() { return auth; }
    public FirebaseUser getCurrentUser() { return auth.getCurrentUser(); }

    // Ham lay uid hien tai, uu tien Firebase va fallback sang tai khoan offline neu Firebase khong dang nhap.
    public String getUid() {
        FirebaseUser u = auth.getCurrentUser();
        if (u != null) return u.getUid();

        OfflineAuthHelper offlineAuth = getOfflineAuth();
        return offlineAuth != null ? offlineAuth.getCurrentUid() : null;
    }

    // Ham kiem tra app dang dung phien dang nhap offline hay khong.
    public boolean isOfflineSession() {
        OfflineAuthHelper offlineAuth = getOfflineAuth();
        return auth.getCurrentUser() == null && offlineAuth != null && offlineAuth.isLoggedIn();
    }

    // Ham lay email hien tai tu Firebase hoac tu phien dang nhap offline.
    public String getCurrentEmail() {
        FirebaseUser u = auth.getCurrentUser();
        if (u != null) return u.getEmail();

        OfflineAuthHelper offlineAuth = getOfflineAuth();
        return offlineAuth != null ? offlineAuth.getCurrentEmail() : null;
    }

    // Ham dang xuat ca Firebase va phien offline de nut Dang xuat xu ly dung moi truong hop.
    public void signOut() {
        auth.signOut();
        OfflineAuthHelper offlineAuth = getOfflineAuth();
        if (offlineAuth != null) offlineAuth.signOut();
    }

    // Ham lay OfflineAuthHelper bang application context, tranh yeu cau Activity tai cac lop dung chung.
    private OfflineAuthHelper getOfflineAuth() {
        Context context = FitTrackApplication.getAppContext();
        return context != null ? OfflineAuthHelper.getInstance(context) : null;
    }

    // ===== USER PROFILE =====
    public void saveProfile(User user, OnCompleteListener<Void> l) {
        if (user.getUid() == null) return;
        db.collection("users").document(user.getUid()).set(user.toMap()).addOnCompleteListener(l);
    }

    public void getProfile(String uid, OnCompleteListener<DocumentSnapshot> l) {
        db.collection("users").document(uid).get().addOnCompleteListener(l);
    }

    // ===== WORKOUTS =====
    public void saveWorkout(WorkoutSession s, OnCompleteListener<Void> l) {
        String id = db.collection("workouts").document().getId();
        s.setId(id);
        db.collection("workouts").document(id).set(s.toMap()).addOnCompleteListener(l);
    }

    public void getHistory(String uid, OnCompleteListener<QuerySnapshot> l) {
        db.collection("workouts")
            .whereEqualTo("userId", uid)
            .orderBy("date", Query.Direction.DESCENDING)
            .get().addOnCompleteListener(l);
    }
}
