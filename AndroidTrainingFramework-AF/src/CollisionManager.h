#pragma once
#include <cstdlib>
#include <entity/Circle.h>
#include <entity/Rectangle.h>

#include "define.h"

class CollisionManager
{
public:
    // check collision btw circle and rect
    static bool CircleToRect(Entity::Circle* c, Entity::Rectangle* r) {
        const Vector2 cPos = c->GetPosition();
        const Vector2 rPos = r->GetPosition();
        Vector2 temp = c->GetPosition();
        if (cPos.x < rPos.x) {
            temp.x = rPos.x;
        } else if(cPos.x > rPos.x + r->GetWidth()) {
            temp.x = rPos.x + r->GetWidth();
        }
        if (cPos.y < rPos.y) {
            temp.y = rPos.y;
        } else if (cPos.y > rPos.y + r->GetHeight()) {
            temp.y = rPos.y + r->GetHeight();
        }

        // get distance from closest edges
        const float distance = (cPos - temp).Length();
        if (distance < static_cast<float>(c->GetRadius())) {
            if (c->GetPosition().x < r->GetPosition().x) {
                c->SetVelocityX(-50);
                r->SetVelocityX(50);
            } else if (c->GetPosition().x > r->GetPosition().x + r->GetWidth()) {
                c->SetVelocityX(50);
                r->SetVelocityX(-50);
            } else {
                if (c->GetPosition().y < r->GetPosition().y) {
                    c->SetPosition(Vector2(c->GetPosition().x, r->GetPosition().y - c->GetRadius()));
                } else if (c->GetPosition().y <= r->GetPosition().y + r->GetHeight()) {
                    r->SetPosition(Vector2(r->GetPosition().x, c->GetPosition().y - c->GetRadius() - r->GetHeight()));
                }
            }
        }

        return distance < static_cast<float>(c->GetRadius());
    }

    // check collision btw rect and rect
    static bool RectToRect(Entity::Rectangle* r1, Entity::Rectangle* r2) {
        if (r1->GetPosition().x + r1->GetWidth() < r2->GetPosition().x
            || r1->GetPosition().y + r1->GetHeight() < r2->GetPosition().y) return false;
        if (r1->GetPosition().x > r2->GetPosition().x + r2->GetWidth()
            || r1->GetPosition().y > r2->GetPosition().y + r2->GetHeight()) return false;
        if (r1->GetPosition().y < r2->GetPosition().y) {
            r1->SetPosition(Vector2(r1->GetPosition().x, r2->GetPosition().y - r1->GetHeight()));
        } else {
            r2->SetPosition(Vector2(r2->GetPosition().x, r1->GetPosition().y - r2->GetHeight()));
        }
        return true;
    }

    // check collision btw object and object
    static bool Intersect(Entity::Object* o1, Entity::Object* o2) {
        auto* c1 = dynamic_cast<Entity::Circle*>(o1);
        auto* r1 = dynamic_cast<Entity::Rectangle*>(o2);
        auto* c2 = dynamic_cast<Entity::Circle*>(o2);
        auto* r2 = dynamic_cast<Entity::Rectangle*>(o1);
        if (c1 && r1) {
            return CircleToRect(c1, r1);
        }
        if (r2 && c2) {
            return CircleToRect(c2, r2);
        }
        if (r1 && r2) {
            return RectToRect(r1, r2);
        }
        return false;
    }

    // check if object is colliding with edges
    static bool CollideWithEdge(Entity::Object* object) {
        const auto position = object->GetPosition();
        auto* c = dynamic_cast<Entity::Circle*>(object);
        if (c) {
            return position.x - c->GetRadius() <= 0 || position.y - c->GetRadius() <= 0 || position.x + c->GetRadius() >= SCREEN_W || position.y + c->GetRadius() >= SCREEN_H;
        }
        auto* r = dynamic_cast<Entity::Rectangle*>(object);
        return position.x <= 0 || position.y <= 0 || position.x + r->GetWidth() >= SCREEN_W || position.y + r->GetHeight() >= SCREEN_H;
    }
};
