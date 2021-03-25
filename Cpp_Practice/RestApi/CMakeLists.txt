project(RestApi)
FILE(
    GLOB rest_src "src/*.h" "src/*.cpp"
)

add_executable(
    RestApi ${rest_src}
)

target_link_libraries(RestApi PRIVATE Lib)