#pragma once
#include <iostream>
#include <string>

#define CLS_SCR system("cls")
#define QUIT system("exit")
constexpr auto WELCOME_MSG = "Welcome To Tic-tac-toe game! Play with your way!\nIf you find any problem, please contact Trainers by mailing list SEAsia - ProgrammersTraining@gameloft.com\nSELECT YOUR MODE (1 - PLAY GAME, 2 - REPLAY GAME, OTHERS - EXIT GAME): ";
constexpr int ROW = 3;
constexpr int COL = 3;
constexpr int BOARD_SIZE = ROW * COL;
constexpr int NAME_SIZE = 30;

namespace Utils {
    inline bool IsInt(const std::string& s) {
        std::string::const_iterator it = s.begin();
        while (it != s.end() && std::isdigit(*it)) ++it;
        return !s.empty() && it == s.end();
    }

    /// <summary>
    /// get and validate integer value from input
    /// </summary>
    /// <param name="min">min value</param>
    /// <param name="max">max value</param>
    /// <param name="message">message display</param>
    /// <returns>valid integer value</returns>
    static int GetInt(int min, int max, const std::string& message = "") {
        while (true) {
            std::cout << message;
            std::string temp;
            std::getline(std::cin, temp);
            if (!temp.empty() && (IsInt(temp) || (IsInt(temp.substr(1, temp.length() - 1)) && temp[0] == '-'))) {
                const int result = std::stoi(temp, nullptr, 10);
                if (result > max || result < min) {
                    std::cout << "PLEASE ENTER AN INTEGER IN THE RANGE FROM " << min << " TO " << max << std::endl;
                    continue;
                }
                return result;
            }
            else {
                std::cout << "PLEASE ENTER AN INTEGER" << std::endl;
            }
        }
    }

    static void GetLine(char* str, size_t size, const std::string& message = "") {
        std::cout << message;
        std::cin.get(str, size);
        std::cin.ignore();
    }
};