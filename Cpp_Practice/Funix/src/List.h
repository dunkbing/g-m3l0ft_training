#ifndef CAR_H
#define CAR_H
#include <vector>

class SettingList final {
public:
    // Default constructor and destructor
    SettingList();
    ~SettingList();

    void inputSettings();
    template<class T = Setting>
    static void inputSettings(std::vector<Setting*>& settings);
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
};

template <class T>
void SettingList::inputSettings(std::vector<Setting*>& settings)
{
    char continues = 'n';
    do {
        const int index = Utils::getInt(1, 100, "Car index: ") - 1;
        if (settings[index] == nullptr) {
            settings[index] = new T();
        }
        settings[index]->inputInfo();
        std::cout << "Will you input for Car " << index + 1 << " ? (y/n): ";
        std::cin >> continues;
        std::cin.ignore();
        std::cout << std::endl;
    } while (continues == 'y');
    std::cout << "Saving-----" << std::endl;
    sort(settings);
}

#endif // CAR_H
