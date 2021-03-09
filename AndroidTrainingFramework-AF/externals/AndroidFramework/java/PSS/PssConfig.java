#if USE_PSS
package APP_PACKAGE.pss;

public interface PssConfig {
	//Activity to launch
	final String LAUNCH_ACTIVITY_PACKAGE = STR_APP_PACKAGE;
	final String LAUNCH_ACTIVITY_CLASS = LAUNCH_ACTIVITY_PACKAGE + "." +GAME_ACTIVITY_NAME_STR;				//Main activity
	
	final boolean USE_DISABLE_OPTION = (PSS_USE_DISABLE_OPTION == 1);
}
#endif