#include <Windows.h>
#include <jni.h>

LRESULT CALLBACK HookProc(int nCode, WPARAM wParam, LPARAM lParam) {
	return 1;
}

#ifndef _Included_com_godpalace_student_module_KeyboardModule
#define _Included_com_godpalace_student_module_KeyboardModule
#ifdef __cplusplus
extern "C" {
#endif

	JNIEXPORT void JNICALL Java_com_godpalace_student_module_KeyboardModule_DisableKeyboard(JNIEnv* env, jobject obj);
	JNIEXPORT void JNICALL Java_com_godpalace_student_module_KeyboardModule_EnableKeyboard(JNIEnv* env, jobject obj);

#ifdef __cplusplus
}
#endif
#endif