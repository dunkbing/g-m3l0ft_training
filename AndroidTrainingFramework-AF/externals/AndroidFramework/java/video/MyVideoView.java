#if USE_VIDEO_PLAYER
package APP_PACKAGE;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.MotionEvent;
import android.view.KeyEvent;

import android.widget.ImageButton;
import android.widget.VideoView;

import android.content.Intent;
import android.media.MediaPlayer;
import android.content.Context;

#if USE_VIDEO_SUBTITLES
import android.os.Handler;
import android.os.Message;
import java.util.ArrayList;
import java.util.Locale;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedReader;
import android.widget.TextView;
import android.widget.LinearLayout;
#endif

#if VIDEO_ENABLE_PHONE_CALL_LISTENER
import android.telephony.TelephonyManager;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
#endif


#if ADS_USE_FLURRY
import APP_PACKAGE.Flurry.GLFlurry;
#endif

public class MyVideoView extends Activity
{
	

    public static boolean IsSoundOn=false;
	private static VideoView mVideoView;
#if USE_ALL_MEDIAPLAYER_FEATURE
	private ImageButton mBackWard;
	private ImageButton mPlay;
	private ImageButton mPause;
	private ImageButton mForward;
	private ImageButton mStop;
#endif
	private ImageButton mSkip;
	private String mIntroVideo;

	private static boolean mIsPausing = false;
	public static int m_CurrentPosition = 0;
	private static boolean flagButton = true;
	public static boolean mbIsVideoFinish = false;
	public static boolean sb_isFocus = false;
#if USE_VIDEO_SUBTITLES	
	private TextView mSrtText;
	private Handler mHandler;
	static ArrayList<Integer> mSrtTimeS = new ArrayList<Integer>();
	static ArrayList<Integer> mSrtTimeE = new ArrayList<Integer>();
	static ArrayList<String> mSrtString = new ArrayList<String>();
	private boolean mSubtitleExists = true;
	private boolean lastFlagButton = true;
#endif	
#if VIDEO_ENABLE_PHONE_CALL_LISTENER	
	public static TelephonyManager m_TelephonyManager = null;
#endif

	private void InitButton()
	{
	#if USE_ALL_MEDIAPLAYER_FEATURE
		mBackWard = (ImageButton) findViewById(R.id.backward);
		mPlay = (ImageButton) findViewById(R.id.play);
		mPause = (ImageButton) findViewById(R.id.pause);
		mForward = (ImageButton) findViewById(R.id.forward);
		mStop = (ImageButton) findViewById(R.id.stop);
		
		mBackWard.setVisibility(View.GONE);
		mPlay.setVisibility(View.GONE);
		mPause.setVisibility(View.GONE);
		mForward.setVisibility(View.GONE);
		mStop.setVisibility(View.GONE);
	#endif
		mSkip = (ImageButton) findViewById(R.id.skip);
		
	#if USE_VIDEO_SUBTITLES
		mSrtText = (TextView) findViewById(R.id.SrtText);
		mSrtText.setText("", TextView.BufferType.NORMAL);
	#endif
	
		HideButton();
	
	#if USE_ALL_MEDIAPLAYER_FEATURE
		mBackWard.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view)
			{
				if (mVideoView != null)
					seekVideoTo(mVideoView.getCurrentPosition() - VIDEO_SEEK_BACK_AND_FORWARD);
			}
		});

		mPlay.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view)
			{
				playVideo();
			}
		});

		mPause.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view)
			{
				pauseVideo();
			}
		});

		mForward.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view)
			{
				if (mVideoView != null)
					seekVideoTo(mVideoView.getCurrentPosition() + VIDEO_SEEK_BACK_AND_FORWARD);
			}
		});

		mStop.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view)
			{
				stopVideo();
			}
		});
	#endif

		mSkip.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view)
			{
				StartGame();
			}
		});
	}

	/**
	 * Makes the control buttons visible
	 */
	public void ShowButton()
	{
	#if USE_ALL_MEDIAPLAYER_FEATURE
	#if !BUILD_FOR_FIRMWARE_1_6
		if (mVideoView != null && mVideoView.canSeekBackward())
	#else
		if (mVideoView != null)
	#endif
			mBackWard.setVisibility(View.VISIBLE);
		mPlay.setVisibility(View.VISIBLE);
	#if !BUILD_FOR_FIRMWARE_1_6
		if (mVideoView != null && mVideoView.canPause())
	#else
		if (mVideoView != null)
	#endif
			mPause.setVisibility(View.VISIBLE);
	#if !BUILD_FOR_FIRMWARE_1_6
		if (mVideoView != null && mVideoView.canSeekForward())
	#else
		if (mVideoView != null)
	#endif
			mForward.setVisibility(View.VISIBLE);
		mStop.setVisibility(View.VISIBLE);
	#endif

		mSkip.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Makes the control buttons invvisile
	 */
	public void HideButton()
	{
	#if USE_ALL_MEDIAPLAYER_FEATURE
		mBackWard.setVisibility(View.GONE);
		mPlay.setVisibility(View.GONE);
		mPause.setVisibility(View.GONE);
		mForward.setVisibility(View.GONE);
		mStop.setVisibility(View.GONE);
	#endif
	
		mSkip.setVisibility(View.GONE);
	}
	

	@Override
	public void onCreate(Bundle icicle)
	{
		DBG("MyVideoView", "****************onCreate()");
		super.onCreate(icicle);

	#if PAUSE_USER_MUSIC
		Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "stop");
	#endif

	#if VIDEO_ENABLE_PHONE_CALL_LISTENER		
		m_TelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		m_TelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
	#endif

		getWindow().setFlags(	WindowManager.LayoutParams.FLAG_FULLSCREEN |
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_FULLSCREEN |
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.videoview);
		InitButton();
		initializeVideo();
	#if USE_VIDEO_SUBTITLES	
		
		importVideoSubtitles(0);
	
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if(mSubtitleExists && MovieRunning()==1)
				{
					mHandler.sendEmptyMessageDelayed(0, 100);
				}
			}
		};
	#endif		
		//playVideo();
		sb_isFocus = true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_SEARCH)
			return true;
		if (keyCode == 24 || keyCode == 25 || keyCode == KeyEvent.KEYCODE_CAMERA)
		{
			return false;
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if( keyCode == KeyEvent.KEYCODE_MENU)
		{
		#if !BUILD_FOR_FIRMWARE_1_6
		   event.startTracking();
		#endif
		   return true;
		}

		if(keyCode == KeyEvent.KEYCODE_SEARCH)
			return true;

		if (keyCode == 24 || keyCode == 25
			|| keyCode == KeyEvent.KEYCODE_CAMERA)
		{
			return false;
		}
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			Intent myIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
            this.startActivity(myIntent);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

#if !OS_16
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event)
	{
		// * Bug Fixed ['3626904']: '[INT]All: The Camera key can not perform an interruption'
		if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_CAMERA) {
			return false;
		}
		return true;

	}
#endif
	private int currentVolume = 0;

	@Override
	protected void onDestroy()
	{
		DBG("MyVideoView", "****************onDestroy()");		
		super.onDestroy();
	}

	@Override
	protected void onPause()
	{
		DBG("MyVideoView", "****************onPause()");
		pauseVideo();
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		DBG("MyVideoView", "****************onResume()");
		
		super.onResume();
		//playVideo();
		if (m_CurrentPosition != 0)
			seekVideoTo(m_CurrentPosition);
	#if VIDEO_ENABLE_PHONE_CALL_LISTENER		
		if ( m_callState == TelephonyManager.CALL_STATE_OFFHOOK )
		{
			Intent myIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
            this.startActivity(myIntent);
			return;
		}
	#endif
	}

	@Override
	protected void onStart()
	{
		super.onStart();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStart(this);
	#endif
		playVideo();
	#if ADS_USE_FLURRY
		GLFlurry.onStartSession(this);
	#endif
	}
	
	@Override
	protected void onStop()
	{
		DBG("MyVideoView", "****************onStop()");
		super.onStop();
	#if USE_GOOGLE_ANALYTICS_TRACKING && GA_AUTO_ACTIVITY_TRACKING
		APP_PACKAGE.utils.GoogleAnalyticsTracker.activityStop(this);
	#endif
	#if ADS_USE_FLURRY
		GLFlurry.onEndSession(this);
	#endif
	}
	
	private void StartGame()
	{
		DBG("MyVideoView", "**************** StartGame()");
		mbIsVideoFinish = true;
		//Intent i = new Intent(MyVideoView.this, CLASS_NAME.class);
		//startActivity(i);
		finishVideo();
	}

	private void finishVideo()
	{
	#if VIDEO_ENABLE_PHONE_CALL_LISTENER
		try
		{
			if (m_TelephonyManager != null)
				m_TelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
			m_TelephonyManager = null;
		} catch (Exception e){}		
	#endif
		stopVideo();
		finish();
	}
	
	/**
	 * Initializes a new VideoView object
	 */
	private void initializeVideo()
	{
		
		/*AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		currentVolume =  am.getStreamVolume(AudioManager.STREAM_MUSIC);

		if(am.getRingerMode() == AudioManager.RINGER_MODE_SILENT || am.getRingerMode() == 1)
		{
			am.setStreamMute(AudioManager.STREAM_MUSIC,false);
		}*/
		
		mbIsVideoFinish = false;
		
		if (mIntroVideo == null)
		{
			mIntroVideo = this.getIntent().getStringExtra("video_name");
			this.getIntent().removeExtra("video_name");
		}
		DBG("MyVideoView", "file name = " + mIntroVideo);
		
		mIsPausing = true;

		try
		{
			mVideoView = (VideoView) findViewById(R.id.surface_view);
			
			mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
				{
					public void onCompletion(MediaPlayer mp)
					{
						DBG("MyVideoView","****************onCompletion()");
						StartGame();
					}
				}
			);
	
			mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener ()
				{
					public boolean onError(MediaPlayer mp, int what, int extra)
					{
						DBG("MyVideoView","****************setOnErrorListener() what = "+what+" extra = "+extra);
	
						//Try to start the game when error occured
						StartGame();
						return true;
					}
				}
			);
				
			if(!IsSoundOn){
					mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener(){
						@Override
						public void onPrepared(MediaPlayer mp) {
							mp.setVolume(0, 0);
						}
						
					});
				}				
				
			
			mVideoView.setVideoPath(mIntroVideo);
			
			if (m_CurrentPosition != 0)
				seekVideoTo(m_CurrentPosition);
		}
		catch (Exception e)
		{
			ERR("MyVideoView", "error: " + e.getMessage());
			if (mVideoView != null)
			{
				mVideoView.stopPlayback();
				mVideoView = null;
				StartGame();
			}
		}
	}
	
	/**
	 * Pauses the video object, if it can be pause or stops it
	 */
	private void pauseVideo()
	{
		if (!mIsPausing)
		{
			if(mVideoView != null)
			{
				try
				{
				#if !BUILD_FOR_FIRMWARE_1_6
					if (mVideoView.canPause())
					{
						mVideoView.pause();
					#if PAUSE_LOGO_VIDEO
						if(mVideoView.getCurrentPosition() > m_CurrentPosition)
							m_CurrentPosition = mVideoView.getCurrentPosition();
					#else
						m_CurrentPosition = 0;
					#endif
					}
					else
				#endif
					{
						mVideoView.stopPlayback();
						mVideoView = null;
						m_CurrentPosition = 0;
					}
				} catch (Exception e){m_CurrentPosition = 0;}
				mIsPausing = true;
			}
		}
	}

	/**
	 * Starts playing the video or resumes the play
	 */
	private void playVideo()
	{
		mbIsVideoFinish = false;
		if (mVideoView == null)
		{
			initializeVideo();
		}
		if (mIsPausing)
		{
		#if !PAUSE_LOGO_VIDEO
			if (m_CurrentPosition == 0)
			{
				seekVideoTo(0);
			}
		#endif
		#if USE_VIDEO_SUBTITLES
			mHandler.sendEmptyMessage(0);
		#endif
			mVideoView.start();
			mVideoView.requestFocus();
			mIsPausing = false;
		}
	}
	
	/**
	 * Sets the video position to seek
	 * @param seek new current position in the movie
	 */
	private void seekVideoTo(int seek)
	{
		if (mVideoView != null)
		{
			if (seek < mVideoView.getCurrentPosition())
			{
			#if !BUILD_FOR_FIRMWARE_1_6
				if (mVideoView.canSeekBackward())
			#endif
				{
				#if !BUILD_FOR_FIRMWARE_1_6
					if (!mIsPausing && mVideoView.canPause())
						mVideoView.pause();
				#endif
					mVideoView.seekTo(seek);
				#if !BUILD_FOR_FIRMWARE_1_6
					if (!mIsPausing && mVideoView.canPause())
						mVideoView.start();
					if (mIsPausing && mVideoView.canPause())
					{
						mVideoView.start();
						try{Thread.sleep(100);}catch(Exception e){}
						mVideoView.pause();
					}
				#endif
				#if USE_VIDEO_SUBTITLES
					mSrtText.setText("", TextView.BufferType.NORMAL);
					if (!mIsPausing)
						mHandler.sendEmptyMessage(0);
				#endif
				if (seek > 0)
					m_CurrentPosition = seek;
				else
					m_CurrentPosition = 0;
				}
			}
			else 
			{
			#if !BUILD_FOR_FIRMWARE_1_6
				if (mVideoView.canSeekForward())
			#endif
				{
				#if !BUILD_FOR_FIRMWARE_1_6
					if (!mIsPausing && mVideoView.canPause())
						mVideoView.pause();
				#endif
					mVideoView.seekTo(seek);
				#if !BUILD_FOR_FIRMWARE_1_6
					if (!mIsPausing && mVideoView.canPause())
						mVideoView.start();
					if (mIsPausing && mVideoView.canPause())
					{
						mVideoView.start();
						try{Thread.sleep(100);}catch(Exception e){}
						mVideoView.pause();
					}
				#endif
				#if USE_VIDEO_SUBTITLES
					mSrtText.setText("", TextView.BufferType.NORMAL);
					if (!mIsPausing)
						mHandler.sendEmptyMessage(0);
						
				#endif
				if (seek > 0)
					m_CurrentPosition = seek;
				else
					m_CurrentPosition = 0;
				}
			}
		}
	}

	/**
	 * Stops the video object and destroys  it
	 */
	private void stopVideo()
	{
		if (mVideoView != null)
		{
			mVideoView.stopPlayback();
			mVideoView = null;
			m_CurrentPosition = 0;
		#if USE_VIDEO_SUBTITLES
			mSrtText.setText("",TextView.BufferType.NORMAL);
		#endif
		}
	}
	
	public boolean onTouchEvent(final MotionEvent event)
	{
		if(mVideoView != null)
		{
			if(mVideoView.getCurrentPosition() > VIDEO_SKIP_TIME)
			{
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
				#if USE_VIDEO_SUBTITLES
					mSrtText.setText("", TextView.BufferType.NORMAL);
				#endif
					if(flagButton == true)
					{
						ShowButton();
						flagButton = false;
					}
					else
					{
						HideButton();
						flagButton = true;
					}
				 }
			}
		}
		return true;
	}

	public void onWindowFocusChanged(boolean focus)
	{
		//Only resume when focused
		if (focus)
		{
			APP_PACKAGE.GLUtils.LowProfileListener.ActivateImmersiveMode(this);
			
		#if VIDEO_ENABLE_PHONE_CALL_LISTENER		
			if ( m_callState == TelephonyManager.CALL_STATE_OFFHOOK )
			{
				Intent myIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
				this.startActivity(myIntent);
				return;
			}
		#endif
			if(!mbIsVideoFinish)
				playVideo();
		}
		else
		{
			pauseVideo();
		}
		
		sb_isFocus = focus;
	}

	public static int isVideoCompleted()
	{
		if( mbIsVideoFinish )
			return 1;
		return 0;
	}
#if USE_VIDEO_SUBTITLES

	/**
	 * Import subtitles if exist
	 * @param check 2 checks (english and default language)
	 */
	private void importVideoSubtitles(int check)
	{
		Locale mLocale = Locale.getDefault();
	    String VideoSrtF = mIntroVideo.substring(0,mIntroVideo.length()-4);
	    if (check == 0)
	    	VideoSrtF = VideoSrtF + "_" + mLocale.getISO3Language() + ".srt";
	    else
	    {
	    	VideoSrtF = VideoSrtF + "_eng.srt";
	    	mSubtitleExists = true;
	    }
	    DBG("MyVideoView", "Subtitle Files:" + VideoSrtF);
	
	    Reader fis;
		String storedString = "";
		TextView textViewToChange = new TextView(this);
		try {
			fis = new InputStreamReader(new FileInputStream(VideoSrtF),"UTF-8");//this.getAssets().open("DH2.srt");
			BufferedReader dataIO = new BufferedReader(fis);
			String strLine = null;
			String s1 = null;
			String TimeStr = null;
	
			while ((strLine = dataIO.readLine()) != null) {
				TimeStr = dataIO.readLine();
				String2Time(TimeStr);	//reading time string and sed for parsing to get time in milliseconds
				while(true)   //reading block of strings
				{
					s1 = dataIO.readLine();
					if(s1 == null )
						break;
					if(s1.compareTo("") == 0)
						break;
					storedString += s1;
					storedString += "\n";
				}
				if(storedString.compareTo("") != 0)
				{
					mSrtString.add(storedString);
					storedString = "";
				}
			}
			dataIO.close();
			fis.close();
		}
		catch  (Exception e) {
			ERR("MyVideoView", e.getMessage());
			mSubtitleExists = false;
			if (check == 0 && !VideoSrtF.endsWith("_eng.srt"))
				importVideoSubtitles(1);
		}
	}

	/**
	 * Chenges the text's subtitle
	 */
	private int MovieRunning()
	{
		if (mVideoView != null && mVideoView.isPlaying())
		{
			if (flagButton != lastFlagButton)
			{
				if (flagButton)
					mSrtText.setPadding(0, 0, 0, 0);
				else
					mSrtText.setPadding(0, 0, 0, mSkip.getHeight());
				lastFlagButton = flagButton;
			}
			for(int i=0;i<mSrtString.size();i++)
			{
				if((mVideoView.getCurrentPosition()  > mSrtTimeS.get(i)) && (mVideoView.getCurrentPosition() < mSrtTimeE.get(i)))
				{
					mSrtText.setText(mSrtString.get(i), TextView.BufferType.NORMAL);
					break;
				}
				else
				{
					mSrtText.setText("", TextView.BufferType.NORMAL);
				}
			}
			if((mVideoView.getCurrentPosition() > mSrtTimeE.get(mSrtString.size()-1)))
			{
				mSrtText.setText("", TextView.BufferType.NORMAL);
				return 0;
			}
		}
		return 1;
	}
	
	private static void String2Time(String StrTime)
	{
		mSrtTimeS.add((Integer.parseInt(StrTime.substring(0,2))*3600000)+(Integer.parseInt(StrTime.substring(3,5))*60000)+(Integer.parseInt(StrTime.substring(6,8))*1000)+Integer.parseInt(StrTime.substring(9,12)));
		mSrtTimeE.add((Integer.parseInt(StrTime.substring(17,19))*3600000)+(Integer.parseInt(StrTime.substring(20,22))*60000)+(Integer.parseInt(StrTime.substring(23,25))*1000)+Integer.parseInt(StrTime.substring(26,29)));
    }
#endif	
#if VIDEO_ENABLE_PHONE_CALL_LISTENER
	static boolean m_allowResumeAfterCall = false;	
	
	static int m_callState = TelephonyManager.CALL_STATE_IDLE;
	private PhoneStateListener mPhoneStateListener = new PhoneStateListener()
	{
		@Override
		public void onCallStateChanged(int state, String incomingNumber)
		{
			String callState = "UNKNOWN";
			switch(state)
			{
				case TelephonyManager.CALL_STATE_IDLE:					
					if( m_allowResumeAfterCall )
					{
						m_allowResumeAfterCall = false;
						if ( m_callState == TelephonyManager.CALL_STATE_RINGING || m_callState == TelephonyManager.CALL_STATE_OFFHOOK )
						{
							try {Thread.sleep(3000);} catch(Exception e){}
		Intent i = new Intent(MyVideoView.this, CLASS_NAME.class);
		i.putExtras(getIntent());
		startActivity(i);
						}
					}					
					callState = "IDLE";

					break;
				case TelephonyManager.CALL_STATE_RINGING:
					callState = "Ringing (" + incomingNumber + ")";					
					m_allowResumeAfterCall = sb_isFocus;					
					Intent myIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
					this.startActivity(myIntent);
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					callState = "Offhook";
					break;
			}
			m_callState  = state;
			DBG("MyVideoView", "****************onCallStateChanged(), state = " + callState);
			super.onCallStateChanged(state, incomingNumber);
		}
	};
#endif	
}
#endif //USE_VIDEO_PLAYER
