#include "StudentKeyboard.h"
#include "StudentMouse.h"
using namespace std;

HINSTANCE hInstance;

BOOL WINAPI DllMain(HINSTANCE hinstDLL, DWORD code, LPVOID lpvReserved) {
	if (code == DLL_PROCESS_ATTACH) {
		hInstance = hinstDLL;
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
