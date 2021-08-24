#include <iomanip>
#include "Sound.h"
#include "Utils.h"

using namespace std;
using namespace Utils;

Sound::Sound(): Setting(){
    _mediaLevel = 0;
    _callLevel = 0;
    _naviLevel = 0;
    _notificationLevel = 0;
}

Sound::Sound(const Setting& s):Setting(s)
{
    _mediaLevel = 0;
    _callLevel = 0;
    _naviLevel = 0;
    _notificationLevel = 0;
}

Sound::~Sound() = default;

// Input sound setting information
void Sound::inputInfo(const std::set<std::string>& keys) {
    Setting::inputInfo(keys);
    _mediaLevel = getInt(1, 10, "Media level: ");
    _callLevel = getInt(1, 10, "Call level: ");
    _naviLevel = getInt(1, 10, "Navi level: ");
    _notificationLevel = getInt(1, 10, "Notification level: ");
}

// Print out the setting information
void Sound::outputInfo() {
    Setting::outputInfo();
    cout << setw(10) << _mediaLevel << setw(10) << _callLevel << setw(10) << _naviLevel << setw(10) << _notificationLevel << endl;
}

void Sound::outputInfoLabel()
{
    cout << setw(20) << "TEN CHU XE" << setw(25) << "Email" << setw(10) << "MSC" << setw(10) << "ODO" << setw(10) << "SERVICES" << setw(10) << "Media" << setw(10) << "Call" << setw(10) << "Navi" << setw(10) << "Notify" << endl;
}

// getter for media level
int Sound::getMediaLevel() const
{
    return _mediaLevel;
}

// getter for call level
int Sound::getCallLevel() const
{
    return _callLevel;
}

// getter for navi level
int Sound::getNaviLevel() const
{
    return _naviLevel;
}

// getter for notification level
int Sound::getNotificationLevel() const
{
    return _notificationLevel;
}

// setter for media level
void Sound::setMediaLevel(const int level){
    _mediaLevel = level;
}

// setter for call level
void Sound::setCallLevel(const int level){
    _callLevel = level;
}

// setter for navi level
void Sound::setNaviLevel(const int level){
    _naviLevel = level;
}

// setter for notification level
void Sound::setNotificationLevel(const int level){
    _notificationLevel = level;
}

