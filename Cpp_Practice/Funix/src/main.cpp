#include <iostream>
#include "General.h"
#include "Utils.h"
#include "SettingManager.h"

using namespace Utils;

int menu();

int main() {
    try
    {
        SettingManager manager;
        // load languages and timezones data
        General::loadLanguages();
        General::loadTimeZones();
        bool exit = false;
        while (!exit)
        {
            const int selection = menu();
            switch (selection)
            {
            case 1:
                manager.inputSettingsMenu();
                break;
            case 2:
                manager.outputSettingsMenu();
                break;
            case 3:
                manager.saveSettings();
                cout << "bye" << endl;
                exit = true;
                break;
            default:
                break;
            }
        }
        General::release();
    }
    catch (exception& e)
    {
        cout << e.what() << endl;
    }
}

// Print out menu for selection
int menu() {
    Utils::clearScr();
    cout << "--- SELECT MENU ---" << endl;
    cout << "1. Input setting information" << endl;
    cout << "2. Print setting information" << endl;
    cout << "3. Exit" << endl;
    cout << "Your selection: ";
    return getInt(1, 3);
}
