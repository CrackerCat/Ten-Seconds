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
Java_com_gh0u1l5_tenseconds_backend_crypto_MasterKey_derive(
        JNIEnv* env,
        jobject obj,
        jstring identity,
        jcharArray passphrase
) {
    // Read identity ID
    jsize identity_len = (*env)->GetStringLength(env, identity);
    const char *identity_data = (*env)->GetStringUTFChars(env, identity, JNI_FALSE);

    // Read passphrase
    jsize passphrase_len = (*env)->GetArrayLength(env, passphrase);
    jchar *passphrase_data = (*env)->GetCharArrayElements(env, passphrase, JNI_FALSE);

    // Convert identity ID to salt
    size_t salt_len = identity_len * sizeof(char);
    uint8_t salt_data[salt_len];
    memcpy(salt_data, identity_data, salt_len);

    // Convert passphrase to password
    size_t password_len = passphrase_len * sizeof(jchar);
    uint8_t password_data[password_len];
    memcpy(password_data, passphrase_data, password_len);

    // Derive key with scrypt algorithm
    uint8_t buffer[KEY_LENGTH];
    int result = crypto_scrypt(
            password_data, password_len,
            salt_data, salt_len,
            SCRYPT_N, SCRYPT_R, SCRYPT_P,
            buffer, KEY_LENGTH
    );
    jbyteArray key = (*env)->NewByteArray(env, KEY_LENGTH);
    (*env)->SetByteArrayRegion(env, key, 0, KEY_LENGTH, (jbyte*)buffer);

    // Erase sensitive data
    memset(buffer, 0, KEY_LENGTH);
    memset(salt_data, 0, salt_len);
    memset(password_data, 0, password_len);
    memset(passphrase_data, 0, passphrase_len * sizeof(jchar));

    // Free memory
    (*env)->ReleaseStringUTFChars(env, identity, identity_data);
    (*env)->ReleaseCharArrayElements(env, passphrase, passphrase_data, JNI_ABORT);

    // Return a result or throw an exception
    if (result == -1) {
        THROW(env, DERIVE_EXCEPTION, "");
    }
    return key;
}