#ifndef __DEFINE_H__
#define __DEFINE_H__

#define SCREEN_W			800
#define SCREEN_H			480
#define SCREEN_CENTER_W		(800/2)
#define SCREEN_CENTER_H		(480/2)
#define LIMIT_FPS			30

// define variable
#define MAX_LENGTH			255

// Macro
#define PI					3.14159265f
#define HALFPI				(PI/2.0f)
#define SAFE_DEL(x)			{if(x){delete x; x = NULL;}}
#define X2GAME(x)			((1.0f*x - SCREEN_CENTER_W)/SCREEN_CENTER_W)
#define Y2GAME(y)			(-1.0f*(1.0f*y - SCREEN_CENTER_H)/SCREEN_CENTER_H)
#include <iostream>
#include <sstream>
#include <vector>


/**
 * \brief extract integers from a string
 * \param str 
 * \return a vector of extracted integers
 */
static std::vector<int> IntsFromStr(std::string& str) {
    std::vector<int> result;
    std::stringstream ss;

    ss << str;

    /* Running loop till the end of the stream */
    std::string temp;
    int found;
    while (!ss.eof()) {

        /* extracting word by word from stream */
        ss >> temp;

        /* Checking the given word is integer or not */
        if (std::stringstream(temp) >> found) {
            result.push_back(found);
        }

        /* To save from space at the end of string */
        temp = "";
    }
    return result;
}


#endif