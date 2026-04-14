package com.fitness.fittrack.utils;

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
    public String getUid() {
        FirebaseUser u = auth.getCurrentUser();
        return u != null ? u.getUid() : null;
    }
    public void signOut() { auth.signOut(); }

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
