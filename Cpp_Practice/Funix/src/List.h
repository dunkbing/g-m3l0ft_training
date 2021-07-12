#ifndef CAR_H
#define CAR_H
#include <vector>
#include "General.h"
#include "CommonInfo.h"
#include "Utils.h"

using namespace std;

class SettingList {
public:
    // Default constructor and destructor
    SettingList();
    ~SettingList();

    void inputSettings();
    template<class T = Setting>
    void inputSettings(vector<Setting*>& settings);
    void outputSettings();

    void outputSoundSettings();
    void outputGeneralSettings();
    void outputDisplaySettings();
    void outputAllSettings();

    void inputSoundSettings();
    void inputGeneralSettings();
    void inputDisplaySettings();

    // load infos
    void loadSettings();
    

    static void sort(vector<Setting*> settings);
private:
    vector<Setting*> _displays;
    vector<Setting*> _sounds;
    vector<Setting*> _generals;
};

template <class T>
void SettingList::inputSettings(vector<Setting*>& settings)
{
    char continues = 'n';
    do {
        const int index = Utils::getInt(1, 100, "Car index: ") - 1;
        if (settings[index] == nullptr) {
            settings[index] = new T();
        }
        settings[index]->inputInfo();
        cout << "Will you input for Car " << index + 1 << " ? (y/n): ";
        cin >> continues;
        cin.ignore();
        cout << endl;
    } while (continues == 'y');
    sort(settings);
}


#endif // CAR_H
