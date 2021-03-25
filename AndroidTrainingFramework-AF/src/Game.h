#pragma once
#include "define.h"
#include "SceneManager.h"
#include "SingleTon.h"
#include "TouchData.h"

class Game : public SingleTon<Game>
{
public:
	Game() {
        m_scene = new SceneManager;
	}
    ~Game() {
        delete m_scene;
        m_scene = NULL;
	}
	void Draw();
    void Update(float dt) const;
	void Exit();
    void MoveObject(int x, int y) const;
    SceneManager* GetScene();
private:
    SceneManager* m_scene;
};
