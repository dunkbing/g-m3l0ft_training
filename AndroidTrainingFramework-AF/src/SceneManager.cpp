#include <fstream>
#include <string>

#include "SceneManager.h"
#include "define.h"
#include "CollisionManager.h"
#include "entity/Circle.h"
#include "entity/Rectangle.h"

SceneManager::SceneManager() {
    Init("SceneManager.txt");
}

SceneManager::~SceneManager() {
    for (auto* object : m_objects) {
        if (object) {
            delete object;
            object = nullptr;
        }
    }
}

// update objects' information between each frame
void SceneManager::Update(float deltaTime) {
    for (auto* object : m_objects) {
        if (object != selectedObject) {
            object->Update(deltaTime);
        }
        for (auto* obj : m_objects) {
            if (obj != object) {
                CollisionManager::Intersect(obj, object);
            }
        }
    }
}

// render all object to screen
void SceneManager::Render() {
    for (auto* object : m_objects) {
        object->Render();
    }
}

// add a new object to screen
void SceneManager::AddObject(Entity::Object* object) {
    m_objects.push_back(object);
}


// load object's info from file
void SceneManager::Init(const std::string& filePath) {
    const std::string ID = "ID";
    const std::string POSITION = "POSITION";
    const std::string VELOCITY = "VELOCITY";
    const std::string TYPE = "TYPE";
    const std::string RECT = "RECT";
    const std::string CIRCLE = "CIRCLE";
    std::ifstream ifs(filePath);

    std::string line;
    while(std::getline(ifs, line)) {
        if (line.empty()) {
            continue;
        }
        if (line.find(ID) != std::string::npos) {
            std::string type;
            std::vector<int> position;
            int velocity = 0;
            while (std::getline(ifs, line)) {
                if (line.empty()) break;
                if (line.find(TYPE) != std::string::npos) {
                    type = line.substr(TYPE.length());
                } else if (line.find(POSITION) != std::string::npos) {
                    position = IntsFromStr(line);
                } else if (line.find(VELOCITY) != std::string::npos) {
                    velocity = IntsFromStr(line)[0];
                }
            }
            if (type.find(RECT) != std::string::npos) {
                auto* rectangle = new Entity::Rectangle(Vector2(position[0], position[1]), position[2], position[3]);
                rectangle->SetVelocityY(velocity);
                m_objects.push_back(rectangle);
            } else if (type.find(CIRCLE) != std::string::npos) {
                auto* circle = new Entity::Circle(Vector2(position[0], position[1]), position[2]);
                circle->SetVelocityY(velocity);
                m_objects.push_back(circle);
            }
        }
    }
}

// move object to a new position
void SceneManager::MoveObject(Vector2& p_coord) {
    if (selectedObject != nullptr) {
        selectedObject->SetPosition(p_coord+offset);
        if (CollisionManager::CollideWithEdge(selectedObject)) {
            printf("edge collision detected\n");
        }
        for (auto* object : m_objects) {
            if (object != selectedObject && CollisionManager::Intersect(selectedObject, object)) {
                printf("object collision detected\n");
            }
        }
    }
}

// select an object to move around
void SceneManager::Select(Vector2& p_coord) {
    for (auto* object : m_objects) {
        if (object->Contains(p_coord)) {
            selectedObject = object;
            selectedObject->SetVelocityX(0);
            const auto objPos = selectedObject->GetPosition();
            offset = objPos - p_coord;
            break;
        }
    }
}

// unselect the object
void SceneManager::UnSelect() {
    if (selectedObject != nullptr) {
        selectedObject = nullptr;
    }
}
