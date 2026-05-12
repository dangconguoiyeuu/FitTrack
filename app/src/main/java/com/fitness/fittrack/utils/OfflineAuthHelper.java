package com.fitness.fittrack.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fitness.fittrack.models.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.UUID;

public class OfflineAuthHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "fittrack_offline_auth.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE_USERS = "offline_users";
    private static final String PREF_NAME = "fittrack_offline_session";
    private static final String KEY_UID = "uid";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";

    private static OfflineAuthHelper instance;
    private final Context appContext;

    // Ham tao singleton de moi man hinh dung chung mot bo luu tai khoan offline.
    public static synchronized OfflineAuthHelper getInstance(Context context) {
        if (instance == null) {
            instance = new OfflineAuthHelper(context.getApplicationContext());
        }
        return instance;
    }

    // Ham khoi tao helper voi application context de tranh giu tham chieu Activity.
    private OfflineAuthHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        appContext = context;
    }

    // Ham tao bang tai khoan offline lan dau, gom thong tin dang nhap, ho so va muc tieu tap.
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " ("
                + "uid TEXT PRIMARY KEY, "
                + "email TEXT UNIQUE NOT NULL, "
                + "password_hash TEXT NOT NULL, "
                + "name TEXT NOT NULL, "
                + "age INTEGER DEFAULT 0, "
                + "height REAL DEFAULT 0, "
                + "weight REAL DEFAULT 0, "
                + "gender TEXT, "
                + "fitness_level TEXT, "
                + "bmi REAL DEFAULT 0, "
                + "target_pushup INTEGER DEFAULT 0, "
                + "target_situp INTEGER DEFAULT 0, "
                + "target_sets INTEGER DEFAULT 0, "
                + "target_steps INTEGER DEFAULT 0"
                + ")");
    }

    // Ham nang cap CSDL offline neu sau nay thay doi cau truc bang.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Ham dang ky tai khoan offline moi, neu email chua ton tai thi luu vao SQLite va tao phien dang nhap.
    public boolean register(String name, String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (emailExists(normalizedEmail)) return false;

        String uid = "offline_" + UUID.randomUUID();
        ContentValues values = new ContentValues();
        values.put("uid", uid);
        values.put("email", normalizedEmail);
        values.put("password_hash", hashPassword(password));
        values.put("name", name);

        long result = getWritableDatabase().insert(TABLE_USERS, null, values);
        if (result == -1) return false;

        saveSession(uid, normalizedEmail, name);
        return true;
    }

    // Ham dang nhap offline bang cach so sanh email va mat khau da bam trong SQLite.
    public boolean login(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        String passwordHash = hashPassword(password);

        try (Cursor cursor = getReadableDatabase().query(
                TABLE_USERS,
                new String[]{"uid", "email", "name"},
                "email = ? AND password_hash = ?",
                new String[]{normalizedEmail, passwordHash},
                null,
                null,
                null
        )) {
            if (!cursor.moveToFirst()) return false;

            String uid = cursor.getString(cursor.getColumnIndexOrThrow("uid"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            saveSession(uid, normalizedEmail, name);
            return true;
        }
    }

    // Ham kiem tra hien tai co phien dang nhap offline tren may hay khong.
    public boolean isLoggedIn() {
        return getCurrentUid() != null;
    }

    // Ham lay uid offline dang dang nhap tu SharedPreferences.
    public String getCurrentUid() {
        return prefs().getString(KEY_UID, null);
    }

    // Ham lay email offline dang dang nhap tu SharedPreferences.
    public String getCurrentEmail() {
        return prefs().getString(KEY_EMAIL, null);
    }

    // Ham lay ten nguoi dung offline dang dang nhap tu SharedPreferences.
    public String getCurrentName() {
        return prefs().getString(KEY_NAME, null);
    }

    // Ham dang xuat offline bang cach xoa thong tin phien dang nhap cuc bo.
    public void signOut() {
        prefs().edit().clear().apply();
    }

    // Ham lay ho so user offline hien tai de cac man hinh Home/Profile hien thi khi khong dung Firebase.
    public User getCurrentUserProfile() {
        String uid = getCurrentUid();
        if (uid == null) return null;
        return getUserByUid(uid);
    }

    // Ham luu ho so user offline sau khi nguoi dung cap nhat ten, BMI, chieu cao, can nang.
    public boolean saveProfile(User user) {
        if (user == null || user.getUid() == null) return false;

        ContentValues values = new ContentValues();
        values.put("name", user.getName());
        values.put("email", normalizeEmail(user.getEmail()));
        values.put("age", user.getAge());
        values.put("height", user.getHeight());
        values.put("weight", user.getWeight());
        values.put("gender", user.getGender());
        values.put("fitness_level", user.getFitnessLevel());
        values.put("bmi", user.getBmi());

        int updated = getWritableDatabase().update(
                TABLE_USERS,
                values,
                "uid = ?",
                new String[]{user.getUid()}
        );
        if (updated > 0) {
            saveSession(user.getUid(), normalizeEmail(user.getEmail()), user.getName());
        }
        return updated > 0;
    }

    // Ham luu muc tieu tap duoc tinh tu BMI cho tai khoan offline.
    public boolean saveTargets(int targetPushup, int targetSitup, int targetSets, int targetSteps) {
        String uid = getCurrentUid();
        if (uid == null) return false;

        ContentValues values = new ContentValues();
        values.put("target_pushup", targetPushup);
        values.put("target_situp", targetSitup);
        values.put("target_sets", targetSets);
        values.put("target_steps", targetSteps);

        int updated = getWritableDatabase().update(
                TABLE_USERS,
                values,
                "uid = ?",
                new String[]{uid}
        );
        return updated > 0;
    }

    // Ham lay muc tieu tap offline; neu chua luu thi tra ve gia tri mac dinh ma man hinh truyen vao.
    public int[] getTargets(int defaultPushup, int defaultSitup, int defaultSets, int defaultSteps) {
        String uid = getCurrentUid();
        if (uid == null) return new int[]{defaultPushup, defaultSitup, defaultSets, defaultSteps};

        try (Cursor cursor = getReadableDatabase().query(
                TABLE_USERS,
                new String[]{"target_pushup", "target_situp", "target_sets", "target_steps"},
                "uid = ?",
                new String[]{uid},
                null,
                null,
                null
        )) {
            if (!cursor.moveToFirst()) return new int[]{defaultPushup, defaultSitup, defaultSets, defaultSteps};

            int pushup = cursor.getInt(cursor.getColumnIndexOrThrow("target_pushup"));
            int situp = cursor.getInt(cursor.getColumnIndexOrThrow("target_situp"));
            int sets = cursor.getInt(cursor.getColumnIndexOrThrow("target_sets"));
            int steps = cursor.getInt(cursor.getColumnIndexOrThrow("target_steps"));

            return new int[]{
                    pushup > 0 ? pushup : defaultPushup,
                    situp > 0 ? situp : defaultSitup,
                    sets > 0 ? sets : defaultSets,
                    steps > 0 ? steps : defaultSteps
            };
        }
    }

    // Ham kiem tra email da co trong SQLite chua de tranh dang ky trung.
    private boolean emailExists(String email) {
        try (Cursor cursor = getReadableDatabase().query(
                TABLE_USERS,
                new String[]{"uid"},
                "email = ?",
                new String[]{email},
                null,
                null,
                null
        )) {
            return cursor.moveToFirst();
        }
    }

    // Ham doc day du thong tin user offline theo uid va chuyen thanh model User san co.
    private User getUserByUid(String uid) {
        try (Cursor cursor = getReadableDatabase().query(
                TABLE_USERS,
                null,
                "uid = ?",
                new String[]{uid},
                null,
                null,
                null
        )) {
            if (!cursor.moveToFirst()) return null;

            User user = new User();
            user.setUid(cursor.getString(cursor.getColumnIndexOrThrow("uid")));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            user.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            user.setAge(cursor.getInt(cursor.getColumnIndexOrThrow("age")));
            user.setHeight(cursor.getDouble(cursor.getColumnIndexOrThrow("height")));
            user.setWeight(cursor.getDouble(cursor.getColumnIndexOrThrow("weight")));
            user.setGender(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
            user.setFitnessLevel(cursor.getString(cursor.getColumnIndexOrThrow("fitness_level")));
            user.setBmi(cursor.getDouble(cursor.getColumnIndexOrThrow("bmi")));
            user.setTargetPushup(cursor.getInt(cursor.getColumnIndexOrThrow("target_pushup")));
            user.setTargetSitup(cursor.getInt(cursor.getColumnIndexOrThrow("target_situp")));
            user.setTargetSteps(cursor.getInt(cursor.getColumnIndexOrThrow("target_steps")));
            return user;
        }
    }

    // Ham ghi phien dang nhap offline vao SharedPreferences de Splash/Home nhan ra user hien tai.
    private void saveSession(String uid, String email, String name) {
        prefs().edit()
                .putString(KEY_UID, uid)
                .putString(KEY_EMAIL, email)
                .putString(KEY_NAME, name)
                .apply();
    }

    // Ham chuan hoa email ve chu thuong va bo khoang trang truoc khi so sanh/luu tru.
    private String normalizeEmail(String email) {
        if (email == null) return "";
        return email.trim().toLowerCase(Locale.US);
    }

    // Ham bam mat khau bang SHA-256 de khong luu mat khau ro trong SQLite.
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format(Locale.US, "%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 khong kha dung", e);
        }
    }

    // Ham lay SharedPreferences dang luu phien offline hien tai.
    private SharedPreferences prefs() {
        return appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
}
