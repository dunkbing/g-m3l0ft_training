#include <array>
#include <cstring>

#include "DengueVirus.h"
#include "Globals.h"

DengueVirus::DengueVirus() : m_protein{""} {
    InitResistance();
}

DengueVirus::DengueVirus(const DengueVirus& otherDengue) : Virus(otherDengue) {
    memcpy(m_protein, otherDengue.m_protein, sizeof(m_protein));
}

DengueVirus::~DengueVirus() = default;

void DengueVirus::DoBorn() {
    LoadADNInformation();
    std::array<std::string, 3> proteins {"NS3", "NS5", "E"};
    strcpy_s(m_protein, proteins[rand() % 3].c_str());
}

DengueVirus* DengueVirus::DoClone() {
    auto* clones = DBG_NEW std::vector<DengueVirus*>({ DBG_NEW DengueVirus(), DBG_NEW DengueVirus });
    return reinterpret_cast<DengueVirus*>(clones);
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
