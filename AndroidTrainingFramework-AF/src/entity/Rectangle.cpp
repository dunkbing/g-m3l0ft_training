#include "Rectangle.h"

#include "render/videoDriver.h"

Entity::Rectangle::Rectangle() : Object() {
    m_width = m_height = 0;
}

Entity::Rectangle::Rectangle(const Vector2& p_position, float p_width, float p_height) : Object(), m_width(p_width), m_height(p_height) {
    m_position = p_position;
}

void Entity::Rectangle::Render() {
    VideoDriver::GetInstance()->DrawRect(m_position.x, m_position.y, m_width, m_height);
}

void Entity::Rectangle::Update(float frameTime) {
    Object::Update(frameTime);
    if (m_position.y + m_height >= SCREEN_H) {
        m_velocityY = 0;
    }
    const int y = m_velocityY < 100 ? (m_velocityY += m_mass) * frameTime : m_velocityY * frameTime;
    Vector2 newPos(0, y);
    SetPosition(m_position + newPos);
}

bool Entity::Rectangle::Contains(const Vector2& p_coord) {
    return m_position.x <= p_coord.x && m_position.y <= p_coord.y && m_position.x + m_width >= p_coord.x && m_position.y + m_height >= p_coord.y;
}

void Entity::Rectangle::SetPosition(const Vector2& p_position) {
    if (p_position.x <= 0) {
        m_position.x = 0;
    } else if (p_position.x + m_width >= SCREEN_W) {
        m_position.x = SCREEN_W - m_width;
    } else {
        m_position.x = p_position.x;
    }

    if (p_position.y <= 0) {
        m_position.y = 0;
    } else if (p_position.y + m_height >= SCREEN_H) {
        m_position.y = SCREEN_H - m_height;
    } else {
        m_position.y = p_position.y;
    }
}

int Entity::Rectangle::GetWidth() const {
    return m_width;
}

int Entity::Rectangle::GetHeight() const {
    return m_height;
}
