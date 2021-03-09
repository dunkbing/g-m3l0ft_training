project(Library)

FILE(GLOB tictactoe "include/tictactoe/*.h" "include/tictactoe/*.cpp")
FILE(GLOB math "include/math/*.h" "include/math/*.cpp")

add_library(
    Lib
    ${tictactoe}
    ${math}
    include/Globals.h
)

target_include_directories(Lib PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}/include")