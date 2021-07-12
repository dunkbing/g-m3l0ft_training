#include "CommonInfo.h"

#include "Utils.h"

string CommonInfo::getNumber() const
{
    return _number;
}

string CommonInfo::getName() const
{
    return _name;
}

void CommonInfo::setNumber(string& number)
{
    _number = number;
}

void CommonInfo::setName(string& name)
{
    _name = name;
}

void CommonInfo::printData(int i)
{
    // TODO: implement later
}
