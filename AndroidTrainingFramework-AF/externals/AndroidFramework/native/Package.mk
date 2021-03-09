$(call __ndk_info, ======================================)
$(call __ndk_info, Adding Package Files)
$(call __ndk_info, ======================================)


# SRC_PACKAGE_NATIVE			:= 
INCLUDE_PACKAGE_NATIVE		:= $(call my-dir)


#include common package files
LOCAL_SRC_FILES +=  $(SRC_PACKAGE_NATIVE)/AndroidOS.cpp \
					$(SRC_PACKAGE_NATIVE)/ABundle.cpp
										
					
LOCAL_C_INCLUDES += $(INCLUDE_PACKAGE_NATIVE)


#include files required for config

#AFile
ifeq ($(USE_AFILE),1)
	LOCAL_SRC_FILES += 	$(SRC_PACKAGE_NATIVE)/Loader/AFile.cpp \
						$(SRC_PACKAGE_NATIVE)/Loader/AResLoader.cpp
endif

# ifeq ($(USE_BACKUP_AGENT),1)
	# LOCAL_SRC_FILES += $(SRC_PACKAGE_NATIVE)/BackupAgent/ABackupAgent.cpp
# endif
