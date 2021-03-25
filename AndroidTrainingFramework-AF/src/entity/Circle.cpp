#include "Circle.h"

#include "render/videoDriver.h"

Entity::Circle::Circle() : Object() {
    m_radius = 0;
}

Entity::Circle::Circle(const Vector2& p_position, int p_radius) : Object() {
    m_position = p_position;
    m_radius = p_radius;
}

void Entity::Circle::Update(float frameTime) {
    Object::Update(frameTime);
    if (m_position.y + m_radius >= SCREEN_H) {
        m_velocityY = 0;
    }
    const int y = m_velocityY < 100 ? (m_velocityY += m_mass) * frameTime : m_velocityY * frameTime;
    m_position += Vector2(0, y);
    SetPosition(m_position);
}

void Entity::Circle::Render() {
    VideoDriver::GetInstance()->DrawCircle(m_position.x, m_position.y, m_radius);
}

bool Entity::Circle::Contains(const Vector2& p_coord) {
    return (p_coord - m_position).Length() < static_cast<float>(m_radius);
}

void Entity::Circle::SetPosition(const Vector2& p_position) {
    if (p_position.x - m_radius <= 0) {
        m_position.x = m_radius;
    } else if (p_position.x + m_radius >= SCREEN_W) {
        m_position.x = SCREEN_W - m_radius;
    } else {
        m_position.x = p_position.x;
    }
    if (p_position.y - m_radius <= 0) {
        m_position.y = m_radius;
    } else if (p_position.y + m_radius >= SCREEN_H) {
        m_position.y = SCREEN_H - m_radius;
    } else {
        m_position.y = p_position.y;
    }
}

int Entity::Circle::GetRadius() const {
    return m_radius;
}
