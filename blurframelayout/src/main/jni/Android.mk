LOCAL_PATH := $(call my-dir)


include $(CLEAR_VARS)
 
LOCAL_LDLIBS    := -llog -ljnigraphics
 
LOCAL_MODULE    := stack-blur
LOCAL_SRC_FILES := blur.c
 
LOCAL_CFLAGS		= -ffast-math -O3 -funroll-loops
LOCAL_CPP_FEATURES	= -fgnustl_shared
 
include $(BUILD_SHARED_LIBRARY)

