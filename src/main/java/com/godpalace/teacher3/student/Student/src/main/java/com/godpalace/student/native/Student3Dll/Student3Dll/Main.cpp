#pragma warning(disable:4996)

#include <iostream>
#include "StudentKeyboard.h"
#include "StudentMouse.h"
#include "StudentProtect.h"
using namespace std;

typedef void(*HookFunc)(DWORD no_pid);
HMODULE hHookModule = NULL;
HookFunc Hook = NULL;

HINSTANCE hInstance = NULL;
BOOL WINAPI DllMain(HINSTANCE hinstDLL, DWORD code, LPVOID lpvReserved) {
	if (code == DLL_PROCESS_ATTACH) {
		hInstance = hinstDLL;

		// Load dll
		string temp = getenv("TEMP");
		temp.append("\\").append("StudentHookDll.dll");

		hHookModule = LoadLibraryA(temp.c_str());
		if (hHookModule != NULL) {
			Hook = (HookFunc) GetProcAddress(hHookModule, "Hook");
		}
	}
	else if (code == DLL_PROCESS_DETACH) {
		// Unload dll
		if (hHookModule != NULL) {
			FreeLibrary(hHookModule);

			Hook = NULL;
			hHookModule = NULL;
		}
	}

	return TRUE;
}

// KeyboardModule
JNIEXPORT void JNICALL Java_com_godpalace_student_module_KeyboardModule_DisableKeyboard(JNIEnv* env, jobject obj) {
	if (hKeyboardHook == NULL) {
		hKeyboardHook    = SetWindowsHookEx(WH_KEYBOARD, KeyboardHookProc, hInstance, 0);
	}
}

JNIEXPORT void JNICALL Java_com_godpalace_student_module_KeyboardModule_EnableKeyboard(JNIEnv* env, jobject obj) {
	if (hKeyboardHook != NULL) {
		UnhookWindowsHookEx(hKeyboardHook);
		hKeyboardHook    = NULL;
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
	if (Hook != NULL) {
		Hook(static_cast<DWORD>(pid));
		return true;
	}
	else {
		return false;
	}
}
