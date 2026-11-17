ONESERVE POINT - Android App Project
====================================

இந்த project-ல் சேர்க்கப்பட்டவை:
- நீங்கள் கொடுத்த ONESERVE POINT HTML website
- நீங்கள் கொடுத்த OSP logo-வை Android App Icon ஆக அமைத்துள்ளோம்
- WebView JavaScript + Local Storage support
- HTML file upload controls-க்கு Android File Picker support
- PDF / JPG / JPEG / PNG தேர்வு support
- Multiple file selection support
- capture input-களுக்கு Camera option
- Image upload திறக்கும் போது தேவையான Android Photos permission prompt
- Android Back button support

முக்கியமான Permission குறிப்பு:
- Android 13+ இல் Photos permission கேட்கப்படும்.
- Android 12 மற்றும் அதற்கு முந்தைய version-களில் தேவையான legacy media permission கேட்கப்படும்.
- PDF போன்ற Documents-க்கு Android System File Picker பயன்படுத்தப்படுகிறது. Android பாதுகாப்பு விதிப்படி, பயனர் தேர்வு செய்யும் file-க்கு மட்டும் access கிடைக்கும்; முழு Storage access தேவையில்லை.
- Permission மறுக்கப்பட்டாலும் File Picker திறக்கும். Permission-ஐ பின்னர் Phone Settings > Apps > ONESERVE POINT > Permissions பகுதியில் மாற்றலாம்.

APK உருவாக்க:
1. Windows PC-ல் Android Studio install செய்யவும்.
2. Android Studio SDK Manager-ல் Android SDK Platform 35 மற்றும் Android SDK Build-Tools 35.0.0 install செய்யவும்.
3. இந்த ZIP-ஐ Extract செய்யவும்.
4. Extract செய்த folder-ல் build_apk.bat-ஐ double-click செய்யவும்.
   (முதல் முறை Gradle download ஆக இணையம் தேவை.)
5. Build வெற்றியடைந்தால் APK இங்கே உருவாகும்:
   app/build/outputs/apk/debug/app-debug.apk
6. அந்த APK-ஐ Android phone-க்கு அனுப்பி install செய்யலாம். Install செய்யும்போது "Install unknown apps" அனுமதி தேவைப்பட்டால் Android கேட்கும்.

Android Studio வழியாக:
- Android Studio > Open > இந்த project folder-ஐ தேர்வு செய்யவும்.
- SDK Platform 35 install செய்யப்பட்டிருக்க வேண்டும்.
- Build > Build Bundle(s) / APK(s) > Build APK(s).

Firebase குறிப்பு:
இந்த HTML-ல் Firebase project configuration ஏற்கனவே உள்ளது. App-இல் Firebase Authentication domain check காரணமாக login issue வந்தால், Firebase Console > Authentication > Settings > Authorized domains பகுதியில்
appassets.androidplatform.net
என்பதைச் சேர்க்கவும். Firebase Firestore / Storage Rules மற்றும் Admin UID அமைப்புகள் உங்கள் Firebase project-ல் சரியாக இருக்க வேண்டும்.

Package name: com.oneserve.point
App name: ONESERVE POINT
Version: 1.0
Minimum Android: Android 6.0 (API 23)

இந்த environment-ல் Android SDK / Android Build Tools நிறுவப்படாததால் APK-ஐ இங்கே compile செய்ய முடியவில்லை. இந்த ZIP ஒரு Android Studio build-ready project; மேலே உள்ள படிகளால் APK உருவாக்கலாம்.
