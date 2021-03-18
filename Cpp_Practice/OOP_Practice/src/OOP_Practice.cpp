// OOP_Practice.cpp : This file contains the 'main' function. Program execution begins and ends there.
//

#include <iostream>
#include <crtdbg.h>
#include <Globals.h>
#include "Patient.h"

int main() {
    Patient* p = new Patient;
    while (p->GetState() == ALIVE) {
        if (p->TotalVirusesResist() <= 0) {
            std::cout << "viruses are dead" << std::endl;
            std::cout << *p;
            p->Release();
            SAFE_DEL(p);
            // _CrtMemState s1;
            // _CrtMemCheckpoint(&s1);
            // OutputDebugString("dump memory leak\n");
            _CrtDumpMemoryLeaks();
            // OutputDebugString("dump statistic\n");
            // _CrtMemDumpStatistics(&s1);
            return 0;
        }
        std::cout << *p;
        printf("Take Medicine (0 = NO, 1 = YES): ");
        const int t = Utils::GetInt(0, 1);
        if (t == 1) {
            const int min = 1;
            const int max = 60;
            const int medicine_resistance = min + (rand() % (max - min + 1));
            p->TakeMedicine(medicine_resistance);
        }
    }
}

// Run program: Ctrl + F5 or Debug > Start Without Debugging menu
// Debug program: F5 or Debug > Start Debugging menu

// Tips for Getting Started: 
//   1. Use the Solution Explorer window to add/manage files
//   2. Use the Team Explorer window to connect to source control
//   3. Use the Output window to see build output and other messages
//   4. Use the Error List window to view errors
//   5. Go to Project > Add New Item to create new code files, or Project > Add Existing Item to add existing code files to the project
//   6. In the future, to open this project again, go to File > Open > Project and select the .sln file
