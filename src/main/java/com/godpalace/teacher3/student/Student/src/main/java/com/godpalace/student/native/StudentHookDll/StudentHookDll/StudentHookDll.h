#include <stdio.h>
#include <stdlib.h>
#include <Windows.h>
#include <detours.h>

#ifdef TESTDLLEXPORT
#define DLLAPI extern "C" __declspec(dllexport)
#else
#define DLLAPI extern "C" __declspec(dllimport)
#endif

static HANDLE(WINAPI* OldOpenProcess)(DWORD, BOOL, DWORD) = OpenProcess;
HANDLE WINAPI NewOpenProcess(DWORD access, BOOL is, DWORD pid) {
	HANDLE hMutex = NULL;
	HANDLE hEvent = NULL;
	HANDLE hShare = NULL;
	LPVOID memory = NULL;

	hShare = OpenFileMapping(FILE_MAP_ALL_ACCESS, FALSE, "StudentHookShareMemory");
	if (hShare == NULL) return NULL;

	memory = MapViewOfFile(hShare, FILE_MAP_ALL_ACCESS, 0, 0, 4);
	if (memory == NULL) return NULL;

	hMutex = OpenMutex(MUTEX_ALL_ACCESS, FALSE, "StudentHookMutex");
	if (hMutex == NULL) return NULL;

	hEvent = OpenEvent(EVENT_ALL_ACCESS, FALSE, "StudentHookEvent");
	if (hEvent == NULL) return NULL;

	DWORD no_pid = 0;
	WaitForSingleObject(hEvent, INFINITE);
	WaitForSingleObject(hMutex, INFINITE);
	memcpy(&no_pid, memory, 4);
	ReleaseMutex(hMutex);

	CloseHandle(hEvent);
	CloseHandle(hMutex);
	UnmapViewOfFile(memory);
	CloseHandle(hShare);

	if (pid != no_pid) {
		return OldOpenProcess(access, is, pid);
	}
	else {
		SetLastError(5);
		return NULL;
	}
}

DLLAPI void Hook(DWORD no_pid);
