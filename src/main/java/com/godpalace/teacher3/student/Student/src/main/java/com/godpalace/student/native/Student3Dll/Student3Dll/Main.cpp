#pragma warning(disable:4996)

#include "StudentKeyboard.h"
#include "StudentMouse.h"
#include "StudentProtect.h"

#include <cstdio>
#include <iostream>
#include <string>
#include <detours.h>
using namespace std;

#pragma comment(lib, "detours.lib")

HMODULE hHookModule = NULL;

HINSTANCE hInstance = NULL;
BOOL WINAPI DllMain(HINSTANCE hinstDLL, DWORD code, LPVOID lpvReserved) {
	if (code == DLL_PROCESS_ATTACH) {
		hInstance = hinstDLL;
	}
	else if (code == DLL_PROCESS_DETACH) {
		FreeLibrary(hHookModule);
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
JNIEXPORT jboolean JNICALL Java_com_godpalace_student_module_ProtectModule_Protect(JNIEnv* env, jobject obj, jint pid) {
	return false;
}

