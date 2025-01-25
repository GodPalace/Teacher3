#pragma warning(disable:4996)

#include "StudentKeyboard.h"
#include "StudentMouse.h"
#include "StudentProtect.h"
#include "StudentUsb.h"
#include "StudentFileManager.h"

#include <cstdio>
#include <iostream>
#include <string>
using namespace std;

HMODULE hProtectModule  = NULL;
HMODULE hUsbModule      = NULL;

HINSTANCE hInstance = NULL;
BOOL WINAPI DllMain(HINSTANCE hinstDLL, DWORD code, LPVOID lpvReserved) {
	if (code == DLL_PROCESS_ATTACH) {
		hInstance = hinstDLL;

		hProtectModule  = LoadLibraryA(string(getenv("TEMP")).append("\\Student3HookDll.dll").c_str());
		hUsbModule      = LoadLibraryA(string(getenv("TEMP")).append("\\Student3UsbDll.dll").c_str());
	}
	else if (code == DLL_PROCESS_DETACH) {
		if (hProtectModule != NULL) FreeLibrary(hProtectModule);
	}

	return TRUE;
}

wchar_t* charToWchar(const char* c) {
	size_t len = strlen(c) + 1;
	wchar_t* wc = new wchar_t[len];
	mbstowcs(wc, c, len);
	return wc;
}

char* jstringToChar(JNIEnv* env, jstring jstr) {
	char* rtn = NULL;
	jclass clsstring = env->FindClass("java/lang/String");
	jstring strencode = env->NewStringUTF("GB2312");
	jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
	jbyteArray barr = (jbyteArray)env->CallObjectMethod(jstr, mid, strencode);
	jsize alen = env->GetArrayLength(barr);
	jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
	if (alen > 0) {
		rtn = (char*)malloc(alen + 1);
		memcpy(rtn, ba, alen);
		rtn[alen] = 0;
	}
	env->ReleaseByteArrayElements(barr, ba, 0);
	return rtn;
}

// KeyboardModule
JNIEXPORT void JNICALL Java_com_godpalace_student_module_KeyboardModule_DisableKeyboard(JNIEnv* env, jobject obj) {
	if (hKeyboardHook == NULL) {
		hKeyboardHook = SetWindowsHookEx(WH_KEYBOARD, KeyboardHookProc, hInstance, 0);
	}
}

JNIEXPORT void JNICALL Java_com_godpalace_student_module_KeyboardModule_EnableKeyboard(JNIEnv* env, jobject obj) {
	if (hKeyboardHook != NULL) {
		UnhookWindowsHookEx(hKeyboardHook);
		hKeyboardHook = NULL;
	}
}

// MouseModule
JNIEXPORT void JNICALL Java_com_godpalace_student_module_MouseModule_DisableMouse(JNIEnv* env, jobject obj) {
	if (hMouseHook == NULL) {
		hMouseHook = SetWindowsHookEx(WH_MOUSE, MouseHookProc, hInstance, 0);
	}
}

JNIEXPORT void JNICALL Java_com_godpalace_student_module_MouseModule_EnableMouse(JNIEnv* env, jobject obj) {
	if (hMouseHook != NULL) {
		UnhookWindowsHookEx(hMouseHook);
		hMouseHook = NULL;
	}
}

// ProtectModule
typedef void(*HOOK_FUNC)(DWORD);
JNIEXPORT jint JNICALL Java_com_godpalace_student_module_ProtectModule_Protect(JNIEnv* env, jobject obj, jint pid) {
	if (hProtectModule == NULL) return GetLastError();

	HOOK_FUNC func = (HOOK_FUNC) GetProcAddress(hProtectModule, "Hook");
	if (func == NULL) return GetLastError();
	func(static_cast<DWORD>(pid));

	return 0;
}

JNIEXPORT jint JNICALL Java_com_godpalace_student_module_ProtectModule_Unprotect(JNIEnv* env, jobject obj) {
	if (hProtectModule == NULL) return GetLastError();

	FARPROC func = GetProcAddress(hProtectModule, "Unhook");
	if (func == NULL) return GetLastError();
	func();

	return 0;
}

// UsbModule
JNIEXPORT jint JNICALL Java_com_godpalace_student_module_UsbModule_Disable(JNIEnv* env, jobject obj, jint pid) {
	if (hUsbModule == NULL) return GetLastError();

	FARPROC func = GetProcAddress(hUsbModule, "Hook");
	if (func == NULL) return GetLastError();
	func();

	return 0;
}

JNIEXPORT jint JNICALL Java_com_godpalace_student_module_UsbModule_Enable(JNIEnv* env, jobject obj) {
	if (hUsbModule == NULL) return GetLastError();

	FARPROC func = GetProcAddress(hUsbModule, "Unhook");
	if (func == NULL) return GetLastError();
	func();

	return 0;
}

// FileManagerModule
JNIEXPORT jboolean JNICALL Java_com_godpalace_student_module_FileManagerModule_LockFile(
		JNIEnv* env, jobject obj, jstring path) {

	HANDLE hFile = CreateFileA(jstringToChar(env, path), GENERIC_READ | GENERIC_WRITE, 0, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);

	DWORD sizeH = 0;
	DWORD sizeL = GetFileSize(hFile, &sizeH);

	BOOL status = LockFile(hFile, 0, 0, sizeL, sizeH);
	CloseHandle(hFile);
	return status;
}

JNIEXPORT jboolean JNICALL Java_com_godpalace_student_module_FileManagerModule_UnlockFile(
		JNIEnv* env, jobject obj, jstring path) {

	HANDLE hFile = CreateFileA(jstringToChar(env, path), GENERIC_READ | GENERIC_WRITE, 0, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);

	DWORD sizeH = 0;
	DWORD sizeL = GetFileSize(hFile, &sizeH);

	BOOL status = UnlockFile(hFile, 0, 0, sizeL, sizeH);
	CloseHandle(hFile);
	return status;
}
