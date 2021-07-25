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
    static void inputSettings(std::vector<Setting*>& settings, std::set<std::string>& keys);
    template<class T = Setting>
    static void copyTo(std::vector<Setting*>& settings, Setting* setting);
    void outputSettings();

    void outputSoundSettings();
    void outputGeneralSettings();
    void outputDisplaySettings();
    void outputAllSettings();

    template<class T = Setting>
    static T* getSetting(std::vector<Setting*>& settings , const std::string& key);

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
void SettingList::inputSettings(std::vector<Setting*>& settings, std::set<std::string>& keys)
{
    char c = 'n';
    const int index = Utils::getInt(1, 100, "Car index: ") - 1;
    Setting* s = new T();
    do {
        s->inputInfo();
        settings[index] = s;
        std::string key = s->getPersonalKey();
        keys.insert(key);
        std::cout << "Will you input for Car " << index + 1 << " ? (y/n): ";
        std::cin >> c;
        std::cin.ignore();
        std::cout << std::endl;
    } while (c == 'y');
    std::cout << "Saving-----" << std::endl;
    sort(settings);
}

template <class T>
void SettingList::copyTo(std::vector<Setting*>& settings, Setting* setting)
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
        settings.push_back(t);
    }
    sort(settings);
}

template <class T>
T* SettingList::getSetting(std::vector<Setting*>& settings, const std::string& key)
{
    const auto it = std::find_if(settings.begin(), settings.end(), [key](Setting* s)
        {
            if (s == nullptr)
            {
                return false;
            }
            return s->getPersonalKey() == key;
        });
    if (it != settings.end())
    {
        return dynamic_cast<T*>(*it);
    }
    return nullptr;
}


#endif // CAR_H
