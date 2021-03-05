#include "Virus.h"

Virus::Virus() {
    m_dna = nullptr;
    m_resistance = 0;
}

Virus::Virus(Virus* p_virus): m_dna(nullptr) {
    *m_dna = *(p_virus->m_dna);
    m_resistance = p_virus->m_resistance;
}

Virus::Virus(const Virus& p_virus): m_dna(nullptr) {
    *m_dna = *(p_virus.m_dna);
    m_resistance = p_virus.m_resistance;
}

Virus::~Virus() {
    if (m_dna) {
        delete m_dna;
        m_dna = nullptr;
    }
}

void Virus::LoadADNInformation() {
}

void Virus::ReduceResistance(int medicine_resistance) {
    m_resistance -= medicine_resistance;
}
