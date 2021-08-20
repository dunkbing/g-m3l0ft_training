#ifndef CAR_H
#define CAR_H
#include <vector>
#include <set>
#include <algorithm>
#include <functional>

class SettingManager final {
public:
    // Default constructor and destructor
    SettingManager();
    ~SettingManager();

    // print a menu for selecting types of input
    void inputSettings();
    
    template<typename T = Setting>
    /// <summary>
    /// input a setting
    /// </summary>
    /// <typeparam name="T">type of Setting(Display, Sound, General)</typeparam>
    /// <param name="settings">container of the setting</param>
    /// <param name="keys">contain all of the settings personal key</param>
    /// <param name="callback">what to do after input the setting</param>
    void inputSettings(std::vector<Setting*>& settings, std::set<std::string>& keys, std::function<void(Setting*)> callback);
    
    template<typename T = Setting>
    // copy a setting's value to another in a different container
    static void copyTo(std::vector<Setting*>& settings, Setting* setting);

    // print a menu for select types of output
    void outputSettings();
    void outputDisplaySettings();
    void outputSoundSettings();
    void outputGeneralSettings();
    void outputAllSettings();

    template<class T = Setting>
    /// <summary>
    /// return a setting from a container with specific key
    /// </summary>
    /// <typeparam name="T">type of setting</typeparam>
    /// <param name="settings">container of the setting</param>
    /// <param name="key">key of the setting</param>
    /// <returns>a setting in the container, null if not found</returns>
    static T* getSetting(std::vector<Setting*>& settings , const std::string& key);

    // load infos
    void loadSettings();
    // save infos
    void saveSettings();

    enum class SortType
    {
        Name = 1, Mscn = 2,
    };
    // print a menu for choosing sort type.
    // return sort type
    SortType chooseSortType();
    // sort a setting container with type(name or mscn)
    static void sort(std::vector<Setting*>& settings, SortType type);
    
    void release();
private:
    std::vector<Setting*> _displays;
    std::vector<Setting*> _sounds;
    std::vector<Setting*> _generals;

    std::set<std::string> _keys;
};

template <typename T>
void SettingManager::inputSettings(std::vector<Setting*>& settings, std::set<std::string>& keys, std::function<void(Setting*)> callback)
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
        auto* existedSetting = getSetting<T>(settings, key);
        if (existedSetting != nullptr)
        {
            *existedSetting = *(T*)s;
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

template <typename T>
void SettingManager::copyTo(std::vector<Setting*>& settings, Setting* setting)
{
    auto* s = getSetting(settings, setting->getPersonalKey());
    
    if (s != nullptr)
    {
        s->setPersonalKey(setting->getPersonalKey());
        s->setCarName(setting->getCarName());
        s->setEmail(setting->getEmail());
        s->setOdo(setting->getOdo());
        s->setServiceRemind(setting->getServiceRemind());
    }
    else
    {
        s = new T();
        s->setPersonalKey(setting->getPersonalKey());
        s->setCarName(setting->getCarName());
        s->setEmail(setting->getEmail());
        s->setOdo(setting->getOdo());
        s->setServiceRemind(setting->getServiceRemind());
        settings.push_back(s);
    }

    sort(settings);
}

template <class T>
T* SettingManager::getSetting(std::vector<Setting*>& settings, const std::string& key)
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
