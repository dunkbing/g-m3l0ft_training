#pragma once

#include "Virus.h"

class DengueVirus final : public Virus {
public:
    DengueVirus();
    DengueVirus(const DengueVirus& otherDengue);
    ~DengueVirus() override;
    void DoBorn() override;
    DengueVirus* DoClone() override;
    void DoDie() override;
    void InitResistance() override;
private:
    char m_protein[4]{};
};

