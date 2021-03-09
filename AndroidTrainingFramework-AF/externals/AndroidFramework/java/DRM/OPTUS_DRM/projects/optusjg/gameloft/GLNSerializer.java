#if USE_OPTUS_DRM
package com.msap.store.drm.android.projects.optusjg.gameloft;

import org.json.*;
import com.msap.store.drm.android.data.*;

/**
 * This class extends serializer to handle GLN specific data types. This 
 * class converts test server data between JSON strings and corresponding
 * internal objects.
 *
 * @see GLNLicenseResponse
 * @author Edison Chan
 */
public class GLNSerializer extends Serializer {
	/**
	 * Construct a new GLNSerializer object.
	 */
	public GLNSerializer() {
		super(new GLNSerializer.Implementation());
	}

	/**
	 * Construct a new GLNSerializer object that uses the given implementation.
	 * @param impl implementation the serializer uses.
	 */
	protected GLNSerializer(GLNSerializer.Implementation impl) {
		super(impl);
	}

	/**
	 * Implementation class used by GLN serializer. This class converts test 
	 * server data between JSON structure and corrresponding internal objects.
	 * @author Edison Chan
	 */
	public static class Implementation extends Serializer.Implementation {
		/**
		 * Inflate a JSON structure to corresponding data object.
		 * @param jData JSON structure to be converted.
		 * @return data object converted from the input JSON structure.
		 * @throws JSONException when the JSON object does not have proper structure.
		 */
		public Data inflateDataStruct(JSONObject jData) throws JSONException {
			String datatype = getJsonObjectType(jData);

			if (datatype.equals(GLNLicenseResponse.TYPE_NAME)) {
				return this.inflateServerResponseStruct(jData);
			} else {
				return super.inflateDataStruct(jData);
			}
		}

		/**
		 * Convert a JSON structure to server response object.
		 * @param jResponse JSON structure to be converted.
		 * @return server response object converted from the input structure.
		 * @throws JSONException when the JSON object does not have proper structure.
		 */
		public ServerResponse inflateServerResponseStruct(JSONObject jResponse) throws JSONException {
			String datatype = getJsonObjectType(jResponse);

			if (datatype.equals(GLNLicenseResponse.TYPE_NAME)) {
				JSONObject jLicense = jResponse.getJSONObject("license");
				License lLicense = this.inflateLicenseStruct(jLicense);

				return new GLNLicenseResponse(lLicense);
			} else {
				return super.inflateServerResponseStruct(jResponse);
			}
		}
	}
}

#endif	//USE_OPTUS_DRM
