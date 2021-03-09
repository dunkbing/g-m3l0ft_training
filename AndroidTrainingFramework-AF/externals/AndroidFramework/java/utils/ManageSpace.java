#if USE_MANAGED_SPACE
package APP_PACKAGE;

import java.util.ArrayList;
import java.io.IOException;
import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.AdapterView;

import android.content.Context;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener; 
import android.widget.AdapterView.OnItemSelectedListener;


import android.content.SharedPreferences;

import android.preference.PreferenceManager;

import APP_PACKAGE.installer.GameInstaller;
import APP_PACKAGE.GLUtils.*;

import APP_PACKAGE.R;

public class ManageSpace extends Activity
{
	ArrayList<Button> buttonList = new ArrayList<Button>();
	final String[] items = new String[] {"WIFI_ONLY", "WIFI_3G"};
	
	CheckBox satView = null;
	Spinner spinner = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		DBG("ManageSpace", "onNewIntent");
		
		setContentView(R.layout.gi_layout_manage_space);
		
		buttonList.add((Button) findViewById(R.id.button1));
		buttonList.add((Button) findViewById(R.id.button2));
		buttonList.add((Button) findViewById(R.id.button3));
		
		try
		{
			for (Button but : buttonList)
			{
				but.setOnClickListener(btnOnClickListener);
			}
		}
		catch (Exception e)
		{
			DBG_EXCEPTION(e);
		}
	
		satView = (CheckBox)findViewById(R.id.checkBox1);
		spinner = (Spinner) findViewById(R.id.spinner1);
		
		TextView textWifiMode = (TextView)findViewById(R.id.textView4);
		TextView textSkipDatavalid = (TextView)findViewById(R.id.textView5);
	
#if !RELEASE_VERSION		
		satView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				WriteDataValidation(isChecked);
			}
		});

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_spinner_item, items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		spinner.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			 public void onItemSelected(AdapterView<?> parent, View view, 
            int pos, long id) {
				DBG("ManageSpace", items[pos]+" "+id); 
				WriteWifiMode(items[pos]);
			}
			
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		}
		);
#else
		textSkipDatavalid.setVisibility(View.GONE);
		textWifiMode.setVisibility(View.GONE);
		satView.setVisibility(View.GONE);
		spinner.setVisibility(View.GONE);
#endif

		SUtils.setContext((Context)this);

	}
	
	@Override
	public void onResume()
	{
		DBG("ManageSpace","onResume");
		super.onResume();
		
		String data = SUtils.getOverriddenSetting(SD_FOLDER + "/qaTestingConfigs.txt", "SKIP_VALIDATION");
		if(data != null)
			satView.setChecked(data.equals("1")? true : false);
			
		data = SUtils.getOverriddenSetting(SD_FOLDER + "/qaTestingConfigs.txt", "WIFI_MODE");
		if(data != null)
			spinner.setSelection(data.equals("WIFI_ONLY") ? 0:1);
	}
	
	public OnClickListener btnOnClickListener = new OnClickListener()
	{
		public void onClick(View v)
		{
			int buttonId;

			try
			{
				Button buttonPressed = (Button) v;
				buttonId = buttonPressed.getId();
			}
			catch (Exception e)
			{
				ImageButton buttonPressed = (ImageButton) v;
				buttonId = buttonPressed.getId();
			}
			
			buttonsAction(buttonId);
		}

	};
	
	public void buttonsAction(int buttonId)
	{
		DBG("ManageSpace", "buttonsAction");
		if (buttonId == R.id.button1)
		{
			DBG("ManageSpace", "button 1 pressed");
			/*SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = settings.edit();
			editor.clear();
			editor.commit();*/
			clearSaveGames();
		}else
		if (buttonId == R.id.button2)
		{
			clearPreferences();
		}else
		if (buttonId == R.id.button3)
		{
			clearAllData();
		}
	}
	
	public void clearSaveGames()
	{
		DBG("ManageSpace", "clearSaveGames");
	}
	
	public void clearPreferences()
	{
		DBG("ManageSpace", "clearPreferences");
	}
	
	private void deleteAllFiles(File dir)
	{
		String mFiles[] = dir.list();
		for (int i = 0; i < mFiles.length; i++)
        {
			File file = new File(dir.getPath()+"/"+mFiles[i]);
			if(file.isDirectory())
				deleteAllFiles(file);
			else
			if(file.exists())
			{
				DBG("ManageSpace","deleting file named: "+mFiles[i]);
				file.delete();
			}
		}
	}
	
	public void clearAllData()
	{
		DBG("ManageSpace", "clearAllData");
		
		try
		{
		File srcDir = new File(SAVE_FOLDER);
		deleteAllFiles(srcDir);
		}catch(Exception ex){DBG_EXCEPTION(ex);}
	}
	public void WriteWifiMode(String value)
	{
		String sd_folder = SUtils.getPreferenceString("SDFolder", "", GameInstaller.mPreferencesName);
		if(sd_folder.equals(""))
			sd_folder = SD_FOLDER;
			
		File qatestConfigs = new File(sd_folder+"/qaTestingConfigs.txt");
		DBG("ManageSpace","qa test config path: "+qatestConfigs.getPath());
		try
		{
		if(!qatestConfigs.exists())
			qatestConfigs.createNewFile();
		}catch(IOException ex){DBG_EXCEPTION(ex);}
		SUtils.setOverriddenSetting(sd_folder + "/qaTestingConfigs.txt","WIFI_MODE", value);
	}
	public void WriteDataValidation(boolean value)
	{
		DBG("ManageSpace","WriteDataValidation with value: "+value);
		String sd_folder = SUtils.getPreferenceString("SDFolder", "", GameInstaller.mPreferencesName);
		if(sd_folder.equals(""))
			sd_folder = SD_FOLDER;
			
		File qatestConfigs = new File(sd_folder+"/qaTestingConfigs.txt");
		DBG("ManageSpace","qa test config path: "+qatestConfigs.getPath());
		try
		{
		if(!qatestConfigs.exists())
			qatestConfigs.createNewFile();
		}catch(IOException ex){DBG_EXCEPTION(ex);}
		
		String data = SUtils.getOverriddenSetting(sd_folder + "/qaTestingConfigs.txt", "SKIP_VALIDATION");

		int int_value = value? 1: 0;
		SUtils.setOverriddenSetting(sd_folder + "/qaTestingConfigs.txt","SKIP_VALIDATION", Integer.toString(int_value));
		
	}

	
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			finish();
	        return true;
	    }
		
		return false;
	}
	
}
#endif //USE_MANAGED_SPACE