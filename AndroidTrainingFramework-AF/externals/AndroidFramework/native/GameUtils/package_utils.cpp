#include "package_utils.h"

#include "_internal\internal.h"
#include "_internal\logger.h"

#include <algorithm>

#include <cstdio>
#include <cstdlib>
#include <fstream>
#include <dirent.h>
#include <sys/stat.h>

namespace acp_utils
{
	//static variables:
	JavaVM*											api::PackageUtils::s_pVM						= 0;
	void*											api::PackageUtils::s_pNativeWindow				= 0;
    jclass                                          api::PackageUtils::s_AndroidUtils               = 0;
	std::map<std::string, jclass>					api::PackageUtils::s_mapLoadedJavaClasses;
	helpers::SystemPath								api::PackageUtils::s_SystemPaths;
	helpers::ConnectionType							api::PackageUtils::s_ConnectionType				= helpers::eConnectivityUnknown;
	helpers::UserLocation							api::PackageUtils::s_UserLocation = {
																						helpers::eLocationUninitialized, //status
																						0,  // latitude
																						0,  // longitude
																						0,  // accuracy
																						"",  // time
																					};
																					
	helpers::HardwareIdentifiers					api::PackageUtils::s_HwIdentif;
	helpers::SoftwareIdentifiers                    api::PackageUtils::s_SwIdentif;
	helpers::DisplayInfo							api::PackageUtils::s_DisplayInfo;
    helpers::GameSpecificIdentifiers                api::PackageUtils::s_GameSpecificIdentif;
    helpers::BatteryInfo                            api::PackageUtils::s_BatteryInfo;

	namespace api
	{
		//JavaVM:
		void PackageUtils::SetJavaVM(JavaVM* vm)
		{
			s_pVM = vm;
		}
		
		JavaVM* PackageUtils::GetJavaVm()
		{
			return s_pVM;
		}

		//native window
		void										PackageUtils::SetNativeWindow(void* i_wnd)
		{
			s_pNativeWindow = i_wnd;
		}
		void*										PackageUtils::GetNativeWindow()
		{
			return s_pNativeWindow;
		}

		//Connectivity:
		void										PackageUtils::SetConnectionType(const acp_utils::helpers::ConnectionType& i_connectionType)
		{
			s_ConnectionType = i_connectionType;
		}
		acp_utils::helpers::ConnectionType			PackageUtils::GetConnectionType()
		{
			return s_ConnectionType;
		}

		//Location:
		acp_utils::helpers::UserLocation			PackageUtils::GetUserLocation()
		{
			return s_UserLocation;
		}

		//System Paths:
		void PackageUtils::SetSystemPath(const acp_utils::helpers::SystemPath& i_sysPath)
		{
			s_SystemPaths = i_sysPath;
		}	
		const std::string&							PackageUtils::GetSdCardPath()
		{
			return s_SystemPaths.sSdCard;
		}
		const std::string&							PackageUtils::GetObbFolderPath()
		{
			return s_SystemPaths.sObb;
		}
		const std::string&							PackageUtils::GetDataFolderPath()
		{
			return s_SystemPaths.sData;
		}
		const std::string&							PackageUtils::GetSaveFolderPath()
		{
			return s_SystemPaths.sSave;
		}
		const std::string&							PackageUtils::GetCacheFolderPath()
		{
			return s_SystemPaths.sTemp;
		}

		jclass PackageUtils::GetClass(std::string i_class)
		{
			std::map<std::string, jclass>::const_iterator it = s_mapLoadedJavaClasses.find(i_class);
		
			if(it == s_mapLoadedJavaClasses.end())
			{
				LOG_ERROR("Class not found. Are you sure you have added %s to class_list.inl in acp_config?", i_class.c_str());
				return 0;
			}

			return it->second;
		}

		void PackageUtils::Jni_CheckForExceptions()
		{
			JNIEnv* pEnv;

			ScopeGetEnv st(pEnv);

			jthrowable exception = pEnv->ExceptionOccurred();

			if (exception != NULL)
			{
				pEnv->ExceptionClear();

				jclass exceptionClass = pEnv->GetObjectClass(exception);

			
				jmethodID mid = pEnv->GetMethodID(GetClass("java/lang/Class"), "getName", "()Ljava/lang/String;");
				jstring exceptionName = (jstring)pEnv->CallObjectMethod(exceptionClass, mid);
				const char* exceptionNameUTF8 = pEnv->GetStringUTFChars(exceptionName, 0);

				mid = pEnv->GetMethodID(exceptionClass, "getMessage", "()Ljava/lang/String;");
				jstring exceptionMessage = (jstring)pEnv->CallObjectMethod(exception, mid);
				if (exceptionMessage != NULL)
				{
					const char* exceptionMessageUTF8 = pEnv->GetStringUTFChars(exceptionMessage, 0);
					LOG_ERROR("Exception Name + Message: %s: %s", exceptionNameUTF8, exceptionMessageUTF8);
					pEnv->ReleaseStringUTFChars(exceptionMessage, exceptionMessageUTF8);
				}
				else
				{
					LOG_ERROR("Exception Name: %s", exceptionNameUTF8);
				}
				pEnv->ReleaseStringUTFChars(exceptionName, exceptionNameUTF8);
			}
		}

		void PackageUtils::MinimizeApplication()
		{
			JNIEnv* pEnv = NULL;
			ScopeGetEnv sta(pEnv);

			jmethodID minApp = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "MinimizeApplication", "()V");
			pEnv->CallStaticVoidMethod(GetClass("/PackageUtils/AndroidUtils"), minApp);
		}

		void	PackageUtils::ExitApplication(bool restart)
		{
			JNIEnv* pEnv = NULL;
			ScopeGetEnv sta(pEnv);

			jmethodID exitApp = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "ExitApplication", "(Z)V");
			pEnv->CallStaticVoidMethod(GetClass("/PackageUtils/AndroidUtils"), exitApp, restart);
		}

		std::vector<char> PackageUtils::GetAssetResource(const std::string& path)
		{
			JNIEnv* pEnv = NULL;
			ScopeGetEnv sta(pEnv);

			jstring str = pEnv->NewStringUTF(path.c_str());
			jbyteArray mJavaArray = (jbyteArray)pEnv->CallStaticObjectMethod(GetClass("/PackageUtils/AndroidUtils"), acp_utils::acp_internal::Internal::s_getAssetId, str);
		
			std::vector<char> res;
			if(mJavaArray != NULL) 
			{
				int len = pEnv->GetArrayLength(mJavaArray);
					
				res.resize(len);
				if(len > 0)
				{
					pEnv->GetByteArrayRegion(mJavaArray, 0, len, (jbyte*)&res[0]);
				}

				pEnv->DeleteLocalRef(mJavaArray);
			}
			pEnv->DeleteLocalRef(str);
	
			return res; 
		}

		std::string PackageUtils::GetMetaDataValue(const std::string& key)
		{
			JNIEnv* pEnv = NULL;
			ScopeGetEnv sta(pEnv);

			jmethodID mGetMetaDataValue		= pEnv->GetStaticMethodID (GetClass("/PackageUtils/AndroidUtils"), "GetMetaDataValue", 		"(Ljava/lang/String;)Ljava/lang/String;");
			jstring jKey = pEnv->NewStringUTF(key.c_str());
			ScopeStringChars str(pEnv, (jstring)pEnv->CallStaticObjectMethod(GetClass("/PackageUtils/AndroidUtils"), mGetMetaDataValue, jKey));
			pEnv->DeleteLocalRef(jKey);
			return std::string(str.Get());
		}

		//Location:
		void		PackageUtils::EnableUserLocation()
		{
			JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);

            jmethodID enableUserLocation = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "EnableUserLocation", "()V");
            pEnv->CallStaticVoidMethod(GetClass("/PackageUtils/AndroidUtils"), enableUserLocation);
		}

		void		PackageUtils::DisableUserLocation()
		{
			JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);

            jmethodID disableUserLocation = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "DisableUserLocation", "()V");
            pEnv->CallStaticVoidMethod(GetClass("/PackageUtils/AndroidUtils"), disableUserLocation);
		}

		bool PackageUtils::GenericUnzipArchive(const char *path, const char *destination)
		{
			JNIEnv* pEnv = NULL;
			ScopeGetEnv sta(pEnv);

			jmethodID mGenericUnzipArchive		= pEnv->GetStaticMethodID (GetClass("/PackageUtils/AndroidUtils"), "GenericUnzipArchive", 		"(Ljava/lang/String;Ljava/lang/String;)Z");
			jstring jPath = pEnv->NewStringUTF(path);
			jstring jDest = pEnv->NewStringUTF(destination);
			bool result = pEnv->CallStaticBooleanMethod(GetClass("/PackageUtils/AndroidUtils"), mGenericUnzipArchive, jPath, jDest);
			pEnv->DeleteLocalRef(jPath);
			pEnv->DeleteLocalRef(jDest);
			return result;
		}
		
		void PackageUtils::DeleteFile(const char *path)
		{
			JNIEnv* pEnv = NULL;
			ScopeGetEnv sta(pEnv);

			jmethodID mDeleteFile		= pEnv->GetStaticMethodID (GetClass("/PackageUtils/AndroidUtils"), "DeleteFile", 		"(Ljava/lang/String;)V");
			jstring jPath = pEnv->NewStringUTF(path);
			pEnv->CallStaticObjectMethod(GetClass("/PackageUtils/AndroidUtils"), mDeleteFile, jPath);
			pEnv->DeleteLocalRef(jPath);
		}
		
		bool PackageUtils::RemoveDirectoryRecursively(const char *path)
		{
			JNIEnv* pEnv = NULL;
			ScopeGetEnv sta(pEnv);

			jmethodID mRemoveDirectoryRecursively		= pEnv->GetStaticMethodID (GetClass("/PackageUtils/AndroidUtils"), "RemoveDirectoryRecursively", 		"(Ljava/lang/String;)Z");
			jstring jPath = pEnv->NewStringUTF(path);
			bool result = pEnv->CallStaticBooleanMethod(GetClass("/PackageUtils/AndroidUtils"), mRemoveDirectoryRecursively, jPath);
			
			pEnv->DeleteLocalRef(jPath);
			return result;
		}
		
        // Shared Preferences API
        void PackageUtils::SavePreferenceInt(const acp_utils::helpers::SharedPreferenceContainer& spc, int value)
        {
			JNIEnv* pEnv;
			ScopeGetEnv st(pEnv);

			jstring j_key = pEnv->NewStringUTF(spc.key.c_str());
			jstring j_name = pEnv->NewStringUTF(spc.pName.c_str());

            jmethodID savePref = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "SavePreferenceInt", "(Ljava/lang/String;Ljava/lang/String;I)V");
            pEnv->CallStaticVoidMethod(GetClass("/PackageUtils/AndroidUtils"), savePref, 
                j_key, j_name, value);

			pEnv->DeleteLocalRef(j_key);
			pEnv->DeleteLocalRef(j_name);
        }

        void PackageUtils::SavePreferenceLong(const acp_utils::helpers::SharedPreferenceContainer& spc, long long value)
        {
			JNIEnv* pEnv;
			ScopeGetEnv st(pEnv);

			jstring j_key = pEnv->NewStringUTF(spc.key.c_str());
			jstring j_name = pEnv->NewStringUTF(spc.pName.c_str());

            jmethodID savePref = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "SavePreferenceLong", "(Ljava/lang/String;Ljava/lang/String;J)V");
            pEnv->CallStaticVoidMethod(GetClass("/PackageUtils/AndroidUtils"), savePref, 
                j_key, j_name, value);

			pEnv->DeleteLocalRef(j_key);
			pEnv->DeleteLocalRef(j_name);
        }

        void PackageUtils::SavePreferenceBool(const acp_utils::helpers::SharedPreferenceContainer& spc, bool value)
        {
			JNIEnv* pEnv;
			ScopeGetEnv st(pEnv);

			jstring j_key = pEnv->NewStringUTF(spc.key.c_str());
			jstring j_name = pEnv->NewStringUTF(spc.pName.c_str());

            jmethodID savePref = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "SavePreferenceBool", "(Ljava/lang/String;Ljava/lang/String;Z)V");
            pEnv->CallStaticVoidMethod(GetClass("/PackageUtils/AndroidUtils"), savePref, 
                j_key, j_name, value);

			pEnv->DeleteLocalRef(j_key);
			pEnv->DeleteLocalRef(j_name);
        }

        void PackageUtils::SavePreferenceString(const acp_utils::helpers::SharedPreferenceContainer& spc, const char* value)
        {
			JNIEnv* pEnv;
			ScopeGetEnv st(pEnv);

			jstring j_key = pEnv->NewStringUTF(spc.key.c_str());
			jstring j_name = pEnv->NewStringUTF(spc.pName.c_str());
			jstring j_value = pEnv->NewStringUTF(value);

            jmethodID getPref = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "SavePreferenceString", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
            pEnv->CallStaticVoidMethod(GetClass("/PackageUtils/AndroidUtils"), getPref, 
                j_key, j_name, j_value);

			pEnv->DeleteLocalRef(j_key);
			pEnv->DeleteLocalRef(j_name);
			pEnv->DeleteLocalRef(j_value);
        }

        int PackageUtils::ReadSharedPreferenceInt(const acp_utils::helpers::SharedPreferenceContainer& spc, int defValue)
        {
			JNIEnv* pEnv;
			ScopeGetEnv st(pEnv);

			jstring j_key = pEnv->NewStringUTF(spc.key.c_str());
			jstring j_name = pEnv->NewStringUTF(spc.pName.c_str());

            jmethodID getPref = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "GetPreferenceInt", "(Ljava/lang/String;Ljava/lang/String;I)I");
			int result = pEnv->CallStaticIntMethod(GetClass("/PackageUtils/AndroidUtils"), getPref, 
                j_key, j_name, defValue);

			pEnv->DeleteLocalRef(j_key);
			pEnv->DeleteLocalRef(j_name);
			
			return result;
        }

        long long PackageUtils::ReadSharedPreferenceLong(const acp_utils::helpers::SharedPreferenceContainer& spc, long defValue)
        {
			JNIEnv* pEnv;
			ScopeGetEnv st(pEnv);

			jstring j_key = pEnv->NewStringUTF(spc.key.c_str());
			jstring j_name = pEnv->NewStringUTF(spc.pName.c_str());

            jmethodID getPref = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "GetPreferenceLong", "(Ljava/lang/String;Ljava/lang/String;J)J");
			long long result = pEnv->CallStaticLongMethod(GetClass("/PackageUtils/AndroidUtils"), getPref, 
                j_key, j_name, defValue);

			pEnv->DeleteLocalRef(j_key);
			pEnv->DeleteLocalRef(j_name);

			return result;
        }

        bool PackageUtils::ReadSharedPreferenceBool(const acp_utils::helpers::SharedPreferenceContainer& spc, bool defValue)
        {
			JNIEnv* pEnv;
			ScopeGetEnv st(pEnv);

			jstring j_key = pEnv->NewStringUTF(spc.key.c_str());
			jstring j_name = pEnv->NewStringUTF(spc.pName.c_str());

            jmethodID getPref = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "GetPreferenceBool", "(Ljava/lang/String;Ljava/lang/String;Z)Z");
			bool result = pEnv->CallStaticBooleanMethod(GetClass("/PackageUtils/AndroidUtils"), getPref, 
                j_key, j_name, defValue);

			pEnv->DeleteLocalRef(j_key);
			pEnv->DeleteLocalRef(j_name);

			return result;
        }

        std::string PackageUtils::ReadSharedPreferenceString(const acp_utils::helpers::SharedPreferenceContainer& spc, const std::string& defValue)
        {
			JNIEnv* pEnv;
			ScopeGetEnv st(pEnv);

			jstring j_key = pEnv->NewStringUTF(spc.key.c_str());
			jstring j_name = pEnv->NewStringUTF(spc.pName.c_str());
			jstring j_defValue = (pEnv)->NewStringUTF(defValue.c_str());

            jmethodID getPref = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "GetPreferenceString", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");			
			jstring jResult = (jstring)pEnv->CallStaticObjectMethod(GetClass("/PackageUtils/AndroidUtils"), getPref, 
                j_key, j_name, j_defValue);


			pEnv->DeleteLocalRef(j_key);
			pEnv->DeleteLocalRef(j_name);
			pEnv->DeleteLocalRef(j_defValue);

			acp_utils::ScopeStringChars	res(pEnv, jResult);
			return res.Get();
        }

		void PackageUtils::RemoveSharedPreference(const acp_utils::helpers::SharedPreferenceContainer& spc)
		{
			JNIEnv* pEnv;
			ScopeGetEnv st(pEnv);

			jstring j_key = pEnv->NewStringUTF(spc.key.c_str());
			jstring j_name = pEnv->NewStringUTF(spc.pName.c_str());

			jmethodID getPref = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "RemovePreference", "(Ljava/lang/String;Ljava/lang/String;)V");
			pEnv->CallStaticVoidMethod(GetClass("/PackageUtils/AndroidUtils"), getPref,
				j_key, j_name);


			pEnv->DeleteLocalRef(j_key);
			pEnv->DeleteLocalRef(j_name);
		}

        jobject PackageUtils::ReadSharedPreference(jobject& bundle)
        {
            JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);                     
            
            jmethodID getSharedPrefFunc = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "getPreference", "(Landroid/os/Bundle;)Landroid/os/Bundle;");
            return (jobject) pEnv->CallStaticObjectMethod(GetClass("/PackageUtils/AndroidUtils"), getSharedPrefFunc, bundle);
        }
        // End of shared preferences API

		// Show Welcome Screen
        void PackageUtils::ShowWelcomeScreen(acp_utils::helpers::Language language)
        {
			LOG_DBG("Showing Welcome Screen with %d", language);

            JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);

            jmethodID showWS = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "ShowWelcomeScreen", "(I)V");
            pEnv->CallStaticVoidMethod(GetClass("/PackageUtils/AndroidUtils"), showWS, language);
        }


		void PackageUtils::ShowCannotGoBack()
		{
			JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);

            jmethodID showCannotGoBackID = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "ShowCannotGoBack", "()V");
            pEnv->CallStaticVoidMethod(GetClass("/PackageUtils/AndroidUtils"), showCannotGoBackID);
		}

		//Logo Methods
		void PackageUtils::ShowLogo(const int& resId)
		{
			LOG_DBG("Showing Logo with res id %d", resId);

            JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);

            jmethodID showLogo = pEnv->GetStaticMethodID(GetClass("/PackageUtils/LogoViewPlugin"), "ShowLogo", "(III)V");
            pEnv->CallStaticVoidMethod(GetClass("/PackageUtils/LogoViewPlugin"), showLogo, resId, GetWidth(), GetHeight());
		}

		void PackageUtils::CloseLogo()
		{
			LOG_DBG("Hiding Logo");

            JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);

            jmethodID closeLogo = pEnv->GetStaticMethodID(GetClass("/PackageUtils/LogoViewPlugin"), "CloseLogo", "()V");
            pEnv->CallStaticVoidMethod(GetClass("/PackageUtils/LogoViewPlugin"), closeLogo);
		}
		
        // Browser Launch
        bool PackageUtils::LaunchBrowser(const char* url)
        {
            JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);

            jstring       urlString = pEnv->NewStringUTF(url);
            
			jmethodID launchBrowser = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "LaunchBrowser", "(Ljava/lang/String;)Z");
			bool result = pEnv->CallStaticBooleanMethod(GetClass("/PackageUtils/AndroidUtils"), launchBrowser, urlString);
			
			pEnv->DeleteLocalRef(urlString);

            return result;
		}


		//Http Execute Asynchronously 
		void PackageUtils::HttpExecuteAsync(const char* url)
		{
			JNIEnv* pEnv = NULL;
			ScopeGetEnv sta(pEnv);

			jstring  urlString = pEnv->NewStringUTF(url);

			jmethodID UrlResponse = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "HttpExecuteAsync", "(Ljava/lang/String;)V");

			pEnv->CallStaticVoidMethod(GetClass("/PackageUtils/AndroidUtils"), UrlResponse, urlString);

			pEnv->DeleteLocalRef(urlString);
		}
		

		////Http Synchronous Response 
		//std::string PackageUtils::GetHttpResponseURL(const char* url)
		//{
		//	JNIEnv* pEnv = NULL;
		//	ScopeGetEnv sta(pEnv);

		//	jstring  urlString = pEnv->NewStringUTF(url);
		//          
		//	jmethodID UrlResponse = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "GetHttpResponseURL", "(Ljava/lang/String;)Ljava/lang/String;");

		//	ScopeStringChars response_str(pEnv, (jstring)pEnv->CallStaticObjectMethod(GetClass("/PackageUtils/AndroidUtils"), UrlResponse, urlString));
		//	
		//	pEnv->DeleteLocalRef(urlString);

		//	return response_str.Get();
		//}

        // Launch Video Player
        bool PackageUtils::LaunchVideoPlayer(const char* filename)
        {
            JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);

            // TODO
            jstring fNameString = pEnv->NewStringUTF(filename);

            jmethodID playVideo = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "LaunchVideoPlayer", "(Ljava/lang/String;)Z");
			bool result = pEnv->CallStaticBooleanMethod(GetClass("/PackageUtils/AndroidUtils"), playVideo, fNameString);            
			
			pEnv->DeleteLocalRef(fNameString);

            return result;
        }

		bool PackageUtils::IsVideoCompleted()
        {
            JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);

            jmethodID isVideoCompleted = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "IsVideoCompleted", "()I");
			int result = pEnv->CallStaticIntMethod(GetClass("/PackageUtils/AndroidUtils"), isVideoCompleted);            
			
			return (result != 0);
        }

        // Game specific
        void PackageUtils::SetGameSpecificIdentifiers(const acp_utils::helpers::GameSpecificIdentifiers& i_gsi)
        {
            s_GameSpecificIdentif = i_gsi;
        }

		void PackageUtils::SetDisplayInfo(const helpers::DisplayInfo& di)
		{
			s_DisplayInfo = di;
		}

        const std::string& PackageUtils::GetDefaultIGP()
        {
            return s_GameSpecificIdentif.sDefaultIgp;
        }

        const std::string& PackageUtils::GetGameName()
        {
            return s_GameSpecificIdentif.sGameName;
        }

        const std::string& PackageUtils::GetInjectedIGP()
        {
            return s_GameSpecificIdentif.sInjectedIgp;
        }

        const std::string& PackageUtils::GetInjectedSerialKey()
        {
            return s_GameSpecificIdentif.sInjectedSerialKey;
        }

		// Hardware Identifiers:
		void PackageUtils::SetHardwareIdentifiers(const acp_utils::helpers::HardwareIdentifiers& i_hwId)
		{
			LOG_DBG("Set hardware identifiers");
			s_HwIdentif = i_hwId;
		}

        const std::string& PackageUtils::GetAndroidId()
        {
            return s_HwIdentif.sAndroidId;
        }

        const std::string& PackageUtils::GetSerial()
        {
            return s_HwIdentif.sSerial;
        }

        const std::string& PackageUtils::GetCPUSerial()
        {
            return s_HwIdentif.sCpuSerial;
        }

        const std::string& PackageUtils::GetDeviceManufacturer()
        {
            return s_HwIdentif.sDeviceManufacturer;
        }

        const std::string& PackageUtils::GetDeviceModel()
        {
            return s_HwIdentif.sDeviceModel;
        }
		
        const std::string& PackageUtils::GetBuildProduct()
        {
            return s_HwIdentif.sBuildProduct;
        }

        const std::string& PackageUtils::GetBuildDevice()
        {
            return s_HwIdentif.sBuildDevice;
        }

        const std::string& PackageUtils::GetFirmware()
        {
            return s_HwIdentif.sFirmware;
        }

        const std::string& PackageUtils::GetMacAddress()
        {
            // TODO -- Do we want the mac address or the ethernet address?
            // Does an ethernet address even exist? Is this for TVs having ethernet ?
            // Do TV's having ethernet also have wifi ?
            return s_HwIdentif.sWiFiMacAddress;
        }

		const std::string& PackageUtils::GetIMEI()
		{
			return s_HwIdentif.sImei;
		}

        const std::string& PackageUtils::GetHDIDFVStr()
        {
            return s_HwIdentif.sHdidfv;
        }

		const float	PackageUtils::GetWidthInInch()
		{
			return s_DisplayInfo.nWidth / s_DisplayInfo.xDpi;
		}
		
		const float	PackageUtils::GetHeightInInch()
		{
			return s_DisplayInfo.nHeight / s_DisplayInfo.yDpi;
		}

		const int&		PackageUtils::GetWidth()
		{
			return s_DisplayInfo.nWidth;
		}
		
		const int&		PackageUtils::GetHeight()
		{
			return s_DisplayInfo.nHeight;
		}

		// Software identifiers
		void PackageUtils::SetSoftwareIdentifiers(const acp_utils::helpers::SoftwareIdentifiers& i_swId)
		{
			s_SwIdentif = i_swId;
		}
		
		// User Location
		void PackageUtils::SetUserLocation(const acp_utils::helpers::UserLocation& i_usrLocation)
		{
			s_UserLocation = i_usrLocation;
		}

        const std::string& PackageUtils::GetCarrierName()
        {
            return s_SwIdentif.sCarrierName;
        }

        const std::string& PackageUtils::GetCountry()
        {
            return s_SwIdentif.sCountry;
        }

        const std::string& PackageUtils::GetDeviceLanguage()
        {
            return s_SwIdentif.sDeviceLanguage;
        }        
		
		const std::string& PackageUtils::GetWebviewUserAgent()
		{
			return s_SwIdentif.sWebviewUserAgent;
		}

		const std::string& PackageUtils::GetApkPath()
		{
			return s_SwIdentif.sApkPath;
		}
		
		// GoogleAdId
		const std::string& PackageUtils::GetGoogleAdId()
        {
			JNIEnv* pEnv;
			ScopeGetEnv st(pEnv);

			static std::string google_adid;
			// Google Ad Id              
            jmethodID getGoogleAdId = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "GetGoogleAdId", "()Ljava/lang/String;");
			ScopeStringChars gaidString(pEnv, (jstring)pEnv->CallStaticObjectMethod(GetClass("/PackageUtils/AndroidUtils"), getGoogleAdId));
			google_adid = gaidString.Get();

			return google_adid;
        }
		
		const acp_utils::helpers::GAIDStatus PackageUtils::GetGoogleAdIdStatus()
        {
			JNIEnv* pEnv;
			ScopeGetEnv st(pEnv);

			static acp_utils::helpers::GAIDStatus	aid_status;
			// Google Ad Id Status
            jmethodID getGoogleAdIdStatus = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "GetGoogleAdIdStatus", "()I");
			aid_status = (acp_utils::helpers::GAIDStatus) pEnv->CallStaticIntMethod(GetClass("/PackageUtils/AndroidUtils"), getGoogleAdIdStatus);

            return aid_status;
        }
		
		// BatteryInfo:
        void PackageUtils::SetBatteryInfo(const acp_utils::helpers::BatteryInfo& i_batteryInfo)
        {
            LOG_DBG("Set Battery Info");
            s_BatteryInfo = i_batteryInfo;
        }

        const bool PackageUtils::GetIsBatteryCharging()
        {
            return s_BatteryInfo.sIsCharging;
        }

        const bool PackageUtils::GetIsUsbCharging()
        {
            return s_BatteryInfo.sUsbCharge;
        }

        const bool PackageUtils::GetIsACCharging()
        {
            return s_BatteryInfo.sACCharge;
        }

        const int PackageUtils::GetBatteryStatus()
        {
            return s_BatteryInfo.sBatteryStatus;
        }

		void PackageUtils::SetOrientationState(bool i_bLock)
		{
			JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);

            jmethodID orientation_func = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "SetOrientation", "(Z)V");
            return pEnv->CallStaticVoidMethod(GetClass("/PackageUtils/AndroidUtils"), orientation_func, i_bLock);
		}
		
		void PackageUtils::SetKeepScreenOn(bool i_bKeepScreen)
		{
			JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);

            jmethodID orientation_func = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "SetKeepScreenOn", "(Z)V");
            return pEnv->CallStaticVoidMethod(GetClass("/PackageUtils/AndroidUtils"), orientation_func, i_bKeepScreen);
		}

		std::string	PackageUtils::ReadInfoFromSystemFile(const char* path, const char* property, const char* splitter)
		{
			std::string line;
			std::ifstream myfile (path);
			if (myfile.is_open())
			{
				while ( myfile.good() )
				{
					getline (myfile, line);
					
					if (strcmp(property, ""))
					{
						if (line.find(property) != 0)
						{
							continue;
						}

						line = line.substr(strlen(property), std::string::npos);
						line = line.substr(line.find(splitter) + 1, std::string::npos);

						line.erase(line.begin(), std::find_if(line.begin(), line.end(), std::not1(std::ptr_fun<int, int>(std::isspace))));

						return line;
					}
					else if (line.compare(""))
					{
						break;
					}
				}
				myfile.close();
			}
			else 
			{
				LOG_ERROR("CDeviceSpecs::readInfoFromFile fail (%s, %s): %s", property , splitter, path);
			}
			return line;
		}
		
		unsigned long long	PackageUtils::GetDiskFreeSpace()
		{
			JNIEnv* pEnv;
			ScopeGetEnv st(pEnv);

            jmethodID getDiskSpace = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "GetDiskFreeSpace", "(Ljava/lang/String;)J");

			jstring j_Path = (pEnv)->NewStringUTF(GetDataFolderPath().c_str());

			unsigned long long result = pEnv->CallStaticLongMethod(GetClass("/PackageUtils/AndroidUtils"), getDiskSpace, j_Path);

			pEnv->DeleteLocalRef(j_Path);

			return result;
		}

		int		PackageUtils::GetNumberOfCpuCores()
		{
			static int numberOfCores = -1;

			if(numberOfCores == -1)
			{
				const char* path = "/sys/devices/system/cpu/";

				struct dirent *dp;
				DIR *fd;
				int result = 0;

				if ((fd = opendir(path)) == NULL)
				{
					LOG_ERROR("numberOfFiles: can't open %s", path);
					return 0;
				}
				while ((dp = readdir(fd)) != NULL)
				{
					if (!strcmp(dp->d_name, ".") || !strcmp(dp->d_name, "..") || (strlen(dp->d_name) != 4) || (strstr(dp->d_name, "cpu") != dp->d_name))
						continue;
					result++;
				}
				closedir(fd);
				numberOfCores = result;
			}
			LOG_DBG("number of cores = %d", numberOfCores);
			return numberOfCores;
		}

		int	PackageUtils::GetCurrentCpuSpeedInHz()
		{

			std::string cpu = ReadInfoFromSystemFile("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq", "", "");
			float cpuSpeedCurrent = atoi(cpu.c_str());
			return cpuSpeedCurrent;
		}

		int	PackageUtils::GetMaxCpuSpeedInHz()
		{
			static int nMaxCpuSpeed = -1;
			if(nMaxCpuSpeed < 0)
			{
				std::string cpu = ReadInfoFromSystemFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "", "");
				nMaxCpuSpeed = atoi(cpu.c_str());
			}

			return nMaxCpuSpeed;
		}

		std::string PackageUtils::GetDeviceChipset(void)
		{
			static std::string chipset = ReadInfoFromSystemFile("/system/build.prop", "ro.board.platform", "=");

			LOG_DBG("GetDeviceChipset %s", chipset.c_str());

			return chipset;
		}

		std::string PackageUtils::GetDeviceArchitecture(void)
		{
			static std::string arch = ReadInfoFromSystemFile("/proc/cpuinfo", "CPU implementer", ":");

			LOG_DBG("GetDeviceArchitecture %s", arch.c_str());

			return arch;
		}

		std::string PackageUtils::GetDeviceMicroArch(void)
		{
			static std::string microArch = ReadInfoFromSystemFile("/proc/cpuinfo", "CPU part", ":");

			LOG_DBG("GetDeviceMicroArch %s", microArch.c_str());

			return microArch;
		}

		float		PackageUtils::GetCurrentAvailableRamInMegaBytes()
		{
			std::string mem = ReadInfoFromSystemFile("/proc/meminfo", "MemFree", ":");
			float result = atoi(mem.c_str());
			if(mem.find("kB") != std::string::npos)
			{
				result = result / 1024.0f;
			}
			else if(mem.find("MB") != std::string::npos)
			{
				//result stays the same
			}
			else
			{
				LOG_DBG("Memory in meminfo is not in kB nor in MB. Returning the value itself...");
			}

			LOG_DBG("GetCurrentAvailableRamInMegaBytes = %s", mem.c_str());

			return result;
		}

		float	PackageUtils::GetMaxAvailableRamInMegaBytes()
		{
			static float s_nMaxMem = -1.0f;
			if(s_nMaxMem < 0.0f)
			{
				std::string mem = ReadInfoFromSystemFile("/proc/meminfo", "MemTotal", ":");
				float result = atoi(mem.c_str());
				if(mem.find("kB") != std::string::npos)
				{
					result = result / 1024.0f;
				}
				else if(mem.find("MB") != std::string::npos)
				{
					//result stays the same
				}
				else
				{
					LOG_DBG("Memory in meminfo is not in kB nor in MB. Returning the value itself...");
				}

				LOG_DBG("mem = %s", mem.c_str());

				s_nMaxMem = result;
			}

			LOG_DBG("GetMaxAvailableRamInMegaBytes = %f", s_nMaxMem);

			return s_nMaxMem;
		}

		bool	PackageUtils::IsDeviceRouted()
		{
			static int CheckRout = -1;
			if(CheckRout == -1)
			{
				bool rtdDevice (false);

				// #1st method
				FILE * pToSUApkFile (fopen ("/system/app/Superuser.apk", "rb"));
				if (NULL != pToSUApkFile) {
					// Erase & set
					fclose (pToSUApkFile);
					rtdDevice = true;
				}

				// #2nd method
				if (false == rtdDevice) {
					struct stat sb;
					if (stat ("/system/bin/su", &sb) == -1 && stat ("/system/xbin/su", &sb) == -1) {
						rtdDevice = false;
					} else {
						rtdDevice = true;
					}
				}
				CheckRout = static_cast<int>(rtdDevice);
			}

			return static_cast<bool>(CheckRout);
		}

		bool	PackageUtils::IsAppEnc()
		{
			JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);

            jmethodID isAppEnc = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "retrieveBarrels", "()[I");
			jintArray arr ((jintArray) pEnv->CallStaticObjectMethod (GetClass("/PackageUtils/AndroidUtils"), isAppEnc));

			// Check
			if (arr) {
				jsize len (pEnv->GetArrayLength (arr));
				jint * body (pEnv->GetIntArrayElements (arr, 0));
				// Check
				if (NULL == body) {
					return true;
				}
				// Check
				int value[2] = {16847, 2021}; // Target is 4042 50541
				bool ret (true); // True to avoid false positives
				// For
				for (int i = 0; i < (int) len; ++i) {
					// Check
					if (i == 0) {
						value[1] *= 2; // Get 4042
					}
					// Check
					if (body[i] == (value[1] * 100000 + (value[0] * 3))) { // == 404250541 GLSignature for Google Play
						pEnv->ReleaseIntArrayElements (arr, body, 0);
						pEnv->DeleteLocalRef(arr);
						return true;
					} else if (body[i] > 0) {
						// Set false if any of the value is incorrect
						ret = false;
					}
				}
				// Erase
				pEnv->ReleaseIntArrayElements (arr, body, 0);
				pEnv->DeleteLocalRef(arr);
				return ret;
			}
			// Avoid false positives
			return true;
		}

#if AMAZON_STORE
		bool PackageUtils::IsDeviceKindle()
		{
			std::string manuf = acp_utils::api::PackageUtils::GetDeviceManufacturer();
			std::transform(manuf.begin(), manuf.end(), manuf.begin(), ::tolower);

			if(manuf.find("ti") != std::string::npos || manuf.find("amazon") != std::string::npos)
			{
				return true;
			}

			return false;
		}

		void PackageUtils::HideKindleBar()
		{
			JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);

			jmethodID showBar = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "FullScreenToggleShowBar", "()V");
            pEnv->CallStaticVoidMethod(GetClass("/PackageUtils/AndroidUtils"), showBar);            
		}

		void PackageUtils::ShowKindleBar()
		{
			JNIEnv* pEnv = NULL;
            ScopeGetEnv sta(pEnv);

			jmethodID hideBar = pEnv->GetStaticMethodID(GetClass("/PackageUtils/AndroidUtils"), "FullScreenToggleHideBar", "()V");
            pEnv->CallStaticVoidMethod(GetClass("/PackageUtils/AndroidUtils"), hideBar);            
		}
#endif //AMAZON_STORE

	}//end namespace api



	//hack for switching the place for jvm
	JavaVM* GetVM()
	{
		return api::PackageUtils::GetJavaVm();
	}

};//end namespace acp_utils
