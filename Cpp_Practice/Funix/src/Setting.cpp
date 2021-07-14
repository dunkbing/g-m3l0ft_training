#include "Setting.h"
#include "Utils.h"
#include <iomanip>

using namespace std;

Setting::Setting() {
    _odo = 0;
    _serviceRemind = 0;
}

Setting::Setting(const Setting& s) {
    _carName = s.getCarName();
    _personalKey = s.getPersonalKey();
    _email = s.getEmail();
    _odo = s.getOdo();
    _serviceRemind = s.getServiceRemind();
}

Setting::~Setting() = default;

string Setting::getCarName() const {
    return _carName;
}

string Setting::getPersonalKey() const {
    return _personalKey;
}

int Setting::getOdo () const {
    return _odo;
}

int Setting::getServiceRemind() const {
    return _serviceRemind;
}

void Setting::setCarName(const string& name) {
    this->_carName = name;
}

void Setting::setPersonalKey(const string key) {
    if (key == _personalKey) {
        cout << "    -> This car already existed, data will be overwritten" << endl;
    } else {
        cout << "    -> This car is new, data will be appended to your list" << endl;
    }
    this->_personalKey = key;
}

void Setting::setOdo(int odo) {
    this->_odo = odo;
}

void Setting::setServiceRemind(const int service) {
    this->_serviceRemind = service;
}

string Setting::getEmail() const {
    return this->_email;
}

void Setting::setEmail(const string& email) {
    this->_email = email;
}

void Setting::inputInfo(){
    const string emailReg = R"((\w+)(\.|_)?(\w*)@(\w+)(\.(\w+))+)";
    setCarName(Utils::getLine("Owner name: "));
    setEmail(Utils::getLine("Email: ", emailReg, "Email must be a string in format abc@xyz.def: "));
    const string oldKey = _personalKey;
    setOdo(Utils::getInt(1, 10, "Odo: "));
    setServiceRemind(Utils::getInt(1, 10, "Service remind: "));
    setPersonalKey(Utils::getLine("Personal key: ", "^[0-9]{8}$", "Personal key must be 8 digits: "));
}

void Setting::outputInfo(){
    cout << setw(20) << _carName << setw(25) << _email << setw(10) << _personalKey << setw(10) << _odo << setw(10) << _serviceRemind;
}
