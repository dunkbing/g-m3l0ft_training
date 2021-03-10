#pragma once
#include <vector>
#include "State.h"
#include "Virus.h"

class Patient {
public:
    Patient();
    ~Patient();
    void InitResistance();
    void DoStart();
    void TakeMedicine(int);
    void DoDie();
    void Release();
    State GetState() const;
    int TotalResist() const;
    int TotalVirusesResist() const;
    friend std::ostream& operator << (std::ostream& os, Patient p);
private:
    int m_resistance;
    std::vector<Virus*> m_viruses;
    State m_state;

};

