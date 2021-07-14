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
    static Setting* inputSettings(std::vector<Setting*>& settings, std::set<std::string>& keys);
    static void copyTo(std::vector<Setting*>& settings, Setting* setting);
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
Setting* SettingList::inputSettings(std::vector<Setting*>& settings, std::set<std::string>& keys)
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
    return settings[index];
}

#endif // CAR_H
