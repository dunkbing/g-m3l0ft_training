#pragma once
#include <list>
#include "Virus.h"
enum State {
    DIE = 0, ALIVE = 1,
};
class Patient {
public:
    Patient();
    ~Patient();
    void InitResistance();
    void DoStart();
    void TakeMedicine(int);
    void DoDie();
    State GetState() const;
private:
    int m_resistance;
    std::list<Virus*> m_viruses;
    State m_state;

    int TotalVirusesResist();
};

