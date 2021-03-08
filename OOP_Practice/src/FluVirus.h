#pragma once
#include "Virus.h"

class FluVirus : public Virus {
public:
    FluVirus();
    ~FluVirus();
    void DoBorn() override;
    FluVirus* DoClone() override;
    void DoDie() override;
    void InitResistance() override;
private:
    int m_color = 0xffffff;
};

