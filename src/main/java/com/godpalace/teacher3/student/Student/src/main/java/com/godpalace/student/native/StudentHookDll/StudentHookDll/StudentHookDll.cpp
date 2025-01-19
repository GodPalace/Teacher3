#include "StudentHookDll.h"
using namespace std;

#pragma comment(lib, "kernel32.lib")
#pragma comment(lib, "detours.lib")

HANDLE hMutex = NULL;
HANDLE hEvent = NULL;
HANDLE hShare = NULL;
LPVOID memory = NULL;

HINSTANCE hInstance;
BOOL WINAPI DllMain(HINSTANCE hinstDLL, DWORD code, LPVOID lpvReserved) {
	if (code == DLL_PROCESS_ATTACH) {
		hInstance = hinstDLL;

		hShare = CreateFileMapping(INVALID_HANDLE_VALUE, NULL, PAGE_READWRITE, 0, 4, "StudentHookShareMemory");
		if (hShare == NULL) return FALSE;

		memory = MapViewOfFile(hShare, FILE_MAP_ALL_ACCESS, 0, 0, 4);
		if (memory == NULL) return FALSE;

		hMutex = CreateMutex(NULL, FALSE, "StudentHookMutex");
		hEvent = CreateEvent(NULL, FALSE, FALSE, "StudentHookEvent");
	}
	else if (code == DLL_PROCESS_DETACH) {
		if (DetourTransactionBegin() == NO_ERROR) {
			DetourUpdateThread(GetCurrentThread());
			DetourDetach(&(PVOID&)OldOpenProcess, NewOpenProcess);
			DetourTransactionCommit();
		}

		CloseHandle(hMutex);
		CloseHandle(hEvent);

		UnmapViewOfFile(memory);
		CloseHandle(hShare);
	}

	return TRUE;
}

void Hook(DWORD no_pid) {
	WaitForSingleObject(hMutex, INFINITE);
	memcpy(memory, &no_pid, 4);

	if (DetourTransactionBegin() == NO_ERROR) {
		DetourUpdateThread(GetCurrentThread());
		DetourAttach(&(PVOID&)OldOpenProcess, NewOpenProcess);
		DetourTransactionCommit();
	}
	else {
		throw "ERROR";
	}

	ReleaseMutex(hMutex);
	SetEvent(hEvent);
}
