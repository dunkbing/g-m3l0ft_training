package com.gameloft.layout;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class DataDownloaderTextView extends TextView
{
	
	public DataDownloaderTextView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}
	
	public DataDownloaderTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}
	
	public DataDownloaderTextView(Context context)
	{
		super(context);
		init();
	}
	
	private void init()
	{
		if (!isInEditMode())
		{
			try
			{
				Typeface tf = Typeface.createFromAsset(getContext().getAssets(), getContext().getString(APP_PACKAGE.R.string.data_downloader_custom_font_name));
				setTypeface(tf);
			}
			catch (Exception e) {}
		}
	}
}
