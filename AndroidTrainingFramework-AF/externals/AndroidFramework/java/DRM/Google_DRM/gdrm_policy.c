#include "gdrm_app.h"
#include "stdio.h"

enum eStrings
{
	ANTI_PIRACY=0,
	LAST_RESPONSE,
	VT_STAMP,
	RT_UNTIL,
	RT_MAX,
	RT_COUNT,
	RUN_FIRST,
	R_TIME,
	L_TIME,
	TOTAL_PREFS
};

const char PREFS_FILE[] = "GDRMPolicy";
const char PREF_LAST_RESPONSE[] = "lastResponse";
const char PREF_VALIDITY_TIMESTAMP[]= "validityTimestamp";
const char PREF_RETRY_UNTIL[] = "retryUntil";
const char PREF_MAX_RETRIES[] = "maxRetries";
const char PREF_RETRY_COUNT[] = "retryCount";
const char DEFAULT_VALIDITY_TIMESTAMP[] = "0";
const char DEFAULT_RETRY_UNTIL[] = "0";
const char DEFAULT_MAX_RETRIES[] = "0";
const char DEFAULT_RETRY_COUNT[] = "0";
const char PREF_LAST_TIME[] = "gdrm_l_time";
const char PREF_REAL_TIME[] = "gdrm_r_time";

const jlong VT_EXTRA = 60*60*24*7*2;//2 weeks ... divide by 1000 in c code
const jlong GT_EXTRA = 60*60*24*7*2 + 3*24*60*60;//2 weeks+ 3 days ... again divide by 1000
const jlong GR_EXTRA = 10;

const char PREF_RUN_FIRST[] = "RunFirst";
const char DEFAULT_RUN_FIRST[] = "true";
const jlong MILLIS_PER_MINUTE = 60 ; //divide by 1000

jlong dataValues[TOTAL_PREFS];
jlong g_nTime=0;
int   serv_responded=0;

int VALID=0;
int INVALID=0;
int RETRY=0;

void drmDebugLog(const char* msg, ...)
{
#ifdef _DEBUG
	char tmp[8*1024];
	va_list marker;
	va_start(marker, msg);
	vsprintf(tmp, msg, marker);
	va_end(marker);
	__android_log_write( ANDROID_LOG_INFO, "License", tmp );
#endif
}

jstring getString(int string)
{
  drmDebugLog("getting string ");
  JNIEnv* env=mEnv;
  switch (string)
  {
	case ANTI_PIRACY:
		return (*env)->NewStringUTF(env, PREFS_FILE);
	case LAST_RESPONSE:
		return (*env)->NewStringUTF(env, PREF_LAST_RESPONSE);
	case VT_STAMP:
		return (*env)->NewStringUTF(env, PREF_VALIDITY_TIMESTAMP);
	case RT_UNTIL:
		return (*env)->NewStringUTF(env, PREF_RETRY_UNTIL);
	case RT_MAX:
		return (*env)->NewStringUTF(env, PREF_MAX_RETRIES);
	case RT_COUNT:
		return (*env)->NewStringUTF(env, PREF_RETRY_COUNT);
	case RUN_FIRST:
		return (*env)->NewStringUTF(env, PREF_RUN_FIRST);
	case R_TIME:
		return (*env)->NewStringUTF(env, PREF_REAL_TIME);
	case L_TIME:
		return (*env)->NewStringUTF(env, PREF_LAST_TIME);
	default:
		return (*env)->NewStringUTF(env, "error");
  }
}

JNIEXPORT void JNICALL JNI_FUNCTION(installer_GDRMPolicy_initNativeAP)(JNIEnv* env, jobject thiz,int valid, int invalid, int retry)
{

	drmDebugLog("entering native code ");
	//mEnv = env;
	drmDebugLog("getting class ");
	Policy	= (jclass)(*mEnv)->NewGlobalRef(mEnv, thiz);
	drmDebugLog("getting method id ");
	updatePrefsID = (*mEnv)->GetStaticMethodID (mEnv, Policy, "UpdatePreferences", "(Ljava/lang/String;Ljava/lang/String;I)V");
	drmDebugLog("getting method2 id ");
	updatePrefs2ID = (*mEnv)->GetStaticMethodID (mEnv, Policy, "UpdatePreferences2", "(Ljava/lang/String;JI)V");
	drmDebugLog("finish ");
	memset(dataValues,0,sizeof(jlong)*TOTAL_PREFS);
	VALID=valid;
	RETRY=retry;
	INVALID=invalid;
	drmDebugLog("finish 2");
}

JNIEXPORT jstring JNI_FUNCTION(installer_GDRMPolicy_getConstString)(JNIEnv* env,jobject thiz,int string)
{
  drmDebugLog("getting const string");
  // assign a lock pointer so the application will crash if it will try to access this function in another order then the one we want
  if (lockPointer2==NULL)
	lockPointer2=(int*) malloc(sizeof(int));
  // dependency on lock pointer 1. The public key should've been asked by now! aritmetic exception might occur
  drmDebugLog(" getting const string lock ");
  lockPointer2[0]=1/lockPointer1[1];
  drmDebugLog(" getting const string lock done");
  return getString(string);

}

JNIEXPORT void JNI_FUNCTION(installer_GDRMPolicy_setConst)(JNIEnv* env, jobject thiz, int idx, int value)
{
	//get Const String should've been called at least once so far. Also lockPointer[1] should've been intialized
	drmDebugLog(" set const lock ");
	lockPointer2[0]=1/lockPointer1[1];
	drmDebugLog(" set const lock passed");
	dataValues[idx]=value;
}
JNIEXPORT void JNI_FUNCTION(installer_GDRMPolicy_setLongConst)(JNIEnv* env, jobject thiz, int idx, jlong value)
{
	drmDebugLog(" set const long lock ");
	lockPointer2[0]=1/lockPointer1[1];
	drmDebugLog(" set const lock passed");
	dataValues[idx]=value;
}



void updatePrefs(int index, int all)
{
	drmDebugLog(" updating prefs");
	JNIEnv* env=mEnv;
	int i;
	if (all==1)
	{
		for (i=LAST_RESPONSE;i<TOTAL_PREFS;i++)
		if (i==LAST_RESPONSE || i==RUN_FIRST)
		{
			char buff[100];
			sprintf(buff,"%d",dataValues[i]);

			(*mEnv)->CallStaticVoidMethod(mEnv, Policy, updatePrefsID,getString(i),(*env)->NewStringUTF(env,buff),i);
		}
		else
			(*mEnv)->CallStaticVoidMethod(mEnv, Policy, updatePrefs2ID,getString(i),dataValues[i],i);
		/*char buff[256] = {0};
		drmDebugLog(" updating prefs 1");
		sprintf(buff,"%d",dataValues[LAST_RESPONSE]);
		(*mEnv)->CallStaticIntMethod(mEnv, Policy, updatePrefsID,(*env)->NewStringUTF(env, PREF_LAST_RESPONSE),(*env)->NewStringUTF(env,buff),LAST_RESPONSE);
		drmDebugLog(" updating prefs 2");

		(*mEnv)->CallStaticIntMethod(mEnv, Policy, updatePrefs2ID,(*env)->NewStringUTF(env, PREF_VALIDITY_TIMESTAMP),dataValues[VT_STAMP],VT_STAMP);
		drmDebugLog(" updating prefs 3");

		(*mEnv)->CallStaticIntMethod(mEnv, Policy, updatePrefs2ID,(*env)->NewStringUTF(env, PREF_RETRY_UNTIL),dataValues[RT_UNTIL],RT_UNTIL);
		drmDebugLog(" updating prefs 4");

		(*mEnv)->CallStaticIntMethod(mEnv, Policy, updatePrefs2ID,(*env)->NewStringUTF(env, PREF_MAX_RETRIES),dataValues[RT_MAX],RT_MAX);
		drmDebugLog(" updating prefs 5");

		(*mEnv)->CallStaticIntMethod(mEnv, Policy, updatePrefs2ID,(*env)->NewStringUTF(env, PREF_RETRY_COUNT),dataValues[RT_COUNT],RT_COUNT);

		drmDebugLog(" updating prefs 6");
		sprintf(buff,"%d",dataValues[RUN_FIRST]);
		(*mEnv)->CallStaticIntMethod(mEnv, Policy, updatePrefsID,(*env)->NewStringUTF(env, PREF_RUN_FIRST),(*env)->NewStringUTF(env,buff),RUN_FIRST);
		drmDebugLog(" updating prefs 7");
		(*mEnv)->CallStaticIntMethod(mEnv, Policy, updatePrefs2ID,(*env)->NewStringUTF(env, PREF_REAL_TIME),dataValues[R_TIME],R_TIME);
		drmDebugLog(" updating prefs 8");
		(*mEnv)->CallStaticIntMethod(mEnv, Policy, updatePrefs2ID,(*env)->NewStringUTF(env, PREF_LAST_TIME),dataValues[L_TIME],L_TIME);*/
	}
	else
	{
		drmDebugLog(" updating pref");
		if (index==LAST_RESPONSE || index== RUN_FIRST)
		{
			char buff[100];
			sprintf(buff,"%d",dataValues[index]);

			(*mEnv)->CallStaticVoidMethod(mEnv, Policy, updatePrefsID,getString(index),(*env)->NewStringUTF(env,buff),index);
		}
		else
			(*mEnv)->CallStaticVoidMethod(mEnv, Policy, updatePrefs2ID,getString(index),dataValues[index],index);
		drmDebugLog(" updating pref - end");
	}

}
void updateTime(jlong time)
{
	drmDebugLog("updating time !");
	if (dataValues[R_TIME]==0)
	{
		drmDebugLog("first time update!");
		dataValues[R_TIME]=time;
		dataValues[L_TIME]=time;
	}
	if (time <= dataValues[L_TIME])
	{
		drmDebugLog("real time not incremented");
		dataValues[L_TIME]=time;
	}
	else
	{
		drmDebugLog("real time incremented");
		dataValues[R_TIME]+=time-dataValues[L_TIME];
		dataValues[L_TIME]=time;
	}
	updatePrefs(R_TIME,0);
	updatePrefs(L_TIME,0);
}
JNIEXPORT void JNI_FUNCTION(installer_GDRMPolicy_setTime)(JNIEnv* env, jobject thiz, jlong time)
{
	//get Const String should've been called at least once so far. Also lockPointer[1] should've been intialized
	mEnv = env;
	drmDebugLog("lock check for set time");
	lockPointer2[0]=1/lockPointer1[1];
	drmDebugLog(" set time lock passed");
	updateTime(time);
	g_nTime=time;

}

JNIEXPORT void JNI_FUNCTION(installer_GDRMPolicy_processServer)(JNIEnv* env, jobject thiz, int attempt, int resp)
{
	mEnv = env;
	drmDebugLog(" processing server");
	serv_responded=1;
	if (attempt==0)
		lockPointer2[0]=0;
	if (attempt==0)
		attempt=INVALID;
	if (attempt==1)
		attempt=VALID;
	if (attempt==2)
		attempt=RETRY;
	if (attempt==resp)
	{
		drmDebugLog(" found response");

		if (resp== INVALID)
		{		// not licensed
			drmDebugLog(" response is fail");
			dataValues[LAST_RESPONSE]=INVALID;
			dataValues[VT_STAMP]=0;
			dataValues[RT_UNTIL]=0;
			dataValues[RT_MAX]=0;
			dataValues[RT_COUNT]=0;
			updatePrefs(0,1);
		}
		else if (resp==VALID)
		{

			drmDebugLog(" response is good");
			dataValues[LAST_RESPONSE]=VALID;
			dataValues[VT_STAMP]=dataValues[R_TIME]+VT_EXTRA;
			dataValues[RT_UNTIL]=dataValues[R_TIME]+GT_EXTRA;
			dataValues[RT_MAX]=GR_EXTRA;
			dataValues[RT_COUNT]=0;
			updatePrefs(0,1);
		}
		else if (resp==RETRY)
		{

			drmDebugLog(" response is retry");
			dataValues[LAST_RESPONSE]=RETRY;
			updatePrefs(LAST_RESPONSE,0);
		}
	}
	else
	   lockPointer2[0]++; // should allways terminate with 2 bogus attempts and only one real !

}

JNIEXPORT int JNI_FUNCTION(installer_GDRMPolicy_nativeAllow)(JNIEnv* env, jobject thiz, jlong time)
{
   drmDebugLog(" nativeAllow ");
   updateTime(time);
   if (dataValues[LAST_RESPONSE]==VALID && dataValues[R_TIME]<= dataValues[VT_STAMP]) //valid
   {
		drmDebugLog("valid policy , performing check");


		dataValues[RUN_FIRST]=0;
		updatePrefs(RUN_FIRST,0);
		drmDebugLog(" lock attempt");
		int size=1;
		lockPointer2[0]=1; // generate crash if lockPointer2 is uninited
		lockPointer3 = (int*) malloc (sizeof(int)*size);
		lockPointer3[0] = 1;
		drmDebugLog(" lock attempt done");
		return 1;

   }
   else if ((dataValues[LAST_RESPONSE]==RETRY || dataValues[LAST_RESPONSE]==VALID) /*&& time < g_nTime + MILLIS_PER_MINUTE*/) //retry
   {
	  drmDebugLog(" grace time period license");
	  if (dataValues[R_TIME] <= dataValues[RT_UNTIL] && dataValues[RT_COUNT] < dataValues[RT_MAX])
	  {

			drmDebugLog(" retry ok");
			dataValues[RUN_FIRST]=0;
			updatePrefs(RUN_FIRST,0);
			drmDebugLog(" lock attempt");
			int size = 1;
			lockPointer2[0]=1;// generate crash if lockPointer2 is uninited
			lockPointer3 = (int*) malloc (sizeof(int)*size);
			lockPointer3[0] = 1;
			drmDebugLog(" lock attempt done");
			dataValues[RT_COUNT]++;
			updatePrefs(RT_COUNT,0);
			return 1;
	  }
   }
   lockPointer3 = 0;
   drmDebugLog(" test failed ");
   return 0;
}
