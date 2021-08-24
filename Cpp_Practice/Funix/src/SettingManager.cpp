#include <fstream>
#include <algorithm>
#include <iomanip>
#include "SettingManager.h"

using namespace std;

SettingManager::SettingManager()
{
    loadSettings();
}

SettingManager::~SettingManager() = default;

void SettingManager::inputSettingsMenu() {
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
            inputSettings<Display>(_displays, _keys, [this](Setting* setting)
            {
                copyTo<Sound>(_sounds, setting);
                copyTo<General>(_generals, setting);
            });
            break;
        }
        case 2:
        {
            cout << " --- Input Sound setting --- " << endl;
            inputSettings<Sound>(_sounds, _keys, [this](Setting* setting)
            {
                copyTo<Display>(_displays, setting);
                copyTo<General>(_generals, setting);
            });
            break;
        }
        case 3:
        {
            cout << " --- Input General setting --- " << endl;
            inputSettings<General>(_generals, _keys, [this](Setting* setting)
            {
                copyTo<Sound>(_sounds, setting);
                copyTo<Display>(_displays, setting);
            });
            break;
        }
        default:
        {
            break;
        }
    }
    saveSettings();
}

void SettingManager::outputSettingsMenu() {
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
            outputSettings<Display>(_displays);
            break;
        case 2:
            outputSettings<Sound>(_sounds);
            break;
        case 3:
            outputSettings<General>(_generals);
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

void SettingManager::outputAllSettings() {
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

void SettingManager::loadSettings()
{
    const string path = "settings.txt";
    try {
        ifstream file(path);
        if (file.is_open()) {
            std::string line;
            while (std::getline(file, line)) {
                if (line.find("Display") != string::npos)
                {
                    line = line.substr(8, line.length());
                    vector<string> displays = Utils::splitString(line, ",");
                    Display display;
                    display.setCarName(displays[0]);
                    display.setEmail(displays[1]);
                    display.setPersonalKey(displays[2]);
                    display.setOdo(stoi(displays[3]));
                    display.setServiceRemind(stoi(displays[4]));
                    display.setLightLevel(stoi(displays[5]));
                    display.setScreenLightLevel(stoi(displays[6]));
                    display.setTaploLightLevel(stoi(displays[7]));
                    _displays.insert(display);
                    _keys.insert(display.getPersonalKey());
                }
                else if (line.find("Sound") != string::npos)
                {
                    line = line.substr(6, line.length());
                    vector<string> sounds = Utils::splitString(line, ",");
                    Sound sound;
                    sound.setCarName(sounds[0]);
                    sound.setEmail(sounds[1]);
                    sound.setPersonalKey(sounds[2]);
                    sound.setOdo(stoi(sounds[3]));
                    sound.setServiceRemind(stoi(sounds[4]));
                    sound.setMediaLevel(stoi(sounds[5]));
                    sound.setCallLevel(stoi(sounds[6]));
                    sound.setNaviLevel(stoi(sounds[7]));
                    sound.setNotificationLevel(stoi(sounds[8]));
                    _sounds.insert(sound);
                    _keys.insert(sound.getPersonalKey());
                }
                else if (line.find("General") != string::npos)
                {
                    line = line.substr(8, line.length());
                    vector<string> generals = Utils::splitString(line, ",");
                    General general;
                    general.setCarName(generals[0]);
                    general.setEmail(generals[1]);
                    general.setPersonalKey(generals[2]);
                    general.setOdo(stoi(generals[3]));
                    general.setServiceRemind(stoi(generals[4]));
                    general.setLanguage(generals[5]);
                    general.setTimeZone(generals[6]);
                    _generals.insert(general);
                    _keys.insert(general.getPersonalKey());
                }
            }
        }
    }
    catch (exception& e) {
        cout << e.what() << endl;
    }
}

void SettingManager::saveSettings()
{
    const string path = "settings.txt";
    try {
        ofstream file(path);
        if (file.is_open()) {
            _displays.inOrder([&file](const Display& display) {
                file << "Display:" << display.getCarName() << ',' << display.getEmail() << ',' << display.getPersonalKey() << ',' << display.getOdo() << ',' << display.getServiceRemind() << ',' << display.getLightLevel() << ',' << display.getScreenLightLevel() << ',' << display.getTaploLightLevel() << endl;
            });
            _sounds.inOrder([&file](const Sound& sound) {
                file << "Sound:" << sound.getCarName() << ',' << sound.getEmail() << ',' << sound.getPersonalKey() << ',' << sound.getOdo() << ',' << sound.getServiceRemind() << ',' << sound.getMediaLevel() << ',' << sound.getCallLevel() << ',' << sound.getNaviLevel() << ',' << sound.getNotificationLevel() << endl;
            });
            _generals.inOrder([&file](const General& general) {
                file << "General:" << general.getCarName() << ',' << general.getEmail() << ',' << general.getPersonalKey() << ',' << general.getOdo() << ',' << general.getServiceRemind() << ',' << general.getLanguage() << ',' << general.getTimeZone() << endl;
            });
            file.close();
        }
    } catch (exception& e) {
        cout << e.what() << endl;
    }
}
