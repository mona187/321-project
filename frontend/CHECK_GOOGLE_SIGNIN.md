# 🔍 Google Sign-In Configuration Check

## Current Configuration

**OAuth Client ID:** `1066689966317-025of7hgf3qc9fg064jv4tpbg2lqpnmv.apps.googleusercontent.com`
- **Project:** `1066689966317`

**google-services.json:** From project `cpen-321-group-project-cb708` (982584664677)
- ⚠️ **MISMATCH!** These should be from the same project

**SHA-1 Fingerprint:** `5D:C8:69:CA:B5:A0:DC:C3:41:88:51:F9:B4:B5:8E:47:DF:C1:68:27`

**Package Name:** `com.example.cpen_321`

## ✅ Fix Steps

### Option 1: Add SHA-1 to Project 1066689966317 (Recommended)

Since you're using OAuth client from project `1066689966317`:

1. **Go to Google Cloud Console:**
   - https://console.cloud.google.com/apis/credentials?project=1066689966317

2. **Find your OAuth client:**
   - Look for: `1066689966317-025of7hgf3qc9fg064jv4tpbg2lqpnmv`
   - If it doesn't exist, you need to create an Android OAuth client

3. **Add Android restrictions:**
   - Package name: `com.example.cpen_321`
   - SHA-1: `5D:C8:69:CA:B5:A0:DC:C3:41:88:51:F9:B4:B5:8E:47:DF:C1:68:27`

4. **Verify the OAuth client type:**
   - It should be "Android application" type, NOT "Web application"
   - If you only have a Web client, create a new Android OAuth client

### Option 2: Get Correct google-services.json

If you want to keep using project `1066689966317`:

1. Go to Firebase Console: https://console.firebase.google.com/project/1066689966317
2. Add Android app with package: `com.example.cpen_321`
3. Add SHA-1: `5D:C8:69:CA:B5:A0:DC:C3:41:88:51:F9:B4:B5:8E:47:DF:C1:68:27`
4. Download new `google-services.json`
5. Replace your current one

## 🚨 Common Issues

1. **Wrong OAuth Client Type:**
   - You need an **Android** OAuth client, not a Web client
   - Web clients won't work with Android Sign-In

2. **SHA-1 Not Registered:**
   - Must be added to the SAME project as your OAuth client ID
   - Changes can take 5-10 minutes to propagate

3. **Project Mismatch:**
   - OAuth client ID and google-services.json must be from same project

## 🔧 Quick Verification

Run this to check your current config:
```bash
cd frontend
echo "OAuth Client ID:" && grep GOOGLE_CLIENT_ID local.properties
echo "SHA-1:" && keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android 2>/dev/null | grep SHA1
```


