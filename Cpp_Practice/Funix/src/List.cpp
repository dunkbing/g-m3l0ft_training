#include <fstream>
#include <algorithm>
#include <iomanip>
#include "Sound.h"
#include "Utils.h"
#include "List.h"
#include "Display.h"
#include "General.h"

using namespace std;

SettingList::SettingList()
{
    _displays.resize(100, nullptr);
    _sounds.resize(100, nullptr);
    _generals.resize(100, nullptr);
}

SettingList::~SettingList() = default;

void SettingList::inputSettings() {
    Utils::clearScr();
    cout << "--- SELECT MENU ---" << endl;
    cout << "1. Display setting" << endl;
    cout << "2. Sound setting" << endl;
    cout << "3. General setting" << endl;
    cout << "0. Exit" << endl;
    cout << "Your selection: ";
    const int selection = Utils::getInt(0, 3);

    Utils::clearScr();
    switch (selection) {
        case 1:
        {
            cout << " --- Input Display setting --- " << endl;
            inputSettings<Display>(_displays, _keys);
            break;
        }
        case 2:
        {
            cout << " --- Input Sound setting --- " << endl;
            inputSettings<Sound>(_sounds, _keys);
            break;
        }
        case 3:
        {
            cout << " --- Input General setting --- " << endl;
            inputSettings<General>(_generals, _keys);
            break;
        }
        default:
        {
            break;
        }
    }
}

void SettingList::outputSettings() {
    Utils::clearScr();
    cout << "--- SELECT MENU ---" << endl;
    cout << "1. Print Display setting information" << endl;
    cout << "2. Print Sound setting information" << endl;
    cout << "3. Print General setting information" << endl;
    cout << "4. Print all setting information" << endl;
    cout << "5. Exit" << endl;
    cout << "Your selection: ";
    const int selection = Utils::getInt(1, 5);

    Utils::clearScr();
    switch (selection) {
        case 1:
            outputDisplaySettings();
            break;
        case 2:
            outputSoundSettings();
            break;
        case 3:
            outputGeneralSettings();
            break;
        case 4:
            outputAllSettings();
            break;
        case 5:
            exit(0);
        default:
            break;
    }
}

void SettingList::outputSoundSettings() {
    cout << setw(20) << "TEN CHU XE" << setw(25) << "Email" << setw(10) << "MSC" << setw(10) << "ODO" << setw(10) << "SERVICES" << setw(10) << "Media" << setw(10) << "Call" << setw(10) << "Navi" << setw(10) << "Notify" << endl;
    for (auto& sound : _sounds)
    {
        if (sound != nullptr) {
            sound->outputInfo();
        }
    }
    cin.get();
}

void SettingList::outputGeneralSettings() {
    cout << setw(20) << "TEN CHU XE" << setw(25) << "Email" << setw(10) << "MSC" << setw(10) << "ODO" << setw(10) << "SERVICES" << setw(30) << "TimeZone" << setw(20) << "Language" << endl;
    for (auto& general : _generals)
    {
        if (general != nullptr) {
            general->outputInfo();
        }
    }
    cin.get();
}

void SettingList::outputDisplaySettings() {
    cout << setw(20) << "TEN CHU XE" << setw(25) << "Email" << setw(10) << "MSC" << setw(10) << "ODO" << setw(10) << "SERVICES" << setw(10) << "Light" << setw(10) << "Taplo" << setw(10) << "Screen" << endl;
    for (auto& display : _displays)
    {
        if (display != nullptr) {
            display->outputInfo();
        }
    }
    cin.get();
}

void SettingList::outputAllSettings() {
    cout << setw(20) << "TEN CHU XE" << setw(25) << "Email" << setw(10) << "MSC" << setw(10) << "ODO" << setw(10) << "SERVICES" << setw(10) << "Light" << setw(10) << "Taplo" << setw(10) << "Screen" << setw(10) << "Media" << setw(15) << "Call" << setw(15) << "Navigation" << setw(15) << "Notification" << setw(10) << "Timezone" << setw(10) << "Language" << endl;

    for (const auto& key : _keys)
    {
        auto* d = getSetting<Display>(_displays, key);
        auto* s = getSetting<Sound>(_sounds, key);
        auto* g = getSetting<General>(_generals, key);

        if (d != nullptr && s == nullptr && g == nullptr)
        {
            s = new Sound(*dynamic_cast<Setting*>(d));
            g = new General(*dynamic_cast<Setting*>(d));
        } else if (d == nullptr && s != nullptr && g == nullptr)
        {
            d = new Display(*dynamic_cast<Setting*>(s));
            g = new General(*dynamic_cast<Setting*>(s));
        } else if (d == nullptr && s == nullptr && g != nullptr)
        {
            d = new Display(*dynamic_cast<Setting*>(g));
            s = new Sound(*dynamic_cast<Setting*>(g));
        }

        cout << setw(20) << d->getCarName() << setw(25) << d->getEmail() << setw(10) << d->getPersonalKey() << setw(10) << d->getOdo() << setw(10) << d->getServiceRemind() << setw(10) << d->getLightLevel() << setw(10) << d->getTaploLightLevel() << setw(10) << d->getScreenLightLevel() << setw(10) << s->getMediaLevel() << setw(15) << s->getCallLevel() << setw(15) << s->getNaviLevel() << setw(15) << s->getNotificationLevel() << setw(10) << g->getTimeZone() << setw(10) << g->getLanguage() << endl;
    }
    cin.get();
}

void SettingList::loadSettings()
{
    const string path = "settings.txt";
    try {
        ifstream file(path);
        if (file.is_open()) {
            int index = 0;
            std::string line;
            while (std::getline(file, line)) {
                vector<string> rawSett = Utils::splitString(line, "|");
                if (rawSett.size() >= 4) {
                    rawSett[0] = rawSett[0].substr(7, rawSett[0].length());
                    rawSett[1] = rawSett[1].substr(8, rawSett[1].length());
                    rawSett[2] = rawSett[2].substr(6, rawSett[2].length());
                    rawSett[3] = rawSett[3].substr(8, rawSett[3].length());
                    vector<string> commons = Utils::splitString(rawSett[0], ",");
                    vector<string> generalsStr = Utils::splitString(rawSett[1], ",");
                    vector<string> soundsStr = Utils::splitString(rawSett[2], ",");
                    vector<string> displaysStr = Utils::splitString(rawSett[3], ",");
                    auto* display = new Display();
                    auto* general = new General();
                    auto* sound = new Sound();
                    if (commons.size() >= 5) {
                        display->setCarName(commons[0]);
                        display->setEmail(commons[1]);
                        display->setPersonalKey(commons[2]);
                        display->setOdo(stoi(commons[3]));
                        display->setServiceRemind(stoi(commons[4]));
                    }
                    if (generalsStr.size() >= 2) {
                        general->setLanguage(generalsStr[0]);
                        general->setTimeZone(generalsStr[1]);
                    }
                    if (soundsStr.size() >= 4) {
                        sound->setMediaLevel(stoi(soundsStr[0]));
                        sound->setCallLevel(stoi(soundsStr[1]));
                        sound->setNaviLevel(stoi(soundsStr[2]));
                        sound->setNotificationLevel(stoi(soundsStr[3]));
                    }
                    if (displaysStr.size() >= 3) {
                        display->setLightLevel(stoi(displaysStr[0]));
                        display->setTaploLightLevel(stoi(displaysStr[1]));
                        display->setScreenLightLevel(stoi(displaysStr[2]));
                    }
                    _displays.push_back(display);
                    _generals.push_back(general);
                    _sounds.push_back(sound);
                    index++;
                }
            }
        }
    }
    catch (exception& e) {
        cout << e.what() << endl;
    }
}

void SettingList::release()
{
    for (auto display : _displays)
    {
        Utils::safeDel(display);
    }
    for (auto sound : _sounds)
    {
        Utils::safeDel(sound);
    }
    for (auto general : _generals)
    {
        Utils::safeDel(general);
    }
}

void SettingList::sort(vector<Setting*> settings)
{
    std::sort(settings.begin(), settings.end(), [](Setting* a, Setting* b)->bool {
        if (a == nullptr) return false;
        if (b == nullptr) return true;
        return a->getPersonalKey() < b->getPersonalKey();
    });
}
