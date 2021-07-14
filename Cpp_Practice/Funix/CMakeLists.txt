project(Funix)
cmake_minimum_required(VERSION 3.1)
set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_STANDARD_REQUIRED True)
FILE(GLOB funix_src "src/*.h" "src/*.cpp")

FILE(COPY languages.txt timezones.txt DESTINATION "${CMAKE_BINARY_DIR}")

add_executable(Funix ${funix_src})
