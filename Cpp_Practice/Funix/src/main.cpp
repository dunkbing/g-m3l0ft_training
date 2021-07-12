#include <iostream>
#include <fstream>

#include "List.h"
#include "Utils.h"

using namespace Utils;

int menu();

/* run this program using the console pause or add your own getch, system("pause") or input loop */
int main() {
    // detect memory leak
    _CrtSetDbgFlag(_CRTDBG_ALLOC_MEM_DF | _CRTDBG_LEAK_CHECK_DF);
    try
    {
        SettingList list;
        // load languages and timezones data
        General::loadLanguages();
        General::loadTimeZones();
        list.loadSettings();

        while (true) {
            const int selection = menu();
            switch (selection) {
            case 1:
                list.inputSettings();
                break;
            case 2:
                list.outputSettings();
                break;
            case 3:
                exit(0);
            default:
                break;
            }
        }
    }
    catch (exception& e)
    {
        cout << e.what() << endl;
    }
}

// Print out menu for selection
int menu() {
    system("cls");
    cout << "--- SELECT MENU ---" << endl;
    cout << "1. Input setting information" << endl;
    cout << "2. Print setting information" << endl;
    cout << "3. Exit" << endl;
    cout << "Your selection: ";
    return getInt(1, 3);
}
