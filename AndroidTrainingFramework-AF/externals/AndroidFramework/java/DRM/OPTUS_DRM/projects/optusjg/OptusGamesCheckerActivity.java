#if USE_OPTUS_DRM
package com.msap.store.drm.android.projects.optusjg;

import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.text.method.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;

import com.msap.store.drm.android.*;
import com.msap.store.drm.android.util.*;
import com.msap.store.drm.android.data.*;

import APP_PACKAGE.R;

/**
 * This class handles all user interface related operations for Optus
 * DRM libraries.
 * @author Edison Chan
 */
abstract public class OptusGamesCheckerActivity extends CheckerActivity {
	private TextView textview;
	private AltHtml.OnClickListener listener;

	//VTN add
		private static int progress = 0;
    	private ProgressBar progressBar;
    	private int progressStatus = 0;
    	private Handler handler = new Handler();
    	//end
	/**
	 * Construct a new OptusCheckerActivity object.
	 * @param impl implementation class of this checker activity.
	 */
	protected OptusGamesCheckerActivity(CheckerImplementation impl) {
		super(impl);
	}

	/**
	 * Initialize the activity and start checking local license. Sublcasses 
	 * can override this method to implement better user interfaces, provided 
	 * that they call the implementation of superclass.
	 * @param savedInstanceState saved data to resurrect destroyed activity.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.drmlib_optusjg_main);

		try {
			this.textview = (TextView) this.findViewById(R.id.drmlib_optusjg_layout_maintext);
			this.listener = new AltHtml.OnClickListener() {
				public void onLinkClick(View view, String url) {
					OptusGamesCheckerActivity self = OptusGamesCheckerActivity.this;
					if (url.equals("action:finish")) {
						self.finish();
					} else if (url.equals("action:help")) {
						self.startActivity(new Intent(self, OptusGamesHelpActivity.class));
					} else {
						self.onLinkClick(url);
					}
				}
			};
			
			//VTN add button help exit
			Button btnExit = (Button) findViewById(R.id.exit);
			btnExit.setOnClickListener(new OnClickListener() {
	            public void onClick(View v) {
	            		DBG("OPTUS", "----------- Exit ---------------");
						moveTaskToBack(true);
						System.exit(0);
	            		finish();
	            }
	        });
		//VTN add
		progressBar = (ProgressBar) findViewById(R.id.progressbar);
		} catch (Exception ex) {
			super.finish();
		}
	}

	/**
	 * Called whenever an unrecognized link is clicked on the user interface.
	 * Subclasses may override this method to handle unrecognized links in the
	 * user interface.
	 * @param url URL of the link clicked.
	 */
	protected void onLinkClick(String url) {
	}

	/**
	 * Shows the validation screen that notifies the users of running 
	 * license validation.
	 */
	protected final void showCheckingScreen() {
		this.setBodyText(R.string.drmlib_optusjg_message_checking);
		//VTN add
		new Thread(new Runnable()
        {
            public void run()
            {

                while (progressStatus < 10)
                {
                    progressStatus = doInProgress();
                }

                //---hides the progress bar---
                handler.post(new Runnable()
                {
                    public void run()
                    {
                        //---0 - VISIBLE; 4 - INVISIBLE; 8 - GONE---
                        progressBar.setVisibility(8);
                    }
                });
            }

            private int doInProgress()
            {
                try {

                    Thread.sleep(500);
                } catch (InterruptedException e)
                {
                    DBG("OPTUS", "Error5555:::"+e.getMessage());
                }
                return ++progress;
            }

        }).start();
	}

	/**
	 * Shows the success screen that notifies the users that the license
	 * validation has completed successfully.
	 */
	protected final void showSuccessScreen() {
		this.setBodyText(R.string.drmlib_optusjg_message_validated);
		progressBar.setVisibility(8);
	}

	/**
	 * Shows the success screen that notifies the users that the license
	 * validation has completed, but the license server denies the license
	 * request.
	 */
	protected final void showDenialScreen() {
		this.setBodyText(R.string.drmlib_optusjg_message_refused);
		progressBar.setVisibility(8);
	}

	/**
	 * Shows the success screen that notifies the users that the license
	 * validation cannot be completed due to network error.
	 */
	protected final void showNetworkErrorScreen() {
		this.setBodyText(R.string.drmlib_optusjg_message_noinet);
		progressBar.setVisibility(8);
	}

	/**
	 * Shows the success screen that notifies the users that the license
	 * validation cannot be completed due to network error.
	 */
	protected final void showMiscErrorScreen() {
		this.setBodyText(R.string.drmlib_optusjg_message_refused);
		progressBar.setVisibility(8);
	}

	/**
	 * Update the screen to show the given resource string.
	 * @param resid ID of the resource string.
	 */
	private void setBodyText(int resid) {
		//progressBar.setVisibility(8);
		String temp = this.getResources().getString(resid);
		Spanned text = AltHtml.fromHtml(temp, listener);
		this.textview.setText(text);
		this.textview.setMovementMethod(LinkMovementMethod.getInstance());
	}
}

#endif	//USE_OPTUS_DRM
