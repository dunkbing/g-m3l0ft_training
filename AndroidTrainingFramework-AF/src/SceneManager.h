#pragma once
#include "entity/Object.h"

class SceneManager
{
public:
    void Update(float frameTime);
    void Render();
    void AddObject(Object* object);
    void Init();
};
