LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
    ../../../ex/carousel/java/com/android/ex/carousel/carousel.rs

LOCAL_JAVA_LIBRARIES := services telephony-common

LOCAL_STATIC_JAVA_LIBRARIES := android-common-carousel \
                               com.mediatek.systemui.ext \
				android-support-v4 \
                               CellConnUtil

LOCAL_PACKAGE_NAME := SystemUI
LOCAL_CERTIFICATE := platform
#Begin:added by xss for blur
#LOCAL_JNI_SHARED_LIBRARIES += libgaussian_blur
LOCAL_REQUIRED_MODULES := libgaussian_blur
#End:added by xss for blur
LOCAL_JAVA_LIBRARIES += mediatek-framework

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

include $(call all-makefiles-under,$(LOCAL_PATH))
