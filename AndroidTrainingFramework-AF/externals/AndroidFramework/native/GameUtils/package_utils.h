#ifndef __PACKAGE_UTILS_H__
#define __PACKAGE_UTILS_H__

#include <jni.h>

#include <string>
#include <map>
#include <vector>


#include "config_Android.h"


#include "helpers.h"
#include "ScopeMutex.h"
#include "ScopeStringChars.h"
#include "ScopeGetEnv.h"

namespace acp_utils
{
	namespace acp_internal
	{
		class Internal;
	};
};

namespace acp_utils
{
    namespace api
	{

		/**
		*	PackageUtils is the main Android OS API class.
		*	It is composed of getters for different identifiers and of minor implementations, like closing the application, or saving values in the application local space. 
		*/

		class PackageUtils
		{
			friend class acp_internal::Internal;

		private:
            /**
             * Shared preference type enum.
             */
            enum SharedPreference 
			{
                PREF_TYPE_INT     = 0,
                PREF_TYPE_LONG    = 1,
                PREF_TYPE_BOOLEAN = 2,
                PREF_TYPE_STRING  = 3
            };

			
			static std::map<std::string, jclass>						s_mapLoadedJavaClasses;

			static JavaVM*												s_pVM;
			static void*												s_pNativeWindow;
            static jclass                                               s_AndroidUtils;         
			
			static acp_utils::helpers::ConnectionType					s_ConnectionType;
			static acp_utils::helpers::UserLocation						s_UserLocation;
			static acp_utils::helpers::SystemPath						s_SystemPaths;
			
			static acp_utils::helpers::HardwareIdentifiers				s_HwIdentif;
			static acp_utils::helpers::SoftwareIdentifiers              s_SwIdentif;
			static acp_utils::helpers::DisplayInfo						s_DisplayInfo;
			static acp_utils::helpers::GameSpecificIdentifiers			s_GameSpecificIdentif;
			
            static acp_utils::helpers::BatteryInfo                      s_BatteryInfo;	
			
			static void SetJavaVM(JavaVM*);
			static void SetNativeWindow(void*);
			
			static void SetConnectionType(const acp_utils::helpers::ConnectionType&);
			static void SetSystemPath(const acp_utils::helpers::SystemPath&);

			static void SetHardwareIdentifiers(const helpers::HardwareIdentifiers&);
			static void SetSoftwareIdentifiers(const helpers::SoftwareIdentifiers&);
			static void SetUserLocation(const helpers::UserLocation&);
			static void SetGameSpecificIdentifiers(const helpers::GameSpecificIdentifiers&);
			static void SetDisplayInfo(const helpers::DisplayInfo&);

            static void SetBatteryInfo(const helpers::BatteryInfo&);

		public:

			/**************************API************************/

			///This function will check for any pending exceptions in JNI. 
			/**	
			*	@note [Native C++ call]
			*
			*	It is more of use when debugging a weird crash related to JNI, and shouldn't be included in the final version of the code.
			*	
			*	The call to the function itself will also clear any JNI exception after showing it, so JNI calls will continue to function correctly after this call, even if previous to CheckForException call correct JNI calls were crashing.
			*/
			static void							Jni_CheckForExceptions();

			///This function will send your application to background, initiating a normal interrupt flow. 
			/**
			*	@note [Java call]
			*	
			*	@see ExitApplication
			*	
			*	It should be used for special cases, like when pressing back button during the first logo screen (according to QA checklist, the application should be minimized).
			*/
			static void							MinimizeApplication();

			///This function will kill the application, with an optional flag for restarting the application.
			/**
			*	@note [Java call]
			*
			*	@see MinimizeApplication
			*	
			*	@param restart [in][optional] When specified, true will restart the application, false will close the application.
			*/
			static void							ExitApplication(bool restart = false);

			///This function will retrieve a resource from the asset folder in the apk.
			/**
			*	@note [Java call]
			*	
			*	@attention The function is slower then normal sdcard access and should be used mostly for retrieving a config file stored in the apk.
			*	@param name [in] The full name of the resource, example "events.json"
			*	@return A raw byte array containing the resource requested. (char is signed on Android if -fsigned-char flag is active)
			*/
			static std::vector<char>			GetAssetResource(const std::string& path);

			///This function provides the user to access meta data values declared in the android manifest
			/**
			*	@note [Java call]
			*
			*	@attention The function will retrieve the value from a meta tag declared in the manifest, following the meta-data format: http://developer.android.com/guide/topics/manifest/meta-data-element.html
			*	@param name key [in] The key associated with the android:name from the mata-data
			*/
			static std::string					GetMetaDataValue(const std::string& key);
			
			///This function allows the game to start listening for location changes.
			/**
			*	@note [Java call]
			*
			*	@see GetUserLocation()
			*
			*	The function allows the game to start listening for location changes.
			*	Must be followed by a call to GetUserLocation() to retrieve the actual location of the user.
			*	@attention The default status for the location is "disabled".
			*/
			static void							EnableUserLocation();
			
			///This function allows the game to start listening for location changes.
			/**
			*	@note [Java call]
			*
			*	The function allows the game to stop listening for location changes.
			*	Must be followed by a call to GetUserLocation() to retrieve the actual location of the user.
			*	@attention The default status for the location is "disabled".
			*/
			static void							DisableUserLocation();
	
	
			///This function will return last known user location 
			/**	
			*	@note [Native C++ call]
			*
			*	@see acp_utils::helpers::UserLocation
			*	@see EnableUserLocation
			*
			*	@return the UserLocation status
			*
			*/
			static acp_utils::helpers::UserLocation					GetUserLocation();			

			

			///This function unzips an archive from a path to a destination
			/**
			*	@note [Java call]
			*	
			*	@see DeleteFile
			*	@see RemoveDirectoryRecursively
			*	
			*	@param path [in] The path towards the archive
			*	@param destination [in] The path where the contents of the archive will be unzipped.
			*	@return true for success, false for failure
			*	@todo change const char* to const std::string&
			*/
			static bool							GenericUnzipArchive(const char *path, const char *destination);

			///Deletes a file from the filesystem
			/**
			*	@note [Java call]
			*
			*	@see GetMetaDataValue
			*	@see RemoveDirectoryRecursively
			*	
			*	@param path [in] The path towards the file to be deleted
			*/
			static void							DeleteFile(const char *path);

			///Recuiresefly deletes folders from a certain path
			/**
			*	@note [Java call]
			*
			*	@see GetMetaDataValue
			*	@see DeleteFile
			*
			*	@param path [in] The path where all directories will be deleted recurively
			*	@todo change const char* to const std::string&
			*/
			static bool							RemoveDirectoryRecursively(const char *path);

			///This function will save an integer value in the local database system. (will not be shared between applications)
			/**
			*	@note [Java call]
			*
			*	@see SavePreferenceInt
			*	@see SavePreferenceLong
			*	@see SavePreferenceBool
			*	@see SavePreferenceString
			*	@see SavePreferenceBundle
			*
			*	@see ReadSharedPreferenceInt
			*	@see ReadSharedPreferenceLong
			*	@see ReadSharedPreferenceBool
			*	@see ReadSharedPreferenceString
			*	@see ReadSharedPreference
			*
			*	@see SharedPreferenceContainer
			*
			*	@param spc [in] The shared preference container, which is a pair of key and name, defining the database and the entry in the database. 
			*	@param value [in] The integer value to be stored.
			*/
			static void							SavePreferenceInt(const acp_utils::helpers::SharedPreferenceContainer& spc, int value);

			///This function will save a long value in the local database system. (will not be shared between applications)
			/**
			*	@note [Java call]
			*
			*	@see SavePreferenceInt
			*	@see SavePreferenceLong
			*	@see SavePreferenceBool
			*	@see SavePreferenceString
			*	@see SavePreferenceBundle
			*
			*	@see ReadSharedPreferenceInt
			*	@see ReadSharedPreferenceLong
			*	@see ReadSharedPreferenceBool
			*	@see ReadSharedPreferenceString
			*	@see ReadSharedPreference
			*
			*	@see SharedPreferenceContainer
			*
			*	@param spc [in] The shared preference container, which is a pair of key and name, defining the database and the entry in the database.
			*	@param value [in] The long type value to be stored.
			*/
			static void							SavePreferenceLong(const acp_utils::helpers::SharedPreferenceContainer& spc, long long value);

			///This function will save a boolean value in the local database system. (will not be shared between applications)
			/**
			*	@note [Java call]
			*
			*	@see SavePreferenceInt
			*	@see SavePreferenceLong
			*	@see SavePreferenceBool
			*	@see SavePreferenceString
			*	@see SavePreferenceBundle
			*	@see ReadSharedPreferenceInt
			*	@see ReadSharedPreferenceLong
			*	@see ReadSharedPreferenceBool
			*	@see ReadSharedPreferenceString
			*	@see ReadSharedPreference
			*
			*	@see SharedPreferenceContainer
			*
			*	@param spc [in] The shared preference container, which is a pair of key and name, defining the database and the entry in the database.
			*	@param value [in] The boolean type value to be stored.
			*/
			static void							SavePreferenceBool(const acp_utils::helpers::SharedPreferenceContainer& spc, bool value);

			///This function will save a string value in the local database system. (will not be shared between applications)
			/**
			*	@note [Java call]
			*
			*	@see SavePreferenceInt
			*	@see SavePreferenceLong
			*	@see SavePreferenceBool
			*	@see SavePreferenceString
			*	@see SavePreferenceBundle
			*	@see ReadSharedPreferenceInt
			*	@see ReadSharedPreferenceLong
			*	@see ReadSharedPreferenceBool
			*	@see ReadSharedPreferenceString
			*	@see ReadSharedPreference
			*
			*	@see SharedPreferenceContainer
			*
			*	@param spc [in] The shared preference container, which is a pair of key and name, defining the database and the entry in the database.
			*	@param value [in] The string to be stored.
			*/
			static void							SavePreferenceString(const acp_utils::helpers::SharedPreferenceContainer& spc, const char* value);

			///This function will save a bundle (http://developer.android.com/reference/android/os/Bundle.html) value in the local database system. (will not be shared between applications)
			/**
			*	@note [Java call]
			*
			*	@see SavePreferenceInt
			*	@see SavePreferenceLong
			*	@see SavePreferenceBool
			*	@see SavePreferenceString
			*	@see SavePreferenceBundle
			*	@see ReadSharedPreferenceInt
			*	@see ReadSharedPreferenceLong
			*	@see ReadSharedPreferenceBool
			*	@see ReadSharedPreferenceString
			*	@see ReadSharedPreference
			*
			*	@see SharedPreferenceContainer
			*
			*	@param spc [in] The shared preference container, which is a pair of key and name, defining the database and the entry in the database.
			*	@param value [in] The bundle (jobject) type value to be stored.
			*/
            static void                         SavePreferenceBundle(const jobject bundle);
	
			///This function will retrieve an integer value from the local database system.
			/**
			*	@note [Java call]
			*
			*	@see SavePreferenceInt
			*	@see SavePreferenceLong
			*	@see SavePreferenceBool
			*	@see SavePreferenceString
			*	@see SavePreferenceBundle
			*	@see ReadSharedPreferenceInt
			*	@see ReadSharedPreferenceLong
			*	@see ReadSharedPreferenceBool
			*	@see ReadSharedPreferenceString
			*	@see ReadSharedPreference
			*
			*	@see SharedPreferenceContainer
			*
			*	@param spc [in] The shared preference container, which is a pair of key and name, defining the database and the entry in the database.
			*	@param defValue [in] A default value, which will be returned in case the SharePreferenceContainer doesn't correspund to an entry in the database
			*	@return The integer value retrieved from the local database, associated with the SharedPreferenceContainer.
			*/
			static int							ReadSharedPreferenceInt(const acp_utils::helpers::SharedPreferenceContainer& spc, int defValue);

			///This function will retrieve a long value from the local database system.
			/**
			*	@note [Java call]
			*
			*	@see SavePreferenceInt
			*	@see SavePreferenceLong
			*	@see SavePreferenceBool
			*	@see SavePreferenceString
			*	@see SavePreferenceBundle
			*	@see ReadSharedPreferenceInt
			*	@see ReadSharedPreferenceLong
			*	@see ReadSharedPreferenceBool
			*	@see ReadSharedPreferenceString
			*	@see ReadSharedPreference
			*
			*	@see SharedPreferenceContainer
			*
			*	@param spc [in] The shared preference container, which is a pair of key and name, defining the database and the entry in the database.
			*	@param defValue [in] A default value, which will be returned in case the SharePreferenceContainer doesn't correspund to an entry in the database
			*	@return The long type value retrieved from the local database, associated with the SharedPreferenceContainer.
			*/
			static long long					ReadSharedPreferenceLong(const acp_utils::helpers::SharedPreferenceContainer& spc, long defValue);//type, key, defaultValue, preferenceName

			///This function will retrieve a boolean value from the local database system.
			/**
			*	@note [Java call]
			*
			*	@see SavePreferenceInt
			*	@see SavePreferenceLong
			*	@see SavePreferenceBool
			*	@see SavePreferenceString
			*	@see SavePreferenceBundle
			*	@see ReadSharedPreferenceInt
			*	@see ReadSharedPreferenceLong
			*	@see ReadSharedPreferenceBool
			*	@see ReadSharedPreferenceString
			*	@see ReadSharedPreference
			*
			*	@see SharedPreferenceContainer
			*
			*	@param spc [in] The shared preference container, which is a pair of key and name, defining the database and the entry in the database.
			*	@param defValue [in] A default value, which will be returned in case the SharePreferenceContainer doesn't correspund to an entry in the database
			*	@return The boolean type value retrieved from the local database, associated with the SharedPreferenceContainer.
			*/
			static bool							ReadSharedPreferenceBool(const acp_utils::helpers::SharedPreferenceContainer& spc, bool defValue);//type, key, defaultValue, preferenceName

			///This function will retrieve a string from the local database system.
			/**
			*	@note [Java call]
			*
			*	@see SavePreferenceInt
			*	@see SavePreferenceLong
			*	@see SavePreferenceBool
			*	@see SavePreferenceString
			*	@see SavePreferenceBundle
			*	@see ReadSharedPreferenceInt
			*	@see ReadSharedPreferenceLong
			*	@see ReadSharedPreferenceBool
			*	@see ReadSharedPreferenceString
			*	@see ReadSharedPreference
			*
			*	@see SharedPreferenceContainer
			*
			*	@param spc [in] The shared preference container, which is a pair of key and name, defining the database and the entry in the database.
			*	@param defValue [in] A default value, which will be returned in case the SharePreferenceContainer doesn't correspund to an entry in the database
			*	@return The string retrieved from the local database, associated with the SharedPreferenceContainer.
			*/
			static std::string					ReadSharedPreferenceString(const acp_utils::helpers::SharedPreferenceContainer& spc, const std::string& defValue);//type, key, defaultValue, preferenceName

			///This function will remove a key from the local database system.
			/**
			*	[Java call]
			*
			*	@see SavePreferenceInt
			*	@see SavePreferenceLong
			*	@see SavePreferenceBool
			*	@see SavePreferenceString
			*	@see SavePreferenceBundle
			*	@see ReadSharedPreferenceInt
			*	@see ReadSharedPreferenceLong
			*	@see ReadSharedPreferenceBool
			*	@see ReadSharedPreferenceString
			*	@see ReadSharedPreference
			*
			*	@see SharedPreferenceContainer
			*
			*	@param spc [in] The shared preference container, which is a pair of key and name, defining the database and the entry in the database.
			*	@param defValue [in] A default value, which will be returned in case the SharePreferenceContainer doesn't correspund to an entry in the database
			*	@return The string retrieved from the local database, associated with the SharedPreferenceContainer.
			*/
			static void					RemoveSharedPreference(const acp_utils::helpers::SharedPreferenceContainer& spc);

			///This function will retrieve a bundle (http://developer.android.com/reference/android/os/Bundle.html) value from the local database system.
			/**
			*	@note [Java call]
			*
			*	@see SavePreferenceInt
			*	@see SavePreferenceLong
			*	@see SavePreferenceBool
			*	@see SavePreferenceString
			*	@see SavePreferenceBundle
			*	@see ReadSharedPreferenceInt
			*	@see ReadSharedPreferenceLong
			*	@see ReadSharedPreferenceBool
			*	@see ReadSharedPreferenceString
			*	@see ReadSharedPreference
			*
			*	@see SharedPreferenceContainer
			*
			*	@param spc [in] The shared preference container, which is a pair of key and name, defining the database and the entry in the database.
			*	@param defValue [in] A default value, which will be returned in case the SharePreferenceContainer doesn't correspund to an entry in the database
			*	@return The bundle object retrieved from the local database, associated with the SharedPreferenceContainer.
			*/
            static jobject                      ReadSharedPreference(jobject& bundle);
	
            
            ///This function will open the welcome screen. 
			/**
			*	@note [Java call]
			*	@note [Starts a new Activity]
			*	@note [Triggers an interrupt]
			*	@req Requires a callback implementation. Check GameFunctionsToImplement.h
			*	@req Requires USE_WELCOME_SCREEN define active.
			*	
			*	This function is usefull for non-CRM shops. For CRM Welcome Screens use Online Libs (Gaia, Glot).
			*	
			*	@see acp_utils::helpers::Language
			*	@see splashScreenFunc
			*	
			*	Refer to the Welcome Screen for more documentation on implementation guidelines.
			*	The Welcome Screen will be launched from the main UI thread, not from the calling thread.
			*	
			*	The Language enum matches the Android Core Package format, so game-specific translations between game-specific language to acp-specific language enum should be performed.
			*	Once the Welcome Screen is closed, the callback will be received in splashScreenFunc(const char*), so make sure you implement such a function. The same naming convention is used for iOS, so you should have the game-specific code from iOS.
			*	@param lang [in] The language recognized in which the Welcome Screen will displayed, in Android Core Package recognizable format.
			*/
            static void                         ShowWelcomeScreen(acp_utils::helpers::Language lang);


			///This function will display a system pop - up with the message “You cannot go back at this stage”.
			/**
			*	@note [Java call]
			*	
			*	The pop-up will be localized in the device language, as it is a system pop-up.
			*
			*	@todo check if we can change the function to use acp_utils::helpers::Language, and display the pop-up in game language instead of system language
			*/
			static void							ShowCannotGoBack();

			///This function will display a picture present in the apk resource raw folder, overlaying your game.
			/**
			*	@note [Java call]
			*	
			*	You will have to specify the resId (which is associated with the R.java resource IDs).
			*	If a resId bellow 0 is specified, the implementation will assume a Gameloft logo exists with the name gameloft_logo in your raw folder, and try to display it.
			*	Calling this function will not call an interrupt (onPause) in your game, and code execution will continue in game-specific code.
			*	The logo will start displaying from the main UI thread, not from the calling thread, so the call isn’t blocking.
			*	
			*	@see CloseLogo
			*/
			static void							ShowLogo(const int& resId);

			///This function will close any logo previously displayed with ShowLogo. It will be a no-op if ShowLogo wasn’t previously called, but will trigger a log error in output.
			/**
			*	@note [Java call]
			*
			*	@see ShowLogo
			*/
			static void							CloseLogo();
			
            ///This function will open a browser with the exact url mentioned.
			/**
			*	@note [Java call]
			*	@note [Starts a webview]
			*	@note [Triggers an interrupt]
			*	
			*	The browser will open in the main UI thread, not in the calling thread.
			*	Warning! Not to be confused with the LanchBrowser from InGameBrowser, which relates to the redirects from ingameads URLs.
			*	@param url [in] The full URL path for the browser.
			*	@return True for success, false for failure.
			*/
			static bool                         LaunchBrowser(const char* url);

			///HTTP Execute Asynchonously
			/**
			*	@note [Java call]
			*	
			*	@todo	add documentation for this function
			*/
			static void							HttpExecuteAsync(const char* url);

            ///This function will open a video from a file located in the asset folder from the apk. 
			/**
			*	@note [Java call]
			*	@note [Starts a new Activity]
			*	@note [Triggers an interrupt]
			*	@req Requires USE_VIDEO_PLAYER define active.
			*
			*	@param filename [in] The asset name, located in the assets folder from the apk.
			*	@return True for success, false for failure if the video player will launch.
			*	@todo In the VideoPlayerPlugin.java, Notify someone video is done. The callback that is supposed to be called when the video is finished is not implemented. This can be handled by the fact that the video pauses/resumes the activity.
			*	@todo change const char* to const std::string&
			*/
            static bool                         LaunchVideoPlayer(const char* filename);	
			
			///This function will report if a video launched by LaucnhVideoPlayer has finished playing or not
			/**
			*	@note [Java call]
			*
			*	@req Requires USE_VIDEO_PLAYER define active.
			*
			*	@see LaunchVideoPlayer
			*
			*	@return True for video is no longer playing. False for video is still playing
			*/
            static bool                         IsVideoCompleted();	

			
			/*************************Getters************************/
			
			///This function will return a jclass object, pointing to a java class set in class_list.inl
			/**	
			*	@note [Native C++ call]
			*
			*	Make sure the class you are trying to retrieve has been prior added to the class_list.inl. Otherwise, AF will report an error in output, and the return will be null.
			*	
			*	@param i_class [in] The jclass name, identical to how is declared in class_list.inl
			*	@return the jclass requested, or NULL if the class was not declared in class_list.inl, or it is not found in the jclasses loaded by AF
			*
			*/
			static jclass											GetClass(std::string i_class);

			///This function will return the JavaVM object
			/**	
			*	@note [Native C++ call]
			*
			*	@return the Java VM object used for JNI operations. 
			*
			*/
			static JavaVM*											GetJavaVm();
			
			
			///This function will return a void pointer, that can be converted to ANativeWindow*. It can also return null if no SurfaceView is active in Java.
			/**	
			*	@note [Native C++ call]
			*
			*	Function that is used from ANativeWindow*. This call is mostly handled by the entry point in game engines. Games shouldn't require it explicitly. 
			*
			*	@return the SurfaceView last sent from Java. This object can be converted to ANativeWindow*.
			*
			*/
			static void*											GetNativeWindow();
			
			
			///This function will return the internet state, in Connection Type
			/**	
			*	@note [Native C++ call]
			*
			*	@see acp_utils::helpers::ConnectionType (eNoConnectivity, eConnectivityWifi, eConnectivityBlueTooth, eConnectivityDummy, eConnectivityEthernet, eConnectivityWimax, eConnectivity2g, eConnectivity3g, eConnectivity4g, eConnectivityUnknown)
			*
			*	@return the ConnectionType
			*
			*/
			static acp_utils::helpers::ConnectionType				GetConnectionType();		

			
			// Paths
			
			///This function will deliver the path to the sdcard of the device. This shouldn't be used for saving or getting resources in actual builds, as anything saved on this path will not be deleted after the user uninstalls the application.
			/**
			*	@note [Native C++ call]
			*
			*	@deprecated This function requires ACCESS_EXTERNAL_STORAGE permission, which is hinted by Google to be banned in the future. 
			*
			*	@return The path to the sdcard. Example of return value: /storage/emulated/0
			*/
			static const std::string&								GetSdCardPath();
			
			///This function will deliver the path to the obb folder of your application. Here the GEF enabled games will find the obb resources.
			/**
			*	@note [Native C++ call]
			*
			*	@return The path to the obb folder. Example of return value: /storage/emulated/0/Android/obb/com.gameloft.android.ANMP.GloftCNRL
			*
			*/
			static const std::string&								GetObbFolderPath();
			
			
			///This function will deliver the path to the data folder of your application. The data folder is the normal place to keep game data, if not using GEF. The data folder gets cleared if the user clears the data from the settings.
			/**
			*	@note [Native C++ call]
			*
			*	@return The path to the data folder. Example of return value: /storage/emulated/0/Android/data/com.gameloft.android.ANMP.GloftCNRL/files
			*
			*/
			static const std::string&								GetDataFolderPath();
			
			///This function will deliver the path to the private folder of your application. Here is the best place to keep your save files. Other applications cannot access this folder.
			/**
			*	Folder can only be accessed by users or other applications then the calling app only with routed access
			*	@note [Native C++ call]
			*
			*	@return The path to the save folder. Example of return value: /data/data/com.gameloft.android.ANMP.GloftCNRL/files
			*
			*/
			static const std::string&								GetSaveFolderPath();
			
			///This function will deliver the path to the cache folder of your application. This folder is good to keep small files, for a temporary time, like a temporary downloaded config.
			/**
			*	Folder can only be accessed by users or other applications then the calling app only with routed access
			*	@note [Native C++ call]
			*
			*	@attention In extreme situations, the OS can decide to clear this folder, if the application is not in foreground. The user can also manually delete an application cache and delete the content of this folder.
			*
			*	@return The path to the cache folder. Example of return value: /data/data/com.gameloft.android.ANMP.GloftCNRL/cache
			*
			*/
			static const std::string&								GetCacheFolderPath();
			
			///This function will return the game name as it is found in the package name, without com.gameloft.android
			/**
			*	@note [Native C++ call]
			*	@return The game name, with the following format: OPERATOR.GloftGGC_GAME_CODE. Example of return value: ANMP.GloftCNRL
			*/
			static const std::string&								GetGameName();				
			
			
			///This function will return the default IGP code. Used for HEP builds, when injecting a new IGP, to keep track of both the default IGP and the injected IGP.
			/**
			*	@note [Native C++ call]
			*	@req Requires USE_HEP_EXT_IGPINFO define active.
			*	@return The default IGP value, or an empty string if USE_HEP_EXT_IGPINFO not enabled.
			*/
			static const std::string&     							GetDefaultIGP();			
			
			
			///This function will return the injected IGP code. Used for HEP builds when injecting a new IGP.
			/**
			*	@note [Native C++ call]
			*	@req Requires USE_HEP_EXT_IGPINFO define active.
			*	@return The injected IGP code, or an empty string if USE_HEP_EXT_IGPINFO not enabled.
			*/
			static const std::string& 								GetInjectedIGP();			//returns the injected igp if present (for GP builds) or empty string otherwise
			
			
			///This function will return the injected IGP code. Used for HEP builds when injecting a new IGP.
			/**
			*	@note [Native C++ call]
			*	@req Requires USE_HEP_EXT_IGPINFO define active.
			*	@return The injected IGP code, or an empty string if USE_HEP_EXT_IGPINFO not enabled.
			*/
			static const std::string&								GetInjectedSerialKey();		//returns the injected serialKey if present (for GP builds) or empty string otherwise
			
			
			// Software identifiers	
			
			///This function will return the carrier name for a device connected to a GSM network.
			/**
			*	@note [Native C++ call]
			*	@return The carrier name if a carrier exists, or an empty string.
			*/
            static const std::string&                               GetCarrierName();
			
			///The country associated with the device. 
			/**
			*	This function will return the country, based on SIM, and has a fallback on the Locale parameter for users without SIM.
			*	@note [Native C++ call]
			*	@return The country associated with the device. Example of return value: "US"
			*/
            static const std::string&                               GetCountry();
			
			///The device language set by the user
			/**
			*	This function will return the device language set by the user, from the Locale parameter.
			*	@note [Native C++ call]
			*	@return The language associated with the device. Example of return value: “en”
			*/
            static const std::string&                               GetDeviceLanguage();
			
			
			///This function will return the webview browser agent
			/**
			*	This function shouldn’t be necessary for game teams. It is exposed mostly for lib usage.
			*	@note [Native C++ call]
			*	@return The default webview user agent (browser build) associated with the device. Example of return value: “Mozilla/5.0 (Linux; U; Android 4.3; en-us; Galaxy Nexus Build/JWR66Y) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30”
			*/
            static const std::string&								GetWebviewUserAgent();
			
			
			///This function will return the ad id of the user
			/**
			*	This function shouldn’t be necessary for game teams. It is exposed mostly for lib usage.
			*	@note [Native C++ call]
			*	@return The Google AD ID associated with the device. 
			*/
            static const std::string&								GetGoogleAdId();
			
			///This function can be queried to find out the current status of GoogleAdId status. The Google AdId can be in unretrieved state when launching the application.
			/**
			*	This function shouldn’t be necessary for game teams. It is exposed mostly for lib usage.
			*	@note [Native C++ call]
			*	@see acp_utils::helpers::GAIDStatus
			*	@return An acp_utils::helpers::GAIDStatus object that reflects the current status of the Google AdId status
			*/
			static const acp_utils::helpers::GAIDStatus				GetGoogleAdIdStatus();
			
			///Uncommented function. Exposed in the public API, but for internal usage. Too lazy to make the class that uses a friend. (best pal)
			static const std::string&								GetApkPath();
			
			
            // Hardware identifiers
			
			///This function will return the android id of the device
			/**
			*	This function shouldn’t be necessary for game teams. It is exposed mostly for lib usage.
			*	@note [Native C++ call]
			*	@return The Android ID associated with the device. Example of return value: "781ae4961b5ab46"
			*/
			static const std::string&								GetAndroidId();
			
			
			///This function will return the serial number of the device
			/**
			*	This function shouldn’t be necessary for game teams. It is exposed mostly for lib usage.
			*	@note [Native C++ call]
			*	@return The Serial No associated with the device. Example of return value: “0146BF511501A004”
			*/
			static const std::string&								GetSerial();	         //returns the Serial of the device
			
			///This function will return the CPU serial number of the device
			/**
			*	This function shouldn’t be necessary for game teams. It is exposed mostly for lib usage.
			*	@note [Native C++ call]
			*	@return The CPU Serial No. In some cases, it is identical to GetSerial, only in low-caps. On some devices, the CPU serial might come empty. Example of return value: “0146bf511501a004”. For all intents and purposes, use GetSerial() instead. 
			*/
            static const std::string&                               GetCPUSerial();          //returns the CPU serial (TODO -- Complete here with what that means?!)
			
			
			///This function will return the device manufacturer of the device, as reported by the API
			/**
			*	@note [Native C++ call]
			*	@return The manufacturer string reported by the device: Example of return value: “samsung”
			*/
            static const std::string&                               GetDeviceManufacturer(); 
			
			
			///This function will return the device model of the device
			/**
			*	@note [Native C++ call]
			*	@return The device model combined with a + string. Example of return value: “Galaxy+Nexus”
			*/
            static const std::string&                               GetDeviceModel();        //returns the device model
			
			
			///This function will return the build product number, as reported by the API in Build.PRODUCT
			/**
			*	@note [Native C++ call]
			*	@return The OS build product string reported by the device. Example: "yakju"
			*/
            static const std::string&                               GetBuildProduct();      
			
			
			///This function will return the build device build number, as reported by the API in Build.DEVICE
			/**
			*	@note [Native C++ call]
			*	@return The OS build device string reported by the device. Example: "maguro"
			*/
            static const std::string&                               GetBuildDevice();        //returns the Build.DEVICE value
			
			
			///This function will return the firmware number, as reported by the OS API
			/**
			*	@note [Native C++ call]
			*	@return The OS firmware in numbers. Example of return value: “4.3”
			*/
			static const std::string&								GetFirmware();	         //returns the Firmware of the device
			
			///This function will return the WiFi MAC Address number, as reported by the OS API
			/**
			*	@note [Native C++ call]
			*	@return The Wi-Fi MAC address. Example: “a0:0b:ba:b4:d7:58”
			*/
			static const std::string&								GetMacAddress();         //returns the MacAddress of the device
			
			///This function will return the IMEI number, as reported by the OS API
			/**
			*	@note [Native C++ call]
			*	@req Requires READ_PHONE_STATE permission. 
			*	@attention Permission required is not accepted on Google Play and Amazon Store. The permission is permitted on other shops.
			*	@deprecated As the permission is banned by Google, the standard identifier is HDIDFV. 
			*	@see GetHDIDFVStr()
			*	@return The IMEI of the device, if available. In case the device doesn’t have a SIM, or READ_PHONE_STATE isn’t enable, the function will return an empty string. Example of return value: “358350040453612” 
			*/
			static const std::string&								GetIMEI();
			
			///This function will return the HDIDFV string. HDIDFV is Gameloft's unique identifier, softawre generated.
			/**
			*	@note [Native C++ call]
			*	@return The HDIDFV for the current device. Example of return value: “67b0c7f4-14a7-49de-86b6-37126d489c09” 
			*/
			static const std::string&								GetHDIDFVStr();	         //returns the generated/saved HDIDFV (empty string if HDIDFV_UPDATE=0)       
			
			
			///This function will return the width in Inch of the screen.
			/**
			*	@note [Native C++ call]
			*	@return width in inch
			*/
			static const float										GetWidthInInch();
			
			///This function will return the height in Inch of the screen.
			/**
			*	@note [Native C++ call]
			*	@return height in inch
			*/
			static const float										GetHeightInInch();	
			
			///This function will return the width in pixels of the screen.
			/**
			*	@note [Native C++ call]
			*	@return width in pixels
			*/
			static const int&										GetWidth();			
			
			///This function will return the height in pixels of the screen.
			/**
			*	@note [Native C++ call]
			*	@return height in pixels
			*/
			static const int&										GetHeight();		
		

            // Battery info getters
			
			///This function will return a cached value set by the battery state listaner, stating if the battery is charging or not
			/**
			*	@note [Native C++ call]
			*	@see GetIsUsbCharging()
			*	@see GetIsACCharging()
			*	@see GetBatteryStatus()
			*	@return True if the device is currently being charged. False if it is not.
			*/
            static const bool                                       GetIsBatteryCharging();
			
			///This function will return a cached value set by the battery state listaner, stating if the battery is charging from a USB charger
			/**
			*	@note [Native C++ call]
			*	@see GetIsBatteryCharging()
			*	@see GetIsACCharging()
			*	@return If the device is charging via USB connection (from a PC), this will return true. If the device is charging via charger, or not charging at all, it will return false.
			*/
            static const bool                                       GetIsUsbCharging();
			
			///This function will return a cached value set by the battery state listaner, stating if the battery is charging from an AC adapter.
			/**
			*	@note [Native C++ call]
			*	@see GetIsBatteryCharging()
			*	@see GetIsUsbCharging()
			*	@return If the device is charging via AC adapter, the call will return true. If the device is charging via other means, or not charging at all, it will return false.
			*/          
            static const bool                                       GetIsACCharging();
			
			///This function will return an int representing the precentega of the battery
			/**
			*	@note [Native C++ call]
			*	@see GetIsUsbCharging()
			*	@see GetIsACCharging()
			*	@see GetBatteryStatus()
			*	@return  This will return the percentage of the battery. It will take int values between [0, 100]
			*/
            static const int                                        GetBatteryStatus();

			
			///This will lock or unlock your current orientation. Useful when in Action Phase you need to lock orientation, and unlock it in menus. 
			/**
			*	@note [Java call]
			*	@param i_bLock [in] Set this parameter to true to lock your current orientation. false to unlock it.
			*/
			static void												SetOrientationState(bool i_bLock);
			
			///Controls whether the screen should remain litten or not.
			/**
			*	@note [Java call]
			*	@param i_bKeepScreen [in] Set this parameter to true to keep your screen always on. Off to disable the option. 
			*/
			static void 											SetKeepScreenOn(bool i_bKeepScreen);

			
			
			//Application Statistics info
			
			///This is for internal use
			/**
			* @todo: remove from public API
			* usage: ReadInfoFromSystemFile("/system/build.prop", "ro.board.platform", "=");
			*/
			static std::string										ReadInfoFromSystemFile(const char* path, const char* property, const char* splitter);
			
			///This function will return how much space is available, in Bytes.
			/**
			*	@note [Java call]
			*	@return The amount of bytes free on the disk. Example of return value: 14495514624
			*/
			static unsigned long long								GetDiskFreeSpace();

			
			///This function will return the number of usable CPU cores in the OS, no matter if they are active or not
			/**
			*	@note [Native C++ call]
			*	@return The number of CPU cores.
			*/
			static int												GetNumberOfCpuCores();
			
			///This function will return the current CPU speed in the first core (cpu0)
			/**
			*	@note [Native C++ call]
			*	@attention The CPU speed can fluctuate. For max cpu, use GetMaxCpuSpeedInHz
			*	@see GetMaxCpuSpeedInHz()
			*	@return The current CPU speed for the first core, in Hz. Example of return value: 1200000 
			*/
			static int												GetCurrentCpuSpeedInHz();
			
			///This function will return the maximum CPU speed for the first core (cpu0)
			/**
			*	@note [Native C++ call]
			*	@return  The maximum speed for the first core. Example of return value: 1200000
			*/
			static int												GetMaxCpuSpeedInHz();
			
			///This function will return the chipset code of the device
			/**
			*	@note [Native C++ call]
			*	@return The device chipset name. It is used for device detection. Example of return value: “omap4”
			*/
			static std::string										GetDeviceChipset();
		
		
			///This function will return the architecture code for the device
			/**
			*	@note [Native C++ call]
			*	@return The device architecture name. It is used for device detection. Example of return value: “0×41”
			*/
			static std::string										GetDeviceArchitecture();
		
		
			///This function will return the micro-architecture code for the device
			/**
			*	@note [Native C++ call]
			*	@return The device micro-architecture name. It is used for device detection. Example of return value: “0xc09”
			*/
			static std::string										GetDeviceMicroArch();

			
			///This function will return the current RAM available on the device in Mega Bytes
			/**
			*	@note [Native C++ call]
			*	@return The currently available value of MB of RAM reported by the device. Used for profile detection. Example of return value: 128.57
			*/
			static float											GetCurrentAvailableRamInMegaBytes();
			
			///This function will return the maximum RAM reported by the OS
			/**
			*	@note [Native C++ call]
			*	@return The maximum value of MB of RAM reported by the device. Used for profile detection. Example of return value: 694.297
			*/
			static float											GetMaxAvailableRamInMegaBytes();
			

			///This function will return if the device is routed
			/**
			*	@note [Native C++ call]
			*	@return True if the device is found with root access. False otherwise
			*/
			static bool												IsDeviceRouted();
			
			///This function will return if the application is signed with the gameloft devloper key
			/**
			*	@note [Native C++ call]
			*	@return True if the application is signed with the developer key. Fasle otherwise
			*/
			static bool												IsAppEnc();

#if AMAZON_STORE
			///This function is used for Kindle device detection
			/**
			*	@note [Native C++ call]
			*	@req AMAZON_STORE define active.
			*	@return True if it’s a Kindle device. False otherwise.
			*/
			static bool												IsDeviceKindle(); 
			
			///This call will hide the Kindle Bar.
			/**
			*	@note [Java call]
			*	@req AMAZON_STORE define active.
			*/
			static void												HideKindleBar();
			
			///This function will show the Kindle Bar
			/**
			*	@note [Java call]
			*	@req AMAZON_STORE define active.
			*/
			static void												ShowKindleBar();
#endif //AMAZON_STORE

		};	
		
	};//end namespace api	

	//the hackis part. we need a GetVM function in acp_utils:
	JavaVM* GetVM();

};//end namespace acp_utils

#endif //__PACKAGE_UTILS_H__
