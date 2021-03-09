#if USE_IN_APP_BILLING
package APP_PACKAGE.iab;

import APP_PACKAGE.billing.common.Base64;
import APP_PACKAGE.billing.common.Base64DecoderException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashSet;

class s {
	SET_TAG("Security");

	private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

	/**
	* This keeps track of the nonces that we generated and sent to the
	* server.  We need to keep track of these until we get back the purchase
	* state and send a confirmation message back to Android Market. If we are
	* killed and lose this list of nonces, it is not fatal. Android Market will
	* send us a new "notify" message and we will re-generate a new nonce.
	* This has to be "static" so that the {@link BillingReceiver} can
	* check if a nonce exists.
	*/
	//private static HashSet<Long> sKnownNonces = new HashSet<Long>(); //Used in native part

	/** Generates a nonce (a random number used once). */
	public static native long gn();

	public static native void rn(long nonce);

	public static native boolean in(long nonce);

	/**
	* Verifies that the data was signed with the given signature, and returns
	* the list of verified purchases. The data is in JSON format and contains
	* a nonce (number used once) that we generated and that was signed
	* (as part of the whole data string) with a private key. The data also
	* contains the {@link PurchaseState} and product ID of the purchase.
	* In the general case, there can be an array of purchase transactions
	* because there may be delays in processing the purchase on the backend
	* and then several purchases can be batched together.
	* @param signedData the signed JSON string (signed, not encrypted)
	* @param signature the signature for the data, signed with the private key
	*/
	public static PurchaseInfo vp(String signedData, String signature) {
		if (signedData == null) {
			ERR(TAG, "data is null");
			return null;
		}
		INFO(TAG, "signedData: " + signedData);
		boolean verified = false;
		if (!TextUtils.isEmpty(signature)) 
		{
			String epk = InAppBilling.gk();
			PublicKey key = s.gk(epk);
			verified = s.v(key, signedData, signature);
			if (!verified) {
				WARN(TAG, "signature does not match data.");
				return null;
			}
		}
		else
			return null;


		PurchaseInfo purchase;
		try {
			purchase = new PurchaseInfo(signedData,signature);
		} catch (JSONException e) {
			ERR(TAG, "JSON exception: "+ e);
			return null;
		}
		return purchase;
	}

	/**
	* Generates a PublicKey instance from a string containing the
	* Base64-encoded public key.
	*
	* @param encodedPublicKey Base64-encoded public key
	* @throws IllegalArgumentException if encodedPublicKey is invalid
	*/
	public static native PublicKey gk(String pk);

	
	public static PublicKey a(KeyFactory kf, X509EncodedKeySpec ks)
	{
		try
		{
			return kf.generatePublic(ks);
		}
		catch (InvalidKeySpecException e) {
			ERR(TAG, "Invalid key specification.");
			throw new IllegalArgumentException(e);
		}
		
	}
	/**
	* Verifies that the signature from the server matches the computed
	* signature on the data.  Returns true if the data is correctly signed.
	*
	* @param publicKey public key associated with the developer account
	* @param signedData signed data from server
	* @param signature server signature
	* @return true if the data and signature match
	*/
	public static native int bq(PublicKey publicKey, String signedData, String signature);
	public static boolean v(PublicKey publicKey, String signedData, String signature) {
		INFO(TAG, "signature: " + signature);
		Signature sig;
		try {
			sig = Signature.getInstance(SIGNATURE_ALGORITHM);
			sig.initVerify(publicKey);
			sig.update(signedData.getBytes());
			if (!sig.verify(Base64.decode(signature))) {
				ERR(TAG, "Signature verification failed.");
				return false;
			}
			return true;
		} catch (NoSuchAlgorithmException e) {
			ERR(TAG, "NoSuchAlgorithmException.");
		} catch (InvalidKeyException e) {
			ERR(TAG, "Invalid key specification.");
		} catch (SignatureException e) {
			ERR(TAG, "Signature exception.");
		} catch (Base64DecoderException e) {
			ERR(TAG, "Base64 decoding failed.");
		}
		return false;
	}
}

#endif //#if USE_IN_APP_BILLING