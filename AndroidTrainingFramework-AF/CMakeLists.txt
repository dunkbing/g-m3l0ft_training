project(AndroidTrainingFramework)

FILE(
    GLOB glob_src
    "src/*.h" "src/*.cpp"
    "src/entity/*.h" "src/entity/*.cpp"
    "src/platform/win32/*.h" "src/platform/win32/*.cpp"
    "src/platform/win32/render/*.h" "src/platform/win32/render/*.cpp"
)

# include_directories(${WinSDK})
include_directories(src)

# set character set to unicode
if(COMPILER_MSVC)
    add_definitions(-DUNICODE -D_UNICODE)
endif()
SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} /UMBCS /D_UNICODE /DUNICODE")

add_executable(
    AndroidTrainingFramework WIN32 ${glob_src}
)

target_link_libraries(AndroidTrainingFramework PRIVATE Lib)
#target_link_libraries(AndroidTrainingFramework ${WinSDK})