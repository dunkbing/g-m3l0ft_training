#define _CRTDBG_MAP_ALLOC
#include <stdlib.h>
#include <crtdbg.h>
#include <iostream>
#include <fstream>

#include "General.h"
#include "Utils.h"
#include "SettingManager.h"

using namespace Utils;

int menu();

/* run this program using the console pause or add your own getch, system("pause") or input loop */
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
                manager.inputSettings();
                break;
            case 2:
                manager.outputSettings();
                break;
            case 3:
                manager.release();
                cout << "bye" << endl;
                exit = true;
                break;
            default:
                break;
            }
        }
        General::release();
        manager.release();
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
