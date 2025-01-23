#include <Windows.h>
#include <jni.h>

#ifndef _Included_com_godpalace_student_module_ProtectModule
#define _Included_com_godpalace_student_module_ProtectModule
#ifdef __cplusplus
extern "C" {
#endif

	JNIEXPORT jint JNICALL Java_com_godpalace_student_module_ProtectModule_Protect(JNIEnv* env, jobject obj, jint pid);
	JNIEXPORT jint JNICALL Java_com_godpalace_student_module_ProtectModule_Unprotect(JNIEnv* env, jobject obj);

#ifdef __cplusplus
}
#endif
#endif
