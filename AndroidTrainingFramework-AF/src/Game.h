#include "define.h"
#include "SingleTon.h"

class Game : public SingleTon<Game>
{
public:
    Game() {}
	void Draw();
	void Exit();
};
