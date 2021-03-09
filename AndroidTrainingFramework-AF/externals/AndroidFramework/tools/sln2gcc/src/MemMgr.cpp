#include "MemMgr.h"

#if (USE_MEMORY_LEAKS_TRACKER)

#include <fstream>

#ifdef malloc
    #undef malloc
#endif

#ifdef free
    #undef free
#endif

void* operator new( size_t size, const char *file, int line ) 
{ 
    void *p = MemMgr::CustomAlloc(size); 

    MemMgr::InternalMemTracker::Instance()->Allocate( p, size, file, line );

    return p;
};
void* operator new[]( size_t size, const char *file, int line ) 
{ 
    void *p = MemMgr::CustomAlloc(size); 

    MemMgr::InternalMemTracker::Instance()->Allocate( p, size, file, line );

    return p;
}

void operator delete( void* p, const char *file, int line) 
{ 
    MemMgr::CustomFree(p); 

    MemMgr::InternalMemTracker::Instance()->Free( p );

};
void operator delete[] ( void* p, const char *file, int line) 
{ 
    MemMgr::CustomFree(p); 

    MemMgr::InternalMemTracker::Instance()->Free( p );
};



void operator delete( void* p) 
{ 
    MemMgr::CustomFree(p); 

    MemMgr::InternalMemTracker::Instance()->Free( p );

};
void operator delete[] ( void* p) 
{ 
    MemMgr::CustomFree(p); 

    MemMgr::InternalMemTracker::Instance()->Free( p );
};




void* MemMgr::CustomAlloc( size_t size)
{
	if(size == 0) 
        return NULL;	

    void* ptr = malloc(size);

	if(!ptr) 
        return NULL;

	memset(ptr, 0, size);

	return ptr;
}


void MemMgr::CustomFree( void* ptr )
{
	if( ptr )
	{
       free(ptr);
	}
}

/**
 * We need to protect the map with this mutex. 
 * If this will be removed may occur some crashes when multiple threads will use the new and/or delete!
 **/
static wxMutex s_mutexProtect;

/**
 * this is enabled by default, but at certain points i can disable the tracking!
**/
static bool s_enabled = true;


 /**
 * special MemMgr malloc used to record the allocation
 */
void* MemMgr::MemMgrMalloc( std::size_t size, const char *file, int line )
{
    void *p = MemMgr::CustomAlloc(size); 

    MemMgr::InternalMemTracker::Instance()->Allocate( p, size, file, line );

    return p;
}

/**
 * special custom free
 */
void MemMgr::MemMgrFree( void* p )
{
    MemMgr::CustomFree(p); 

    MemMgr::InternalMemTracker::Instance()->Free( p );
}


// Initialize instance pointer
MemMgr::InternalMemTracker* MemMgr::InternalMemTracker::s_instance = 0;


/**
 * CONSTRUCTOR
**/
MemMgr::InternalMemTracker::InternalMemTracker() 
{
    
    m_memoryMap.clear();
    m_totalSize = 0;
   
    m_outputFilename = "sln2gcc_LeaksMemTracker.log";
}


/**
 * DESTRUCTOR
 * delete all units and clear the map.
**/
MemMgr::InternalMemTracker::~InternalMemTracker() 
{   
    s_mutexProtect.Lock();
    s_enabled = false;//if this is false the "new"  will not record allocations

    map< const void*, MemMgr::InternalMemTracker::AllocUnit* >::iterator it;
    for( it = m_memoryMap.begin(); it != m_memoryMap.end(); it++ ) 
    {
        MemMgr::InternalMemTracker::AllocUnit* unit =  (MemMgr::InternalMemTracker::AllocUnit*)it->second;
        delete unit;
    }

    m_memoryMap.clear();
    m_totalSize = 0;
    
    s_mutexProtect.Unlock();
}


/**
 * Destroys the instance.
**/
void MemMgr::InternalMemTracker::Destroy() 
{
    s_mutexProtect.Lock();
    s_enabled = false;
    s_mutexProtect.Unlock();

    delete s_instance;
    s_instance = 0;
}


/**
 * Returns the instance of this singleton.
**/
MemMgr::InternalMemTracker* MemMgr::InternalMemTracker::Instance() 
{
    if( s_instance == 0 ) 
    {
        s_instance = new MemMgr::InternalMemTracker;
    }
    
    return s_instance;
}


/**
 * Returns the instance of this singleton.
**/
void MemMgr::InternalMemTracker::SetOutputFileName(const string& fileName) 
{
    s_mutexProtect.Lock();
    m_outputFilename = fileName;
    s_mutexProtect.Unlock();  
}


/**
 * Call this when allocating memory with overloaded new or new[].
**/
void MemMgr::InternalMemTracker::Allocate( const void* ptr, const size_t size, const char *fileName, int line) 
{
    if(!s_enabled) return;

    

    
    s_mutexProtect.Lock();
    s_enabled = false;

    AllocUnit *alloc = new AllocUnit(size, fileName, line);
    m_memoryMap.insert( pair<const void*, MemMgr::InternalMemTracker::AllocUnit*>( ptr, alloc ) );
    m_totalSize += size;

    s_enabled = true;
    s_mutexProtect.Unlock();
    

    
}


/**
 * Call this when freeing memory with delete or delete[].
**/
void MemMgr::InternalMemTracker::Free( const void* ptr ) 
{
    if(!s_enabled) return;
    
    
    s_mutexProtect.Lock();
    s_enabled = false;

    if(m_memoryMap.find(ptr) != m_memoryMap.end())
    {
        AllocUnit* unit = m_memoryMap[ ptr ];
        if(unit)
        {
            m_memoryMap.erase( ptr );
            m_totalSize -= unit->m_size;

            
            delete unit;
        }
    }
    s_enabled = true;
    s_mutexProtect.Unlock();
    
}

/**
 * enable or disable the tracker;
**/
void MemMgr::InternalMemTracker::Enable(bool enable)
{
    s_mutexProtect.Lock();
    s_enabled = enable;
    s_mutexProtect.Unlock();
}


/**
 * Prints the status of all the allocated memory being tracked to file.
**/
void MemMgr::InternalMemTracker::PrintStatus() 
{
    s_mutexProtect.Lock();

    if(m_outputFilename.size() != 0)
    {   
        // Open the log file for output
        ofstream out( m_outputFilename.c_str() );
        
        if(out.is_open())
        {        
            out << "Total Number of Pointers: " << m_memoryMap.size() << endl;
            out << "Total Size Allocated: " << m_totalSize << endl;
            
            out << endl << endl;
            
            out << "Pointer Information:" << endl;
            map< const void*, MemMgr::InternalMemTracker::AllocUnit* >::iterator it;
            for( it = m_memoryMap.begin(); it != m_memoryMap.end(); it++ ) 
            {
                MemMgr::InternalMemTracker::AllocUnit* unit =  (MemMgr::InternalMemTracker::AllocUnit*)it->second;

                if(unit)
                    out << "Address: " << it->first << " | Size: " << unit->m_size << " | line: " << unit->m_line << " | file: " << unit->m_fileName << endl;
            }
            
            out.close();
        }
    }

    s_mutexProtect.Unlock();
}

#elif USE_ELEPHANT_MEMORY_MANAGER


/* 
(C) Copyright 2007-2010 Jury Rig Software Limited. All Rights Reserved. 

Use of this software is subject to the terms of an end user license agreement.
This software contains code, techniques and know-how which is confidential and proprietary to Jury Rig Software Ltd.
Not for disclosure or distribution without Jury Rig Software Ltd's prior written consent. 

This is a simple example showing how to use some of the most basic features of Elephant. MMgr.h has basic examples of new/delete overloading you may want to expand on
and malloc/free define to route directly to Elephant.
*/

using namespace std;

// Basic memory manager overloading
void MemoryManagerTTYPrint(const jrs_i8 *pText)
{
    printf("%s\n", pText);			// Output to console window for example


    static wxMutex s_mutexProtect;
    static string  s_outputFilename = "sln2gcc_ElephantMeM.log";

    s_mutexProtect.Lock();
    

    FILE* fileout = fopen(s_outputFilename.c_str(), "a");

    if(fileout)
    {
        fprintf(fileout, "%s\n", pText);
        fclose(fileout);
    }

    /*    
    ofstream fileout( s_outputFilename.c_str(), ios_base::ate|ios_base::out);
    
    if(fileout.is_open())
    {
        fileout << pText << endl;

        fileout.close();
    }
    */
    
    

    s_mutexProtect.Unlock();
}

void MemoryManagerErrorHandle(const jrs_i8 *pError, jrs_u32 uErrorID)
{
    __debugbreak();
}

#endif