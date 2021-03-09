#include <android/log.h>

#include "../../AssetReader/AssetReader.h"
#include "../../package_utils.h"

#if ACP_UT
#include "AndroidUnitTestLogPrinter.h"
#include "UnitTests_DeviceConfig.h"
#include "gtest/gtest.h"

#include <cstring>
#include <string>
#include <unistd.h>
#include <fcntl.h>
#include <sys/sendfile.h>
#include <cstdio>

#if USE_ASSET_READER

class AssetReaderAPI_UT : public ::testing::Test
{
public:

protected:
	std::string comparePath;

	 AssetReaderAPI_UT()
	{
		// TODO (maybe?)
	}
	
	virtual ~ AssetReaderAPI_UT()
	{
		// TODO (maybe?)
	}
	
	virtual void SetUp()
	{
		comparePath = acp_utils::api::PackageUtils::GetDataFolderPath();
		comparePath += UT_ASSET_COMPARE_FILENAME;

		// copy asset to sd card
		/*int source = open(UT_ASSET_APK_FILENAME, O_RDONLY);
		int dest = open(comparePath.c_str(), O_WRONLY | O_CREAT | O_TRUNC, 0644);

		// get source file size
		struct stat stat_source;
		fstat(source, &stat_source);

		// sendfile -> kernel buffers (no user space transfer)
		sendfile(dest, source, 0, stat_source.st_size);

		close(source);
		close(dest);
		*/

		// write data to file
		FILE *compare = fopen(comparePath.c_str(), "w");

		fprintf(compare, "%s", UT_ASSET_COMPARE_DATA);

		fclose(compare);

	}
	
	virtual void TearDown()
	{
		unlink(comparePath.c_str());
	}
};

TEST_F( AssetReaderAPI_UT, BaseAPI)
{
	FILE *compareFile = fopen(comparePath.c_str(), "rb");
	FILE *assetFile = acp_utils::modules::AssetReader::open(UT_ASSET_APK_FILENAME, "rb");

	// check if both files exist
	EXPECT_TRUE(compareFile != NULL);
	EXPECT_TRUE(assetFile != NULL);

	int cRes, aRes, compareFileSize, assetFileSize;
	char compareData = 0, assetData = 0;
	
	// compare file size (test seek)
	fseek(compareFile, 0L, SEEK_END);
	compareFileSize = ftell(compareFile);
	fseek(compareFile, 0L, SEEK_SET);

	fseek(assetFile, 0L, SEEK_END);
	assetFileSize = ftell(assetFile);
	fseek(assetFile, 0L, SEEK_SET);

	EXPECT_TRUE(compareFileSize == assetFileSize);

	// check file data
	do {
		cRes = fread(&compareData, sizeof(char), 1, compareFile);
		aRes = fread(&assetData, sizeof(char), 1, assetFile);

		EXPECT_TRUE(compareData == assetData);
	} while(cRes > 0 && aRes > 0 && compareData == assetData);

	fclose(compareFile);
	fclose(assetFile);
}

#endif // USE_ASSET_READER

#endif // ACP_UT