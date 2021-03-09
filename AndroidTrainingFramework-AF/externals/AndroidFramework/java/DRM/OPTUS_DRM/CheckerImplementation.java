#if USE_OPTUS_DRM
package com.msap.store.drm.android;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import org.json.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.telephony.*;
import android.util.*;

import com.msap.store.drm.android.data.*;
import com.msap.store.drm.android.util.*;

import APP_PACKAGE.GLUtils.Device;
/**
 * This class is the implementation class of CheckerActivity. This class 
 * handles all behind-the-scene actions used by the CheckerActivity class,
 * such as reading/writing obfuscated data in application specified file,
 * calling remote server, etc.
 *
 * This class is intended for library extension. Normal library client
 * should never deal with this class.
 *
 * @see CheckerActivity
 * @author Edison Chan
 */
public class CheckerImplementation {
	private static final int VALID_MASK_ALL = 0x05ff;
	private static final int VALID_MASK_APPID = 0x0001;
	private static final int VALID_MASK_USERID = 0x0002;
	private static final int VALID_MASK_SERIAL = 0x0004;
	private static final int VALID_MASK_SERVER_CALL_URL = 0x0008;
	private static final int VALID_MASK_PORTAL_CALL_URL = 0x0010;
	private static final int VALID_MASK_CERTIFICATE = 0x0020;
	private static final int VALID_MASK_OBFUSCATOR = 0x0040;
	private static final int VALID_MASK_SERIALIZER = 0x0080;
	private static final int VALID_MASK_IMEI = 0x0100;
	private static final int VALID_MASK_MSISDN = 0x0200;
	private static final int VALID_MASK_CACHE_FILE = 0x0400;

	protected int pStatus;
	protected String pAppId;
	protected String pUserId;
	protected String pSerialNumber;
	protected String pIMEI;
	protected String pMSISDN;
	protected URL pLicenseServerCallUrl;
	protected String pLicenseServerPortalUrl;
	protected String pLicenseCacheFile;
	protected String pCertificateId;
	protected Certificate pCertificateObject;
	protected byte[] pObfuscationSalt;
	protected int pObfuscationRound;
	protected Serializer pSerializer;

	private byte[] sObfuscationSecret;

	/**
	 * Construct a new CheckerImplementation object.
	 */
	protected CheckerImplementation() {
		this.pStatus = 0;
		this.pAppId = null;
		this.pUserId = null;
		this.pSerialNumber = null;
		this.pIMEI = null;
		this.pMSISDN = null;
		this.pLicenseServerCallUrl = null;
		this.pLicenseServerPortalUrl = null;
		this.pLicenseCacheFile = null;
		this.pCertificateId = null;
		this.pCertificateObject = null;
		this.pObfuscationSalt = null;
		this.pObfuscationRound = 0;
		this.pSerializer = null;
		this.sObfuscationSecret = null;
	}

	/**
	 * Set the application ID of this app.
	 * @param appid application identifier of this app.
	 */
	public void setApplicationId(String appid) {
		if (appid != null) {
			this.pAppId = appid;
			this.pStatus |= VALID_MASK_APPID;
		}
	}

	/**
	 * Set the user ID of the owner of this app.
	 * @param userid user identifier of the app owner.
	 */
	public void setUserId(String userid) {
		if (userid != null) {
			this.pUserId = userid;
			this.pStatus |= VALID_MASK_USERID;
		}
	}

	/**
	 * Set the serial number of this app.
	 * @param serial serial number of this app.
	 */
	public void setSerialNumber(String serial) {
		if (serial != null) {
			this.pSerialNumber = serial;
			this.pStatus |= VALID_MASK_SERIAL;
		}
	}

	/**
	 * Set license server API url.
	 * @param url url of the license server api entry point.
	 */
	public void setLicenseServerCallUrl(String url) {
		try {
			this.pLicenseServerCallUrl = new URL(url);
			this.pStatus |= VALID_MASK_SERVER_CALL_URL;
		} catch (Exception ex) {
			// do nothing
		}
	}

	/**
	 * Set license server portal url.
	 * @param url url of the license server portal entry point.
	 */
	public void setLicenseServerPortalUrl(String url) {
		this.pLicenseServerPortalUrl = url;
		this.pStatus |= VALID_MASK_PORTAL_CALL_URL;
	}

	/**
	 * Set name of the license cache file.
	 * @param name name of the license cache file.
	 */
	public void setLicenseCacheFilename(String name) {
		this.pLicenseCacheFile = name;
		this.pStatus |= VALID_MASK_CACHE_FILE;
	}

	/**
	 * Set the certificate used for signature and data encryption.
	 * @param id identifier of the certificate.
	 * @param data the actual certificate data in X.509 format.
	 */
	public void setCertificate(String id, byte[] data) {
		try {
			ByteArrayInputStream certstream = new ByteArrayInputStream(data);
			CertificateFactory certloader = CertificateFactory.getInstance("X.509");
	
			this.pCertificateObject = certloader.generateCertificate(certstream);
			this.pCertificateId = id;
			this.pStatus |= VALID_MASK_CERTIFICATE;
		} catch (Exception ex) {
			// do nothing
		}
	}

	/**
	 * Set the obfuscator settings used for protecting cache data.
	 * @param salt salt used in key generation.
	 * @param round number of round used in key generation.
	 */
	public void setObfuscationSettings(byte[] salt, int round) {
		if (round > 500 && salt != null && salt.length >= 8) {
			this.pObfuscationRound = round;
			this.pObfuscationSalt = salt;
			this.pStatus |= VALID_MASK_OBFUSCATOR;
		}
	}

	/**
	 * Set the serializer used for converting data.
	 * @param serializer serializer used for data conversion.
	 */
	public void setSerializer(Serializer serializer) {
		if (serializer != null) {
			this.pSerializer = serializer;
			this.pStatus |= VALID_MASK_SERIALIZER;
		}
	}

	/**
	 * Set the IMEI device identifier.
	 * @param imei IMEI of the device.
	 */
	public void setIMEIIdentifier(String imei) {
		if (imei != null) {
			this.pIMEI = imei;
			this.pStatus |= VALID_MASK_IMEI;
		}
	}

	/**
	 * Set the MSISDN subscriber identifier.
	 * @param msisdn MSISDN of the device.
	 */
	public void setMSISDNIdentifier(String msisdn) {
		if (msisdn != null) {
			this.pMSISDN = msisdn;
			this.pStatus |= VALID_MASK_MSISDN;
		}
	}

	/**
	 * Set all device identifiers by gathering them from the system.
	 * @param context Context of the running app.
	 */
	public void setIdentifiers(Context context) {
		try {
			this.setIMEIIdentifier(Device.getDeviceId());
			this.setMSISDNIdentifier(Device.getLineNumber());
		} catch (Exception ex) {
			// do nothing
		}
	}

	/**
	 * Check if the implementation is in working order or not.
	 * @return true if implementation is valid, false otherwise.
	 */
	public boolean isReady() {
		return ((this.pStatus & VALID_MASK_ALL) == VALID_MASK_ALL);
	}

	/**
	 * Restore any saved states from a bundle.
	 * @param bundle bundle where the saved states are stored.
	 * @see android.app.Activity#onCreate
	 */
	public void loadSavedState(Bundle bundle) {
		if (bundle != null) {
			String classname = this.getClass().getName();

			if (bundle.containsKey(classname + ":ObfuscatorKey")) {
				this.sObfuscationSecret = bundle.getByteArray(classname + ":ObfuscatorKey");
			}
		}
	}

	/**
	 * Save current states to bundle.
	 * @param bundle bundle where the current states are saved.
	 */
	public void saveCurrentState(Bundle bundle) {
		String classname = this.getClass().getName();

		if (this.sObfuscationSecret != null) {
			bundle.putByteArray(classname + ":ObfuscatorKey", this.sObfuscationSecret);
		}
	}

	/**
	 * Get the result of the activity.
	 * @return intent containing the result of this activity.
	 */
	public Intent getResultData() {
		return new Intent();
	}

	/**
	 * Return if this activity code is to be handled by this object.
	 * @param req request code.
	 * @param resCode result code.
	 * @param data an Intent object holding the response data.
	 * @return true if the result should be handled by us, false otherwise.
	 */
	public boolean isInternalActivityResult(int reqCode, int resCode, Intent data) {
		if (reqCode == 1) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Handle a onActivityResult call.
	 * @param reqCode request code.
	 * @param resCode result code.
	 * @param data an Intent object holding the response data.
	 * @param activity activity whose callback method should be called.
	 */
	public void processInternalActivityResult(int reqCode, int resCode, Intent data, CheckerActivity activity) {
		if (reqCode == 1) {
			if (this.isReady()) {
				if (resCode == Activity.RESULT_OK && data.getBooleanExtra(WebsiteActivity.ACTIVITY_RESULT_STATUS, false)) {
					try {
						ServerRequest request = (ServerRequest) data.getSerializableExtra("request");
						byte[] key = data.getByteArrayExtra("key");
						String cnonce = data.getStringExtra("cnonce");

						String out3 = data.getStringExtra(WebsiteActivity.ACTIVITY_RESULT_DATA);
						String out2 = ProtocolUtility.unwrapCryptoLayer(out3, key);
						String out1 = ProtocolUtility.unwrapNonceLayer(out2, cnonce);
						ServerResponse response = this.pSerializer.inflateServerResponse(out1);

						this.processLicenseServerResponse(request, response, activity);
					} catch (Exception ex) {
						activity.onError(CheckerActivity.ERROR_NETWORK);
					}
				} else {
					activity.onError(CheckerActivity.ERROR_CANCELLED);
				}
			} else {
				activity.onError(CheckerActivity.ERROR_SANITY_CHECK);
			}
		}
	}

	/**
	 * Fill a server request builder with basic data.
	 * @param activity activity.
	 * @param builder server request builder where data will be inserted.
	 */
	protected void updateServerRequestBuilder(ServerRequestBuilder builder) {
		builder.putParameter("imei", this.pIMEI);
		builder.putParameter("msisdn", this.pMSISDN);
		builder.putParameter("devid", "imei:" + this.pIMEI);
		builder.putParameter("appid", this.pAppId);
		builder.putParameter("userid", this.pUserId);
		builder.putParameter("serial", this.pSerialNumber);
		builder.putParameter("Build_BOARD", android.os.Build.BOARD);
		builder.putParameter("Build_DEVICE", android.os.Build.DEVICE);
		builder.putParameter("Build_DISPLAY", android.os.Build.DISPLAY);
		builder.putParameter("Build_MODEL", android.os.Build.MODEL);
		builder.putParameter("Build_PRODUCT", android.os.Build.PRODUCT);
		builder.putParameter("option_version", "20110630");
		builder.putParameter("option_signed_license_enabled", "yes");
		builder.putParameter("option_signed_license_key", this.pCertificateId);
	}

	/**
	 * Call license server to execute a request in a new async task. This 
	 * method is implemented by creating an AsyncTask to communicate with 
	 * the license server in a worker thread, and then process the response
	 * in the UI thread. After processing the response, appropriate callback
	 * methods in the activity class will be invoked in the UI thread.
	 * However, be warned that there is a special case: if the call fails 
	 * due to sanity check failure, then the onError call will be executed 
	 * inline before this method returns.
	 * @param request request to be executed on the license server.
	 * @param activity activity where callbacks should be called.
	 */
	protected void callLicenseServer(ServerRequest request, CheckerActivity activity) {
		if (this.isReady()) {
			new LicenseServerCallTask(this, activity).execute(request);
		} else {
			activity.onError(CheckerActivity.ERROR_SANITY_CHECK);
		}
	}

	/**
	 * Submit an request to license server via portal. This method will open 
	 * a new WebsiteActivity activity that includes a WebView widget. The 
	 * activity will export a Javascript API for the portal to retrieve 
	 * requests and set the response. Once a response is set, the 
	 * WebsiteActivity will parse the response and return it as the result of
	 * the activity. Just like callLicenseServer, there is one exceptional case: 
	 * if the call fails due to sanity check failure, then the onError call 
	 * will be executed inline before this method returns.
	 * @param request request to be executed on the license server.
	 * @param activity activity where callbacks should be called.
	 */
	protected void openLicenseServerPortal(ServerRequest request, CheckerActivity activity) {
		if (this.isReady()) {
			try {
				Serializer serializer = this.pSerializer;
				String certid = this.pCertificateId;
				Certificate certobj = this.pCertificateObject;
				byte[] key = ProtocolUtility.generateSessionKey();
				String cnonce = ProtocolUtility.generateClientNonce();
				Intent intent = new Intent(activity, WebsiteActivity.class);

				String out1 = serializer.deflateServerRequest(request);
				String out2 = ProtocolUtility.wrapNonceLayer(out1, cnonce);
				String out3 = ProtocolUtility.wrapCryptoLayer(out2, certid, certobj, key);
	
				intent.putExtra("url", this.pLicenseServerPortalUrl);
				intent.putExtra("data", out3);
				intent.putExtra("request", request);
				intent.putExtra("cnonce", cnonce);
				intent.putExtra("key", key);
		
				activity.startActivityForResult(intent, 1);
			} catch (Exception ex) {
				activity.onError(CheckerActivity.ERROR_UNKNOWN);
			}
		} else {
			activity.onError(CheckerActivity.ERROR_SANITY_CHECK);
		}
	}

	/**
	 * Process server response and dispatch it to appropriate callback methods.
	 * Regardless of the origin of the response, this method will process the
	 * given license server request/response pair and make the appropriate call
	 * to the given activity class. This method should be invoked in the UI 
	 * thread, so that the callback method is invoked in the UI thread as well.
	 * @param request server request that is executed.
	 * @param response server response to be processed.
	 * @param activity activity whose callback method should be called.
	 */
	protected void processLicenseServerResponse(ServerRequest request, Object response, CheckerActivity activity) {
		if (response instanceof ActionResponse) {
			activity.executeAction(((ActionResponse) response).getAction());
		}
		else if (response instanceof Integer) {
			DBG("CheckerImplementation", "Failure to call license server");
			DBG("CheckerImplementation", ">>> Error Code: " + ((Integer) response).intValue());
			activity.onError(((Integer) response).intValue());
		}
		else if (response instanceof ErrorResponse) {
			DBG("CheckerImplementation", "License server reports some error");
			DBG("CheckerImplementation", ">>> Error Code: " + ((ErrorResponse) response).getCode());
			activity.onError(((ErrorResponse) response).getCode());
		}
		else {
			DBG("CheckerImplementation", "Unknown response from license server");
			DBG("CheckerImplementation", ">>> Response Type: " + response.getClass().getName());
			activity.onError(CheckerActivity.ERROR_UNKNOWN);
		}
	}

	/**
	 * Check if a given license is valid or not. This method should return 
	 * quickly so there are no restrictions where it can be called.
	 * @param license license to be checked for validity.
	 * @return true if the license is valid, false otherwise.
	 */
	protected boolean validateLicense(License license) {
		if (license != null) {
			if (license.getDeviceId().equals(this.pIMEI) == false) {
				return false;
			} else if (license.getUserId().equals(this.pUserId) == false) {
				return false;
			} else if (license.getApplicationId().equals(this.pAppId) == false) {
				return false;
			} else if (license.getSerialNumber().equals(this.pSerialNumber) == false) {
				return false;
			}

			if (license instanceof SignedLicense) {
				try {
					SignedLicense slicense = (SignedLicense) license;
					String rawdata = slicense.getRawData();
					String key = slicense.getKey();
					String signature = slicense.getSignature();

					if (this.pCertificateId.equals(key) == false) {
						return false;
					}

					Signature signer = Signature.getInstance("SHA1WithRSA");
					signer.initVerify(this.pCertificateObject);
					signer.update(rawdata.getBytes("UTF-8"));

					if (signer.verify(HexEncoding.decode(signature)) == false) {
						return false;
					}
				} catch (Exception ex) {
					return false;
				}
			}

			return true;
		} else {
			return false;
		}
	}

	/**
	 * Read cache file for cached data. This operation may require calculation
	 * of obfuscation keys which may take some time, so this method should only
	 * be called in a worker thread.
	 * @return cached data in the backing file.
	 * @throws IOException when the backing file cannot be read.
	 * @throws JSONException when the backing file cannot be parsed.
	 * @throws GeneralSecurityException when data cannot be decrypted.
	 */
	protected Data readCacheFile(Context context) 
			throws IOException, JSONException, GeneralSecurityException {
		InputStream in = context.openFileInput(this.pLicenseCacheFile);

		try {
			ByteArrayOutputStream cache = new ByteArrayOutputStream();
			byte[] buffer = new byte[10240];
			int size = 0;
			int total = 0;
		
			while ((size = in.read(buffer)) >= 0) {
				cache.write(buffer, 0, size);
				total += size;
			}
	
			if (total > 0) {
				byte[] temp1 = cache.toByteArray();
				byte[] temp2 = ObfuscationUtility.deobfuscate(temp1, this.getObfuscationKey());
				String temp3 = new String(temp2, "UTF-8");

				if (temp3.length() > 0) {
					return this.pSerializer.inflateData(temp3);
				} else {
					throw new FileNotFoundException("No data found in the cache file");
				}
			} else {
				throw new FileNotFoundException("No data found in the cache file");
			}
		} finally {
			in.close();
		}
	}
	
	/**
	 * Write cached data to cache file. This operation may require calculation
	 * of obfuscation keys which may take some time, so this method should only
	 * be called in a worker thread.
	 * @param data Data to be stored. It should be serializable by the serializer.
	 * @throws IOException when the backing file cannot be read.
	 * @throws JSONException when the backing file cannot be parsed.
	 * @throws GeneralSecurityException when data cannot be encrypted.
	 */
	protected void writeCacheFile(Context context, Data data) 
			throws IOException, JSONException, GeneralSecurityException {
		String temp1 = this.pSerializer.deflateData(data);
		byte[] temp2 = temp1.getBytes();
		byte[] temp3 = ObfuscationUtility.obfuscate(temp2, this.getObfuscationKey());
		OutputStream out = context.openFileOutput(this.pLicenseCacheFile, Context.MODE_PRIVATE);

		try {
			out.write(temp3);
			out.flush();
		} finally {
			out.close();
		}
	}

	/**	
	 * Call license server to retrieve a new nonce for subsequent call. Since
	 * this method needs to make a network operation and can take some times
	 * to complete, this method should only be called on a separate worker 
	 * thread, rather than the UI thread to avoid blocking it.
	 * @return Server nonce or an integer containing the error code.
	 */
	final Object executeLicenseServerNonceCall() {
		if (this.isReady()) {
			ServerRequestBuilder builder = new ServerRequestBuilder("nonce");
			ServerRequest request = builder.getServerRequest();
			Object response = this.executeLicenseServerCall(request, null);

			if (response instanceof NonceResponse) {
				return ((NonceResponse) response).getNonce();
			} else {
				return response;
			}
		} else {
			return new Integer(CheckerActivity.ERROR_SANITY_CHECK);
		}
	}

	/**
	 * Call license server to execute a request. Since this method needs to 
	 * make a network operation and can take some times to complete, this 
	 * method should only be called on a separate worker thread, rather than 
	 * the UI thread to avoid blocking it.
	 * @param request request to be executed on the license server.
	 * @param snonce server nonce, null if server nonce is not needed.
	 * @return response from license server, either a ServerResponse object or
	 * an integer object containing the error code.
	 */
	final Object executeLicenseServerCall(ServerRequest request, String snonce) {
		if (this.isReady()) {
			try {
				Serializer serializer = this.pSerializer;
				String certid = this.pCertificateId;
				Certificate certobj = this.pCertificateObject;
				URL url = this.pLicenseServerCallUrl;

				byte[] key = ProtocolUtility.generateSessionKey();
				String cnonce = ProtocolUtility.generateClientNonce();
				String out1 = serializer.deflateServerRequest(request);
				String out2 = ProtocolUtility.wrapNonceLayer(out1, cnonce, snonce);
				String out3 = ProtocolUtility.wrapCryptoLayer(out2, certid, certobj, key);
				HttpURLConnection connection = HttpUtility.startPost(url, "application/json", out3);

		   	try {
					if (connection.getURL().getHost().equals(url.getHost())) {
						String in3 = HttpUtility.readTextualResponseData(connection);
						String in2 = ProtocolUtility.unwrapCryptoLayer(in3, key);
						String in1 = ProtocolUtility.unwrapNonceLayer(in2, cnonce);
	
						return serializer.inflateServerResponse(in1);
					} else {
						return new Integer(CheckerActivity.ERROR_HOTSPOT);
					}
		 		} finally {
					if (connection != null) {
						connection.disconnect();
					}
		 		}
			} catch (IOException ex) {
				return new Integer(CheckerActivity.ERROR_NETWORK);
			} catch (JSONException ex) {
				return new Integer(CheckerActivity.ERROR_NETWORK);
			} catch (Exception ex) {
				return new Integer(CheckerActivity.ERROR_UNKNOWN);
			}
		} else {
			return new Integer(CheckerActivity.ERROR_SANITY_CHECK);
		}
	}

	/**
	 * Get the secret key used by the obfuscator. Compute the key if it is not
	 * computed yet. Otherwise, use the cached copy. This operation can take some
	 * time so it should only be called in a worker thread.
	 * @return obfuscator secret key.
	 */
	private final byte[] getObfuscationKey()
			throws GeneralSecurityException {
		if (this.sObfuscationSecret == null) {
			StringBuilder password = new StringBuilder();
			byte[] salt = this.pObfuscationSalt;
			int round = this.pObfuscationRound;

			password.append(this.pIMEI);
			password.append("/");
			password.append(this.pAppId);
	
			this.sObfuscationSecret = ObfuscationUtility.generateKey(
				password.toString().getBytes(), 
				this.pObfuscationSalt, 
				this.pObfuscationRound);
		}

		return this.sObfuscationSecret;
	}
}

#endif	//USE_OPTUS_DRM
