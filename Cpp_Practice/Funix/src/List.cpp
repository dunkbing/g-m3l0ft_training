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
    loadSettings();
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

        if (d == nullptr)
        {
            if (s != nullptr)
            {
                d = new Display(*dynamic_cast<Setting*>(s));
            }
            else if (g != nullptr)
            {
                d = new Display(*dynamic_cast<Setting*>(g));
            }
        }
        if (s == nullptr)
        {
            if (d != nullptr)
            {
                s = new Sound(*dynamic_cast<Setting*>(d));
            }
            else if (g != nullptr)
            {
                s = new Sound(*dynamic_cast<Setting*>(g));
            }
        }
        if (g == nullptr)
        {
            if (d != nullptr)
            {
                g = new General(*dynamic_cast<Setting*>(d));
            }
            else if (s != nullptr)
            {
                g = new General(*dynamic_cast<Setting*>(s));
            }
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
                if (line.find("Display") != string::npos)
                {
                    line = line.substr(8, line.length());
                    vector<string> displays = Utils::splitString(line, ",");
                    auto* display = new Display;
                    display->setCarName(displays[0]);
                    display->setEmail(displays[1]);
                    display->setPersonalKey(displays[2]);
                    display->setOdo(stoi(displays[3]));
                    display->setServiceRemind(stoi(displays[4]));
                    display->setLightLevel(stoi(displays[5]));
                    display->setScreenLightLevel(stoi(displays[6]));
                    display->setTaploLightLevel(stoi(displays[7]));
                    _displays.push_back(display);
                    _keys.insert(display->getPersonalKey());
                }
                else if (line.find("Sound") != string::npos)
                {
                    line = line.substr(6, line.length());
                    vector<string> sounds = Utils::splitString(line, ",");
                    auto* sound = new Sound;
                    sound->setCarName(sounds[0]);
                    sound->setEmail(sounds[1]);
                    sound->setPersonalKey(sounds[2]);
                    sound->setOdo(stoi(sounds[3]));
                    sound->setServiceRemind(stoi(sounds[4]));
                    sound->setMediaLevel(stoi(sounds[5]));
                    sound->setCallLevel(stoi(sounds[6]));
                    sound->setNaviLevel(stoi(sounds[7]));
                    sound->setNotificationLevel(stoi(sounds[8]));
                    _sounds.push_back(sound);
                    _keys.insert(sound->getPersonalKey());
                }
                else if (line.find("General") != string::npos)
                {
                    line = line.substr(8, line.length());
                    vector<string> generals = Utils::splitString(line, ",");
                    auto* general = new General;
                    general->setCarName(generals[0]);
                    general->setEmail(generals[1]);
                    general->setPersonalKey(generals[2]);
                    general->setOdo(stoi(generals[3]));
                    general->setServiceRemind(stoi(generals[4]));
                    general->setLanguage(generals[5]);
                    general->setTimeZone(generals[6]);
                    _generals.push_back(general);
                    _keys.insert(general->getPersonalKey());
                }
            }
        }
    }
    catch (exception& e) {
        cout << e.what() << endl;
    }
    _displays.resize(100, nullptr);
    _sounds.resize(100, nullptr);
    _generals.resize(100, nullptr);
}

void SettingList::saveSettings()
{
    const string path = "settings.txt";
    try {
        ofstream file(path);
        if (file.is_open()) {
            for (auto & _display : _displays)
            {
                if (_display != nullptr)
                {
                    auto* display = dynamic_cast<Display*>(_display);
                    file << "Display:" << display->getCarName() << ',' << display->getEmail() << ',' << display->getPersonalKey() << ',' << display->getOdo() << ',' << display->getServiceRemind() << ',' << display->getLightLevel() << ',' << display->getScreenLightLevel() << ',' << display->getTaploLightLevel() << endl;
                }
            }
            for (auto& _sound : _sounds)
            {
                if (_sound != nullptr)
                {
                    auto* sound = dynamic_cast<Sound*>(_sound);
                    file << "Sound:" << sound->getCarName() << ',' << sound->getEmail() << ',' << sound->getPersonalKey() << ',' << sound->getOdo() << ',' << sound->getServiceRemind() << ',' << sound->getMediaLevel() << ',' << sound->getCallLevel() << ',' << sound->getNaviLevel() << ',' << sound->getNotificationLevel() << endl;
                }
            }
            for (auto& _general : _generals)
            {
                if (_general != nullptr)
                {
                    auto* general = dynamic_cast<General*>(_general);
                    file << "General:" << general->getCarName() << ',' << general->getEmail() << ',' << general->getPersonalKey() << ',' << general->getOdo() << ',' << general->getServiceRemind() << ',' << general->getLanguage() << ',' << general->getTimeZone() << endl;
                }
            }
            file.close();
        }
    } catch (exception& e) {
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

void SettingList::sort(vector<Setting*>& settings)
{
    std::sort(settings.begin(), settings.end(), [](Setting* a, Setting* b)->bool {
        if (a == nullptr) return false;
        if (b == nullptr) return true;
        return a->getPersonalKey() < b->getPersonalKey();
    });
}
