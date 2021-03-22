#include <fstream>
#include <string>

#include "SceneManager.h"
#include "define.h"
#include "CollisionManager.h"
#include "entity/Circle.h"
#include "entity/Rectangle.h"

SceneManager::SceneManager() {
    Init("SceneManager.txt");
    m_objects.push_back(new Entity::Rectangle(Vector2(100, 100), 100, 100));
}

SceneManager::~SceneManager() {
    for (int i = 0; i < m_objects.size(); i++) {
        if (m_objects[i]) {
            delete m_objects[i];
            m_objects[i] = NULL;
        }
    }
}

// update objects' information between each frame
void SceneManager::Update(float deltaTime) {
    for (int i = 0; i < m_objects.size(); i++) {
        if (m_objects[i] != selectedObject) {
            m_objects[i]->Update(deltaTime);
        }
        for (int j = 0; j < m_objects.size(); j++) {
            if (m_objects[j] != m_objects[i]) {
                CollisionManager::Intersect(m_objects[j], m_objects[i]);
            }
        }
    }
}

// render all object to screen
void SceneManager::Render() {
    for (int i = 0; i < m_objects.size(); i++) {
        m_objects[i]->Render();
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
    std::ofstream ofs("filePath.txt");
    ofs << "test";
    ofs.close();

    std::ifstream ifs(filePath.c_str());

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
                Entity::Rectangle* rectangle = new Entity::Rectangle(Vector2(position[0], position[1]), position[2], position[3]);
                rectangle->SetVelocityY(velocity);
                m_objects.push_back(rectangle);
            } else if (type.find(CIRCLE) != std::string::npos) {
                Entity::Circle* circle = new Entity::Circle(Vector2(position[0], position[1]), position[2]);
                circle->SetVelocityY(velocity);
                m_objects.push_back(circle);
            }
        }
    }
}

// move object to a new position
void SceneManager::MoveObject(Vector2& p_coord) {
    if (selectedObject != NULL) {
        selectedObject->SetPosition(p_coord+offset);
        if (CollisionManager::CollideWithEdge(selectedObject)) {
            printf("edge collision detected\n");
        }
        for (int i = 0; i < m_objects.size(); i++) {
            if (m_objects[i] != selectedObject && CollisionManager::Intersect(selectedObject, m_objects[i])) {
                printf("object collision detected\n");
            }
        }
    }
}

// select an object to move around
void SceneManager::Select(Vector2& p_coord) {
    for (int i = 0; i < m_objects.size(); i++) {
        if (m_objects[i]->Contains(p_coord)) {
            selectedObject = m_objects[i];
            selectedObject->SetVelocityX(0);
            const Vector2 objPos = selectedObject->GetPosition();
            offset = objPos - p_coord;
            break;
        }
    }
}

// unselect the object
void SceneManager::UnSelect() {
    if (selectedObject != NULL) {
        selectedObject = NULL;
    }
}
