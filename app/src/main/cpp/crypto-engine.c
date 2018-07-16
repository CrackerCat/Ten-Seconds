#include <jni.h>
#include <memory.h>
#include "scrypt/crypto_scrypt.h"

#define THROW(env, clazz, msg) do { (*env)->ThrowNew(env, (*env)->FindClass(env, clazz), msg); } while (0)
#define DERIVE_EXCEPTION "com/gh0u1l5/tenseconds/backend/crypto/MasterKey/DeriveException"

#define SCRYPT_N 32768
#define SCRYPT_R 8
#define SCRYPT_P 1

#define KEY_LENGTH 32

JNIEXPORT jbyteArray JNICALL
Java_com_gh0u1l5_tenseconds_backend_crypto_Scrypt_derive(
        JNIEnv* env,
        jobject obj,
        jcharArray passphrase,
        jbyteArray salt
) {
    // Read passphrase
    jsize passphrase_len = (*env)->GetArrayLength(env, passphrase);
    jchar *passphrase_data = (*env)->GetCharArrayElements(env, passphrase, JNI_FALSE);

    // Read salt
    jsize salt_len = (*env)->GetArrayLength(env, salt);
    jbyte *salt_data = (*env)->GetByteArrayElements(env, salt, JNI_FALSE);

    // Copy passphrase to uint8_t array
    size_t passphrase_size = passphrase_len * sizeof(jchar);
    uint8_t passphrase_buffer[passphrase_size];
    memcpy(passphrase_buffer, passphrase_data, passphrase_size);

    // Copy salt to uint8_t array
    size_t salt_size = salt_len * sizeof(jbyte);
    uint8_t salt_buffer[salt_size];
    memcpy(salt_buffer, salt_data, salt_size);

    // Derive key with scrypt algorithm
    uint8_t key_buffer[KEY_LENGTH];
    int result = crypto_scrypt(
            passphrase_buffer, passphrase_size,
            salt_buffer, salt_size,
            SCRYPT_N, SCRYPT_R, SCRYPT_P,
            key_buffer, KEY_LENGTH
    );
    jbyteArray key = (*env)->NewByteArray(env, KEY_LENGTH);
    (*env)->SetByteArrayRegion(env, key, 0, KEY_LENGTH, (jbyte*)key_buffer);

    // Erase sensitive data
    memset(key_buffer, 0, KEY_LENGTH);
    memset(salt_buffer, 0, salt_size);
    memset(passphrase_buffer, 0, passphrase_size);
    memset(passphrase_data, 0, passphrase_size);

    // Free memory
    (*env)->ReleaseByteArrayElements(env, salt, salt_data, JNI_ABORT);
    (*env)->ReleaseCharArrayElements(env, passphrase, passphrase_data, JNI_ABORT);

    // Return a result or throw an exception
    if (result == -1) {
        THROW(env, DERIVE_EXCEPTION, "");
    }
    return key;
}