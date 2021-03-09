#include <cstdlib>
#include <ctime>
#include "Globals.h"
#include "FluVirus.h"


FluVirus::FluVirus() = default;

FluVirus::FluVirus(const FluVirus& otherFlu) : Virus(otherFlu) {
    m_color = otherFlu.m_color;
}

FluVirus::~FluVirus() {
    FluVirus::DoDie();
}

void FluVirus::DoBorn() {
    srand(time(nullptr));
    m_color = rand() % 2 == 0 ? RED : BLUE;
    LoadADNInformation();
}

FluVirus* FluVirus::DoClone() {
    return new FluVirus;
}

void FluVirus::DoDie() {
    SAFE_DEL(m_dna);
}

void FluVirus::InitResistance() {
    // from 10 to 20 if red
    // else 10 to 15
    m_resistance = m_color == RED ? Utils::Rng(10, 20) : Utils::Rng(10, 15);
}
