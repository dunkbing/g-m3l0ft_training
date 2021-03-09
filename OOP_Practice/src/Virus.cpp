#include <fstream>
#include <string>
#include <Globals.h>
#include "Virus.h"

Virus::Virus() {
    m_dna = nullptr;
    m_resistance = 0;
    m_state = ALIVE;
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
    SAFE_DEL(m_dna);
}

void Virus::LoadADNInformation() {
    std::ifstream ifs("ATGX.bin", std::ifstream::in);
    if (ifs.is_open()) {
        std::string line;
        std::getline(ifs, line);
        ifs.close();
        if (!m_dna) {
            m_dna = new char[strlen(line.c_str())+1];
        }
        strcpy_s(m_dna, strlen(line.c_str()) + 1, line.c_str());
    }
}

void Virus::ReduceResistance(int medicine_resistance) {
    m_resistance -= medicine_resistance;
    if (m_resistance <= 0) {
        m_state = DIE;
    }
}

int Virus::GetResist() const {
    return m_resistance;
}

State Virus::GetState() const {
    return m_state;
}
