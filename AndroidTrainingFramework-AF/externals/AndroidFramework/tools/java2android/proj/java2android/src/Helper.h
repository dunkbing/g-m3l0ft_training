#pragma once

#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>

std::string toLower(std::string str);

void errorFunction(std::string msg, int val = EXIT_FAILURE);
void errorContinueFunction(std::string msg);
void giveWarningFunction(std::string msg);
inline void noWarningFunction(std::string msg);
extern void(*warningFunction)(std::string msg);
void enableWarnings();
void disableWarnings();

std::string putInQuotes(std::string val);

void printInQuotes(std::string val);

void printDBG(std::string msg);

void extractPackage(const std::string &content, std::vector<std::string> &package, bool &errorOccurred, std::string &errorMsg);
void extractPackageFromFile(const std::string &filename, std::vector<std::string> &package, bool &errorOccurred, std::string &errorMsg);
