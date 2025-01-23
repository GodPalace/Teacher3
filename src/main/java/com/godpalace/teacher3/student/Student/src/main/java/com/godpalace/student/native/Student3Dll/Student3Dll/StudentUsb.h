#include <Windows.h>
#include <jni.h>

#ifndef _Included_com_godpalace_student_module_UsbModule
#define _Included_com_godpalace_student_module_UsbModule
#ifdef __cplusplus
extern "C" {
#endif

	JNIEXPORT jint JNICALL Java_com_godpalace_student_module_UsbModule_Disable(JNIEnv* env, jobject obj, jint pid);
	JNIEXPORT jint JNICALL Java_com_godpalace_student_module_UsbModule_Enable(JNIEnv* env, jobject obj);

#ifdef __cplusplus
}
#endif
#endif
