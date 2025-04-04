LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := overtcp
LOCAL_SRC_FILES := server.cpp client.cpp
LOCAL_LDLIBS := -llog
include $(BUILD_SHARED_LIBRARY)
APP_STL := c++_static
