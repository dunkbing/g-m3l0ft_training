#ifndef _AFILE_H_
#define _AFILE_H_

#include <stdio.h>

	typedef struct {
		unsigned char*	m_pFileData; 
		size_t	m_fileSize;
		size_t	m_filePos;
		size_t	m_currentChunkSize;
		int		m_chunksTotal;
		int		m_currentChunk;
		char	m_filename[1024];
		char	m_filemode[4];
		FILE	*m_pFile;
	} AFILE;
	
	// Override  File Functions
	extern AFILE*   afopen(const char* filename, const char* mode);
    extern int		fseek(AFILE* pfile, long Offset, int start);
    extern int		fclose(AFILE* pfile);
    extern size_t	fwrite(const void* buffer, size_t elements, size_t size, AFILE* pfile);
    extern size_t	fread(void* buffer, size_t elements, size_t size, AFILE* pfile);
    extern long		ftell(AFILE* pfile);
	
	extern int		fflush(AFILE* pfile);
	extern int		afeof(AFILE* pfile) ;


#define 	FILE 	AFILE
#define 	FOPEN	afopen

#ifdef feof
	#undef feof
#endif
#define 	feof	afeof

// #define 	fopen 	faopen
// #define 	fseek 	afseek
// #define 	fclose 	afclose
// #define 	fwrite 	afwrite
// #define 	ftell 	aftell
// #define 	fread 	afread
// #define 	feof 	afeof
// #define 	fflush 	afflush


////////////////////////////////////////////////////////////////////////////////////////////////////
#endif // __AFile_h__

////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
