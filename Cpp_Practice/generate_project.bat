if exist build (
    cd build && cmake ..
) else (
    mkdir build
    cd build
    cmake ..
)
PAUSE