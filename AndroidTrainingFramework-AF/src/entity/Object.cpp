#include "Object.h"

Entity::Object::Object() {
    m_mass = 3;
}

void Entity::Object::Update(float frameTime) {
    if (m_velocityX > 0) {
        m_position.x += (m_velocityX -= 1) * frameTime;
    }
    else if (m_velocityX < 0) {
        m_position.x += (m_velocityX += 1) * frameTime;
    }
}

int Entity::Object::GetVelocityX() const {
    return m_velocityX;
}

int Entity::Object::GetVelocityY() const {
    return m_velocityY;
}

void Entity::Object::SetVelocityX(int p_velocity) {
    m_velocityX = p_velocity;
}

void Entity::Object::SetVelocityY(int p_velocity) {
    m_velocityY = p_velocity;
}

int Entity::Object::GetMass() const {
    return m_mass;
}

void Entity::Object::SetMass(int p_mass) {
    m_mass = p_mass;
}

Vector2 Entity::Object::GetPosition() const {
    return m_position;
}

void Entity::Object::SetPosition(const Vector2& p_position) {
    m_position = p_position;
}

