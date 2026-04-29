package com.fitness.fittrack.utils;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public final class StreakHelper {
    public interface Listener {
        void onComplete(boolean success, int streakDays);
    }

    private StreakHelper() {}

    public static void updateAfterWorkout(String uid, Listener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(uid);
        LocalDate today = LocalDate.now();

        db.runTransaction(transaction -> {
            var snapshot = transaction.get(userRef);
            String lastWorkoutDate = snapshot.getString("lastWorkoutDate");
            Long currentStreakValue = snapshot.getLong("streakDays");
            Long bestStreakValue = snapshot.getLong("bestStreakDays");

            int currentStreak = currentStreakValue != null ? currentStreakValue.intValue() : 0;
            int bestStreak = bestStreakValue != null ? bestStreakValue.intValue() : 0;
            int nextStreak;

            if (today.toString().equals(lastWorkoutDate)) {
                nextStreak = Math.max(1, currentStreak);
            } else if (isYesterday(lastWorkoutDate, today)) {
                nextStreak = currentStreak + 1;
            } else {
                nextStreak = 1;
            }

            Map<String, Object> update = new HashMap<>();
            update.put("lastWorkoutDate", today.toString());
            update.put("streakDays", nextStreak);
            update.put("bestStreakDays", Math.max(bestStreak, nextStreak));
            transaction.set(userRef, update, SetOptions.merge());
            return nextStreak;
        }).addOnSuccessListener(streak -> {
            if (listener != null) listener.onComplete(true, streak);
        }).addOnFailureListener(e -> {
            if (listener != null) listener.onComplete(false, 0);
        });
    }

    private static boolean isYesterday(String dateValue, LocalDate today) {
        if (dateValue == null || dateValue.trim().isEmpty()) return false;
        try {
            LocalDate last = LocalDate.parse(dateValue);
            return last.plusDays(1).equals(today);
        } catch (Exception ignored) {
            return false;
        }
    }
}
