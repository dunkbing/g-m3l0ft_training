#ifndef __JNI_ABUNDLE_H__
#define __JNI_ABUNDLE_H__

#include <jni.h>

#ifdef  __cplusplus
extern "C" {
#endif
///////////////////////////////////////////////////////////////////////////////////
// ABundle Interface
///////////////////////////////////////////////////////////////////////////////////
	void 			ABundle_PutString(const char* key, const char* value, jobject bundle);
	const char* 	ABundle_ReadString(const char* key, jobject bundle);
	int 			ABundle_ReadInt(const char* key, jobject bundle);
	void 			ABundle_PutInt(const char* key, int value, jobject bundle);
	long long 		ABundle_ReadLong(const char* key, jobject bundle);
	void 			ABundle_PutLong(const char* key, long long value, jobject bundle);
	void			ABundle_PutBool(const char* key, bool value, jobject bundle);
	bool			ABundle_ReadBool(const char* key, jobject bundle);
	jboolean 		ABundle_ContainsKey(const char* key, jobject bundle);
	jobject 		ABundle_New();
	void 			ABundle_Clear(jobject bundle);
	
	jbyteArray 		ABundle_ReadBArray(const char* key, jobject bundle);
	void 			ABundle_PutBArray(const char* key, jbyteArray value, jobject bundle);

#ifdef  __cplusplus
}
#endif


#endif
