#ifndef __ACP_SCOPE_MUTEX_H__
#define __ACP_SCOPE_MUTEX_H__

#include <pthread.h>

namespace acp_utils
{
	/**
	*	These classes can be used to simply implement a mutex system.
	*	Useage:
	*	add a Mutex object in your class
	*	then, whenever you want to use it make the following call:
	*	ScopeMutex(Mutex);
	*	Example:
	*	
	*	.h file:
	*
	*	class MutexExample
	*	{
	*	private:
	*		Mutex m_Mutex;
	*	public:
	*		void FunctionThatNeedsToBeMutexed();
	*	};
	*
	*	.cpp file:
	*	
	*	void MutexExample::FunctionThatNeedsToBeMutexed()
	*	{
	*		ScopeMutex(m_Mutex);
	*		//here comes code that requires mutex
	*	}
	*	//the m_Mutex will be unlocked when leaving the scope
	*
	*/
	class Mutex
	{
		pthread_mutex_t mMutex;
	
	public:
		void Lock() 
		{
			int result = pthread_mutex_lock(&mMutex);
		}

		void Unlock() 
		{
			int result = pthread_mutex_unlock(&mMutex);
		}

		Mutex()
		{	
			int result = pthread_mutex_init(&mMutex, 0);
		}
		~Mutex()
		{
			pthread_mutex_destroy(&mMutex);
		}
	};


	class ScopeMutex
	{
		Mutex&	mMutex;
	public:
		ScopeMutex(Mutex& m) : mMutex(m)
		{
			mMutex.Lock();
		}
		~ScopeMutex()
		{
			mMutex.Unlock();
		}
	};
};

#endif //__ACP_SCOPE_MUTEX_H__
