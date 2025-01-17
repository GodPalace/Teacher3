#include <Windows.h>
#include <jni.h>

HHOOK hMouseHook = NULL;

LRESULT CALLBACK MouseHookProc(int nCode, WPARAM wParam, LPARAM lParam) {
	return 1;
}

#ifndef _Included_com_godpalace_student_module_MouseModule
#define _Included_com_godpalace_student_module_MouseModule
#ifdef __cplusplus
extern "C" {
#endif

	JNIEXPORT void JNICALL Java_com_godpalace_student_module_MouseModule_DisableMouse(JNIEnv* env, jobject obj);
	JNIEXPORT void JNICALL Java_com_godpalace_student_module_MouseModule_EnableMouse(JNIEnv* env, jobject obj);

#ifdef __cplusplus
}
#endif
#endif