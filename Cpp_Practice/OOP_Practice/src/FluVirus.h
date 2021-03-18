#pragma once
#include "Virus.h"

class FluVirus final : public Virus {
public:
    FluVirus();
    FluVirus(const FluVirus& otherFlu);
    ~FluVirus() override;
    void DoBorn() override;
    FluVirus* DoClone() override;
    void DoDie() override;
    void InitResistance() override;
private:
    int m_color = 0xffffff;
};
