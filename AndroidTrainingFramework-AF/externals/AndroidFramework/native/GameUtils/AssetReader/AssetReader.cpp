/* Local includes */

#include "AssetReader.h"

#include "../package_utils.h"
#include <android/asset_manager_jni.h>

/* Library includes */

/* Standard includes */
#include "acp_utils.h"

namespace acp_utils
{
	namespace modules
	{
		AAssetManager* AssetReader::assetManager = NULL;

		FILE* AssetReader::open(const char* filename, const char* mode) 
		{
			// can't write to assets
			if(mode[0] == 'w') return NULL;

			AAsset* asset = AAssetManager_open(assetManager, filename, AASSET_MODE_STREAMING);
			if (!asset) return NULL;

			return funopen(asset, read, write, seek, close);
		}

		int AssetReader::read(void* stream, char* ptr, int size) 
		{
			return AAsset_read(static_cast<AAsset*>(stream), ptr, size);
		}

		// can't write to assets
		int AssetReader::write(void* stream, const char* ptr, int size) 
		{
			return 0;
		}

		fpos_t AssetReader::seek(void* stream, fpos_t offset, int origin) 
		{
			return AAsset_seek(static_cast<AAsset*>(stream), offset, origin);
		}

		int AssetReader::close(void *stream) 
		{
			AAsset_close(static_cast<AAsset*>(stream));
			return 0;
		}

		void AssetReader::InitAssetManager() 
		{
			if(assetManager != NULL)
			{
				return;
			}

			JNIEnv* pEnv;
			ScopeGetEnv st(pEnv);

			// AndroidUtils.GetAssetManager()
			jclass AndroidUtils = acp_utils::api::PackageUtils::GetClass("/PackageUtils/AndroidUtils");

			jmethodID methodID = pEnv->GetStaticMethodID(AndroidUtils, "GetAssetManager", "()Landroid/content/res/AssetManager;");	
			jobject manager = pEnv->CallStaticObjectMethod(AndroidUtils, methodID);

			// get native AssetManager from java object
			assetManager = AAssetManager_fromJava(pEnv, manager);
		}

	} //namespace modules
} //namespace acp_utils
