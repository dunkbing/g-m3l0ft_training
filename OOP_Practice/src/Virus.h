#pragma once
class Virus {
public:
    Virus();
    explicit Virus(Virus* p_virus);
    Virus(const Virus& p_virus);
    ~Virus();
    void LoadADNInformation();
    void ReduceResistance(int medicine_resistance);
    virtual void DoBorn() = 0;
    virtual void DoClone() = 0;
private:
    char* m_dna;
    int m_resistance;
};

