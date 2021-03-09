#ifndef __PLATFORMBASE_H__
#define __PLATFORMBASE_H__

#include <string>
#include <memory>
#include <mutex>

namespace platform
{	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/** TODO: add here members that means something for platforms (for example product id, or GGI)*/
	struct Settings
	{

		Settings(){};
	};


	/////////////////////////////////////////////////////////////////////////////////////////////////
	/** This is the Interface that must be implemented by the Frameworks (aka AndroidFramework, 
	* AndroidCorePackage, Windows8Framework, Win32CorePackage, etc...), to have the same interface for
	* the GeoLocator serivce.*/
	template <class TGeoLocatorImpl>
	class GeoLocatorBase
	{
	public:
	
		/** The structure that store the location obtained from GeoLocator. */
		struct Location
		{
			double latitude;
			double longitude;
			double altitude;
			double accuracy;	
			
		    /** The constructor must exists to initialize the members with the default values.*/
			Location() :latitude(0.0), longitude(0.0), altitude(0.0), accuracy(0.0){}
		};

		/// ----------- Functions that cam/must be implemented -----------------------------

		/** This will enable the service. The OS can/will prompt the user with a question 
		regarding this action. The status of the service will be SERVICE__ENABLED. */
		virtual void Enable(){}
		
		/** This will Disable the service. The status of the service will be SERVICE__DISABLED. */
		virtual void Disable(){}
		
		/** Use this to check if the service was enabled or not. After you call Enabled(), the OS will 
		* initialize the service. 
		* @return true if the service was enabled.*/
		virtual bool IsEnabled(){ return false; }
		
		/** Use this to set the text that can/will be displayed in the OS popup message box 
		* @param text is a const ref to the string containg the message */
		virtual void SetConfirmationText(const std::string& text){};
		
		/** Use this to check if the GeoLocator can retrieve valid coordinates. The locations cannot be available from the moment 
		* when the service is enabled, so , we need to check if the coordinates are valid.
		* @reutrn true if the coordinates are valid. */
		virtual bool HasValidCoordinates(){ return false; }
		
		/** Use it to get the Location.
		* @return a structure of Location type. */
		virtual Location GetLocation(){ return Location(); }

		/// --------------------------------------------------------------------------------


	public:

        /** Use this to get the single instance for the GeoLocator. This function will fail if the 
		* previous instance is not deleted => You cannot have two different instances at a same time.
		* @return a shared_ptr for GeoLocator */
		static TGeoLocatorImpl* CreateSingleInstance(Settings* settings)
        {
			std::lock_guard<std::mutex> lock(s_mutex);
			if (s_instance)
				return nullptr; // instance must expire

			s_instance = new TGeoLocatorImpl();
			((GeoLocatorBase*)s_instance)->m_settings = settings;
			return s_instance;
        }

	protected:
		Settings* m_settings;

	protected:
		GeoLocatorBase() :m_settings(nullptr){};
		virtual ~GeoLocatorBase()
		{ 
			std::lock_guard<std::mutex> lock(s_mutex); 
			delete s_instance;
		};

	private:
		static std::mutex s_mutex;
		static TGeoLocatorImpl* s_instance;
	};
	template <class TGeoLocatorImpl>
	std::mutex GeoLocatorBase<TGeoLocatorImpl>::s_mutex;

	template <class TGeoLocatorImpl>
	TGeoLocatorImpl* GeoLocatorBase<TGeoLocatorImpl>::s_instance = nullptr;
	




	/////////////////////////////////////////////////////////////////////////////////////////////////
    /** This is the main Interface used to abstractize the platforms. This is a template, that must be derived 
    * and implemented by each Framework.*/
	template <class TPlatformImpl, class TGeoLocatorImpl>
	class BaseInterface
	{
	public:

		/// ----------- Functions that cam/must be implemented -----------------------------

		virtual void DummyFunction1(){}
		virtual void DummyFunction2(){}

		/// --------------------------------------------------------------------------------


		/** Getter for GeoLocator service. Do not store the result for a later usage (can be invalid later), 
		* Use, always, this function to access the GeoLocator service!
		* @return a raw pointer to access the GeoLocator */
		TGeoLocatorImpl* GetGeoLocator() { return m_geolocator; }

	private:
		/** This deleter class is a helper to allow the shared_ptr to access private destructors */
		class deleter;
		friend class deleter;
		class deleter
		{
		public:
			void operator()(TPlatformImpl * p) { delete p; }
		};

	public:		
		/** Use this to get the single instance for the GeoLocator. This function will fail if the
		* previous instance is not deleted => You cannot have two different instances at a same time.
		* @return a shared_ptr for Platform */
		static std::shared_ptr<TPlatformImpl> CreateSingleInstance(const Settings& settings)
		{
			std::lock_guard<std::mutex> lock(s_mutex);
			if (!s_instance.expired())
				return nullptr; // instance must expire
			
			std::shared_ptr<TPlatformImpl> instance = std::shared_ptr<TPlatformImpl>(new TPlatformImpl(), BaseInterface<TPlatformImpl, TGeoLocatorImpl>::deleter());
			instance->m_settings = settings;
			s_instance = instance;
			return instance;
		}
				
	protected:
		BaseInterface()
		{
			m_geolocator = GeoLocatorBase<TGeoLocatorImpl>::CreateSingleInstance(&m_settings);
		}
		
		virtual ~BaseInterface()
		{
			std::lock_guard<std::mutex> lock(s_mutex); 
		}
		
	protected:
		Settings m_settings;
		TGeoLocatorImpl* m_geolocator;

	private:
		static std::mutex s_mutex;
		static std::weak_ptr<TPlatformImpl> s_instance;
	};

	template <class TPlatformImpl, class TGeoLocatorImpl>
	std::mutex BaseInterface<TPlatformImpl, TGeoLocatorImpl>::s_mutex;

	template <class TPlatformImpl, class TGeoLocatorImpl>
	std::weak_ptr<TPlatformImpl> BaseInterface<TPlatformImpl, TGeoLocatorImpl>::s_instance = std::weak_ptr<TPlatformImpl>();


} //platform

#endif //__PLATFORMBASE_H__




 
 
 
 
 
 