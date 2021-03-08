#pragma once
#include "Patient.h"
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
    State GetState() const;
protected:
    virtual void DoBorn() = 0;
    virtual Virus* DoClone() = 0;
    virtual void DoDie() = 0;
    virtual void InitResistance() = 0;

    char* m_dna;
    int m_resistance;
    State m_state;
};

