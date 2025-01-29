#include <Windows.h>
#include <jni.h>

#ifndef _Included_com_godpalace_student_module_FileManagerModule
#define _Included_com_godpalace_student_module_FileManagerModule
#ifdef __cplusplus
extern "C" {
#endif

	JNIEXPORT jlong JNICALL Java_com_godpalace_student_module_FileManagerModule_LockFile(JNIEnv* env, jobject obj, jstring path);
	JNIEXPORT jboolean JNICALL Java_com_godpalace_student_module_FileManagerModule_UnlockFile(JNIEnv* env, jobject obj, jlong ptr);

#ifdef __cplusplus
}
#endif
#endif
