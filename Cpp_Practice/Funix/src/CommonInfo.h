#ifndef COMMON_H_
#define COMMON_H_

#include <iostream>
#include <string>
#include <vector>

using namespace std;

class CommonInfo {
public:
    string getNumber() const;
    string getName() const;
    void setNumber(string& number);
    void setName(string& name);
    void printData(int i);
private:
    string _number;
    string _name;		
};

#endif // COMMON_H_
