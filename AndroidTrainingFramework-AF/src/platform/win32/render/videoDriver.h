#ifndef __VIDEO_DRIVER_H__
#define __VIDEO_DRIVER_H__

#include <objidl.h>
#include <gdiplus.h>
using namespace Gdiplus;
#pragma comment (lib,"Gdiplus.lib")

class VideoDriver
{
private:
	float color[4];
	RECT rect;
	static VideoDriver* s_Instance;


public:

	VideoDriver(void);
	~VideoDriver(void);

	void Init(HWND hWnd);
	void Render();
	void DrawRect(int x, int y, int width, int height, int weight = 1);
	void FillRect(int x, int y, int width, int height);
	void DrawCircle(int cx, int cy, int radius);
	void FillCircle(int cx, int cy, int radius);
	void DrawLine(int x1, int y1, int x2, int y2);

	void SetColor(unsigned int color);
	void CleanScreen();

	static void CreateInstance();
	static VideoDriver* GetInstance();
	static void DestroyInstance();
};

#endif