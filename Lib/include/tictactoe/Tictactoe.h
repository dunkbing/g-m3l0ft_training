#pragma once
#include <vector>

class Tictactoe {
public:
    Tictactoe();
    ~Tictactoe();
    int Start();
    static void DisplayBoard(std::vector<char>& p_pieces);
    void Play();
    void Replay();
    void TurnPlay(char piece);
    bool Save();

private:
    char* player1;
    char* player2;
    std::vector<char> pieces;
    bool running;
    int turn;
    std::vector<std::vector<char>> steps;

    bool CheckWin();
    // void AddStep(const std::vector<char>& step);
};

