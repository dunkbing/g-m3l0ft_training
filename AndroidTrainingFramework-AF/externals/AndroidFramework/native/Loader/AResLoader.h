#ifndef __ARESLOADER__
	#define __ARESLOADER__

#include <jni.h>
// #include <stdio.h>
// #include <stdlib.h>

void AResLoader_Init();
unsigned int AResLoader_GetLength (const char* filename);
unsigned char* AResLoader_GetData (const char* filename);
// unsigned char* AResLoader_GetData (const char* filename, int offset, int loadSize);

#endif //__ARESLOADER__

