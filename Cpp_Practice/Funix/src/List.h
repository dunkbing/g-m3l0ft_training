#ifndef CAR_H
#define CAR_H
#include <vector>
#include <set>
#include <algorithm>
#include <functional>

class SettingList final {
public:
    // Default constructor and destructor
    SettingList();
    ~SettingList();

    void inputSettings();
    template<class T = Setting>
    void inputSettings(std::vector<Setting*>& settings, std::set<std::string>& keys, std::function<void(Setting*)> callback);
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
    // save infos
    void saveSettings();

    void release();

    static void sort(std::vector<Setting*>& settings);
private:
    std::vector<Setting*> _displays;
    std::vector<Setting*> _sounds;
    std::vector<Setting*> _generals;

    std::set<std::string> _keys;
};

template <class T>
void SettingList::inputSettings(std::vector<Setting*>& settings, std::set<std::string>& keys, std::function<void(Setting*)> callback)
{
    char c = 'n';
    do {
        const int index = Utils::getInt(1, 100, "Car index: ") - 1;
        Setting* s = new T();
        std::vector<Setting*>::iterator it = settings.begin();
        while (*it != nullptr)
        {
            it++;
        }
        s->inputInfo(keys);
        callback(s);
        string key = s->getPersonalKey();
        auto* existedSetting = getSetting(settings, key);
        if (existedSetting != nullptr)
        {
            copyTo(settings, s);
        } else
        {
            *it = s;
        }
        keys.insert(key);
        std::cout << "Will you input for Car " << index + 1 << " ? (y/n): ";
        std::cin >> c;
        std::cin.ignore();
        std::cout << std::endl;
    } while (c == 'y');
    std::cout << "Saving-----" << std::endl;
    sort(settings);
    saveSettings();
}

template <class T>
void SettingList::copyTo(std::vector<Setting*>& settings, Setting* setting)
{
    auto* s = getSetting(settings, setting->getPersonalKey());
    
    if (s != nullptr)
    {
        s->setCarName(setting->getCarName());
        s->setEmail(setting->getEmail());
        s->setOdo(setting->getOdo());
        s->setServiceRemind(setting->getServiceRemind());
    }
    else
    {
        s = new T();
        s->setCarName(setting->getCarName());
        s->setEmail(setting->getEmail());
        s->setOdo(setting->getOdo());
        s->setServiceRemind(setting->getServiceRemind());
        settings.push_back(s);
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
