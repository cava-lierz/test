cmake_minimum_required(VERSION 3.16)
project(embedding_service)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 设置编译选项
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -march=native")
set(CMAKE_CXX_FLAGS_DEBUG "${CMAKE_CXX_FLAGS_DEBUG} -g -O0")

# 添加第三方库路径
include_directories(third_party)

# 添加llama.cpp子模块
add_subdirectory(third_party/llama.cpp)

# 添加embedding库
add_subdirectory(embedding)

# 创建embedding server可执行文件
add_executable(embedding_server embedding_server.cc)

# 链接库
target_link_libraries(embedding_server 
    embedding
    llama
    ${CMAKE_THREAD_LIBS_INIT}
)

# 在Linux上链接pthread
if(UNIX AND NOT APPLE)
    target_link_libraries(embedding_server pthread)
endif()

# 设置输出目录
set_target_properties(embedding_server PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin
)

# 安装规则
install(TARGETS embedding_server
    RUNTIME DESTINATION bin
)

# 打印配置信息
message(STATUS "Build type: ${CMAKE_BUILD_TYPE}")
message(STATUS "C++ standard: ${CMAKE_CXX_STANDARD}")
message(STATUS "Compiler: ${CMAKE_CXX_COMPILER_ID} ${CMAKE_CXX_COMPILER_VERSION}") 