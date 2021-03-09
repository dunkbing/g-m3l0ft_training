#ifndef __ACP_SCOPE_STRING_CHARS_H__
#define __ACP_SCOPE_STRING_CHARS_H__

#include <jni.h>

namespace acp_utils
{
	/**
	*	This class transforms a jstring to a string, while keeping simple the JNI allocation
	*	Useage:
	*	jstring js = "my java string";
	*	ScopeStringChars	tmp(env, js);
	*	std::string finalstring = std::string(tmp.Get());
	*	//the jstring, and tmp will be cleaned when leaving the scope
	*/
	class ScopeStringChars
	{
	
		JNIEnv* mEnv;
		jstring mString;
		const char* mChars;
	
	public:

		ScopeStringChars(JNIEnv* env, jstring str): 
			mEnv(env), 
			mString(str), 
			mChars(env->GetStringUTFChars(str, JNI_FALSE)) 
		{
		}

		~ScopeStringChars() 
		{
			mEnv->ReleaseStringUTFChars(mString, mChars);
			mEnv->DeleteLocalRef(mString);
		}

		const char* Get() 
		{
			return mChars;
		}
	};
};

#endif //__ACP_SCOPE_STRING_CHARS_H__
