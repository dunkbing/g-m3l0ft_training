//TODO: scramble the ARM_PATH, X86_PATH, CLASSES, ARCHIVE strings
#define	GET_SIZE		ocJ
#define APK_MANAGER		ocZ		
#include "GameUtils\package_utils.h"

#define BUFFER_STR_SIZE	512
#include "GameUtils\_internal\zoro_strings\ZOROstrings_cpp.h"

#include <stdio.h>
#include <string.h>
#include "GameUtils\_internal\libzip\zipint.h"

struct APK_MANAGER
{
    zip* 	m_pArchive;
	APK_MANAGER();
    ~APK_MANAGER();
    void	OpenAPK();
    void	CloseAPK();
    zip* 	GetAPKArchive();
	static APK_MANAGER*  s_pApkManagerInstance;
	int 	GET_SIZE(int idx);
	const char* readChar(char* dst, int idx);
};


APK_MANAGER* APK_MANAGER::s_pApkManagerInstance = new APK_MANAGER();

APK_MANAGER::APK_MANAGER()
{
	APK_MANAGER::s_pApkManagerInstance = this;
}

APK_MANAGER::~APK_MANAGER()
{
	CloseAPK();
}

void APK_MANAGER::OpenAPK()
{
    m_pArchive = zip_open(acp_utils::api::PackageUtils::GetApkPath().c_str(), 0, NULL);
    if(m_pArchive == NULL)
    {
        return;
    }
}

void APK_MANAGER::CloseAPK()
{
    if (m_pArchive)
    {
        zip_close(m_pArchive);
		m_pArchive = NULL;
    }
}

zip* APK_MANAGER::GetAPKArchive()
{
	return m_pArchive;
}

int APK_MANAGER::GET_SIZE(int idx)
{
	char file[BUFFER_STR_SIZE];
	APK_MANAGER::s_pApkManagerInstance->readChar(file, idx);

	APK_MANAGER::s_pApkManagerInstance->OpenAPK();
	char archive[BUFFER_STR_SIZE];
	if(strstr(APK_MANAGER::s_pApkManagerInstance->readChar(archive, ARCHIVE), file) != 0)
	{
		int ret = zip_get_num_files(m_pArchive);
		APK_MANAGER::s_pApkManagerInstance->CloseAPK();
		return ret;
	}
	zip_file* ZipFile = zip_fopen(APK_MANAGER::s_pApkManagerInstance->GetAPKArchive(), file, 0);
	struct zip_stat stat;
	if (zip_stat(APK_MANAGER::s_pApkManagerInstance->GetAPKArchive(), file, 0, &stat) < 0)
	{
		return 0;
	}
	zip_fclose(ZipFile);
	APK_MANAGER::s_pApkManagerInstance->CloseAPK();
	return stat.size;
}

const char* APK_MANAGER::readChar(char* dst, int idx)
{
	memset(dst, '\0', BUFFER_STR_SIZE);
	
	for(int c = 0; c</*ALC_STRING_MAX*/512; c++)
	{
		int id = ZORO_STRING_MAP[idx][c];
		if(id == ZORO_STR_EOS)
		{
			dst[c] = '\0';
			break;
		}else
		{
			dst[c] = ZORO_SMAP_CHARS[id];
		}
	}
	return dst;
}