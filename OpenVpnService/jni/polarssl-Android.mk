LOCAL_PATH := $(call my-dir)

POLARSSL_SRC := polarssl/library
POLARSSL_INCLUDE := $(LOCAL_PATH)/polarssl/include

POLARSSL_SRC_FILES := \
	$(POLARSSL_SRC)/aes.c \
	$(POLARSSL_SRC)/aesni.c \
	$(POLARSSL_SRC)/arc4.c \
	$(POLARSSL_SRC)/asn1parse.c \
	$(POLARSSL_SRC)/asn1write.c \
	$(POLARSSL_SRC)/base64.c \
	$(POLARSSL_SRC)/bignum.c \
	$(POLARSSL_SRC)/blowfish.c \
	$(POLARSSL_SRC)/camellia.c \
	$(POLARSSL_SRC)/ccm.c \
	$(POLARSSL_SRC)/certs.c \
	$(POLARSSL_SRC)/cipher_wrap.c \
	$(POLARSSL_SRC)/cipher.c \
	$(POLARSSL_SRC)/ctr_drbg.c \
	$(POLARSSL_SRC)/debug.c \
	$(POLARSSL_SRC)/des.c \
	$(POLARSSL_SRC)/dhm.c \
	$(POLARSSL_SRC)/ecdh.c \
	$(POLARSSL_SRC)/ecdsa.c \
	$(POLARSSL_SRC)/ecp_curves.c \
	$(POLARSSL_SRC)/ecp.c \
	$(POLARSSL_SRC)/entropy_poll.c \
	$(POLARSSL_SRC)/entropy.c \
	$(POLARSSL_SRC)/error.c \
	$(POLARSSL_SRC)/gcm.c \
	$(POLARSSL_SRC)/havege.c \
	$(POLARSSL_SRC)/hmac_drbg.c \
	$(POLARSSL_SRC)/md_wrap.c \
	$(POLARSSL_SRC)/md.c \
	$(POLARSSL_SRC)/md2.c \
	$(POLARSSL_SRC)/md4.c \
	$(POLARSSL_SRC)/md5.c \
	$(POLARSSL_SRC)/memory_buffer_alloc.c \
	$(POLARSSL_SRC)/net.c \
	$(POLARSSL_SRC)/oid.c \
	$(POLARSSL_SRC)/padlock.c \
	$(POLARSSL_SRC)/pbkdf2.c \
	$(POLARSSL_SRC)/pem.c \
	$(POLARSSL_SRC)/pk_wrap.c \
	$(POLARSSL_SRC)/pk.c \
	$(POLARSSL_SRC)/pkcs11.c \
	$(POLARSSL_SRC)/pkcs12.c \
	$(POLARSSL_SRC)/pkcs5.c \
	$(POLARSSL_SRC)/pkparse.c \
	$(POLARSSL_SRC)/pkwrite.c \
	$(POLARSSL_SRC)/platform.c \
	$(POLARSSL_SRC)/ripemd160.c \
	$(POLARSSL_SRC)/rsa.c \
	$(POLARSSL_SRC)/sha1.c \
	$(POLARSSL_SRC)/sha256.c \
	$(POLARSSL_SRC)/sha512.c \
	$(POLARSSL_SRC)/ssl_cache.c \
	$(POLARSSL_SRC)/ssl_ciphersuites.c \
	$(POLARSSL_SRC)/ssl_cli.c \
	$(POLARSSL_SRC)/ssl_srv.c \
	$(POLARSSL_SRC)/ssl_tls.c \
	$(POLARSSL_SRC)/threading.c \
	$(POLARSSL_SRC)/timing.c \
	$(POLARSSL_SRC)/version_features.c \
	$(POLARSSL_SRC)/version.c \
	$(POLARSSL_SRC)/x509_create.c \
	$(POLARSSL_SRC)/x509_crl.c \
	$(POLARSSL_SRC)/x509_crt.c \
	$(POLARSSL_SRC)/x509_csr.c \
	$(POLARSSL_SRC)/x509.c \
	$(POLARSSL_SRC)/x509write_crt.c \
	$(POLARSSL_SRC)/x509write_csr.c \
	$(POLARSSL_SRC)/xtea.c
	
POLARSSL_INCLUDE_FILES := $(POLARSSL_INCLUDE)

include $(CLEAR_VARS)

LOCAL_MODULE := polarssl
LOCAL_C_INCLUDES:= $(POLARSSL_INCLUDE_FILES)
LOCAL_SRC_FILES := $(POLARSSL_SRC_FILES)

include $(BUILD_SHARED_LIBRARY)
