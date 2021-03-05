add_library(
    Lib
    include/Game.h
    include/Game.cpp
    include/Globals.h
)

target_include_directories(Lib PUBLIC "${CMAKE_CURRENT_SOURCE_DIR}/include")