#include "Display.h"

#include <iomanip>

#include "Utils.h"

Display::Display() {
	_lightLevel = 0;
	_screenLightLevel = 0;
	_taploLightLevel = 0;
}

Display::Display(const Setting& s) : Setting(s)
{
	_lightLevel = 0;
	_screenLightLevel = 0;
	_taploLightLevel = 0;
}

Display::~Display() = default;

int Display::getLightLevel() const
{
	return _lightLevel;
}

int Display::getScreenLightLevel() const
{
	return _screenLightLevel;
}

int Display::getTaploLightLevel() const
{
	return _taploLightLevel;
}
void Display::setLightLevel(const int lightLevel) {
	_lightLevel = lightLevel;
}

void Display::setScreenLightLevel(const int lightLevel) {
	_screenLightLevel = lightLevel;
}

void Display::setTaploLightLevel(const int lightLevel) {
	_taploLightLevel = lightLevel;
}

void Display::inputInfo(const std::set<std::string>& keys) {
	Setting::inputInfo(keys);
	_lightLevel = Utils::getInt(1, 10, "Light level: ");
	_screenLightLevel = Utils::getInt(1, 10, "Screen light level: ");
	_taploLightLevel = Utils::getInt(1, 10, "Taplo light level: ");
}

void Display::outputInfo() {
	Setting::outputInfo();
	cout << setw(10) << _lightLevel << setw(10) << _taploLightLevel << setw(10) << _screenLightLevel << endl;
}

void Display::outputInfoLabel()
{
	cout << setw(20) << "TEN CHU XE" << setw(25) << "Email" << setw(10) << "MSC" << setw(10) << "ODO" << setw(10) << "SERVICES" << setw(10) << "Light" << setw(10) << "Taplo" << setw(10) << "Screen" << endl;
}

