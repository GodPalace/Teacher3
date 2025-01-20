#pragma warning(disable:4996)

#include "StudentKeyboard.h"
#include "StudentMouse.h"
#include "StudentProtect.h"

#include <iostream>
#include <cstdio>
#include <string>
using namespace std;

HMODULE hHookModule = NULL;

HINSTANCE hInstance = NULL;
BOOL WINAPI DllMain(HINSTANCE hinstDLL, DWORD code, LPVOID lpvReserved) {
	if (code == DLL_PROCESS_ATTACH) {
		hInstance = hinstDLL;
	}
	else if (code == DLL_PROCESS_DETACH) {
		FreeLibrary(hHookModule);
	}

	return TRUE;
}

// KeyboardModule
JNIEXPORT void JNICALL Java_com_godpalace_student_module_KeyboardModule_DisableKeyboard(JNIEnv* env, jobject obj) {
	if (hKeyboardHook == NULL) {
		hKeyboardHook = SetWindowsHookEx(WH_KEYBOARD, KeyboardHookProc, hInstance, 0);
	}
}

JNIEXPORT void JNICALL Java_com_godpalace_student_module_KeyboardModule_EnableKeyboard(JNIEnv* env, jobject obj) {
	if (hKeyboardHook != NULL) {
		UnhookWindowsHookEx(hKeyboardHook);
		hKeyboardHook = NULL;
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
typedef BOOL(WINAPI* HOOKFUNC)(DWORD protect_pid);
struct ARGS {
	LPCSTR dllPath;
	LPCSTR funcName;
	DWORD pid;
	HMODULE hDllModule;
	HOOKFUNC hookFunc;
};

DWORD WINAPI HookThreadProc(PVOID p) {
	ARGS* args = (ARGS*) p;

	args->hDllModule = LoadLibraryA(args->dllPath);
	if (args->hDllModule == NULL) return 0;
	args->hookFunc = (HOOKFUNC) GetProcAddress(args->hDllModule, args->funcName);
	args->hookFunc(args->pid);
	FreeLibrary(args->hDllModule);
}
void End() {}

JNIEXPORT jboolean JNICALL Java_com_godpalace_student_module_ProtectModule_Protect(JNIEnv* env, jobject obj, jint pid) {
	DWORD dPid = static_cast<DWORD>(pid);
	
	HANDLE hSnap = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
	PROCESSENTRY32 pe = { sizeof(PROCESSENTRY32) };

	string dllPath = getenv("TEMP");
	dllPath.append("\\").append("Student3HookDll.dll");
	const char* cDllPath = dllPath.c_str();

	for (BOOL status = Process32First(hSnap, &pe); status; status = Process32Next(hSnap, &pe)) {
		if (pe.th32ProcessID == GetCurrentProcessId()) continue;
		HANDLE hProcess = OpenProcess(PROCESS_ALL_ACCESS, FALSE, pe.th32ProcessID);

		// Func
		DWORD funcSize = (DWORD) End - (DWORD) HookThreadProc;
		LPVOID funcPtr = VirtualAllocEx(hProcess, NULL, funcSize, MEM_COMMIT, PAGE_READWRITE);
		if (funcPtr == NULL) continue;
		if (!WriteProcessMemory(hProcess, funcPtr, HookThreadProc, funcSize, NULL)) continue;

		// Args
		DWORD argsSize = sizeof(ARGS);
		ARGS args;
		args.dllPath = cDllPath;
		args.funcName = "Hook";
		args.pid = pid;
		LPVOID argsPtr = VirtualAllocEx(hProcess, NULL, argsSize, MEM_COMMIT, PAGE_READWRITE);
		if (argsPtr == NULL) continue;
		if (!WriteProcessMemory(hProcess, argsPtr, &args, argsSize, NULL)) continue;

		// Execute
		HANDLE hRemoteThread = CreateRemoteThread(hProcess, NULL, 0,
			(LPTHREAD_START_ROUTINE) funcPtr, argsPtr, NULL, NULL);
	}
}
