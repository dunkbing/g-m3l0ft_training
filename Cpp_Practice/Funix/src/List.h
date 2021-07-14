#ifndef CAR_H
#define CAR_H
#include <vector>
#include <set>
#include <algorithm>

class SettingList final {
public:
    // Default constructor and destructor
    SettingList();
    ~SettingList();

    void inputSettings();
    template<class T = Setting>
    static int inputSettings(std::vector<Setting*>& settings, std::set<std::string>& keys);
    template<class T = Setting>
    static void copyTo(std::vector<Setting*>& settings, int index, Setting* setting);
    void outputSettings();

    void outputSoundSettings();
    void outputGeneralSettings();
    void outputDisplaySettings();
    void outputAllSettings();

    // load infos
    void loadSettings();

    void release();

    static void sort(std::vector<Setting*> settings);
private:
    std::vector<Setting*> _displays;
    std::vector<Setting*> _sounds;
    std::vector<Setting*> _generals;

    std::set<std::string> _keys;
};

template <class T>
int SettingList::inputSettings(std::vector<Setting*>& settings, std::set<std::string>& keys)
{
    char continues = 'n';
    const int index = Utils::getInt(1, 100, "Car index: ") - 1;
    do {
        if (settings[index] == nullptr) {
            settings[index] = new T();
        }
        settings[index]->inputInfo();
        std::string key = settings[index]->getPersonalKey();
        keys.insert(key);
        std::cout << "Will you input for Car " << index + 1 << " ? (y/n): ";
        std::cin >> continues;
        std::cin.ignore();
        std::cout << std::endl;
    } while (continues == 'y');
    std::cout << "Saving-----" << std::endl;
    sort(settings);
    return index;
}

template <class T>
void SettingList::copyTo(std::vector<Setting*>& settings, int index, Setting* setting)
{
    const auto it = std::find_if(settings.begin(), settings.end(), [setting](Setting* s)->bool {
        if (s == nullptr) return false;
        return s->getPersonalKey() == setting->getPersonalKey();
        });
    if (it != settings.end()) {
        *it = setting;
    }
    else {
        Setting s = *setting;
        T* t = new T(s);
        settings[index] = t;
    }
    sort(settings);
}

#endif // CAR_H
