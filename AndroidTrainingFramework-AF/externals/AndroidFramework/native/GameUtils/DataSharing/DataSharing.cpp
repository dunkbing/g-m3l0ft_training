#include "config_Android.h"

#include "DataSharing.h"

#include "..\package_utils.h"

#include <stdio.h>
#include <string.h>



jmethodID 	acp_utils::modules::DataSharing::mSetSharedValue	= 0;
jmethodID 	acp_utils::modules::DataSharing::mGetSharedValue	= 0;
jmethodID 	acp_utils::modules::DataSharing::mDeleteSharedValue	= 0;
jmethodID 	acp_utils::modules::DataSharing::mIsSharedValue		= 0;
jclass 		acp_utils::modules::DataSharing::mClassDataSharing	= 0;

namespace acp_utils
{
	namespace modules
	{

		void DataSharing::SetJniVars()
		{
			if(mClassDataSharing == 0)
			{
				JNIEnv* pEnv = NULL; 
				acp_utils::ScopeGetEnv st(pEnv);

				mClassDataSharing 		= acp_utils::api::PackageUtils::GetClass("/DataSharing");

				mSetSharedValue		= pEnv->GetStaticMethodID (mClassDataSharing, "setSharedValue", "(Ljava/lang/String;Ljava/lang/String;)V");
				mGetSharedValue		= pEnv->GetStaticMethodID (mClassDataSharing, "getSharedValue", "(Ljava/lang/String;)Ljava/lang/String;");
				mDeleteSharedValue	= pEnv->GetStaticMethodID (mClassDataSharing, "deleteSharedValue", "(Ljava/lang/String;)V");
				mIsSharedValue		= pEnv->GetStaticMethodID (mClassDataSharing, "isSharedValue", "(Ljava/lang/String;)Z");
			}
		}


		void DataSharing::SetSharedValue(const char* key, const char* value)
		{
			SetJniVars();

			JNIEnv* pEnv = NULL; 
			acp_utils::ScopeGetEnv st(pEnv);

			jstring skey = pEnv->NewStringUTF(key);
			jstring svalue = pEnv->NewStringUTF(value);
			
			pEnv->CallStaticVoidMethod(mClassDataSharing, mSetSharedValue, skey, svalue);

			pEnv->DeleteLocalRef(skey);
			pEnv->DeleteLocalRef(svalue);
		}

		std::string DataSharing::GetSharedValue(const char* key)
		{
			SetJniVars();

			JNIEnv* pEnv = NULL; 
			acp_utils::ScopeGetEnv st(pEnv);

		
			jstring skey = pEnv->NewStringUTF(key);
			
			acp_utils::ScopeStringChars		res(pEnv, (jstring)pEnv->CallStaticObjectMethod(mClassDataSharing, mGetSharedValue, skey));
			
			pEnv->DeleteLocalRef(skey);

			return res.Get();
		}

		void DataSharing::DeleteSharedValue(const char* key)
		{
			SetJniVars();

			JNIEnv* pEnv = NULL; 
			acp_utils::ScopeGetEnv st(pEnv);

			jstring skey = pEnv->NewStringUTF(key);
			pEnv->CallStaticVoidMethod(mClassDataSharing, mDeleteSharedValue, skey);
			pEnv->DeleteLocalRef(skey);
		}

		bool DataSharing::IsSharedValue(const char* key)
		{
			SetJniVars();

			JNIEnv* pEnv = NULL; 
			acp_utils::ScopeGetEnv st(pEnv);

			jstring skey = pEnv->NewStringUTF(key);
			
			jboolean result = pEnv->CallStaticBooleanMethod(mClassDataSharing, mIsSharedValue, skey);
			
			pEnv->DeleteLocalRef(skey);

			return result;
		}

	}//namespace modules
}//namespace acp_utils
