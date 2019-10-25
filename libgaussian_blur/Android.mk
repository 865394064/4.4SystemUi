LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libgaussian_blur

LOCAL_SRC_FILES_32:= libgaussian_blur.so

LOCAL_MODULE_CLASS := SHARED_LIBRARIES

LOCAL_MODULE_SUFFIX := .so

include $(BUILD_PREBUILT) 
