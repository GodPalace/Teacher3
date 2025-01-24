#include <Windows.h>
#include <TlHelp32.h>
#include <jni.h>

#ifndef _Included_com_godpalace_student_module_KillSoftManagerModule
#define _Included_com_godpalace_student_module_KillSoftManagerModule
#ifdef __cplusplus
extern "C" {
#endif

	JNIEXPORT void JNICALL Java_com_godpalace_student_module_KillSoftModule_Kill(JNIEnv* env, jobject obj);
	JNIEXPORT void JNICALL Java_com_godpalace_student_module_KillSoftModule_Unkill(JNIEnv* env, jobject obj);

#ifdef __cplusplus
}
#endif
#endif
