#if USE_IN_GAME_VIDEO
package APP_PACKAGE.GLUtils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.ImageButton;
import APP_PACKAGE.R;

import android.os.Handler;
import android.os.Message;
import java.util.ArrayList;
import java.util.Locale;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedReader;
import android.widget.TextView;

public class DialogVideo extends Dialog implements OnTouchListener {

	private String videoPath;
	private String subtitlePath;
	private static DialogVideo mInstance;
	private Context context;
	int mVidPosition;

	private boolean mbSkipEnabled;


	private DialogVideo(Context context, String videoPath, String subtitlePath, boolean skipEnabled) 
	{
		super(context,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
		
		this.context = context;
		this.videoPath = videoPath;
		this.subtitlePath = subtitlePath;
		mVidPosition = 0;
		mbSkipEnabled = skipEnabled;
	}



	
	VideoView vidView;
	ImageButton skipLabel;
	RelativeLayout relativeLayout;


	//subtitles
	private TextView mSrtText;
	private Handler mHandler;
	static ArrayList<Integer> mSrtTimeS = new ArrayList<Integer>();
	static ArrayList<Integer> mSrtTimeE = new ArrayList<Integer>();
	static ArrayList<String> mSrtString = new ArrayList<String>();
	private boolean mSubtitleExists = true;
	private boolean lastFlagButton = true;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		DBG("DialogVideo","onCreate dialog video START");

		setContentView(R.layout.igv_dialog_video_layout);
		
		DBG("DialogVideo","onCreate dialog video setted cView");

		relativeLayout = (RelativeLayout) findViewById(R.id.igv_main_layout);
				
		//video view
		vidView = (VideoView) findViewById(R.id.igv_video_view);
		vidView.setVideoPath(videoPath);
		vidView.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer arg0) {
				DBG("DialogView" ,"Video Completed");
				doStopVideo();
			}
		});
		

		vidView.setOnErrorListener(new OnErrorListener ()
			{
				public boolean onError(MediaPlayer mp, int what, int extra)
				{
					onVideoFinished();
					return true;
				}
			}
		);




		vidView.setOnTouchListener(this);
		
		//skip Label
		skipLabel = (ImageButton) findViewById(R.id.igv_skipLabel);
		// skipLabel.setOnTouchListener( new OnTouchListener() {
			
		// 	@Override
		// 	public boolean onTouch(View v, MotionEvent event) {
		// 		DBG("DialogVideo", "on touch skip label");
		// 		doStopVideo();
		// 		return true;
		// 	}
		// });

		skipLabel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				INFO("DialogVideo", "on CLICK skip label");
				stopVideo();
			}
		});



		//subtitles
		mSrtText = (TextView) findViewById(R.id.SrtText);
		mSrtText.setText("", TextView.BufferType.NORMAL);
		
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

		setCancelable(false); //disable back
		DBG("DialogVideo","onCreate dialog video");
		
		
	}

	@Override
    public void onWindowFocusChanged(boolean focus)
    {
    	super.onWindowFocusChanged(focus);
    	if(!focus)
    	{
    		onPause();
    	} else 
    	{
    		resumePlayback();
    	}
    }
	
	
	private void doStopVideo()
	{
		if(vidView != null)
		{
			vidView.stopPlayback();
		}

		mSrtText.setText("",TextView.BufferType.NORMAL); //is this even necessary?

		dismiss();
		mInstance = null;

		onVideoFinished();
	}




	// plays a video, skip is enabled from the beginning 
	// if IN_GAME_VIDEO_READ_FROM_APK==1 , subtitles must exist in assets folder
	// if IN_GAME_VIDEO_READ_FROM_APK==0 , subtitles are ignored and taken from SDCard
	public static void playVideo(Context context, String videoPath, String subtitles)
	{
		playVideo(context, videoPath, subtitles, true);
	}


	public static void playVideo(Context context, String videoPath, String subtitles, boolean skipEnabled)
	{
		if(mInstance != null)
		{
			mInstance.doStopVideo();
			mInstance = null;
		}

		mInstance = new DialogVideo(context, videoPath, subtitles, skipEnabled);
		mInstance.show();
		mInstance.startVideo();
	}


	//lets you enable/disable SKIP button
	public static void setSkipEnabled(boolean skipEnabled)
	{
		if(mInstance != null)
		{
			mInstance.mbSkipEnabled = skipEnabled;
		}
	}


	private void startVideo()
	{
		if(vidView == null)
			return;
		
		vidView.start();

		//subtitles
		mHandler.sendEmptyMessage(0);

		INFO("DialogVideo","playing video");
	}
	

	
	
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(vidView == null || !mbSkipEnabled)
		{
			return false;
		}
		
		DBG("DialogVideo","onTouch DialogVideo");
		if(skipLabel.getVisibility() == View.INVISIBLE)
		{
			relativeLayout.removeView(skipLabel);
			relativeLayout.addView(skipLabel);
			
			skipLabel.setVisibility(View.VISIBLE);
		} else  
		{
			skipLabel.setVisibility(View.INVISIBLE);
		}
		
		
		return false;
	}


	/*
		* Must be called from Main Activity onPause();
	*/
	public static void onPause()
	{
		if(mInstance == null)
			return;
		
		mInstance.savePosition();
	}


	private void savePosition()
	{
		if(vidView != null)
		{
			if(vidView.isPlaying())
			{
				//do not call vidView.pause(), let the system handle that
				mVidPosition = vidView.getCurrentPosition();
				vidView.pause();
				INFO("DialogVideo","saved video position:"+ mVidPosition);
			}
		}
	}


	/*
		* Call this function to continue playing a paused video
	*/
	public static void resumePlayback()
	{
		if(mInstance == null)
			return;
		
		mInstance.doResumePlayback();
	}

	
	private void doResumePlayback()
	{
		if(vidView != null)
		{
			INFO("DialogVideo","resuming video to position:"+ mVidPosition);

			vidView.seekTo(mVidPosition);
			vidView.start(); //do not call vidView.resume()!!
		}
	}

	public static void stopVideo()
	{
		if(mInstance == null)
			return;

		mInstance.doStopVideo();
	}


	private void onVideoFinished()
	{
		SUtils.onVideoFinished();
	}


		

	/**
	 * Import subtitles if exist
	 * @param check 2 checks (english and default language)
	 */
	private void importVideoSubtitles(int check)
	{
		Locale mLocale = Locale.getDefault();

		#if IN_GAME_VIDEO_READ_FROM_APK
		String VideoSrtF = subtitlePath.substring(0,subtitlePath.length()-4);
		#else
	    String VideoSrtF = videoPath.substring(0,videoPath.length()-4);
	    #endif
	    INFO("DialogVideo","Video subtitles: "+VideoSrtF);
	    if (check == 0)
	    	VideoSrtF = VideoSrtF + "_" + mLocale.getISO3Language() + ".srt";
	    else
	    {
	    	VideoSrtF = VideoSrtF + "_eng.srt";
	    	mSubtitleExists = true;
	    }
	    INFO("DialogVideo", "Subtitle Files:" + VideoSrtF + "   locale lang:"+mLocale.getISO3Language());
	
	    Reader fis;
		String storedString = "";
		
		try {

		#if IN_GAME_VIDEO_READ_FROM_APK
			java.io.InputStream is;
			is = context.getAssets().open(VideoSrtF);
			fis = new InputStreamReader(is, "UTF-8");
		#else
			fis = new InputStreamReader(new FileInputStream(VideoSrtF),"UTF-8");//this.getAssets().open("DH2.srt");
		#endif


			BufferedReader dataIO = new BufferedReader(fis);
			String strLine = null;
			String s1 = null;
			String TimeStr = null;
	
			while ((strLine = dataIO.readLine()) != null) {
				TimeStr = dataIO.readLine();
				INFO("DialogVideo", "times string" +TimeStr);
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
			DBG("DialogVideo","Subtitles not found");
			DBG_EXCEPTION(e);

			e.printStackTrace();
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
		if (vidView != null && vidView.isPlaying())
		{
			for(int i=0;i<mSrtString.size();i++)
			{
				if((vidView.getCurrentPosition()  > mSrtTimeS.get(i)) && (vidView.getCurrentPosition() < mSrtTimeE.get(i)))
				{
					mSrtText.setText(mSrtString.get(i), TextView.BufferType.NORMAL);
					break;
				}
				else
				{
					mSrtText.setText("", TextView.BufferType.NORMAL);
				}
			}
			if((vidView.getCurrentPosition() > mSrtTimeE.get(mSrtString.size()-1)))
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
	



}
#endif