project(Library)
add_library(
    Lib
    include/tictactoe/Game.h
    include/tictactoe/Game.cpp
    include/Globals.h
)

target_include_directories(Lib PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}/include")