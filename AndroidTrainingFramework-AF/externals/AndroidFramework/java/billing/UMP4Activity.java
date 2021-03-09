#if USE_BILLING && USE_BILLING_FOR_CHINA
package APP_PACKAGE.billing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

import APP_PACKAGE.GLUtils.Device;
import APP_PACKAGE.GLUtils.XPlayer;
import APP_PACKAGE.GLUtils.SUtils;
import APP_PACKAGE.billing.ServerInfo;
import APP_PACKAGE.billing.Profile;
import APP_PACKAGE.billing.common.AServerInfo;
import APP_PACKAGE.billing.common.AModelActivity;
import APP_PACKAGE.billing.common.LManager;
import APP_PACKAGE.R;

import com.umpay.huafubao.plugin.android.intf.Huafubao;
import com.umpay.huafubao.plugin.android.intf.HuafubaoListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class UMP4Activity extends Activity implements HuafubaoListener
{

    SET_TAG("UMP4Activity");

	private static AModelActivity	mBillingActivity = null;
	private static Device	mDevice;
	private static String glOrderId = null;
	
	private int lasErrorCode = 0;

    private boolean purchaseInProgress = false;
	private ProgressDialog dialog = null;
	private AlertDialog huafubao_dialog = null;
	final public static int HUAHUBAO_INSTALL = 109;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		DBG(TAG,"onCreate()");
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		DBG(TAG,"onStart()");
		lasErrorCode = 0;
		StartUMPBilling();
	}
	
	@Override
	protected void onDestroy() {
		DBG(TAG,"onDestroy()");
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		DBG(TAG,"onResume()");
		super.onResume();
	}
	
	@Override
	protected void onStop()
	{
		DBG(TAG,"onStop()");
		super.onStop();
	}
	
	public boolean StartUMPBilling() {
		try {
			ServerInfo si = (ServerInfo)mDevice.getServerInfo();
			Profile profile = si.getCurrentProfileSelected();
			String glGoodsId = profile.getUmpGoodsid();
			String glGoodsAmount = profile.getUmpGoodsAmount();
			String glMerId = profile.getUmpMerid();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String strDate = sdf.format(new Date());
			String glMerPrivte = "gameloft";
			String glExpand = "gameloft";
			
			Huafubao huafubao=null;
			huafubao = new Huafubao(this, this);
			
			Map<String, String> map = new HashMap<String, String>();
			map.put(Huafubao.MERID_STRING, glMerId);
			map.put(Huafubao.GOODSID_STRING, glGoodsId);
			map.put(Huafubao.ORDERID_STRING, glOrderId);
			map.put(Huafubao.MERDATE_STRING, strDate);
			map.put(Huafubao.AMOUNT_STRING, glGoodsAmount);
			map.put(Huafubao.MERPRIV_STRING, glMerPrivte);
			map.put(Huafubao.EXPAND_STRING, glExpand);
			//Debug
			DBG(TAG, "gl_merid = " + glMerId);
			DBG(TAG, "gl_goodsid = " + glGoodsId);
			DBG(TAG, "gl_orderid = " + glOrderId);
			DBG(TAG, "strDate = " + strDate);
			DBG(TAG, "gl_goodsamount = " + glGoodsAmount);
			DBG(TAG, "gl_merprivte = " + glMerPrivte);
			DBG(TAG, "gl_expand = " + glExpand);
			// remember transaction
			SUtils.setPreference(LManager.PREFERENCES_SMS_UMP_PENDING, glOrderId, LManager.PREFERENCES_NAME);
			huafubao.setRequest(map, true);
        } catch (Exception e) {
			ERR(TAG, "Error "+e.getMessage());
            return false;//TODO: put error code here
        }
		return true;
    }
	
	public static boolean LaunchUMPBilling(AModelActivity activity, Device device) 
	{
		if(glOrderId != null)
		{
			mDevice = device;
			mBillingActivity = activity;
			Intent i = new Intent();
			String packageName = SUtils.getContext().getPackageName();
			i.setClassName(packageName, packageName + ".billing.UMP4Activity");
			SUtils.getContext().startActivity(i);
			return true;
		}
		return false;
    }
	
	public static boolean requestOrderId(Device device)
	{
		DBG(TAG, "requestOrderId()");
		ServerInfo si = (ServerInfo)device.getServerInfo();
		Profile profile = si.getCurrentProfileSelected();
		glOrderId = null;
		
		String orderUrl = profile.getUmp_get_transid();
		String price = profile.getGame_price();
		String timestamp = System.currentTimeMillis() + "";
		String umpVersion = "R4";
		String signOrder = device.getDemoCode()+"_"+GL_PRODUCT_ID+"_"+price+"_"+device.getIMEI()+"_"+umpVersion + "_" + timestamp + "_"+SUtils.GetSerialKey()+"_" + "UMPGAMELOFT";
		signOrder = XPlayer.md5(signOrder);
		String orderQuery = "igpcode="+device.getDemoCode()+"&product="+GL_PRODUCT_ID+"&price="+price+"&imei="+device.getIMEI()+"&umpversion="+umpVersion+"&d="+SUtils.GetSerialKey()+"&timestamp="+timestamp+"&sign="+signOrder;
		XPlayer mXPlayer = new XPlayer(device);
		mXPlayer.sendRequest(orderUrl, orderQuery);
		while (!mXPlayer.handleRequest())
		{
			try {
				Thread.sleep(50);
			} catch (Exception exc) {}
			DBG(TAG, "[requestOrderId]Waiting for response");
		}
		if(mXPlayer.getLastErrorCode() != XPlayer.ERROR_NONE)
		{
			return false;
		}
		String response = XPlayer.getWHTTP().m_response;
		DBG(TAG, "response = " + response);
		if(response.indexOf("SUCCESS") > -1)
		{
			String[] tempArgs = response.split("\\|"); // single |
			glOrderId = tempArgs[1];
			DBG(TAG, "requestOrderId() = "+glOrderId);
			return true;
		}
		return false;
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		DBG(TAG, "onActivityResult() requestCode = " + requestCode + " resultCode = " + resultCode);
		if(requestCode == Huafubao.HUAFUBAOREQCODE)
		{
			if(data == null)
			{
				DBG(TAG, "onActivityResult data = null -> Failed");
				((AndroidBilling)mBillingActivity).updateBillingResult(false, R.string.AB_TRANSACTION_FAILED);
			}
			else 
			{
				boolean info = data.getExtras().getBoolean(Huafubao.SUCC);
				if(info)
				{
					DBG(TAG, "onActivityResult info = success -> Unlocked");
					((AndroidBilling)mBillingActivity).updateBillingResult(true, R.string.AB_THANKS_FOR_THE_PURCHASE);
				}
				else
				{
					DBG(TAG, "onActivityResult info = null -> Failed");
					((AndroidBilling)mBillingActivity).updateBillingResult(false, R.string.AB_TRANSACTION_FAILED);
				}
			}
			finish();
		}
		else if(requestCode == HUAHUBAO_INSTALL)
		{
			StartUMPBilling();
		}
	}
	
	public boolean onError(int code, String msg)
	{
		DBG(TAG, "onError() code = " + code + " msg = " + msg);
		boolean flag = false;
		// remove for fail
		SUtils.setPreference(LManager.PREFERENCES_SMS_UMP_PENDING, null, LManager.PREFERENCES_NAME);
		switch (code) 
		{
			case Huafubao.ERROR_NO_INSTALL:  // 6: Alipay service potential not install
				//lasErrorCode = Huafubao.ERROR_NO_INSTALL;
				HuafubaoReqest();
				flag = false;
				break;
			case Huafubao.ERROR_NO_MERID:   // 1: missing merId
				flag = false;
				break;
			case Huafubao.ERROR_NO_GOODSID:  // 2: missing goodsId
				flag = false;
				break;
			case Huafubao.ERROR_NO_MERDATE:  //  4: missing merDate
				flag = false;
				break;
			case Huafubao.ERROR_NO_AMOUNT:  // 5: missing Goods Amount
				lasErrorCode = Huafubao.ERROR_NO_AMOUNT;
				flag = true; 
				break;
			case Huafubao.ERROR_NO_NETWORK: // 7: no network
				lasErrorCode = Huafubao.ERROR_NO_NETWORK;
				flag =true;
				break;
			default:
				break;
		}
		return flag;
	}
	
	@Override
	public void onWindowFocusChanged(boolean focus)
	{
		DBG(TAG, "onWindowFocusChanged() = " + focus);
		super.onWindowFocusChanged(focus);
		if(focus)
		{
			if(lasErrorCode != 0)
			{
				((AndroidBilling)mBillingActivity).updateBillingResult(false, R.string.AB_TRANSACTION_FAILED);
				finish();
			}
		}
	}
	
	public void HuafubaoReqest()
	{
		AlertDialog.Builder alert_builder = new AlertDialog.Builder(this);
		alert_builder.setMessage(getString(R.string.AB_HUAFUBAO_QUESTION)).setCancelable(false);
		alert_builder.setPositiveButton(getString(R.string.SKB_OK), new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {				
				installHuafubao();
				dialog.cancel();
    		}
    	})
		.setNegativeButton(getString(R.string.SKB_CANCEL), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				lasErrorCode = -1;
				dialog.cancel();
			}				
		});
		huafubao_dialog = alert_builder.create();
		huafubao_dialog.show();
		alert_builder = null;
	}
	
	public void installHuafubao()
	{
		try {
			byte[] in = SUtils.ReadFileByte(R.raw.huafubao);
			java.io.FileOutputStream fOut = openFileOutput("huafubao.apk", MODE_WORLD_READABLE);
			fOut.write(in);
			fOut.close();
			java.io.File f = getFileStreamPath("huafubao.apk");
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(android.net.Uri.fromFile(f), "application/vnd.android.package-archive");
			startActivityForResult(intent, HUAHUBAO_INSTALL);
		} catch (Exception e) { lasErrorCode = -1;}
	}
}
#endif
