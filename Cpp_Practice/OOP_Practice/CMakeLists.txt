project(OOP_Practice)
FILE(
    GLOB oop_src "src/*.h" "src/*.cpp"
)

add_executable(
    OOP_Practice ${oop_src}
)

target_link_libraries(OOP_Practice PRIVATE Lib)