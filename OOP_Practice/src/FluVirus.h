#pragma once
#include "Virus.h"

class FluVirus : public Virus {
public:
    void DoBorn() override;
    void DoClone() override;
    void DoDie() override;
    // void InitResistance() override;
private:
    int m_color;
};

