#pragma once
#include <vector>
#include "SceneManager.h"
#include "math/Vector2.h"

namespace Entity {
    class Object;
}

#pragma comment (lib,"Gdiplus.lib")

class SceneManager
{
public:
    SceneManager();
    ~SceneManager();
    void Update(float deltaTime);
    void Render();
    void AddObject(Entity::Object* object);
    void Init(const std::string& filePath);
    void MoveObject(Vector2& p_coord);
    void Select(Vector2& p_coord);
    void UnSelect();
private:
    std::vector<Entity::Object*> m_objects;
    Entity::Object* selectedObject = NULL;
    Vector2 offset;
};
