#include "CommonInfo.h"

#include "Utils.h"

std::string CommonInfo::getNumber() const
{
    return _number;
}

std::string CommonInfo::getName() const
{
    return _name;
}

void CommonInfo::setNumber(std::string& number)
{
    _number = number;
}

void CommonInfo::setName(std::string& name)
{
    _name = name;
}

void CommonInfo::printData(int i)
{
    // TODO: implement later
}
