#pragma once
struct Vector2
{
    Vector2() : x(0.0f), y(0.0f) {}
    Vector2(int p_x, int p_y) : x(p_x), y(p_y) {}
    Vector2(int* pArg) : x(pArg[0]), y(pArg[1]) {}
    Vector2(const Vector2& vector) : x(vector.x), y(vector.y) {}

    //Vector's operations
    float Length() const;
    Vector2& Normalize();
    Vector2 operator + (Vector2& vector) const;
    Vector2& operator += (const Vector2& vector);
    Vector2 operator - () const;
    Vector2 operator - (Vector2 & vector) const;
    Vector2& operator -= (const Vector2 & vector);
    Vector2 operator * (int k) const;
    Vector2& operator *= (int k);
    Vector2 operator / (int k) const;
    Vector2& operator /= (int k);
    Vector2& operator = (const Vector2 & vector);
    Vector2 Modulate(Vector2 & vector) const;
    float Dot(Vector2 & vector) const;

    // access to elements
    float operator[] (unsigned int idx) const;

    // data members
    int x;
    int y;
};

