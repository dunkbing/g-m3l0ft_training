#include <fstream>
#include <algorithm>
#include "Sound.h"
#include "Utils.h"
#include "List.h"

#include "Display.h"

SettingList::SettingList()
{
    _displays.resize(100, nullptr);
}

SettingList::~SettingList() = default;

void SettingList::inputSettings() {
    system("cls");
    // Your code
    cout << "--- SELECT MENU ---" << endl;
    cout << "1. Display setting" << endl;
    cout << "2. Sound setting" << endl;
    cout << "3. General setting" << endl;
    cout << "0. Exit" << endl;
    cout << "Your selection: ";
    const int selection = Utils::getInt(0, 3);

    system("cls");
    switch (selection) {
        case 1:
        {
            cout << " --- Input Display setting --- " << endl;
            inputDisplaySettings();
            break;
        }
        case 2:
        {
            cout << " --- Input Sound setting --- " << endl;
            inputSoundSettings();
            break;
        }
        case 3:
        {
            cout << " --- Input General setting --- " << endl;
            inputGeneralSettings();
            break;
        }
        case 0:
        default:
        {
            break;
        }
    }
}

void SettingList::outputSettings() {
    system("cls");
    cout << "--- SELECT MENU ---" << endl;
    cout << "1. Print Display setting information" << endl;
    cout << "2. Print Sound setting information" << endl;
    cout << "3. Print General setting information" << endl;
    cout << "4. Print all setting information" << endl;
    cout << "5. Exit" << endl;
    cout << "Your selection: ";
    const int selection = Utils::getInt(1, 5);

    system("cls");
    switch (selection) {
        // Your code
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
    for (auto& _general : _generals)
    {
        if (_general != nullptr) {
            _general->outputInfo();
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
    cout << setw(20) << "TEN CHU XE" << setw(25) << "Email" << setw(10) << "MSC" << setw(10) << "ODO" << setw(10) << "SEVICES" << setw(10) << "Light" << setw(10) << "Taplo" << setw(10) << "Screen" << setw(10) << "Media" << setw(15) << "Call" << setw(15) << "Navigation" << setw(15) << "Notification" << setw(10) << "Timezone" << setw(10) << "Language" << endl;
    for (int i = 0; i < 100; i++) {
        if (_displays[i] != nullptr) {
            auto* d = _displays[i];
            auto* s = _sounds[i];
            auto* g = _generals[i];
            // cout << setw(20) << d->getCarName() << setw(25) << d->getEmail() << setw(10) << d->getPersonalKey() << setw(10) << d->getOdo() << setw(10) << d->getServiceRemind() << setw(10) << d->getLightLevel() << setw(10) << d->getTaploLightLevel() << setw(10) << d->getScreenLightLevel() << setw(10) << s->getMediaLevel() << setw(15) << s->getCallLevel() << setw(15) << s->getNaviLevel() << setw(15) << s->getNotificationLevel() << setw(10) << g->getTimeZone() << setw(10) << g->getLanguage() << endl;
        }
    }
    cin.get();
}

void SettingList::inputSoundSettings()
{
    char continues = 'n';
    do {
        const int index = Utils::getInt(1, 100, "Car index: ") - 1;
        if (_sounds[index] == nullptr) {
            _sounds[index] = new Sound();
        }
        _sounds[index]->inputInfo();
        cout << "Will you input for Car " << index << " ? (y/n): ";
        cin >> continues;
        cin.ignore();
        cout << endl;
    } while (continues == 'y');
    sort(_sounds);
}

void SettingList::inputGeneralSettings()
{
    char continues = 'n';
    do {
        const int index = Utils::getInt(1, 100, "Car index: ") - 1;
        if (_generals[index] == nullptr) {
            _generals[index] = new General();
        }
        _generals[index]->inputInfo();
        cout << "Will you input for Car " << index + 1 << " ? (y/n): ";
        cin >> continues;
        cin.ignore();
        cout << endl;
    } while (continues == 'y');
    sort(_generals);
}

void SettingList::inputDisplaySettings()
{
    char continues = 'n';
    do {
        const int index = Utils::getInt(1, 100, "Car index: ") - 1;
        if (_displays[index] == nullptr) {
            _displays[index] = new Display();
        }
        _displays[index]->inputInfo();
        cout << "Will you input for Car " << index + 1 << " ? (y/n): ";
        cin >> continues;
        cin.ignore();
        cout << endl;
    } while (continues == 'y');
    sort(_displays);
}

void SettingList::loadSettings()
{
    const string PATH = "settings.txt";
    try {
        ifstream file(PATH);
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
                        // general->copy(display);
                        // sound->copy(display);
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

void SettingList::sort(vector<Setting*> settings)
{
    std::sort(settings.begin(), settings.end(), [](Setting* a, Setting* b)->bool {
        if (a == nullptr) return false;
        if (b == nullptr) return true;
        return a->getCarName() < b->getCarName();
    });
}
