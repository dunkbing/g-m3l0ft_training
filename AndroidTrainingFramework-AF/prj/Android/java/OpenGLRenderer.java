package APP_PACKAGE;

import android.opengl.GLSurfaceView;
import android.view.Window;
import android.view.WindowManager;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLES20;

import android.view.Display;

class OpenGLRenderer implements Renderer {	
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config){		
				
		WindowManager wm = (WindowManager) GAME_ACTIVITY_NAME.m_sInstance.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay(); 
		int width  = display.getWidth();
		int height  = display.getHeight();
		
		GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
		//nativeInit(width, height);
	}

	public void onDrawFrame(GL10 gl){
		
		
		//GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
		nativeRender();		
	}
	 
	public void onSurfaceChanged(GL10 gl, int width, int height){
		 GLES20.glViewport(0, 0, width, height);
	}
	
	private native void nativeInit(int width, int height);
	private native void nativeRender();
	
	private native void nativeAppResume();
	private native void nativeAppStop();
	
	private boolean isAppRunning = true;
	private boolean isAppPaused = false;
}