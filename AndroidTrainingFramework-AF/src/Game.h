#include "define.h"
#include "SceneManager.h"
#include "SingleTon.h"

class Game : public SingleTon<Game>
{
public:
	Game() {
        m_scene = new SceneManager;
	}
    ~Game() {
        delete m_scene;
        m_scene = nullptr;
	}
	void Draw();
	void Exit();
    void MoveObject(int x, int y);
    SceneManager* GetScene();
private:
    SceneManager* m_scene;
};
