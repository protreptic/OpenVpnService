LOCAL_PATH := $(call my-dir)

SNAPPY_SRC_FILES := \
	snappy/snappy-c.cc \
	snappy/snappy-sinksource.cc \
	snappy/snappy-stubs-internal.cc \
	snappy/snappy.cc

SNAPPY_INCLUDE_FILES := $(LOCAL_PATH)/snappy $(LOCAL_PATH)/snappy/conf

include $(CLEAR_VARS)

LOCAL_CPP_EXTENSION := .cc

LOCAL_C_INCLUDES:= $(SNAPPY_SRC_FILES)
LOCAL_SRC_FILES := $(SNAPPY_SRC_FILES)

LOCAL_MODULE := snappy

include $(BUILD_SHARED_LIBRARY)
