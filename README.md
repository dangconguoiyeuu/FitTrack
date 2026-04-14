# FitTrack v3 - Firebase + Wireframe Final

## CAU HINH FIREBASE (BAT BUOC)
1. Truy cap https://console.firebase.google.com
2. Tao project "FitTrack"
3. Them Android App: package = com.fitness.fittrack
4. Download google-services.json -> copy vao thu muc app/
5. Bat Email/Password Authentication
6. Tao Cloud Firestore (test mode)

## Security Rules (Firestore > Rules):
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /workouts/{workoutId} {
      allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
      allow read: if request.auth != null && resource.data.userId == request.auth.uid;
    }
  }
}

## TINH NANG MOI (v3):
- Firebase Auth (dang ky/dang nhap)
- Cloud Firestore (luu profile + lich su)
- Man hinh Thiet lap phien tap (chon bai tap, huong dan, muc tieu +/-)
- Man hinh Dang tap: vong tron tien do, thoi gian, calo
- Phat hien dung 20s -> hien dialog Tiep tuc / Ket thuc
- Am thanh beep moi nhip hop le
- Rung khi dung qua lau
- Uoc tinh calo (MET * weight * hours)
- Sets ho tro
