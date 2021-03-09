#include "Game.h"

#include "platform/win32/render/videoDriver.h"

void Game::Draw()
{
	// Render something here
	VideoDriver::GetInstance()->FillRect(150, 150, 50, 50);
	VideoDriver::GetInstance()->DrawCircle(100, 100, 50);
}

void Game::Exit()
{

}

