#pragma once
#include "Virus.h"

class DengueVirus final : public Virus {
public:
    DengueVirus();
    ~DengueVirus();
    void DoBorn() override;
    Virus* DoClone() override;
    std::array< DengueVirus*, 2> DoClone2();
    void DoDie() override;
    void InitResistance() override;
private:
    char m_protein[4];
};

