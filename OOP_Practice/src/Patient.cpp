#include <numeric>
#include <functional>

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
    std::list<Virus*> clonedViruses;
    const std::function<bool(Virus*)> pred = [&clonedViruses](Virus* v)->bool {
        if (v->GetState() == ALIVE) {
            if (typeid(v).name() == typeid(FluVirus*).name()) {
                auto* flu = dynamic_cast<FluVirus*>(v);
                clonedViruses.push_back(flu->DoClone());
            } else {
                auto* dengue = dynamic_cast<DengueVirus*>(v);
                auto* dengueClones = reinterpret_cast<std::list<DengueVirus*>*>(dengue->DoClone());
                clonedViruses.insert(clonedViruses.end(), dengueClones->begin(), dengueClones->end());
            }
            return true;
        }
        return false;
    };
    m_viruses.erase(
        std::remove_if(
            m_viruses.begin(),
            m_viruses.end(),
            pred
        ),
        m_viruses.end()
    );
    m_viruses.insert(m_viruses.end(), clonedViruses.begin(), clonedViruses.end());
    if (m_resistance < this->TotalVirusesResist()) {
        DoDie();
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

int Patient::TotalResist() const {
    return m_resistance;
}

int Patient::TotalVirusesResist() const {
    return std::accumulate(m_viruses.begin(), m_viruses.end(), 0, [](int val, Virus* v) {
        return val + v->GetResist();
    });
}

std::ostream& operator<<(std::ostream& os, Patient p) {
    os << "Patient's resistance: " << p.m_resistance << '\n' << "Viruses' resistance: " << p.TotalVirusesResist() << std::endl;
    return os;
}
