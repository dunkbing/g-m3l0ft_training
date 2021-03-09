package APP_PACKAGE.PackageUtils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

class LogoView extends ImageView
{
	public LogoView(Activity activity, int res, int width, int height) 
	{
		super((Context) activity);
		
		setImageResource(res);

		setScaleType(ImageView.ScaleType.CENTER_CROP);
		
		SetScreenSize(width, height);
	}
	
	public void SetScreenSize(int width, int height)
	{
		setMinimumWidth(width);
	    setMinimumHeight(height);
	    setMaxWidth(width);
	    setMaxHeight(height);
	}
}
