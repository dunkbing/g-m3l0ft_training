#ifndef MEMORY_MANAGER_H_INCLUDED__
#define MEMORY_MANAGER_H_INCLUDED__


#ifdef USE_MEMORY_LEAKS_TRACKER
#undef USE_MEMORY_LEAKS_TRACKER
#endif //USE_MEMORY_LEAKS_TRACKER

#ifdef USE_ELEPHANT_MEMORY_MANAGER
#undef USE_ELEPHANT_MEMORY_MANAGER
#endif //USE_ELEPHANT_MEMORY_MANAGER


#ifdef _DEBUG
    #define USE_MEMORY_LEAKS_TRACKER 0
    #define USE_ELEPHANT_MEMORY_MANAGER 0
#else
    #define USE_MEMORY_LEAKS_TRACKER 0
    #define USE_ELEPHANT_MEMORY_MANAGER 0
#endif




#ifndef NEW                                  //use the "NEW" instead "new"                  
    #if (USE_MEMORY_LEAKS_TRACKER)
        #define	NEW new(__FILE__, __LINE__)  //use this to call the new operator from MemTracker!
        #define	malloc(size) MemMgr::MemMgrMalloc(size, __FILE__, __LINE__) 
        #define	free(pointer) MemMgr::MemMgrFree(pointer) 
    #else
        #define	NEW new
    #endif
#endif





#if (USE_MEMORY_LEAKS_TRACKER)

#include <iostream>
#include <map>
#include <wx/thread.h>


using namespace std;

#ifdef WIN32 
    #include <crtdbg.h> 
#endif 


void* operator new( std::size_t size, const char *file, int line);
void* operator new[]( std::size_t size, const char *file, int line);

void operator delete( void* p, const char *file, int line);
void operator delete[] ( void* p, const char *file, int line);

void operator delete( void* p);
void operator delete[] ( void* p);


namespace MemMgr
{
    /**
     * special custom allocator
     */
    void*	CustomAlloc( std::size_t size);

    /**
     * special custom de alocator
     */
    void	CustomFree( void* ptr );



    /**
     * special MemMgr malloc used to record the allocation
     */
    void*	MemMgrMalloc( std::size_t size, const char *file, int line );

    /**
     * special custom free
     */
    void	MemMgrFree( void* ptr );
    


     /**
     * Used to track the memory leaks. This is a singleton type class that will store all the allocated pointers, size, line and file.
     */
    class InternalMemTracker 
    {
    public:

        /**
         * Get or create the instance. Use this instead the constructor, because this is a singleton.
         */
        static InternalMemTracker* Instance();

        /**
         * Set the filename of the output. Is not mandatory to call this function. There is already a default name.
        **/
        void SetOutputFileName(const string& fileName);

        /**
         * use this to destroy the InternalMemTracker instead of the destructor (because is private and you cannot use it)
         */
        void Destroy();


        /**
         * use it to record the info (in to the map). 
         */
        void Allocate( const void* ptr, const size_t size, const char *fileName, int line );

        /**
         * used to remove a record from the map.
         */
        void Free( const void* ptr );


        /**
         * enable or disable the tracker;
        **/
        void Enable(bool enable);

        /**
         * print to console the content of map. 
         */
        void PrintStatus();

    protected:


        /**
         * The constructor and destructor are protected because this is a singleton.
         * use Instance() instead.
         */
        InternalMemTracker();
        ~InternalMemTracker();

         /**
         * this is a helper class that will be used to store info about a pointer.
         **/
        class AllocUnit
        {
        public:
            AllocUnit(size_t size, const char *fileName, int line):m_size(size), m_fileName(fileName), m_line(line){}
            ~AllocUnit(){}
            size_t m_size;
            const char *m_fileName;
            int m_line;
        };

    private:
    
        /**
         * the filename where the output will be printed
        **/
        string                          m_outputFilename;

        /**
         * Because is a singleton ws need a pointer to the this unique intance.
        **/
        static InternalMemTracker*      s_instance;

        /**
         * This will store all allocations. At new an entry will be added. At delete that entry will be removed.
         * At a certain point if you have something in map, means that are valid pointers (aka memory leaks)
        **/
        map<const void *, AllocUnit*>   m_memoryMap;

        /**
         * the size in bytes of all the allocated memoryes
        **/
        size_t                          m_totalSize;

    };


    /**
     * used to track the instances of the classes derived from it.
     * this class only overload the new and delete
    **/
    class MemTracker
    {
    public:
        /*
        void* operator new( std::size_t size, const char *file, int line);
        void* operator new[]( std::size_t size, const char *file, int line);

        void operator delete( void* p);
        void operator delete[] ( void* p);
        */
    };



} //MemMgr

#elif USE_ELEPHANT_MEMORY_MANAGER


/* 
(C) Copyright 2007-2010 Jury Rig Software Limited. All Rights Reserved. 

Use of this software is subject to the terms of an end user license agreement.
This software contains code, techniques and know-how which is confidential and proprietary to Jury Rig Software Ltd.
Not for disclosure or distribution without Jury Rig Software Ltd's prior written consent. 

This header file contains some basic new overloads which call Elephant Memory Manager.  It is in no way a complete replacement but will demonstrate
its use behind the scenes.
*/

#include <stdlib.h>
#include <stdio.h>
#include <memory.h>
#include <windows.h>
#include <wx/thread.h>
#include <iostream>
#include <fstream>

#include "JRSMemory.h"

void MemoryManagerTTYPrint(const jrs_i8 *pText);

void MemoryManagerErrorHandle(const jrs_i8 *pError, jrs_u32 uErrorID);

#define malloc cMemoryManager::Get().Malloc
#define free cMemoryManager::Get().Free

// New and delete #define method
inline void *_cdecl operator new(size_t cbSize)
{
    void *p = cMemoryManager::Get().Malloc(cbSize, 16, JRSMEMORYFLAG_NEW, "New");  
    return p;
}

inline void _cdecl operator delete(void *pMemory) 
{
    cMemoryManager::Get().Free(pMemory, JRSMEMORYFLAG_NEW);
}

inline void *_cdecl operator new[](size_t cbSize)
{
    void *p = cMemoryManager::Get().Malloc(cbSize, 16, JRSMEMORYFLAG_NEWARRAY, "New");  
    return p;
}

inline void _cdecl operator delete[](void *pMemory) 
{
    cMemoryManager::Get().Free(pMemory, JRSMEMORYFLAG_NEWARRAY);
}

// For debugging
inline void *_cdecl operator new(size_t cbSize, char *pText)
{
    void *p = cMemoryManager::Get().Malloc(cbSize, 16, JRSMEMORYFLAG_NEW, pText);  
    return p;
}

inline void _cdecl operator delete(void *pMemory, char *pText) 
{
    cMemoryManager::Get().Free(pMemory, JRSMEMORYFLAG_NEW, pText);
}

inline void *_cdecl operator new[](size_t cbSize, char *pText)
{
    void *p = cMemoryManager::Get().Malloc(cbSize, 16, JRSMEMORYFLAG_NEWARRAY, pText);  
    return p;
}

inline void _cdecl operator delete[](void *pMemory, char *pText) 
{
    cMemoryManager::Get().Free(pMemory, JRSMEMORYFLAG_NEWARRAY, pText);
}

// heap overloaded new
inline void *_cdecl operator new(size_t cbSize, cHeap *pHeap)
{
    void *p = pHeap->AllocateMemory(cbSize, 16, JRSMEMORYFLAG_NEW, "Heap New");  
    return p;
}

inline void _cdecl operator delete(void *pMemory, cHeap *pHeap) 
{
    cMemoryManager::Get().Free(pMemory, JRSMEMORYFLAG_NEW, "Heap New");
}

inline void *_cdecl operator new[](size_t cbSize, cHeap *pHeap)
{
    void *p = cMemoryManager::Get().Malloc(cbSize, 16, JRSMEMORYFLAG_NEWARRAY, "Heap New[]");  
    return p;
}

inline void _cdecl operator delete[](void *pMemory, cHeap *pHeap) 
{
    cMemoryManager::Get().Free(pMemory, JRSMEMORYFLAG_NEWARRAY, "Heap New[]");
}

// placement new and delete
inline void *_cdecl operator new(size_t cbSize, void* pPlacement)
{
    return pPlacement;
}

inline void *_cdecl operator new[](size_t cbSize, void* pPlacement)
{
    return pPlacement;
}

inline void *_cdecl operator new(size_t cbSize, void* pPlacement, char *pText)
{
    return pPlacement;
}

inline void *_cdecl operator new[](size_t cbSize, void* pPlacement, char *pText)
{
    return pPlacement;
}

inline void operator delete(void* p, void*)
{
    // Here to stop the compiler moaning.  Placement delete does nothing.
}

inline void operator delete[](void* p, void*) 
{
    // Here to stop the compiler moaning.  Placement delete does nothing.
}


#endif


#endif // MEMORY_MANAGER_H_INCLUDED__
