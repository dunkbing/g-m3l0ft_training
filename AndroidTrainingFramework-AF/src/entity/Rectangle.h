#pragma once
#include "Object.h"

namespace Entity {
    class Rectangle : public Object
    {
    public:
        Rectangle();
        Rectangle(const Vector2& p_position, float p_width, float p_height);
        void Render() override;
        void Update(float frameTime) override;
        bool Contains(const Vector2& p_coord) override;
        void SetPosition(const Vector2& p_position) override;
        int GetWidth() const;
        int GetHeight() const;
    private:
        int m_width;
        int m_height;
    };
}

