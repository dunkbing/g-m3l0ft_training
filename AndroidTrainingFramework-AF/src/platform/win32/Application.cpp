#include "Application.h"
#include "game.h"
#include "render\videoDriver.h"
#include <iostream>
#include <sstream>
using namespace std;

Application* Application::s_instance = nullptr;

Application::Application(int width, int height) : m_width(width),
	m_height(height)
{
	Application::s_instance = this;
	//key
	m_keyPressed = new bool[256];
	ZeroMemory(m_keyPressed, sizeof(bool) * 256);
}

Application::~Application()
{
	delete[] m_keyPressed;
	VideoDriver::DestroyInstance();
}

LRESULT CALLBACK Application::WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam)
{
	return Application::s_instance->ProcessWindowMessage(hwnd, msg, wParam, lParam);
}

bool Application::Init(HINSTANCE hInstance)
{
	WNDCLASSEX wc;

	wc.cbSize = sizeof(WNDCLASSEX);
	wc.style = 0;
	wc.lpfnWndProc = Application::WndProc;
	wc.cbClsExtra = 0;
	wc.cbWndExtra = 0;
	wc.hInstance = hInstance;
	wc.hIcon = LoadIcon(NULL, IDI_APPLICATION);
	wc.hCursor = LoadCursor(NULL, IDC_ARROW);
	wc.hbrBackground = (HBRUSH)(COLOR_WINDOW + 1);
	wc.lpszMenuName = NULL;
	wc.lpszClassName = L"Graphics+";
	wc.hIconSm = LoadIcon(NULL, IDI_APPLICATION);

	if (!RegisterClassEx(&wc))
	{
		MessageBox(NULL, L"Cannot Register window", L"Error", MB_ICONEXCLAMATION | MB_OK);
	}

	// Compute window rectangle dimensions based on requested client area dimensions.
	RECT R = { 0, 0, SCREEN_W, SCREEN_H };
	AdjustWindowRect(&R, WS_OVERLAPPEDWINDOW, false);
	int width = R.right - R.left;
	int height = R.bottom - R.top;

	HWND hwnd;
	hwnd = CreateWindow(wc.lpszClassName, L"Test C++", WS_OVERLAPPEDWINDOW, CW_USEDEFAULT, CW_USEDEFAULT, width, height, NULL, NULL, hInstance, NULL);

	if (hwnd == NULL)
	{
		MessageBox(NULL, L"Window Creation Failed!", L"Error!", MB_ICONEXCLAMATION | MB_OK);
		return false;
	}

	ShowWindow(hwnd, SW_SHOWDEFAULT);
	UpdateWindow(hwnd);

	return true;
}

void Application::Update(float dt)
{
    Game::GetInstance()->Update(dt/100);
}

void Application::Render()
{
	Game::GetInstance()->Draw();
}

void Application::Run()
{
	MSG msg;
	DWORD start = GetTickCount();

	while (GetMessage(&msg, NULL, 0, 0) > 0)
	{
		TranslateMessage(&msg);
		DispatchMessage(&msg);

		// Clean the screen each render
		VideoDriver::GetInstance()->CleanScreen();

		DWORD end = GetTickCount();
		DWORD deltaTime = end - start;
		start = end;

		Update((float)deltaTime);
		Render();
		VideoDriver::GetInstance()->Render();

		// Limit FPS
		DWORD targetTime = 1000 / LIMIT_FPS;
		if (deltaTime < targetTime)
			Sleep(targetTime - deltaTime);
	}
}

LRESULT Application::ProcessWindowMessage(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam)
{
	switch (msg)
	{
	case WM_CREATE:
		VideoDriver::CreateInstance();
		VideoDriver::GetInstance()->Init(hwnd);
		break;
	case WM_COMMAND:
		break;
	case WM_PAINT:
		{
			break;
		}
	case WM_CLOSE:
		DestroyWindow(hwnd);
		break;
	case WM_DESTROY:
		PostQuitMessage(0);
		break;
	case WM_LBUTTONDOWN:
	case WM_MBUTTONDOWN:
	case WM_RBUTTONDOWN:
		OnMouseDown(wParam, GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam));
		break;
	case WM_LBUTTONUP:
	case WM_MBUTTONUP:
	case WM_RBUTTONUP:
		OnMouseUp(wParam, GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam));
		break;
	case WM_MOUSEMOVE:
		OnMouseMove(wParam, GET_X_LPARAM(lParam), GET_Y_LPARAM(lParam));
		break;
	case WM_KEYDOWN:
		OnKeyDown(wParam);
		break;
	case WM_KEYUP:
		OnKeyUp(wParam);
		break;
	default:
		return DefWindowProc(hwnd, msg, wParam, lParam);
	}
	return 0;
}

void Application::OnMouseDown(WPARAM btnState, int x, int y)
{
    Game::GetInstance()->GetScene()->Select(Vector2(x, y));
}

void Application::OnMouseUp(WPARAM btnState, int x, int y)
{
    Game::GetInstance()->GetScene()->UnSelect();
}

void Application::OnMouseMove(WPARAM btnState, int x, int y)
{
    if (btnState) {
        Game::GetInstance()->GetScene()->MoveObject(Vector2(x, y));
    }
}

void Application::OnKeyDown(WPARAM wParam)
{
	m_keyPressed[wParam] = true;
}

void Application::OnKeyUp(WPARAM wParam)
{
	m_keyPressed[wParam] = false;
}