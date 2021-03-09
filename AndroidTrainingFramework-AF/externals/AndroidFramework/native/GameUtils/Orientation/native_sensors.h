#ifndef __NATIVE_SENSORS_H__
#define __NATIVE_SENSORS_H__

#include <android/sensor.h>
#include <android/log.h>
#include <libgen.h>


#if defined DEBUG_SENSOR
	#define TAG_SENSOR "NativeSensor"
	#define DBG_SENSOR(...) 		__android_log_print(ANDROID_LOG_INFO, TAG_SENSOR, __VA_ARGS__ )
	#define DBG_FN_SENSOR()			__android_log_print(ANDROID_LOG_INFO, TAG_SENSOR, "%s: %s", basename(__FILE__), __FUNCTION__)
	#define DBG_FNE_SENSOR()		__android_log_print(ANDROID_LOG_INFO, TAG_SENSOR, "%s: END %s", basename(__FILE__), __FUNCTION__)
#else
	#define DBG_SENSOR(...)
	#define DBG_FN_SENSOR()
	#define DBG_FNE_SENSOR()
#endif

namespace acp_utils
{
	namespace modules
	{
		typedef int (*SensorEventCallback)(float, float, float);  	

		class AndroidSensorManager
		{

		private:
			
			static AndroidSensorManager*		s_pInstance;
			
			ASensorEventQueue* 					m_pSensorQueue;
			bool								m_bIsSensorRegistered;// = false;
			ASensorRef							m_refAccelerometerSensor;// = NULL;
			ASensorRef							m_refGyroscopeSensor;// = NULL;
			ASensorRef							m_refMagnetometerSensor;// = NULL;
			ASensorRef							m_refOrientationSensor;// = NULL;
			int									m_nDeviceRotation;
			int									m_nRollThreashold;
			SensorEventCallback					m_pCallback;
			bool m_bSensorsEnabled;


			void 								AndroidEnableOrientationSensor();
			bool 								AndroidEnableAccelerometerSensor(int freq);//freq = events per second
			bool 								AndroidEnableGyroscopeSensor(int freq);
			bool 								AndroidEnableMagnetometerSensor(int freq);
			
			
			void 								AndroidDisableOrientationSensor();
			void 								AndroidDisableAccelerometerSensor();
			void 								AndroidDisableGyroscopeSensor();
			void 								AndroidDisableMagnetometerSensor();


			static int							OnSensorChanged(int fd, int events, void* data);
			
		public:
		
			enum
			{
				NO_ERROR						= 	0,
				SENSORS_ALREADY_INITED			=	1,
				SENSORS_NOT_AVAILABLE			=	2,
				ORIENTATION_NOT_AVAILABLE		=	3,
				ACCELEROMETER_NOT_AVAILABLE		=	4,
				GYROSCOPE_NOT_AVAILABLE			=	5,
				MAGNETOMETER_NOT_AVAILABLE		=	6,
			};
			
			static int							AndroidInitSensors();//SensorEventCallback callbackFunction, int rollThreshold = 35);				//call on Init function
			static void							RegisterCallback(SensorEventCallback callbackFunction, int rollThreshold = 35);				//call when you want to register the callback function
			static void							UnregisterCallback();				//call when you want to unregister the callback function
			static void							AndroidEnableSensors(int freq); 														//must call on resume function
			static void							AndroidDisableSensors();																//must call on pause function
			static void							AndroidDestroySensors();																//call on destroy function
			
		};

	};//namespace modules
};//namespace acp_utils

#endif //__NATIVE_SENSORS_H__
