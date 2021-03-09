////////////////////////////////////////////////////////////////////////////////////////////////////

#include <stdio.h>
#include <assert.h>
#include <string.h>
#include <malloc.h>

#include "AFile.h"

//don't wrap FILE to AFILE, in this file
#undef FILE
#undef feof

#define MAX_CHUNK_SIZE (1*1024*1024)


#ifdef OS_ANDROID
	#include "config_Android.h"
	#include "AResLoader.h"
#else

	unsigned int AResLoader_GetLength (const char* filename);
	unsigned char* AResLoader_GetData (const char* filename);

	#define AFILE_WIN32_EMULATION
#endif

//to define in your game specific, wich files are save files.
extern bool AFile_OpenAsNormalFile(const char* filename);
//// Example: Return if file should be open using fopen,
//// Usually save files (that can be saved on app path, or sd card)
// bool AFile_OpenAsNormalFile(const char* filename)
// {
	// return (strstr(filename, ".sav") || strstr(filename, ".data"));
// }

// void	FRelease(AFILE* Release);
void	FCacheInfo(AFILE* CacheInfo);
void    FCacheChunkData(AFILE* CacheChunkData);
	
	
using namespace acp_utils;


void GetFileName(char* filename, const char* Url){
     int LenString = 0;
	 int Index = 0;
	 int FoundSeparator = 0;
 
	 LenString = strlen(Url);
     Index = LenString - 1;

	int len = AResLoader_GetLength(Url);
	if(len > 0)
	{
		strcpy(filename, Url);
		return;
	}

	 if(LenString > 0){
          /// From Bottom
		 while(Index>=0 && FoundSeparator!=1){
			 if(Url[Index]=='\\'|| Url[Index]=='/'){
                FoundSeparator = 1;
			 }else{
                Index--;
			 } 
		 }
         Index++;
		 if(Index<LenString){
			 strcpy(filename, Url+Index);  
			 return; 
		 }
	 }
     filename = NULL;
}


void FCacheInfo(AFILE* afile)
{
	char chunkName[255];
	int len = AResLoader_GetLength(afile->m_filename);

	if(len > 0)
	{	
		afile->m_fileSize = len;
	}
	else
	{
		afile->m_currentChunk = -1;
		afile->m_chunksTotal = 0;
		do 
		{
			afile->m_fileSize += len; 
			if(afile->m_chunksTotal<10)
				sprintf(chunkName, "%s00%d", afile->m_filename, afile->m_chunksTotal);
			else if(afile->m_chunksTotal< 100)
				sprintf(chunkName, "%s0%d", afile->m_filename, afile->m_chunksTotal);
			else
				sprintf(chunkName, "%s%d", afile->m_filename, afile->m_chunksTotal);
			afile->m_chunksTotal++;

		}while(len = AResLoader_GetLength(chunkName));
	}
	if(afile->m_fileSize && afile->m_chunksTotal == 0)
		FCacheChunkData(afile);
}

void FCacheChunkData(AFILE* afile)
{
	char chunkName[255];
	if(afile->m_pFileData)
		free(afile->m_pFileData);
	afile->m_pFileData = NULL;

	if(afile->m_chunksTotal == 0)
	{
		afile->m_pFileData = AResLoader_GetData(afile->m_filename);
	}
	else
	{
		if(afile->m_currentChunk<10)
			sprintf(chunkName, "%s00%d", afile->m_filename, afile->m_currentChunk);
		else if(afile->m_currentChunk< 100)
			sprintf(chunkName, "%s0%d", afile->m_filename, afile->m_currentChunk);
		else
			sprintf(chunkName, "%s%d", afile->m_filename, afile->m_currentChunk);
		
		afile->m_currentChunkSize = AResLoader_GetLength(chunkName);
		if(afile->m_currentChunkSize > 0)
			afile->m_pFileData = AResLoader_GetData(chunkName);
		else
			assert(0);		
	}
}

//////////////////////////////////////////////////////////////////////////////////////////////////////

AFILE* afopen(const char* filename, const char* mode)
{
	AFILE* file = (AFILE*)malloc(sizeof(AFILE));
	// Clean Memory
	memset (file,0,sizeof(AFILE));
	
	char myFilename[1024];
	GetFileName(myFilename, filename);
	
    strcpy(file->m_filemode, mode);
	
	// #if USE_SD_CARD_STORE
		// sprintf(file->m_filename,"%s%s",AndroidOS_GetSDFolder(),myFilename);
		// file->m_pFile	= fopen(file->m_filename, file->m_filemode);
		// if(!file->m_pFile) 
			// return NULL; //file not found
	// #else
    // Check out if has a Ext    
	if(!strchr(myFilename, '.')){
        // Cat a Ext
		strcat (myFilename,".bin");
	}
	
	//open for read (from APK) or open the game save file
	if(mode[0] == 'r' && !AFile_OpenAsNormalFile(filename))
	{
        sprintf(file->m_filename,"%s",myFilename);
		FCacheInfo(file);
		if(file->m_fileSize == 0)
			return NULL;
	}
	else	//open for write, for android only can be package folder
	{
		sprintf(file->m_filename,"%s/%s",acp_utils::api::GetSdCardPath().c_str(),myFilename);
		file->m_pFile	= fopen(file->m_filename, file->m_filemode);		
		if(!file->m_pFile) 
			return NULL; //file not found
	}

	file->m_filePos = 0;		
	
	return file;
}

int fclose(AFILE* file)
{
	if(file->m_pFileData)
	{
		free(file->m_pFileData);
		file->m_pFileData = NULL;
	}

	if(file->m_pFile)
	{
		fclose(file->m_pFile);
		file->m_pFile = NULL;
	}

	free(file);
	return 0;
}

int fseek(AFILE* file, long offset, int start)
{
	if(file->m_pFile)
	{
		return fseek(file->m_pFile, offset, start);
	}
	else
	{
		if(start == SEEK_SET)
			file->m_filePos = offset;
		else if(start == SEEK_CUR)
			file->m_filePos += offset;		
		else if(start == SEEK_END)
			file->m_filePos = file->m_fileSize - offset;
		else
			assert(0);
		return (file->m_filePos >= 0 && file->m_filePos < file->m_fileSize);
	}
}

long ftell(AFILE* file)
{
	if(file->m_pFile)
		return ftell(file->m_pFile);
	else
		return file->m_filePos;
	
}

int afeof ( AFILE* file )
{
	if(file->m_pFile)
		return feof(file->m_pFile);
	else
		return (file->m_filePos >= file->m_fileSize);
	
}

int fflush(AFILE* file)
{
	if(file->m_pFile)
		return fflush(file->m_pFile);
	return 0;
}

size_t fwrite(const void *buffer, size_t elements, size_t size, AFILE* file)
{
	if(file->m_pFile){
	   return fwrite(buffer, elements, size, file->m_pFile);	
    }
	return 0;	
}

size_t fread(void *buffer, size_t elements, size_t size, AFILE* file)
{
	unsigned char*	pOut;
	int readSize = 0;
	int rem = 0;
	int offset, read, chunk = 0;

	if(file->m_pFile)
	{
		return fread(buffer, elements, size, file->m_pFile);
	}
	else
	{

		//assert(m_filemode[0] == 'r');
		
		pOut	=	(unsigned char*)buffer;
		readSize = elements * size;
		
		if(( file->m_filePos + readSize) > file->m_fileSize )
		{
			if(file->m_filePos < file->m_fileSize){
				int rz=file->m_fileSize-file->m_filePos;
				while(rz > 0){
					chunk = file->m_filePos / MAX_CHUNK_SIZE;
					if(chunk != file->m_currentChunk)
					{
						file->m_currentChunk = chunk;
						FCacheChunkData(file);
					}
					offset = file->m_filePos % MAX_CHUNK_SIZE;
					if(rz > file->m_currentChunkSize - offset)
						read = file->m_currentChunkSize - offset;
					else
						read = rz;
					
					memcpy(pOut, &file->m_pFileData[offset], read);
					file->m_filePos += read;
					pOut += read;
					rz -= read;
				}
				return readSize;
			}else{
				//assert(0);
				return 0;
			}
		}
		if(file->m_chunksTotal > 0)
		{
			rem = readSize;
			while(rem > 0)
			{
				chunk = file->m_filePos / MAX_CHUNK_SIZE;
				if(chunk != file->m_currentChunk)
				{
					file->m_currentChunk = chunk;
					FCacheChunkData(file);
				}
				offset = file->m_filePos % MAX_CHUNK_SIZE;
				if(rem > file->m_currentChunkSize - offset)
					read = file->m_currentChunkSize - offset;
				else
					read = rem;
				
				memcpy(pOut, &file->m_pFileData[offset], read);
				file->m_filePos += read;
				pOut += read;
				rem -= read;
			}
		}
		else
		{
			if(!file->m_pFileData)
				return 0;
			memcpy(pOut, &file->m_pFileData[file->m_filePos], readSize);
			file->m_filePos += readSize;
		}
		return readSize;
	}
}



#ifdef AFILE_WIN32_EMULATION
unsigned int AResLoader_GetLength(const char* filename)
{
	FILE* file = fopen(filename, "rb");
	int size = 0;
	if(file)
	{
		fseek(file, 0, SEEK_END);
		size = ftell(file);
		fclose(file);
		return size;
	}
	return 0;
}

unsigned char* AResLoader_GetData(const char* filename)
{

	FILE* file = fopen(filename, "rb");
	unsigned char *data;
	int size;
	if(file)
	{
		fseek(file, 0, SEEK_END);
		size = ftell(file);
		fseek(file, 0, SEEK_SET);

		data = (unsigned char *) malloc (sizeof(char) * size);
		fread(data, size, 1, file);
		fclose(file);
		return data;
	}
	return NULL;
}
#endif

