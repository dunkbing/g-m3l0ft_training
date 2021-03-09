package APP_PACKAGE.iab;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import java.util.Locale;

import APP_PACKAGE.billing.common.AServerInfo;
import APP_PACKAGE.iab.InAppBilling;



abstract class IABTransaction
{
	SET_TAG("InAppBilling");
	
	public AServerInfo mServerInfo = null;
	
	public IABTransaction (AServerInfo si)
	{
		mServerInfo = si;
	}
	
	public boolean isBillingSupported()
	{
		return true;
	}
	
	public void sendConfirmation(){}

	public void sendNotifyConfirmation(String notifyId) {}
	
	public boolean restoreOnlineTransactions() {return true;};
	
	public boolean restoreTransactions() {return true;};

	public abstract boolean requestTransaction(String id); 

	public abstract void showDialog(int idx, String id);

}
