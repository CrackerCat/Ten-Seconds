# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

############################################################
##  libcrypto
############################################################

add_library(crypto SHARED IMPORTED)

set_property(
        TARGET crypto
        PROPERTY
            IMPORTED_LOCATION
            ${PROJECT_SOURCE_DIR}/src/main/cpp/openssl/${CMAKE_ANDROID_ARCH_ABI}/libcrypto.so
)

############################################################
##  libssl
############################################################

add_library(ssl SHARED IMPORTED)

set_property(
        TARGET ssl
        PROPERTY
            IMPORTED_LOCATION
            ${PROJECT_SOURCE_DIR}/src/main/cpp/openssl/${CMAKE_ANDROID_ARCH_ABI}/libssl.so
)

############################################################
##  libcperciva
############################################################

add_library(cperciva SHARED
        src/main/cpp/cperciva/alg/sha256.c
        src/main/cpp/cperciva/cpusupport/cpusupport_x86_aesni.c
        src/main/cpp/cperciva/cpusupport/cpusupport_x86_sse2.c
        src/main/cpp/cperciva/crypto/crypto_aes.c
        src/main/cpp/cperciva/crypto/crypto_aesctr.c
        src/main/cpp/cperciva/crypto/crypto_entropy.c
        src/main/cpp/cperciva/util/asprintf.c
        src/main/cpp/cperciva/util/entropy.c
        src/main/cpp/cperciva/util/getopt.c
        src/main/cpp/cperciva/util/humansize.c
        src/main/cpp/cperciva/util/insecure_memzero.c
        src/main/cpp/cperciva/util/readpass.c
        src/main/cpp/cperciva/util/warnp.c
)

target_include_directories(cperciva PRIVATE
        src/main/cpp/include
        src/main/cpp/cperciva/alg
        src/main/cpp/cperciva/cpusupport
        src/main/cpp/cperciva/crypto
        src/main/cpp/cperciva/util
)

target_link_libraries(cperciva crypto ssl)

############################################################
##  libscrypt
############################################################

add_library(scrypt SHARED
        src/main/cpp/scrypt/crypto_scrypt.c
        src/main/cpp/scrypt/crypto_scrypt_smix.c
)

target_include_directories(scrypt PRIVATE
        src/main/cpp/cperciva/alg
        src/main/cpp/cperciva/cpusupport
        src/main/cpp/cperciva/crypto
        src/main/cpp/cperciva/util
)

target_link_libraries(scrypt cperciva)

target_compile_definitions(scrypt PRIVATE HAVE_CONFIG_H)

############################################################
##  libcrypto-engine
############################################################

add_library(crypto-engine SHARED
        src/main/cpp/crypto-engine.c
)

target_link_libraries(crypto-engine log scrypt)
