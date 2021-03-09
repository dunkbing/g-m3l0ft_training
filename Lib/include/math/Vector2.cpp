#include "Vector2.h"

#include <cmath>

float Vector2::Length() const {
    return sqrt(x * x + y * y);
}

Vector2& Vector2::Normalize() {
    const float length = Length();
    x /= length;
    y /= length;

    return *this;
}

Vector2 Vector2::operator+(Vector2& vector) const {
    return Vector2(x + vector.x, y + vector.y);
}

Vector2& Vector2::operator+=(Vector2& vector) {
    x += vector.x;
    y += vector.y;
    return *this;
}

Vector2 Vector2::operator-() const {
    return Vector2(-x, -y);
}

Vector2 Vector2::operator-(Vector2& vector) const {
    return Vector2(x - vector.x, y - vector.y);
}

Vector2& Vector2::operator-=(Vector2& vector) {
    x -= vector.x;
    y -= vector.y;

    return *this;
}

Vector2 Vector2::operator*(float k) const {
    return Vector2(x * k, y * k);
}

Vector2& Vector2::operator*=(float k) {
    x *= k;
    y *= k;

    return *this;
}

Vector2 Vector2::operator/(float k) const {
    return Vector2(x / k, y / k);
}

Vector2& Vector2::operator/=(float k) {
    return operator *= (1.0f / k);
}

Vector2& Vector2::operator=(const Vector2& vector) = default;

Vector2 Vector2::Modulate(Vector2& vector) const {
    return Vector2(x * vector.x, y * vector.y);
}

float Vector2::Dot(Vector2& vector) const {
    return x * vector.x + y * vector.y;
}

float Vector2::operator[](unsigned int idx) const {
    return (&x)[idx];
}


