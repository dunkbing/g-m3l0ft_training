package APP_PACKAGE.PackageUtils.PluginSystem;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.content.Intent;
import android.app.Activity;
import android.view.ViewGroup;

public interface IPluginEventReceiver 
{
    public void onPluginStart(Activity mainActivity, ViewGroup viewGroup);

    /*
     * Functions that are dispatched to all Receivers
     * Pre/Post pairs are required for all plugins, as they might need to call logic depending 
     * on the state of the game
     */
    public void onPreNativePause();
    public void onPostNativePause();

	public void onPreNativeResume();
    public void onPostNativeResume();
    
    /*
     * Functions that can be consumed by receivers when returning true
     */

    public boolean onActivityResult(int requestCode, int resultCode, Intent data);
    
}


