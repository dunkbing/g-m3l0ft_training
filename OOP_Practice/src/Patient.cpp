#include <numeric>
#include <Globals.h>
#include "Patient.h"


#include "DengueVirus.h"
#include "FluVirus.h"
using namespace Utils;
Patient::Patient() : m_resistance(0), m_state(ALIVE) {
    InitResistance();
    DoStart();
}

Patient::~Patient() = default;

void Patient::InitResistance() {
    // init resistance from 1000 - 9000
    m_resistance = Rng(1000, 9000);
}

void Patient::DoStart() {
    const size_t size = Rng(10, 20);
    for (size_t i = 0; i < size; i++) {
        if (Rng(1, 2) == 1) {
            m_viruses.push_back(new FluVirus());
        } else {
            m_viruses.push_back(new DengueVirus());
        }
    }
    if (TotalVirusesResist() > m_resistance) {
        DoDie();
    }
}

void Patient::TakeMedicine(int medicine_resistance) {
    for (Virus* virus : m_viruses) {
        virus->ReduceResistance(medicine_resistance);
    }
    for (Virus* virus : m_viruses) {
        if (virus->GetState() == ALIVE) {
            if (typeid(virus).name() == typeid(FluVirus).name()) {

            }
        }
    }
}

void Patient::DoDie() {
    m_state = DIE;
    if (!m_viruses.empty()) {
        for (auto* virus : m_viruses) {
            SAFE_DEL<Virus*>(virus);
        }
    }
}

State Patient::GetState() const {
    return m_state;
}

int Patient::TotalVirusesResist() {
    return std::accumulate(m_viruses.begin(), m_viruses.end(), 0);
}
