#include <cstdio>
#include <iostream>
#include <fstream>
#include "Game.h"
#include "Globals.h"

void Game::DisplayBoard(std::vector<char>& p_pieces) {
    for (size_t i = 0; i < BOARD_SIZE; i++) {
        printf("%c ", p_pieces.at(i));
        if ((i + 1) % 3 == 0) {
            printf("\n");
        }
    }
}

void Game::Play() {
    running = true;
    Utils::GetLine(player1, NAME_SIZE, "Enter Player 1 Name: ");
    Utils::GetLine(player2, NAME_SIZE, "Enter Player 2 Name: ");

    CLS_SCR;
    while (running) {
        DisplayBoard(pieces);
        if (turn == 1) {
            printf("Your Turn %s - O: ", player1);
            TurnPlay('O');
        } else {
            printf("Your turn %s - X: ", player2);
            TurnPlay('X');
        }
        CLS_SCR;
    }
    DisplayBoard(pieces);
    printf("%s won\n", turn == 1 ? player1 : player2);
    Save();
}

// saving game steps to binary file
bool Game::Save() {
    std::ofstream ofs("save.bin", std::ofstream::binary | std::ofstream::out);
    if (ofs.fail()) {
        return false;
    }
    size_t s1 = steps.size();
    ofs.write(reinterpret_cast<char*>(&s1), sizeof(s1));
    for (auto step : steps) {
        size_t size = step.size();
        ofs.write(reinterpret_cast<char*>(&size), sizeof(size));
        ofs.write(&step[0], size * sizeof(char));
    }
    ofs.close();
    return true;
}

// load games' steps from binary file
void Game::Replay() {
    std::ifstream ifs("save.bin", std::fstream::binary);
    if (ifs.fail()) {
        printf("failed to load replay");
        return;
    }
    steps.clear();
    size_t size1 = 0;
    ifs.read(reinterpret_cast<char*>(&size1), sizeof(size1));
    steps.resize(size1);
    for (size_t i = 0; i < size1; ++i) {
        size_t size2 = 0;
        ifs.read(reinterpret_cast<char*>(&size2), sizeof(size2));
        char f;
        for (size_t j = 0; j < size2; ++j) {
            ifs.read(&f, sizeof(f));
            steps[i].push_back(f);
        }
    }
    ifs.close();
    if (steps.empty()) {
        printf("no data");
        return;
    }
    for (auto step : steps) {
        DisplayBoard(step);
        printf("\n\n");
    }
}

// players take their turn
void Game::TurnPlay(char piece) {
    while (true) {
        size_t userIndex = Utils::GetInt(11, 33);
        const size_t index = (userIndex / 10 - 1) * 3 + userIndex % 10 - 1;
        if (index < pieces.size() && pieces[index] != 'T') {
            printf("invalid move: ");
            userIndex = Utils::GetInt(11, 33);
            continue;
        }
        pieces[index] = piece;
        steps.emplace_back(pieces);
        // AddStep(pieces);
        running = !CheckWin();
        if (running) {
            turn = turn == 1 ? 2 : 1;
        }
        break;
    }
        
}

// checking for win conditions
bool Game::CheckWin() {
    // cols
    for (size_t i = 0; i < ROW; i++) {
        if (pieces[i] == pieces[i + ROW] && pieces[i] == pieces[i + ROW * 2] && pieces[i] != 'T') {
            return true;
        }
    }
    // rows
    for (size_t i = 0; i < BOARD_SIZE; i += COL) {
        if (pieces[i] == pieces[i + 1] && pieces[i] == pieces[i + 2] && pieces[i] != 'T') {
            return true;
        }
    }
    // diagonals
    if (pieces[0] == pieces[4] && pieces[0] == pieces[8] && pieces[0] != 'T') {
        return true;
    }
    if (pieces[2] == pieces[4] && pieces[2] == pieces[6] && pieces[2] != 'T') {
        return true;
    }
    return false;
}

// init stuffs
Game::Game() {
    pieces.assign(BOARD_SIZE, 'T');
    running = false;
    player1 = new char[NAME_SIZE];
    player2 = new char[NAME_SIZE];
    turn = 1;
}

// release resources
Game::~Game() {
    delete player1;
    delete player2;
};

// start the games
int Game::Start() {
    printf(WELCOME_MSG);
    const int choice = Utils::GetInt(1, 3);
    fflush(stdin);
    switch (choice) {
    case 1:
        Play();
        break;
    case 2:
        Replay();
        break;
    default:
        exit(EXIT_SUCCESS);
    }
    return choice;
}
