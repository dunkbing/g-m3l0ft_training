#pragma once
#include "math/Vector2.h"

class Object
{
public:
    void Render();
    void Update(float frameTime);
private:
    Vector2 position;
    float mass;
    float velocity;
};

