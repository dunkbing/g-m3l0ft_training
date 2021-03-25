#pragma once
#include "Object.h"

namespace Entity {
    class Circle : public Object
    {
    public:
        Circle();
        Circle(const Vector2& p_position, int p_radius);
        void Update(float frameTime) override;
        void Render() override;
        bool Contains(const Vector2& p_coord) override;
        void SetPosition(const Vector2& p_position) override;
        int GetRadius() const;
    private:
        int m_radius;
    };
}
