#define _CRTDBG_MAP_ALLOC
#include <iostream>
#include <fstream>

#include "General.h"
#include "Utils.h"
#include "List.h"

using namespace Utils;

int menu();

/* run this program using the console pause or add your own getch, system("pause") or input loop */
int main() {
    // detect memory leak
#if _WIN32
    _CrtSetDbgFlag(_CRTDBG_ALLOC_MEM_DF | _CRTDBG_LEAK_CHECK_DF);
    _CrtSetReportMode(_CRT_WARN, _CRTDBG_MODE_DEBUG);
#endif
    try
    {
        SettingList list;
        // load languages and timezones data
        General::loadLanguages();
        General::loadTimeZones();
        bool exit = false;
        while (!exit) {
            const int selection = menu();
            switch (selection) {
            case 1:
                list.inputSettings();
                break;
            case 2:
                list.outputSettings();
                break;
            case 3:
                list.release();
                exit = true;
                break;
            default:
                break;
            }
        }
        list.release();
    }
    catch (exception& e)
    {
        cout << e.what() << endl;
    }
#if _WIN32
    _CrtDumpMemoryLeaks();
#endif
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
