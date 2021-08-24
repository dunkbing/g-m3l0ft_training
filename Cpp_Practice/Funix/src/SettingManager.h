#ifndef CAR_H
#define CAR_H
#include <vector>
#include <set>
#include <algorithm>
#include <functional>
#include "BinaryTree.h"
#include "Display.h"
#include "Sound.h"
#include "General.h"
#include "Utils.h"

class SettingManager final {
public:
    // Default constructor and destructor
    SettingManager();
    ~SettingManager();

    // print a menu for selecting types of input
    void inputSettingsMenu();
    
    /// <summary>
    /// input a setting
    /// </summary>
    /// <typeparam name="T">type of Setting(Display, Sound, General)</typeparam>
    /// <param name="settings">container of the setting</param>
    /// <param name="keys">contain all of the settings personal key</param>
    /// <param name="callback">what to do after input the setting</param>
    template<class T = Setting> static void inputSettings(BTree<T>& settings, std::set<std::string>& keys, const std::function<void(Setting*)>& callback);
    
    // copy a setting's value to another in a different container
    template<class T = Setting> static void copyTo(BTree<T>& settings, Setting* setting);

    void outputSettingsMenu();
    // print a menu for select types of output
    template<class T = Setting> void outputSettings(BTree<T>& settings);
    void outputAllSettings();

    /// <summary>
    /// return a setting from a container with specific key
    /// </summary>
    /// <typeparam name="T">type of setting</typeparam>
    /// <param name="settings">container of the setting</param>
    /// <param name="key">key of the setting</param>
    /// <returns>a setting in the container, null if not found</returns>
    template<class T = Setting>
    static T* getSetting(BTree<T>& settings , const std::string& key);

    // load infos
    void loadSettings();
    // save infos
    void saveSettings();

private:
    BTree<Display> _displays;
    BTree<Sound> _sounds;
    BTree<General> _generals;

    std::set<std::string> _keys;
};

template<class T>
inline void SettingManager::inputSettings(
        BTree<T>& settings,
        std::set<std::string>& keys,
        const std::function<void(Setting*)>& callback)
{
    char c = 'n';
    do
    {
        const int index = Utils::getInt(1, 100, "Car index: ") - 1;
        T* s = new T();
        s->inputInfo(keys);
        callback(dynamic_cast<Setting*>(s));
        string key = s->getPersonalKey();
        settings.insert(*s);
        keys.insert(key);
        std::cout << "Will you input for Car " << index + 1 << " ? (y/n): ";
        std::cin >> c;
        std::cin.ignore();
        std::cout << std::endl;
    } while (c == 'y');
    std::cout << "Saving-----" << std::endl;
}

template<class T>
inline void SettingManager::outputSettings(BTree<T>& settings)
{
    T::outputInfoLabel();
    settings.inOrder([](T t)
    {
        t.outputInfo();
    });
    cin.get();
}

template<class T>
inline void SettingManager::copyTo(BTree<T>& settings, Setting* setting)
{
    T* s = getSetting<T>(settings, setting->getPersonalKey());

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
        settings.insert(*dynamic_cast<T*>(s));
    }
}

template <typename T>
T* SettingManager::getSetting(BTree<T>& settings, const std::string& key)
{
    Setting* st = nullptr;
    settings.preOrder([&st, &key](T& s) {
        if (s.getPersonalKey() == key)
        {
            st = dynamic_cast<Setting*>(&s);
        }
    });
    return dynamic_cast<T*>(st);
}

#endif // CAR_H
