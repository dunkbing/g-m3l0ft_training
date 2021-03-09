#ifndef __WINDOW_H__
#define __WINDOW_H__

#include <Windows.h>
#include <windowsx.h>
#include <string>

class Application
{
	HWND				m_hwnd;
	int					m_width;
	int					m_height;

	static Application* s_instance;

	bool			*m_keyPressed;
public:
	Application(int width, int height);
	~Application();
	bool Init(HINSTANCE hInstance);
	void Update(float dt);
	void Render();
	void Run();

	LRESULT ProcessWindowMessage(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam);

	static LRESULT CALLBACK WndProc(HWND hwnd, UINT msg, WPARAM wParam, LPARAM lParam);

	bool IsKeyPressed(WPARAM keyCode) { return m_keyPressed[keyCode]; }

	void OnMouseDown(WPARAM btnState, int x, int y);
	void OnMouseUp(WPARAM btnState, int x, int y);
	void OnMouseMove(WPARAM btnState, int x, int y);

	void OnKeyDown(WPARAM wParam);
	void OnKeyUp(WPARAM wParam);
};

#endif