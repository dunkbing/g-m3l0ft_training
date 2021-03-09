#ifndef __JNI_DATA_SHARING_H__
#define __JNI_DATA_SHARING_H__

#include <jni.h>
#include <string>

namespace acp_utils
{
	namespace modules
	{
		class DataSharing
		{
			static jmethodID	mSetSharedValue;
			static jmethodID	mGetSharedValue;
			static jmethodID	mDeleteSharedValue;
			static jmethodID	mIsSharedValue;

			static jclass		mClassDataSharing;
			
			static void				SetJniVars();

		public:
			
			
			static void				SetSharedValue(const char* key, const char* value);
			static std::string		GetSharedValue(const char* key);
			static void				DeleteSharedValue(const char* key);
			static bool				IsSharedValue(const char* key);

		};
	}
}


#endif
