#ifndef _ACP_ASSET_READER_
#define _ACP_ASSET_READER_

#include <stdio.h>
#include <android/asset_manager.h>

/**
 * Wrapper functions for Android assets
 */
namespace acp_utils
{
	namespace modules
	{
		class AssetReader
		{
		public:
			static AAssetManager* assetManager;
			
			static FILE* open(const char* filename, const char* mode);

			static int read(void* stream, char* ptr, int size);

			static int write(void* stream, const char* ptr, int size);

			static fpos_t seek(void* stream, fpos_t offset, int origin);

			static int close(void *stream);

			static void InitAssetManager();
			
		};
	} //namespace modules
} // namespace acp_utils
#endif // _ACP_ASSET_READER_