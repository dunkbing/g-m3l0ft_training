#pragma once

#define CLS_SCR system("cls")
#define QUIT system("exit")
constexpr auto WELCOME_MSG = "Welcome To Tic-tac-toe game! Play with your way!\nIf you find any problem, please contact Trainers by mailing list SEAsia - ProgrammersTraining@gameloft.com\nSELECT YOUR MODE (1 - PLAY GAME, 2 - REPLAY GAME, OTHERS - EXIT GAME): ";
constexpr int ROW = 3;
constexpr int COL = 3;
constexpr int BOARD_SIZE = ROW * COL;
constexpr int NAME_SIZE = 30;

static int GetInt(int min, int max) {
    int input;
    int temp, check = scanf_s("%d%c", &input);
    fflush(stdin);
    while (check != 2) {
        if (input < min || input > max) {
            while ((temp = getchar()) != EOF && temp != '\n');
            printf("invalid move: ");
            check = scanf_s("%d%c", &input);
            fflush(stdin);
        }
    }
    return input;
}