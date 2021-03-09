#include <stdlib.h>
#include "internal.h"
#include "../package_utils.h"
#include "logger.h"

#include <vector>
#include <string>

#if USE_NATIVE_SENSORS
	#include "../Orientation/native_sensors.h"
#endif //USE_NATIVE_SENSORS

#if USE_ASSET_READER
	#include "../AssetReader/AssetReader.h"
#endif //USE_ASSET_READER

//for #ifdefs in class_list.inl
#include "config_Android.h"

namespace acp_utils 
{
	namespace acp_internal
	{

		//static inits:
		jmethodID			Internal::s_getAssetId = 0;
        bool                Internal::s_bInitialized = false;

        void Internal::Init()
        {
            if(!s_bInitialized)
            {
                InitializeCachedVars();
                s_bInitialized = true;

	#if USE_NATIVE_SENSORS
				acp_utils::modules::AndroidSensorManager::AndroidInitSensors();
	#endif //USE_NATIVE_SENSORS

	#if USE_ASSET_READER
				acp_utils::modules::AssetReader::InitAssetManager();
	#endif //USE_ASSET_READER
            }
        }

		void Internal::LoadClasses(JNIEnv* pEnv)
		{
			if(!acp_utils::api::PackageUtils::s_mapLoadedJavaClasses.empty())
			{
				LOG_INFO("You are trying to load the classes too many times. Check your code. This is called from JNI_OnLoad only!");
				return;
			}

			#include "..\..\..\..\AndroidFrameworkConfig\configs\plugin\class_list.inl"
			std::vector<const char*> jni_class_OS (jclass_OS, jclass_OS+sizeof(jclass_OS) / sizeof(const char*) );
			for(std::vector<const char*>::iterator it = jni_class_OS.begin(); it!=jni_class_OS.end(); ++it)
			{
				std::string j_class = *it;
				jclass temp = pEnv->FindClass(j_class.c_str());
				acp_utils::api::PackageUtils::Jni_CheckForExceptions();
				acp_utils::api::PackageUtils::s_mapLoadedJavaClasses[j_class] = (jclass)pEnv->NewGlobalRef(temp);
			}

			std::vector<const char*> jni_class (jclass_const, jclass_const+sizeof(jclass_const) / sizeof(const char*) );
			for(std::vector<const char*>::iterator it = jni_class.begin(); it!=jni_class.end(); ++it)
			{
				std::string j_class = *it;
				std::string formatted_j_class = JNI_CLASSES + j_class;
				jclass temp = pEnv->FindClass(formatted_j_class.c_str());
				acp_utils::api::PackageUtils::Jni_CheckForExceptions();
				acp_utils::api::PackageUtils::s_mapLoadedJavaClasses[j_class] = (jclass)pEnv->NewGlobalRef(temp);
			}
		}
	
		void Internal::SetVm(JavaVM* vm)
		{
            acp_utils::api::PackageUtils::SetJavaVM(vm);
		}

		void Internal::SetWindow(ANativeWindow* wnd, int w, int h)
		{
			if(wnd != 0)
			{
				acp_utils::helpers::DisplayInfo tmpDi;
				tmpDi.nWidth = w;
				tmpDi.nHeight = h;
				
				{
					JNIEnv* pEnv;
					ScopeGetEnv st(pEnv);

					jclass AndroidUtils = acp_utils::api::PackageUtils::GetClass("/PackageUtils/AndroidUtils");

					jmethodID javaXDpi = pEnv->GetStaticMethodID(AndroidUtils, "GetXDpi", "()F");
					jmethodID javaYDpi = pEnv->GetStaticMethodID(AndroidUtils, "GetYDpi", "()F");

					tmpDi.xDpi = pEnv->CallStaticFloatMethod(AndroidUtils, javaXDpi);
					tmpDi.yDpi = pEnv->CallStaticFloatMethod(AndroidUtils, javaYDpi);
				}

				acp_utils::api::PackageUtils::SetDisplayInfo(tmpDi);
			}

			acp_utils::api::PackageUtils::SetNativeWindow(wnd);
		}

		void Internal::SetConnection(const acp_utils::helpers::ConnectionType& ct)
		{
			acp_utils::api::PackageUtils::SetConnectionType(ct);
		}

		void Internal::SetUserLocation(const acp_utils::helpers::UserLocation& ul)
		{
			acp_utils::api::PackageUtils::SetUserLocation(ul);
		}
		
		void Internal::InitializeCachedVars()
		{
			JNIEnv* pEnv;
			ScopeGetEnv st(pEnv);

			jclass AndroidUtils = acp_utils::api::PackageUtils::GetClass("/PackageUtils/AndroidUtils");				
		
			//System paths:
			{
				jmethodID retrieve_sd_card_path = pEnv->GetStaticMethodID(AndroidUtils, "RetrieveSDCardPath", "()Ljava/lang/String;");	
				jmethodID retrieve_obb_path = pEnv->GetStaticMethodID(AndroidUtils, "RetrieveObbPath", "()Ljava/lang/String;");	
				jmethodID retrieve_data_path = pEnv->GetStaticMethodID(AndroidUtils, "RetrieveDataPath", "()Ljava/lang/String;");	
				jmethodID retrieve_save_path = pEnv->GetStaticMethodID(AndroidUtils, "RetrieveSavePath", "()Ljava/lang/String;");	
				jmethodID retrieve_cache_path = pEnv->GetStaticMethodID(AndroidUtils, "RetrieveTempPath", "()Ljava/lang/String;");	

				ScopeStringChars sd_str(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, retrieve_sd_card_path));
				ScopeStringChars obb_str(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, retrieve_obb_path));
				ScopeStringChars data_str(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, retrieve_data_path));
				ScopeStringChars save_str(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, retrieve_save_path));
				ScopeStringChars cache_str(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, retrieve_cache_path));

				//acp_utils::api::PackageUtils::s_SystemPaths                
				acp_utils::api::PackageUtils::s_SystemPaths.sSdCard = sd_str.Get();
				acp_utils::api::PackageUtils::s_SystemPaths.sObb = obb_str.Get();
				acp_utils::api::PackageUtils::s_SystemPaths.sData = data_str.Get();
				acp_utils::api::PackageUtils::s_SystemPaths.sSave = save_str.Get();
				acp_utils::api::PackageUtils::s_SystemPaths.sTemp = cache_str.Get();               
			}

			//Hardware Identifiers:
			{
				acp_utils::helpers::HardwareIdentifiers hw;
                
                // Android Id
                jmethodID getAndroidId = pEnv->GetStaticMethodID(AndroidUtils, "GetAndroidID", "()Ljava/lang/String;");
                ScopeStringChars androidId(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, getAndroidId));
                hw.sAndroidId = androidId.Get();

                // Serial
                jmethodID getSerial = pEnv->GetStaticMethodID(AndroidUtils, "GetSerial", "()Ljava/lang/String;");
                ScopeStringChars serial(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, getSerial));
                hw.sSerial = serial.Get();

                // CPU Serial
                jmethodID getCPUSerial = pEnv->GetStaticMethodID(AndroidUtils, "GetCPUSerial", "()Ljava/lang/String;");
                ScopeStringChars cpuSerial(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, getCPUSerial));
				hw.sCpuSerial = cpuSerial.Get();

                // Device Manufacturer
                jmethodID getDeviceManufacturer = pEnv->GetStaticMethodID(AndroidUtils, "GetDeviceManufacturer", "()Ljava/lang/String;");
                ScopeStringChars deviceManufacturer(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, getDeviceManufacturer));
				hw.sDeviceManufacturer = deviceManufacturer.Get();

                // Device Model
                jmethodID getDeviceModel = pEnv->GetStaticMethodID(AndroidUtils, "GetDeviceModel", "()Ljava/lang/String;");
                ScopeStringChars deviceModel(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, getDeviceModel));
				hw.sDeviceModel = deviceModel.Get();

				// Build.Product
                jmethodID getBuildProduct = pEnv->GetStaticMethodID(AndroidUtils, "GetPhoneProduct", "()Ljava/lang/String;");
                ScopeStringChars buildProduct(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, getBuildProduct));
				hw.sBuildProduct = buildProduct.Get();
								
				// Build.Product
                jmethodID getBuildDevice = pEnv->GetStaticMethodID(AndroidUtils, "GetPhoneDevice", "()Ljava/lang/String;");
                ScopeStringChars buildDevice(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, getBuildDevice));
				hw.sBuildDevice = buildDevice.Get();
				
                // Firmware
                jmethodID getFirmware = pEnv->GetStaticMethodID(AndroidUtils, "GetFirmware", "()Ljava/lang/String;");
                ScopeStringChars firmware(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, getFirmware));
                hw.sFirmware = firmware.Get();

                // Mac Address
                jmethodID getMacAddress = pEnv->GetStaticMethodID(AndroidUtils, "GetMacAddress", "()Ljava/lang/String;");
                ScopeStringChars macAddress(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, getMacAddress));
                hw.sWiFiMacAddress = macAddress.Get();

				// imei:
				jmethodID imei_mid = pEnv->GetStaticMethodID(AndroidUtils, "GetDeviceIMEI", "()Ljava/lang/String;");	
				ScopeStringChars imei(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, imei_mid));
				hw.sImei = imei.Get();

                // HDIDFV
                jmethodID getHDIDFV = pEnv->GetStaticMethodID(AndroidUtils, "GetHDIDFV", "()Ljava/lang/String;");
                ScopeStringChars hdidfv(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, getHDIDFV));
                hw.sHdidfv = hdidfv.Get();

				acp_utils::api::PackageUtils::SetHardwareIdentifiers(hw);
			}	

			//caching some method ids for later use:
			s_getAssetId = pEnv->GetStaticMethodID(AndroidUtils, "GetAssetAsString", "(Ljava/lang/String;)[B");
		}

        void Internal::RefreshCachedVars()
        {
			JNIEnv* pEnv;
			ScopeGetEnv st(pEnv);

			jclass AndroidUtils = acp_utils::api::PackageUtils::GetClass("/PackageUtils/AndroidUtils");		

			//ConnectionType:
			{
				jmethodID init_con_type = pEnv->GetStaticMethodID(AndroidUtils, "initCheckConnectionType", "()I");	
				acp_utils::api::PackageUtils::SetConnectionType(static_cast<acp_utils::helpers::ConnectionType>(pEnv->CallStaticIntMethod(AndroidUtils, init_con_type)));
			}

			//Software Identifiers:
			{
				acp_utils::helpers::SoftwareIdentifiers sw;
				
                // Carrier Agent                 
                jmethodID getCarrier = pEnv->GetStaticMethodID(AndroidUtils, "GetCarrierAgent", "()Ljava/lang/String;");
				ScopeStringChars carrierString(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, getCarrier));
				sw.sCarrierName = carrierString.Get();

                // Country
                jmethodID getCountry = pEnv->GetStaticMethodID(AndroidUtils, "GetCountry", "()Ljava/lang/String;");
				ScopeStringChars countryString(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, getCountry));
				sw.sCountry = countryString.Get();

                // Language
                jmethodID getLanguage = pEnv->GetStaticMethodID(AndroidUtils, "GetDeviceLanguage", "()Ljava/lang/String;");
				ScopeStringChars languageString(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, getLanguage));
				sw.sDeviceLanguage = languageString.Get();

				// Webview User Agent
				jmethodID wvUserAgent = pEnv->GetStaticMethodID(AndroidUtils, "GetUserAgent", "()Ljava/lang/String;");
				ScopeStringChars wvUAString(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, wvUserAgent));
				sw.sWebviewUserAgent = wvUAString.Get();

				// Apk Path                
                jmethodID getApkPath = pEnv->GetStaticMethodID(AndroidUtils, "GetApkPath", "()Ljava/lang/String;");
				ScopeStringChars apkPath(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, getApkPath));
				sw.sApkPath = apkPath.Get();
				
				// And flush to cache
				acp_utils::api::PackageUtils::SetSoftwareIdentifiers(sw);
			}

			//GameSpecific Identifiers:
			{
                acp_utils::helpers::GameSpecificIdentifiers gsi;
                // Default IGP
				jmethodID getDefaultIGP = pEnv->GetStaticMethodID(AndroidUtils, "GetDefaultIGP", "()Ljava/lang/String;");	
				ScopeStringChars igp(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, getDefaultIGP));
                gsi.sDefaultIgp = igp.Get();				

                // Game Name
				jmethodID getGameName = pEnv->GetStaticMethodID(AndroidUtils, "GetGameName", "()Ljava/lang/String;");	
				ScopeStringChars gameName(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, getGameName));
                gsi.sGameName = gameName.Get();

                // Injected IGP
				jmethodID getInjectedIGP = pEnv->GetStaticMethodID(AndroidUtils, "GetInjectedIGP", "()Ljava/lang/String;");	
				ScopeStringChars injectedIGP(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, getInjectedIGP));
                gsi.sInjectedIgp = injectedIGP.Get();

                // Injected Serial Key
				jmethodID getInjectedSK = pEnv->GetStaticMethodID(AndroidUtils, "GetInjectedSerialKey", "()Ljava/lang/String;");	
				ScopeStringChars injectedSK(pEnv, (jstring)pEnv->CallStaticObjectMethod(AndroidUtils, getInjectedSK));
                gsi.sInjectedSerialKey = injectedSK.Get();

                acp_utils::api::PackageUtils::SetGameSpecificIdentifiers(gsi);
			}

            //Battery Info:
            {
                // Will call the battery info init, which will set the battery info struct through JNIBridge
                acp_utils::helpers::BatteryInfo bi;
                jmethodID initBatteryInfo = pEnv->GetStaticMethodID(AndroidUtils, "initBatteryInfo", "()V");
                pEnv->CallStaticVoidMethod(AndroidUtils, initBatteryInfo);
            }

        }

		void Internal::CleanCachedVars()
		{
			//this function is no longer in use
			//todo: remove this function
		}

		
		void Internal::PreGameResume()
		{
			RefreshCachedVars();
		}

		void Internal::PostGameResume()
		{
		}

		void Internal::PreGamePause()
		{
			//The PreGamePause and PostGamePause are no longer usefull. The game will usually just set a flag that the game enters pause state, and on the main thread the actual pause will get executed
			//Hence, the PostGamePause code will be executed before the actual game pause code will get called
			//If, in any chances, a cleaning is required for ACP after game pause code is executed, an OnPause function needs to be exposed in the public API
		}

		void Internal::PostGamePause()
		{
			//The PreGamePause and PostGamePause are no longer usefull. The game will usually just set a flag that the game enters pause state, and on the main thread the actual pause will get executed
			//Hence, the PostGamePause code will be executed before the actual game pause code will get called
			//If, in any chances, a cleaning is required for ACP after game pause code is executed, an OnPause function needs to be exposed in the public API
		}

        // BateryInfo:
        void Internal::SetBatteryInfo(const acp_utils::helpers::BatteryInfo& bi)
        {
            acp_utils::api::PackageUtils::SetBatteryInfo(bi);
        }
		

	};//end namespace acp_internal
};//end namespace acp_utils