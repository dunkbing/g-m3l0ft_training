#include "config_Android.h"
#include "native_sensors.h"

#include <jni.h>


#define ASENSOR_TYPE_ORIENTATION 3 //this "hack" is required as this define is omitted in android/sensor.h
#define INVERSE_GRAVITY -(1.0f / ASENSOR_STANDARD_GRAVITY) 	//ASENSOR_STANDARD_GRAVITY = (9.80665f)


#define ROTATION_0					0			//portrait
#define ROTATION_90					1			//landscape
#define ROTATION_180				2			//reversed portrait
#define ROTATION_270				3			//reversed landscape


namespace acp_utils
{
	namespace modules
	{
		
		AndroidSensorManager*		AndroidSensorManager::s_pInstance = NULL;

		//public functions first:

		void AndroidSensorManager::UnregisterCallback()
		{
			DBG_FN_SENSOR();
			if(s_pInstance == NULL || s_pInstance->m_pCallback == NULL || s_pInstance->m_pSensorQueue == NULL)
				return;
			s_pInstance->m_pCallback = NULL;
			DBG_SENSOR("Callback unregistered");
			DBG_FNE_SENSOR();
		}

		void AndroidSensorManager::RegisterCallback(SensorEventCallback callbackFunction, int rollThreshold)
		{
			DBG_FN_SENSOR();
			
			if(s_pInstance == NULL || s_pInstance->m_pSensorQueue == NULL)
				return;
			if( s_pInstance->m_pCallback != NULL)
			{
				DBG_SENSOR("Callback already registered, call UnregisterCallback if you wish to register a new callback function.");
				return;
			}
			s_pInstance->m_pCallback = callbackFunction;
			s_pInstance->m_nRollThreashold = rollThreshold;
			DBG_SENSOR("Callback registered");
			DBG_FNE_SENSOR();
		}

		int AndroidSensorManager::AndroidInitSensors()//SensorEventCallback callbackFunction, int rollThreshold)
		{
			DBG_FN_SENSOR();
			if(s_pInstance != NULL)
			{
				return SENSORS_ALREADY_INITED;
			}
			s_pInstance = new AndroidSensorManager();
			ASensorManager* pSensorManager = ASensorManager_getInstance();
			if(pSensorManager == NULL)
			{
				return SENSORS_NOT_AVAILABLE;
			}
			s_pInstance->m_bIsSensorRegistered = false;
			s_pInstance->m_refAccelerometerSensor = NULL;
			s_pInstance->m_refGyroscopeSensor = NULL;
			s_pInstance->m_refMagnetometerSensor = NULL;
			s_pInstance->m_nDeviceRotation = ROTATION_90; //let's assume a default, landscape. it will re-adapt in a few seconds anyway
			
			s_pInstance->m_bSensorsEnabled = false; //no bSensorDisabled needed
			s_pInstance->m_pCallback = NULL; //no m_bCallbackRegistered needed

			// s_pInstance->m_pCallback = callbackFunction;
			// s_pInstance->m_nRollThreashold = rollThreshold;
			
			ALooper* looper = ALooper_forThread();
			if(looper == NULL)
				looper = ALooper_prepare(ALOOPER_PREPARE_ALLOW_NON_CALLBACKS);

				
			s_pInstance->m_refAccelerometerSensor = ASensorManager_getDefaultSensor(pSensorManager, ASENSOR_TYPE_ACCELEROMETER);
			if (s_pInstance->m_refAccelerometerSensor == NULL)
				return ACCELEROMETER_NOT_AVAILABLE;

			//s_pInstance->m_refMagnetometerSensor = ASensorManager_getDefaultSensor(pSensorManager, ASENSOR_TYPE_MAGNETIC_FIELD);
			//s_pInstance->m_refGyroscopeSensor = ASensorManager_getDefaultSensor(pSensorManager, ASENSOR_TYPE_GYROSCOPE);
			
			s_pInstance->m_refOrientationSensor = ASensorManager_getDefaultSensor(pSensorManager, ASENSOR_TYPE_ORIENTATION);
			if(s_pInstance->m_refOrientationSensor == NULL)
				return ORIENTATION_NOT_AVAILABLE;
			
			s_pInstance->m_pSensorQueue = ASensorManager_createEventQueue(pSensorManager, looper, 1, AndroidSensorManager::OnSensorChanged, NULL);
			if(s_pInstance->m_pSensorQueue == NULL)
				return SENSORS_NOT_AVAILABLE;
			DBG_FNE_SENSOR();
			return NO_ERROR;
		}

		void AndroidSensorManager::AndroidEnableSensors(int freq)
		{
			DBG_FN_SENSOR();
			if(s_pInstance == NULL || s_pInstance->m_pCallback == NULL || s_pInstance->m_pSensorQueue == NULL)
				return;
			
			if (s_pInstance->m_bSensorsEnabled == true)
			{
				DBG_SENSOR("Sensor already enabled");
				return;
			}
			s_pInstance->AndroidEnableAccelerometerSensor(freq);
			//s_pInstance->AndroidEnableMagnetometerSensor(freq);
			//s_pInstance->AndroidEnableGyroscopeSensor(freq);
			
			s_pInstance->AndroidEnableOrientationSensor();
			s_pInstance->m_bSensorsEnabled = true;
			DBG_FNE_SENSOR();
		}

		void AndroidSensorManager::AndroidDisableSensors()
		{
			DBG_FN_SENSOR();
			if(s_pInstance == NULL || s_pInstance->m_pCallback == NULL || s_pInstance->m_pSensorQueue == NULL)
				return;

			if (s_pInstance->m_bSensorsEnabled == false)
			{
				DBG_SENSOR("Sensor already disabled");
				return;
			}
			s_pInstance->AndroidDisableAccelerometerSensor();
			//s_pInstance->AndroidDisableGyroscopeSensor();
			//s_pInstance->AndroidDisableMagnetometerSensor();
			
			s_pInstance->AndroidDisableOrientationSensor();
			s_pInstance->m_bSensorsEnabled = false;
			DBG_FNE_SENSOR();
		}

		// void AndroidSensorManager::AndroidDestroySensors()
		// {
			// DBG_FN_SENSOR();
			// if(s_pInstance == NULL)
				// return;
			// if(m_bSensorsDisabled == false)//maybe disable the sensors here
			// {
				// DBG_SENSOR("Sensores not disabled, can't destroy");
				// return;
			// }
			// DBG_SENSOR("Going to destroy sensors");
			// delete(s_pInstance);
			// s_pInstance = NULL;
			// DBG_FNE_SENSOR();
		// }

		void AndroidSensorManager::AndroidEnableOrientationSensor()
		{
			if(m_refOrientationSensor) 
			{
				int errorr = ASensorEventQueue_enableSensor(m_pSensorQueue, m_refOrientationSensor);
				if(errorr ==0)
				{
					// We'd like to get 1 events each 3 seconds.
					ASensorEventQueue_setEventRate(m_pSensorQueue, m_refOrientationSensor, 3 * 1000 * 1000L);
				}
			}
		}

		bool AndroidSensorManager::AndroidEnableAccelerometerSensor(int freq)
		{
			if (m_refAccelerometerSensor)
			{
				int errorrr = ASensorEventQueue_enableSensor(m_pSensorQueue, m_refAccelerometerSensor);
				if (errorrr == 0)
				{
					ASensorEventQueue_setEventRate(m_pSensorQueue, m_refAccelerometerSensor, (1000L/freq)*1000);
					return true;
				}
			}
			return false;
		}

		bool AndroidSensorManager::AndroidEnableGyroscopeSensor(int freq)
		{
			if (m_refGyroscopeSensor)
			{
				if (ASensorEventQueue_enableSensor(m_pSensorQueue, m_refGyroscopeSensor) >= 0)
				{
					ASensorEventQueue_setEventRate(m_pSensorQueue, m_refGyroscopeSensor, (1000L/freq)*1000);
					return true;
				}
			}
			return false;
		}

		bool AndroidSensorManager::AndroidEnableMagnetometerSensor(int freq)
		{
			if (m_refMagnetometerSensor)
			{
				if (ASensorEventQueue_enableSensor(m_pSensorQueue, m_refMagnetometerSensor) >= 0)
				{
					ASensorEventQueue_setEventRate(m_pSensorQueue, m_refMagnetometerSensor, (1000L/freq)*1000);
					return true;
				}
			}
			return false;
		}

		void AndroidSensorManager::AndroidDisableOrientationSensor()
		{
			if (m_refOrientationSensor)
			{
				ASensorEventQueue_disableSensor(m_pSensorQueue, m_refOrientationSensor);
			}
		}

		void AndroidSensorManager::AndroidDisableAccelerometerSensor()
		{
			if (m_refAccelerometerSensor)
			{
				ASensorEventQueue_disableSensor(m_pSensorQueue, m_refAccelerometerSensor);
			}
			//else: not enabled
		}

		void AndroidSensorManager::AndroidDisableGyroscopeSensor()
		{
			if (m_refGyroscopeSensor)
			{
				ASensorEventQueue_disableSensor(m_pSensorQueue, m_refGyroscopeSensor);
			}
			//else: not enabled
		}

		void AndroidSensorManager::AndroidDisableMagnetometerSensor()
		{
			if (m_refMagnetometerSensor)
			{
				ASensorEventQueue_disableSensor(m_pSensorQueue, m_refMagnetometerSensor);
			}
			//else: not enabled
		}

		int AndroidSensorManager::OnSensorChanged(int fd, int events, void* data)
		{
			ASensorEvent event;
			while (ASensorEventQueue_getEvents(s_pInstance->m_pSensorQueue, &event, 1) > 0) 
			{
				if(event.type == ASENSOR_TYPE_ACCELEROMETER) 
				{
					switch(s_pInstance->m_nDeviceRotation)
					{
						case ROTATION_0: //Surface.ROTATION_0:
							s_pInstance->m_pCallback(
								event.vector.y * INVERSE_GRAVITY, 
								event.vector.x * INVERSE_GRAVITY, 
								event.vector.z * INVERSE_GRAVITY);
							break;
						case ROTATION_90: //Surface.ROTATION_90:
							s_pInstance->m_pCallback(
								event.vector.x * INVERSE_GRAVITY, 
								-event.vector.y * INVERSE_GRAVITY, 
								event.vector.z * INVERSE_GRAVITY);
							break;
						case ROTATION_180: //Surface.ROTATION_180:
							s_pInstance->m_pCallback(
								-event.vector.x * INVERSE_GRAVITY, 
								-event.vector.z * INVERSE_GRAVITY, 
								event.vector.y * INVERSE_GRAVITY);
							break;
							
						case ROTATION_270: //Surface.ROTATION_270:
							s_pInstance->m_pCallback(
								event.vector.x * INVERSE_GRAVITY, 
								event.vector.y * INVERSE_GRAVITY, 
								event.vector.z * INVERSE_GRAVITY);
							break;
						default: 
							s_pInstance->m_pCallback(
								event.vector.x * INVERSE_GRAVITY, 
								event.vector.y * INVERSE_GRAVITY, 
								event.vector.z * INVERSE_GRAVITY);
							break;
					}
				}
				else if(event.type == ASENSOR_TYPE_ORIENTATION) 
				{
					float roll = event.vector.roll;
					float pitch = event.vector.pitch;
					float azimuth = event.vector.azimuth;

					if(roll <= -1 * s_pInstance->m_nRollThreashold)
					{	
						if (s_pInstance->m_nDeviceRotation != ROTATION_270)
						{
							DBG_SENSOR("rotation = REVERSED LANDSCAPE because of roll == %f", roll);
						}
						s_pInstance->m_nDeviceRotation = ROTATION_270;	// reveresed landscape
					}
					else if(roll >= s_pInstance->m_nRollThreashold)
					{
						if (s_pInstance->m_nDeviceRotation != ROTATION_90)
						{
							DBG_SENSOR("rotation = LANDSCAPE because of roll == %f", roll);	
						}
						s_pInstance->m_nDeviceRotation = ROTATION_90;	// landscape
					}

					else if(pitch <= -1 * s_pInstance->m_nRollThreashold)
					{
						if (s_pInstance->m_nDeviceRotation != ROTATION_0)
						{
							DBG_SENSOR("rotation = PORTRAIT because of pitch == %f", pitch);	
						}
						s_pInstance->m_nDeviceRotation = ROTATION_0;	// portrait
					}
					else if(pitch >= s_pInstance->m_nRollThreashold)
					{
						if (s_pInstance->m_nDeviceRotation != ROTATION_180)
						{
							DBG_SENSOR("rotation = REVERSED PORTRAIT because of pitch == %f", pitch);	
						}
						s_pInstance->m_nDeviceRotation = ROTATION_180;	// reversed portait
					}

					else
					{
						// keep orientation
					}

				}
				
				//else if(event.type == ASENSOR_TYPE_GYROSCOPE) 
				//{
					//LOGI("ASENSOR_TYPE_GYROSCOPE(x,y,z,t): %f %f %f %lld", event.vector.x, event.vector.y,  event.vector.z, event.timestamp);                                                                                                                                        
				//}
				//else if(event.type == ASENSOR_TYPE_MAGNETIC_FIELD) 
				//{
					//LOGI("ASENSOR_TYPE_MAGNETIC_FIELD(x,y,z,t): %f %f %f %lld", event.vector.x, event.vector.y, event.vector.z, event.timestamp);                                                                                                                                        
				//}
			}

			//should return 1 to continue receiving callbacks, or 0 to unregister                                                                                                                           
			return 1;
		}
	

	}//namespace modules
}//namespace acp_utils