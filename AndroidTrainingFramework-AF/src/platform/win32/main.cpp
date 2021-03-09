// FrameworkC++.cpp : Defines the entry point for the application.
//

#include "stdafx.h"
#include "main.h"
#include "Application.h"

#define MAX_LOADSTRING 100

#define _CRTDBG_MAP_ALLOC
#include <stdlib.h>
#include <crtdbg.h>

#include "define.h"


#ifdef _DEBUG
#ifndef DBG_NEW
#define DBG_NEW new ( _NORMAL_BLOCK , __FILE__ , __LINE__ )
#define new DBG_NEW
#endif
#endif  // _DEBUG

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow)
{
	_CrtSetDbgFlag(_CRTDBG_ALLOC_MEM_DF | _CRTDBG_LEAK_CHECK_DF);
	Application *win = new Application(SCREEN_W, SCREEN_H);
	win->Init(hInstance);
	win->Run();
	delete win;

	return 0;
}