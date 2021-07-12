#ifndef COMMON_H_
#define COMMON_H_
#include <string>

class CommonInfo {
public:
    std::string getNumber() const;
    std::string getName() const;
    void setNumber(std::string& number);
    void setName(std::string& name);
    void printData(int i);
private:
    std::string _number;
    std::string _name;		
};

#endif // COMMON_H_
