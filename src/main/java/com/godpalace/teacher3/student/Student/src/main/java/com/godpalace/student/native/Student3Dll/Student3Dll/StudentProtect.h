#include <Windows.h>
#include <TlHelp32.h>
#include <jni.h>

#ifndef _Included_com_godpalace_student_module_ProtectModule
#define _Included_com_godpalace_student_module_ProtectModule
#ifdef __cplusplus
extern "C" {
#endif

	JNIEXPORT jboolean JNICALL Java_com_godpalace_student_module_ProtectModule_Protect(JNIEnv* env, jobject obj, jint pid);

#ifdef __cplusplus
}
#endif
#endif
