#pragma once
#include "math/Vector2.h"

namespace Entity {
    class Object
    {
    public:
        Object();
        virtual ~Object() = default;
        virtual void Render() = 0;
        virtual void Update(float frameTime);
        virtual int GetVelocityX() const;
        virtual int GetVelocityY() const;
        virtual void SetVelocityX(int p_velocity);
        virtual void SetVelocityY(int p_velocity);
        virtual int GetMass() const;
        virtual void SetMass(int p_mass);
        virtual Vector2 GetPosition() const;
        virtual void SetPosition(const Vector2& p_position);
        // check if a point is inside object or not
        virtual bool Contains(const Vector2& p_coord) = 0;
    protected:
        Vector2 m_position;
        int m_mass = 0;
        int m_velocityX = 0;
        int m_velocityY = 0;
    };
}
