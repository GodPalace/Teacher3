#include "StudentKeyboard.h"
using namespace std;

HHOOK hHook = NULL;
HINSTANCE hInstance;

BOOL WINAPI DllMain(HINSTANCE hinstDLL, DWORD code, LPVOID lpvReserved) {
	if (code == DLL_PROCESS_ATTACH) {
		hInstance = hinstDLL;
	}

	return TRUE;
}

JNIEXPORT void JNICALL Java_com_godpalace_student_module_KeyboardModule_DisableKeyboard(JNIEnv* env, jobject obj) {
	if (hHook == NULL) {
		hHook = SetWindowsHookEx(WH_KEYBOARD, HookProc, hInstance, 0);
	}
}

JNIEXPORT void JNICALL Java_com_godpalace_student_module_KeyboardModule_EnableKeyboard(JNIEnv* env, jobject obj) {
	if (hHook != NULL) {
		UnhookWindowsHookEx(hHook);
		hHook = NULL;
	}
}
