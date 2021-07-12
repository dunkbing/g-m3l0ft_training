#ifndef UTILS_H
#define UTILS_H
#include <iostream>
#include <string>
#include <algorithm>
#include <regex>

// message constants
constexpr auto LOGIN_MESSAGE = "NHAP MA SO CA NHAN: ";
constexpr auto PARK_MESSAGE_P = "DA VE SO P.\nCHU Y SU DUNG PHANH TAY DE DAM BAO AN TOAN\n";
constexpr auto PARK_MESSAGE_N = "DA VE SO N.\nCHU Y SU DUNG PHANH TAY DE DAM BAO AN TOAN\n";
constexpr auto PARK_FAIL_MSG = "HAY CHAC CHAN XE DA DUNG VA VAN TOC LA 0 KM/H\n";
constexpr auto SPEED_WARNING = "TOC DO NGUY HIEM, BAN NEN GIAM TOC DO\n";

namespace Utils
{
    using namespace std;

    // main menu options
    // string options[8] = { "P", "R", "N", "D", "POWER OFF", "RESET ID" };

    // clear screen
    inline void clearScr()
    {
#if defined(_WIN32)
        system("cls");
#elif defined(linux)
        system("clear");
#endif
    }

    // check if string contains only number
    inline bool isInt(const std::string& s) {
        auto it = s.begin();
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
    inline int getInt(const int min, const int max, const string& message = "") {
        while (true) {
            cout << message;
            std::string tempStr;
            std::getline(std::cin, tempStr);
            if (isInt(tempStr) || (!tempStr.empty() && isInt(tempStr.substr(1, tempStr.length() - 1)) && tempStr[0] == '-')) {
                const int result = std::stoi(tempStr, nullptr, 10);
                if (result > max || result < min) {
                    cout << "PLEASE ENTER AN INTEGER IN THE RANGE FROM " << min << " TO " << max << endl;
                    continue;
                }
                return result;
            }
            else {
                std::cout << "PLEASE ENTER AN INTEGER" << endl;
            }
        }
    }

    /// <summary>
    /// split string into an array of strings.
    /// </summary>
    /// <param name="s">string to be split</param>
    /// <param name="c"></param>
    /// <returns></returns>
    /*inline vector<string> explode(const string& s, const char& c) {
        string buff;
        vector<string> v;

        for (auto n : s) {
            if (n != c)
                buff += n;
            else if (n == c && !buff.empty()) {
                v.push_back(buff);
                buff = "";
            }
        }
        if (!buff.empty())
            v.push_back(buff);

        return v;
    }*/

    /// <summary>
    /// split string into an array of strings by a regex.
    /// </summary>
    /// <param name="stringToSplit"></param>
    /// <param name="reg"></param>
    /// <returns></returns>
    inline std::vector<std::string> splitString(const std::string& stringToSplit, const std::string& reg) {
        std::vector<std::string> result;
        const std::regex rgx(reg);
        std::sregex_token_iterator iterator(stringToSplit.begin(), stringToSplit.end(), rgx, -1);
        for (const std::sregex_token_iterator end; iterator != end; ++iterator) {
            result.push_back(iterator->str());
        }
        return result;
    }

    inline string getLine(const string& message = "", const string& regex = "", const string& errMsg = "") {
        const std::regex pattern(regex);
        cout << message;
        string result;
        std::getline(std::cin, result);
        if (regex.empty()) {
            return result;
        }
        while (!std::regex_match(result, pattern)) {
            cout << errMsg;
            std::getline(std::cin, result);
        }
        return result;
    }

    // print elements of array to console
    inline void printArray(const string& id) {
        for (char i : id)
        {
            cout << i << " ";
        }
        cout << endl;
    }

    /// <summary>
    /// sort an array using bubble sort algorithm
    /// </summary>
    /// <param name="arr">arr to be sorted</param>
    inline void bubbleSort(string& arr) {
        int count = 0;
        for (int i = 0; i < arr.size() - 1; i++) {
            for (int j = i + 1; j < arr.size(); j++) {
                if (arr[i] > arr[j]) {
                    swap(arr[i], arr[j]);
                    count += 3; // swap takes 3 steps
                }
            }
        }
        cout << "bubble sort takes " << count << " steps" << endl;
    }

    /// <summary>
    /// sort an array using selection sort algorithm
    /// </summary>
    /// <param name="arr">arr to be sorted</param>
    inline void selectionSort(string& arr) {
        int count = 0;
        for (auto i = 0; i < arr.size() - 1; i++) {
            int minIndex = i;
            count++;
            for (int j = i + 1; j < arr.size(); j++) {
                if (arr[j] < arr[minIndex]) {
                    minIndex = j;
                    count++;
                }
            }
            swap(arr[i], arr[minIndex]);
            count += 3; // swap take 3 steps
        }
        cout << "selection sort takes " << count << " steps" << endl;
    }

    // select a car
    inline int selectCar() {
        cout << "1. XE SO 1" << endl;
        cout << "2. XE SO 2" << endl;
        const int option = getInt(1, 2, "choose an option (1 or 2)");
        return option;
    }
}
#endif // UTILS_H
