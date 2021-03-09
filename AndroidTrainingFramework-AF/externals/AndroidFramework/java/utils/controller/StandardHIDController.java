#if USE_HID_CONTROLLER
package APP_PACKAGE.GLUtils.controller;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.InputDevice.MotionRange;
import APP_PACKAGE.*;
//Some controllers have different MotionEvents then the usual HID (MogaPro). 
//We will use an Internal Event and Hardware Events. We will do the assignation internally between Hardware Events and Internal Events.
//By doing this, we are about to do:
//1) Account for controllers which are delivering non standard events
//2) Leave room to add an Input Interface for the users
//3) When a new "weirdo" controller comes, that generates a new type of (Motion/Key) event, and the current implementation cannot find a match, add it to the switch statement, and everything should work
//This is done in SetAxisInfo and SetButtonInfo


public class StandardHIDController
{
	private static StandardHIDController s_CurrentHID = null;//singleton
	
	private static TimerTask 	ControllerListener = null;
	
	private static boolean		ListenerCanRun = false;
	private static boolean		ListenerIsActive = false;
	
	private int					m_nInputDevice; //this will be the input device id
	
	private boolean 			m_bHasTriggersAsKeyEvents = false;	
	
	private static final int 	Key_Multiplier = -1; //to avoid clashes between keycodes and MotionEvent.getAxisValue

	private int[] StandardKeySequence = 
	{
			KeyEvent.KEYCODE_BUTTON_L1,
			KeyEvent.KEYCODE_BUTTON_R1,
			KeyEvent.KEYCODE_BUTTON_Y,
			KeyEvent.KEYCODE_BUTTON_X,
			KeyEvent.KEYCODE_BUTTON_A,
			KeyEvent.KEYCODE_BUTTON_B,
			KeyEvent.KEYCODE_BUTTON_THUMBL,
			KeyEvent.KEYCODE_BUTTON_THUMBR,
			KeyEvent.KEYCODE_BACK,
			KeyEvent.KEYCODE_BUTTON_SELECT,
			KeyEvent.KEYCODE_BUTTON_START,	
	};
	
	private int[] InternalKeySequence = 
		{
			Events.LeftBumper,
			Events.RightBumper,
			Events.ButtonY,
			Events.ButtonX,
			Events.ButtonA,
			Events.ButtonB,
			Events.LeftStickButton,
			Events.RightStickButton,
			Events.ButtonBack,
			Events.ButtonSelect,
			Events.ButtonStart,	
		};
	
	private static void ControllerConnected(InputDevice device)
	{
		
		s_CurrentHID = new StandardHIDController(device);
		NativeBridgeHIDControllers.NativeControllerConnected(device.getName());
	}
		
	private static void ControllerDisconnected()
	{
		s_CurrentHID = null;//this should cut all links, and gc should clean all class members
		System.gc();//give a hint to collect garbage
		NativeBridgeHIDControllers.NativeControllerDisconnected();
	}
		
	//known limitation-issue: you can only connect one controller at a time. The implementation will treat events only from the first
	private static void CheckForJoysticks()
	{
		InputDevice id = findBySource(InputDevice.SOURCE_JOYSTICK);
		if(s_CurrentHID == null)
		{
			//a controller is not connected at the moment 
			if(id == null)
			{
				//do nothing, controller isn't connected, and never was
			}
			else
			{
				//a controller has been detected for the first time
				ControllerConnected(id);
			}
		}
		else
		{
			//a controller is currently connected
			if(id == null)
			{
				//the controller has been disconnected
				ControllerDisconnected();
			}
			else
			{
				//a controller is connected. we need to check if it's the same just in case:
				if(s_CurrentHID.m_nInputDevice == id.getId())
				{
					//same controller, do nothing
				}
				else
				{
					//another controller has been connected then what we had.
					//disconnect the current one. we will handle the new connection in the next tick
					ControllerDisconnected();
				}
			}
		}
	}
		
		
	private static InputDevice findBySource(int sourceType) 
	{
		int[] ids = InputDevice.getDeviceIds(); 

        // Return the first matching source we find...
		for (int i = 0; i < ids.length; i++) 
		{
			InputDevice dev = InputDevice.getDevice(ids[i]);
			int sources = dev.getSources();

			if ((sources & ~InputDevice.SOURCE_CLASS_MASK & sourceType) != 0) 
			{
				final List<MotionRange> ranges = dev.getMotionRanges();
				for(int rangeindex = 0; rangeindex < ranges.size(); rangeindex++)
                {
                    MotionRange range = ranges.get(rangeindex);
                    if ((range.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) != 0) {
						 return dev;
                    }
                }
			}
        }
        
        return null;
	}

	
	

	
	private class BaseEvent
	{
				
		int				m_nSystemEvent 		= -1;//this is the system event
		int				m_nInternalEvent 	= Events.UNDEFINED;//this is our local event
		
		double			Value;//for Axis, range, for Buttons, 1.0 for press, 0.0 for release
		double			CenterValueEpsilon;//Axis Events don't have an absolute 0 when in idle, but rather an epsilon
	}
	
	
	
	//Actual Controller -> what's to be expected from a HID Controller (we would very much want that all of these are available):
	
		
	ArrayList<BaseEvent>		m_arrControllerInput = new ArrayList<BaseEvent>();
	
	
	private void GenericAddEvent(int SystemEvent, int InteralEvent, BaseEvent button)
	{
			
		//we will make the LeftTrigger behave like an Axis with 0.0 or 1.0, depending on a keypress...
		button.m_nSystemEvent = SystemEvent;
		button.m_nInternalEvent = InteralEvent;
		
		
		button.Value = 0.0f;//make sure it is in idle //not pressed
		
		m_arrControllerInput.add(button);
	}
	
	private BaseEvent GetBaseEventFromInternalEvent(int InternalEvent)
	{
		for (int i = 0; i < m_arrControllerInput.size(); i++)
		{
			BaseEvent ev = m_arrControllerInput.get(i);
			if(ev.m_nInternalEvent == InternalEvent)
			{
				return ev;
			}
		}
		
		return null;
	}
	
	private void RemoveButtonAssignation(BaseEvent button)
	{
		m_arrControllerInput.remove(button);
	}
	
	private BaseEvent AssignInputInternal(int keyCode, int InternalEvent, boolean override)
	{
		BaseEvent ev = GetBaseEventFromInternalEvent(InternalEvent);
		
		if(ev == null)//first time assignation
		{
			ev = new BaseEvent();
			ev.CenterValueEpsilon = 0.0;
			GenericAddEvent(keyCode * Key_Multiplier, InternalEvent, ev);
		}
		else
		{
			if(override == true)
			{
				RemoveButtonAssignation(ev);
				ev = new BaseEvent();
				ev.CenterValueEpsilon = 0.0;
				GenericAddEvent(keyCode * Key_Multiplier, InternalEvent, ev);
			}
		}
		
		return ev;
	}
	
	private BaseEvent AssignMotionInternal(InputDevice.MotionRange range, int InternalEvent, boolean override)
	{
		BaseEvent ev = GetBaseEventFromInternalEvent(InternalEvent);
		
		if(ev == null)//first time assignation
		{
			ev = new BaseEvent();
			ev.CenterValueEpsilon = range.getFlat() + range.getFuzz();
			GenericAddEvent(range.getAxis(), InternalEvent, ev);
			
		}
		else
		{
			if(override == true)
			{
				RemoveButtonAssignation(ev);
				ev = new BaseEvent();
				ev.CenterValueEpsilon = range.getFlat() + range.getFuzz();
				GenericAddEvent(range.getAxis(), InternalEvent, ev);
			}
		}
		
		return ev;
	}
	
	/**
	 * 
	 * all assing/add are associating internally the type in the function name, to a whatever system key.
	 * for instance, calling AddLeftTrigger(InputDevice.MotionRange range, boolean override)
	 * will internally assign LeftTrigger
	 * to a whatever MotionRange
	 * so if the user decides to call this function and passing MotionRange for AXIS_X, then the left joystick axis X will act as left trigger
	 * LeftTrigger is an internal event, internal to this implementation!
	 * 
	 * TODO (if we ever want to add an interface to map keys) expose the assignation functions
	 */
	

		
	/**
	 * 
	 * @param value
	 * @param eventToDispatch
	 * @return
	 */
	private boolean DispatchGenericEvent (float value, BaseEvent eventToDispatch)
	{
		if (Math.abs(value) >  eventToDispatch.CenterValueEpsilon) 
		{			
			eventToDispatch.Value = value;
			
			NativeBridgeHIDControllers.NativeHandleInputEvents(eventToDispatch.m_nInternalEvent, eventToDispatch.Value);
			
			return true;
        }
		else if(Math.abs(eventToDispatch.Value) > eventToDispatch.CenterValueEpsilon)
		{
			eventToDispatch.Value = 0.0f;//set in idle
			
			NativeBridgeHIDControllers.NativeHandleInputEvents(eventToDispatch.m_nInternalEvent, eventToDispatch.Value);
			
			return true;
		}
		
		return false;
	}
	
	private boolean HandleGenericMotionEvent (MotionEvent event, int InternalEvent)
	{
		BaseEvent eventToDispatch = GetBaseEventFromInternalEvent(InternalEvent);
		if(eventToDispatch == null || event == null)
		{
			return false;
		}
		
		float Val = event.getAxisValue(eventToDispatch.m_nSystemEvent);
		return DispatchGenericEvent(Val, eventToDispatch);
	}
	
	
	private void SetButtonInfoFromSequence(int[] sequence)
	{
		for (int i = 0; i < sequence.length; i++)
		{
			AssignInputInternal(sequence[i], InternalKeySequence[i], true);	
		}
		//force add the DPADs:
		AssignInputInternal(KeyEvent.KEYCODE_DPAD_UP, Events.DpadEventUp, true); 
		AssignInputInternal(KeyEvent.KEYCODE_DPAD_DOWN, Events.DpadEventDown, true);
		AssignInputInternal(KeyEvent.KEYCODE_DPAD_LEFT, Events.DpadEventLeft, true);
		AssignInputInternal(KeyEvent.KEYCODE_DPAD_RIGHT, Events.DpadEventRight, true);
	}
	
	/** 
	 * SetButtonInfo replaced by SetButtonInfoFromSequence(StandardKeySequence)
	*/
	
	private void SetAxisInfo(InputDevice dev) 
	{
		List<InputDevice.MotionRange> ranges = dev.getMotionRanges();

		Iterator<InputDevice.MotionRange> iterator = ranges.iterator();
		while ( iterator.hasNext() )
		{
			InputDevice.MotionRange range = iterator.next();
			
			//av TODO to check if we actually need normalization values for max/min
			//av: it seems that we don't, but keeping the todo just in case, to remember if a device provides values outside (-1,1) boundries
			
			switch(range.getAxis())
			{
				case MotionEvent.AXIS_BRAKE:
				{
					AssignMotionInternal(range, Events.LeftTrigger, true);
					break;
				}
				case MotionEvent.AXIS_GAS:
				{
					AssignMotionInternal(range, Events.RightTrigger, true);
					break;
				}
				case MotionEvent.AXIS_LTRIGGER:
				{
					AssignMotionInternal(range, Events.LeftTrigger, false);
					break;
				}
				case MotionEvent.AXIS_RTRIGGER:
				{
					AssignMotionInternal(range, Events.RightTrigger, false);
					break;
				}
				case MotionEvent.AXIS_X:
				{
					AssignMotionInternal(range, Events.LeftStickX, true);
					break;
				}
				case MotionEvent.AXIS_Y:
				{
					AssignMotionInternal(range, Events.LeftStickY, true);
					break;
				}
				case MotionEvent.AXIS_Z:
				{
					AssignMotionInternal(range, Events.RightStickX, true);
					break;
				}
				case MotionEvent.AXIS_RZ:
				{
					AssignMotionInternal(range, Events.RightStickY, true);
					break;
				}
				case MotionEvent.AXIS_RX:
				{
					AssignMotionInternal(range, Events.RightStickX, false);
					break;
				}
				case MotionEvent.AXIS_RY:
				{
					AssignMotionInternal(range, Events.RightStickY, false);
					break;
				}
			}
		}
		
	}
	
	private void SpecialAssignmentForTriggersWithoutMotionEvent()
	{
		//special controller cases, when they don't have Triggers (a lot of them!)
		//hack to check if the 2 are available
		//assigning the hardware event L1 & R1 to internal events Right & Left Trigger
		
		m_bHasTriggersAsKeyEvents = false;
		
		if(GetBaseEventFromInternalEvent(Events.LeftTrigger) == null &&
			GetBaseEventFromInternalEvent(Events.RightTrigger) == null )
			
		{	
			if(KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BUTTON_L2) && KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BUTTON_R2))
			{
				AssignInputInternal(KeyEvent.KEYCODE_BUTTON_L2, Events.LeftTrigger, true);
				AssignInputInternal(KeyEvent.KEYCODE_BUTTON_R2, Events.RightTrigger, true);
				
				m_bHasTriggersAsKeyEvents = true;//will handle Triggers on Keys and not on Motions
			}
			else if(KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BUTTON_L1) && KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BUTTON_R1))
			{				
				//we are going to assume that a controller without triggers at least has bumpers :)
				//so we are going to remove the bumpers, and assign the triggers to bumpers. For special games, the bumpers might be more important, so patch out this if statement when necessery
				RemoveButtonAssignation(GetBaseEventFromInternalEvent(Events.LeftBumper));
				AssignInputInternal(KeyEvent.KEYCODE_BUTTON_L1, Events.LeftTrigger, true);
			
				RemoveButtonAssignation(GetBaseEventFromInternalEvent(Events.RightBumper));
				AssignInputInternal(KeyEvent.KEYCODE_BUTTON_R1, Events.RightTrigger, true);
				
				m_bHasTriggersAsKeyEvents = true;//will handle Triggers on Keys and not on Motions
			}
			else
			{
				//do nothing, but if this else gets hit, it should be interesting to check for an alternative keycode to assign
			}
		}
		else
		{
			//do nothing, we already have LeftTrigger and RightTrigger assigned
		}
		
		//end hackish thing
	}
	
	private void SetAdditionalEventsNoOverride()
	{
		AssignInputInternal(KeyEvent.KEYCODE_BUTTON_L2, Events.LeftTrigger, false);
		AssignInputInternal(KeyEvent.KEYCODE_BUTTON_R2, Events.RightTrigger, false);
		
		SpecialAssignmentForTriggersWithoutMotionEvent();
	}
	
	
	private StandardHIDController(InputDevice device)
	{
		/****
		 * Added SetButtonInfoFromSequence
		 * This leaves room for hardcoding when very weird controllers will appear, by creating other type of Key Sequences
		 * For example, right after setButtonInfo you can add:
		 * if(WeirdController) //detect WeirdController by looking into InputDevice
		 * SetButtonInfoFromSequence(WeirdKeySequence);//look at StandardKeySequence to create your own sequence
		 */
		
		
		
		m_nInputDevice = device.getId();
		SetButtonInfoFromSequence(StandardKeySequence);
		SetAxisInfo(device);
		
		SetAdditionalEventsNoOverride();
	}
	
	private boolean HandleInputEventPressedInternal(KeyEvent event)
	{
		if (event.getDevice().getId() == m_nInputDevice && event.getAction() == KeyEvent.ACTION_DOWN)
		{
			for(int i = 0; i < m_arrControllerInput.size(); i++)
			{
				BaseEvent LocalInput = m_arrControllerInput.get(i);
				if(event.getKeyCode() == (LocalInput.m_nSystemEvent /  Key_Multiplier))
				{
					return DispatchGenericEvent(1.0f, LocalInput);
				}
			}
		}
		return false;
	}
	
	private boolean HandleInputEventReleasedInternal(KeyEvent event)
	{
		if (event.getDevice().getId() == m_nInputDevice && event.getAction() == KeyEvent.ACTION_UP)
		{
			for(int i = 0; i < m_arrControllerInput.size(); i++)
			{
				BaseEvent LocalInput = m_arrControllerInput.get(i);
				if(event.getKeyCode() == LocalInput.m_nSystemEvent / Key_Multiplier)
				{
					return DispatchGenericEvent(0.0f, LocalInput);
				}
			}
		}
		return false;
	}
	
	private boolean HandleMotionEventInternal(MotionEvent event)
	{
		if (event != null && event.getDevice() != null && event.getDevice().getId() == m_nInputDevice && event.getAction() == MotionEvent.ACTION_MOVE)
		{
			boolean lt = false;
			boolean rt = false;
			
			if(m_bHasTriggersAsKeyEvents == false) //hack for controllers that don't have triggers
			{
				lt = HandleGenericMotionEvent(event, Events.LeftTrigger);
				rt = HandleGenericMotionEvent(event, Events.RightTrigger);
			}		
			
			boolean ljX = HandleGenericMotionEvent(event, Events.LeftStickX);
			boolean ljY = HandleGenericMotionEvent(event, Events.LeftStickY);
			
			boolean rjX = HandleGenericMotionEvent(event, Events.RightStickX);
			boolean rjY = HandleGenericMotionEvent(event, Events.RightStickY);
			
			
			//doing the following return statement, means that we only care to treat joysticks and triggers.
			//if any of the above events are generated, we will lose any "HAT" events, but we wanted to do this anyway (bug in controllers where they generate both AXIS_X and HAT_X)
			//meaning, that when we don't press a joystick/trigger, we will get dpad events as keyevents, otherwise, ignore them
			return lt || rt || ljX || ljY || rjX || rjY; 
		}
		
		return false;
	}
	
	/**
	 * Public Interface:
	 */
	
	/**
	 * 
	 * @param event	The Event that is received in your Activity on onKeyDown
	 * @return True if the Event is associated with a connected controller. In this case return true on onKeyDown. Otherwise false. 
	 */
	public static boolean HandleInputEventPressed(KeyEvent event)
	{
		if(s_CurrentHID == null) return false;//no controller connected, do nothing
		
		return s_CurrentHID.HandleInputEventPressedInternal(event);
	}
	
	/**
	 * 
	 * @param event	The Event that is received in your Activity on onKeyUp
	 * @return True if the Event is associated with a connected controller. In this case return true on onKeyUp. Otherwise false. 
	 */
	public static boolean HandleInputEventReleased(KeyEvent event)
	{
		if(s_CurrentHID == null) return false;//no controller connected, do nothing
		
		return s_CurrentHID.HandleInputEventReleasedInternal(event);
	}
	
	/**
	 * 
	 * @param event	The Event that is received in your Activity on onMotionEvent
	 * @return True if the Event is associated with a connected controller. In this case return true on onMotionEvent. Otherwise false. 
	 */
	public static boolean HandleMotionEvent(MotionEvent event)
	{
		if(s_CurrentHID == null) return false;//no controller connected, do nothing
		
		return s_CurrentHID.HandleMotionEventInternal(event);
	}
	
	/**
	 * Starts Listening for Controllers.
	 * @param seconds Number of seconds to check if a state of a controller has been changed (connected/disconnected). 
	 */
	public static void StartControllerListener(final int seconds) //better send positive seconds :)
	{
		ListenerIsActive = true;
		if(StandardHIDController.ControllerListener == null)
		{
			StandardHIDController.ControllerListener = new TimerTask() 
			{	
				@Override
				public void run() 
				{
					try
					{
						while (ListenerIsActive == true)
						{
							if(ListenerCanRun == true)
							{
								CheckForJoysticks();
							}
							
							Thread.sleep(seconds * 1000);
						}
					}
					catch(Exception e)
					{
					}
				}
			};
			
			Executors.newSingleThreadScheduledExecutor().schedule(ControllerListener, 0, TimeUnit.SECONDS);
		}
	}
	
	public static void StopControllerListener()
	{
		ListenerIsActive = false;
		StandardHIDController.ControllerListener = null;
	}
	
	/**
	 * Resumes Listening for Controllers
	 */
	public static void ResumeControllerListener()
	{
		ListenerCanRun = true;
	}
	
	/**
	 * Stops Listening for Controllers
	 */
	public static void PauseControllerListener()
	{
		ListenerCanRun = false;
	}
}
#endif //#if USE_HID_CONTROLLER
