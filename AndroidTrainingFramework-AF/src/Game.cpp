#include "game.h"

#include "entity/Circle.h"
#include "entity/Rectangle.h"
#include "render/videoDriver.h"

void Game::Draw()
{
	// Render something here
	//VideoDriver::GetInstance()->FillRect(150, 150, 50, 50);
	//VideoDriver::GetInstance()->DrawCircle(100, 100, 50);
    // m_scene.AddObject(new Entity::Rectangle(Vector2(150, 150), 50, 50));
    // m_scene.AddObject(new Entity::Circle(Vector2(100, 100), 50));
    m_scene->Render();
}

void Game::Exit()
{

}

void Game::MoveObject(int x, int y) {
    m_scene->MoveObject(Vector2(x, y));
}

SceneManager* Game::GetScene() {
    return m_scene;
}

