#pragma once
#include <memory>
#include "State.h"
constexpr int BLUE = 0x0000ff;
constexpr int RED = 0xff0000;
class Virus {
public:
    Virus();
    explicit Virus(Virus* p_virus);
    Virus(const Virus& p_virus);
    virtual  ~Virus();
    void LoadADNInformation();
    void ReduceResistance(int medicine_resistance);
    int GetResist() const;
    State GetState() const;
    virtual void DoBorn() = 0;
    virtual Virus* DoClone() = 0;
    virtual void DoDie() = 0;
    virtual void InitResistance() = 0;

protected:
    char* m_dna;
    int m_resistance;
    State m_state;
};

