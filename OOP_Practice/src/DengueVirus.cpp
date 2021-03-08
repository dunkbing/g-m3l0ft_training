#include "DengueVirus.h"


#include <array>
#include <cstring>

#include "Globals.h"

DengueVirus::DengueVirus() : m_protein{""} {
    InitResistance();
}

DengueVirus::~DengueVirus() = default;

void DengueVirus::DoBorn() {
    LoadADNInformation();
    std::array<std::string, 3> proteins {"NS3", "NS5", "E"};
    strcpy_s(m_protein, proteins[rand() % 3].c_str());
}

Virus* DengueVirus::DoClone() {
    return nullptr;
}

std::array<DengueVirus*, 2> DengueVirus::DoClone2() {
    return std::array< DengueVirus*, 2>({new DengueVirus(), new DengueVirus});
}

void DengueVirus::DoDie() {
    SAFE_DEL(m_dna);
}

void DengueVirus::InitResistance() {
    this->DoBorn();
    if (strcmp(m_protein, "NS3") == 0) {
        m_resistance = Utils::Rng(1, 10);
    } else if (strcmp(m_protein, "NS5") == 0) {
        m_resistance = Utils::Rng(11, 20);
    } else {
        m_resistance = Utils::Rng(21, 30);
    }
}
