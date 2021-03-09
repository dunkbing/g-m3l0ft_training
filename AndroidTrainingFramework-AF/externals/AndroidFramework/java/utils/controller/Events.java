#if USE_HID_CONTROLLER
package APP_PACKAGE.GLUtils.controller;


public interface Events 
{
	public static int UNDEFINED			=	0;
	
	public static int LeftTrigger		=	1;		//will take continuous values between (0, 1) 
	public static int RightTrigger		=	2;		//will take continuous values between (0, 1)
		
	public static int LeftStickX		=	3;		//will take continuous values between (-1, 1)
	public static int LeftStickY		=	4;		//will take continuous values between (-1, 1)
	
	public static int RightStickX		=	5;		//will take continuous values between (-1, 1)
	public static int RightStickY		=	6;		//will take continuous values between (-1, 1)
		
	public static int DpadEventUp		=	7;		//will take fixed values, 1 when pressed, 0 when released
	public static int DpadEventDown		=	8;		//will take fixed values, 1 when pressed, 0 when released
	public static int DpadEventLeft		=	9;		//will take fixed values, 1 when pressed, 0 when released
	public static int DpadEventRight	=	10;		//will take fixed values, 1 when pressed, 0 when released
	
	public static int LeftBumper		=	11;		//will take fixed values, 1 when pressed, 0 when released
	public static int RightBumper		=	12;		//will take fixed values, 1 when pressed, 0 when released
	
	public static int ButtonY			=	13;		//will take fixed values, 1 when pressed, 0 when released
	public static int ButtonA			=	14;		//will take fixed values, 1 when pressed, 0 when released
	public static int ButtonX			=	15;		//will take fixed values, 1 when pressed, 0 when released
	public static int ButtonB			=	16;		//will take fixed values, 1 when pressed, 0 when released
	
	public static int ButtonStart		=	17;		//will take fixed values, 1 when pressed, 0 when released
	public static int ButtonSelect		=	18;		//will take fixed values, 1 when pressed, 0 when released
	public static int ButtonBack		=	19;		//will take fixed values, 1 when pressed, 0 when released
	
	public static int LeftStickButton 	= 	20;		//will take fixed values, 1 when pressed, 0 when released
	public static int RightStickButton 	= 	21;		//will take fixed values, 1 when pressed, 0 when released
	
}
#endif