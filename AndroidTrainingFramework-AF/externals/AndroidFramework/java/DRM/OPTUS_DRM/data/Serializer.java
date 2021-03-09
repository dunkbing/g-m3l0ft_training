#if USE_OPTUS_DRM
package com.msap.store.drm.android.data;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import org.json.*;

/**
 * This class implements object marshalling and unmarshalling methods for data 
 * objects defined in this package.
 *
 * This class enables one to convert between data objects and human-readable 
 * JSON data without dealing with the JSON library. The class also accepts
 * plugins to enable conversion of new license restrictions and license 
 * server responses.
 */
public class Serializer {
	private Serializer.Implementation impl;

	/**
 	 * Default constructor.
 	 */
	public Serializer() {
		this.impl = new Serializer.Implementation();
	}

	/**
	 * Parse a JSON string to data object.
	 * @param json JSON string to be parsed.
	 * @return data object parsed from the JSON string.
	 * @throws JSONException when the JSON string cannot be parsed.
	 */
	public final Data inflateData(String json) throws JSONException {
		return impl.inflateDataStruct(parseJsonObjectFromString(json));
	}
	
	/**
	 * Serialize a data object to JSON string.
	 * @param data data object to be serialized.
	 * @return JSON string serialized from the data object.
	 * @throws JSONException when the object cannot be serialized.
	 */
	public final String deflateData(Data data) throws JSONException {
		return impl.deflateDataStruct(data).toString();
	}

	/**
	 * Parse a JSON string to license object.
	 * @param json JSON string to be parsed.
	 * @return license object parsed from the JSON string.
	 * @throws JSONException when the JSON string cannot be parsed.
	 */
	public final License inflateLicense(String json) throws JSONException {
		return impl.inflateLicenseStruct(parseJsonObjectFromString(json));
	}
	
	/**
	 * Serialize a license object to JSON string.
	 * @param license license object to be serialized.
	 * @return JSON string serialized from the license data object.
	 * @throws JSONException when the object cannot be serialized.
	 */
	public final String deflateLicense(License license) throws JSONException {
		return impl.deflateLicenseStruct(license).toString();
	}

	/**
	 * Parse a JSON string to license list object.
	 * @param json JSON string to be parsed.
	 * @return license list object parsed from the JSON string.
	 * @throws JSONException when the JSON string cannot be parsed.
	 */
	public final LicenseList inflateLicenseList(String json) throws JSONException {
		return impl.inflateLicenseListStruct(parseJsonObjectFromString(json));
	}
	
	/**
	 * Serialize a license list object to JSON string.
	 * @param list License list object to be serialized.
	 * @return JSON string serialized from the license list object.
	 * @throws JSONException when the object cannot be serialized to JSON string.
	 */
	public final String deflateLicenseList(LicenseList list) throws JSONException {
		return impl.deflateLicenseListStruct(list).toString();
	}

	/**
	 * Parse a JSON string to license cache object.
	 * @param json JSON string to be parsed.
	 * @return license cache object parsed from the JSON string.
	 * @throws JSONException when the JSON string cannot be parsed.
	 */
	public final LicenseCache inflateLicenseCache(String json) throws JSONException {
		return impl.inflateLicenseCacheStruct(parseJsonObjectFromString(json));
	}

	/**
	 * Serialize a license list object to JSON string.
	 * @param cache License cache object to be serialized.
	 * @return JSON string serialized from the license cache object.
	 * @throws JSONException when the object cannot be serialized to JSON string.
	 */
	public final String deflateLicenseCache(LicenseCache cache) throws JSONException {
		return impl.deflateLicenseCacheStruct(cache).toString();
	}

	/**
	 * Parse a JSON string to server request object.
	 * @param json JSON string to be parsed.
	 * @return server request object parsed from the JSON string.
	 * @throws JSONException when the JSON string cannot be parsed.
	 */
	public final ServerRequest inflateServerRequest(String json) throws JSONException {
		return impl.inflateServerRequestStruct(parseJsonObjectFromString(json));
	}

	/**
	 * Serialize a license server request object to JSON string.
	 * @param request request object to be serialized.
	 * @return JSON string serialized from the request object
	 * @throws JSONException when the object cannot be serialized to JSON string.
	 */
	public final String deflateServerRequest(ServerRequest request) throws JSONException {
		return impl.deflateServerRequestStruct(request).toString();
	}

	/**
	 * Parse a JSON string to license server response object.
	 * @param json JSON string to be parsed.
	 * @return server response object parsed from the JSON string.
	 * @throws JSONException when the JSON string cannot be parsed.
	 */
	public final ServerResponse inflateServerResponse(String json) throws JSONException {
		return impl.inflateServerResponseStruct(parseJsonObjectFromString(json));
	}


	/**
 	 * Construct a serializer using an alternative implementation.
 	 * @param impl Alternative implementation used by this serializer.
 	 * @throws NullPointerException when the implementation is null.
 	 */
	protected Serializer(Serializer.Implementation impl) {
		if (impl != null) {
			this.impl = impl;
		} else {
			throw new NullPointerException();
		}
	}

	/**
	 * Parse JSON string to extract the JSON structure inside.
	 * @param json JSON string to be parsed.
	 * @return JSON object constructed from the JSON text.
	 * @throws JSONException when the JSON string cannot be parsed to JSON object.
	 */
	protected final JSONObject parseJsonObjectFromString(String json) throws JSONException {
		JSONTokener parser = new JSONTokener(json);
		Object object = parser.nextValue();

		if (object instanceof JSONObject) {
			return (JSONObject) object;
		} else {
			throw new JSONException("Data not a JSON object");
		}
	}


	/**
	 * Helper class for serializer classes and its plugins.
	 * @author kmchan
	 */
	public static class Implementation {
		public static final String TYPE_FIELD = "[type]";

		/**
		 * Inflate a JSON structure to corresponding data object.
		 * @param jData JSON structure to be converted.
		 * @return data object converted from the input JSON structure.
		 * @throws JSONException when the JSON object does not have proper structure.
		 */
		public Data inflateDataStruct(JSONObject jData) throws JSONException {
			String datatype = getJsonObjectType(jData);

			if (datatype.equals(SignedLicense.TYPE_NAME)) {
				return this.inflateLicenseStruct(jData);
			}
			else if (datatype.equals(License.TYPE_NAME)) {
				return this.inflateLicenseStruct(jData);
			}
			else if (datatype.equals(LicenseCache.TYPE_NAME)) {
				return this.inflateLicenseCacheStruct(jData);
			}
			else if (datatype.equals(LicenseList.TYPE_NAME)) {
				return this.inflateLicenseListStruct(jData);
			}
			else if (datatype.equals(CancelAction.TYPE_NAME)) {
				return this.inflateActionStruct(jData);
			}
			else if (datatype.equals(PromptAction.TYPE_NAME)) {
				return this.inflateActionStruct(jData);
			}
			else if (datatype.equals(DirectServerRequestAction.TYPE_NAME)) {
				return this.inflateActionStruct(jData);
			}
			else if (datatype.equals(DirectServerRequestAction.TYPE_NAME_COMPAT)) {
				return this.inflateActionStruct(jData);
			}
			else if (datatype.equals(WebFormServerRequestAction.TYPE_NAME)) {
				return this.inflateActionStruct(jData);
			}
			else if (datatype.equals(ServerRequest.TYPE_NAME)) {
				return this.inflateServerRequestStruct(jData);
			}
			else if (datatype.equals(ErrorResponse.TYPE_NAME)) {
				return this.inflateServerResponseStruct(jData);
			}
			else if (datatype.equals(NonceResponse.TYPE_NAME)) {
				return this.inflateServerResponseStruct(jData);
			}
			else if (datatype.equals(ActionResponse.TYPE_NAME)) {
				return this.inflateServerResponseStruct(jData);
			}
			else {
				throw new JSONException("Unsupported type");
			}
		}

		/**
		 * Deflate a data object to corresponding JSON structure.
		 * @param lData data object to be converted.
		 * @return JSON structure converted from the input data object.
		 * @throws JSONException when the JSON structure cannot be built.
		 */
		public JSONObject deflateDataStruct(Data lData) throws JSONException {
			if (lData instanceof License) {
				return this.deflateLicenseStruct((License) lData);
			} 
			else if (lData instanceof LicenseCache) {
				return this.deflateLicenseCacheStruct((LicenseCache) lData);
			}
			else if (lData instanceof LicenseList) {
				return this.deflateLicenseListStruct((LicenseList) lData);
			}
			else if (lData instanceof Action) {
				return this.deflateActionStruct((Action) lData);
			}
			else if (lData instanceof ServerRequest) {
				return this.deflateServerRequestStruct((ServerRequest) lData);
			}
			else {
				throw new JSONException("Unsupported type");
			}	
		}

		/**
		 * Convert a JSON structure to license object.
		 * @param jLicense JSON structure to be converted.
		 * @return license object converted from the input structure.
		 * @throws JSONException when the JSON object does not have proper structure.
		 */
		public License inflateLicenseStruct(JSONObject jLicense) throws JSONException {
			if (this.checkJsonObjectType(jLicense, SignedLicense.TYPE_NAME)) {
				String rawdata = jLicense.getString("data");
				String key = jLicense.getString("key");
				String signature = jLicense.getString("signature");
				JSONTokener parser = new JSONTokener(rawdata);
				JSONObject jLicenseData = (JSONObject) parser.nextValue();

				int type = jLicenseData.getInt("type");
				String id = jLicenseData.getString("id");
				String devid = jLicenseData.getString("devid");
				String userid = jLicenseData.getString("userid");
				String appid = jLicenseData.getString("appid");
				String serial = jLicenseData.getString("serial");
				LicenseRestriction[] restrictions = this.getJsonLicenseObjectRestrictions(jLicenseData);
			
				if (restrictions != null) {
					return new SignedLicense(rawdata, id, type, devid, userid, appid, serial, restrictions, key, signature);
				} else {
					return new SignedLicense(rawdata, id, type, devid, userid, appid, serial, key, signature);
				}
			} else if (this.checkJsonObjectType(jLicense, License.TYPE_NAME)) {
				int type = jLicense.getInt("type");
				String id = jLicense.getString("id");
				String devid = jLicense.getString("devid");
				String userid = jLicense.getString("userid");
				String appid = jLicense.getString("appid");
				String serial = jLicense.getString("serial");
				LicenseRestriction[] restrictions = this.getJsonLicenseObjectRestrictions(jLicense);

				if (restrictions != null) {
					return new License(id, type, devid, userid, appid, serial, restrictions);
				} else {
					return new License(id, type, devid, userid, appid, serial);
				}
			} else {
				throw new JSONException("Input data is not a license object");
			}
		}
	
		/**
		 * Convert a license object to a JSON structure
		 * @param lLicense license object to be converted.
		 * @return JSON structure converted from the input license data.
		 * @throws JSONException when the structure cannot be built.
		 */
		public JSONObject deflateLicenseStruct(License lLicense) throws JSONException {
			JSONObject jLicense = new JSONObject();

			if (lLicense instanceof SignedLicense) {
				SignedLicense lSignedLicense = (SignedLicense) lLicense;
				jLicense.put(TYPE_FIELD, SignedLicense.TYPE_NAME);
				jLicense.put("data", lSignedLicense.getRawData());
				jLicense.put("signature", lSignedLicense.getSignature());
				jLicense.put("key", lSignedLicense.getKey());
			} else {
				JSONArray jRestrictionList = new JSONArray();
				int rcount = lLicense.getRestrictionCount();
	
				for (int i = 0; i < rcount; i++) {
					LicenseRestriction lRestriction = lLicense.getRestriction(i);
					jRestrictionList.put(this.deflateLicenseRestrictionStruct(lRestriction));
				}
	
				jLicense.put(TYPE_FIELD, License.TYPE_NAME);
				jLicense.put("type", lLicense.getType());
				jLicense.put("id", lLicense.getId());
				jLicense.put("restrictions", jRestrictionList);
			}

			return jLicense;
		}

		/**
		 * Convert a JSON structure to license restriction object.
		 * @param jRestriction JSON structure to be converted.
		 * @return license restriction object converted from the input structure.
		 * @throws JSONException when the JSON object does not have proper structure.
		 */
		public LicenseRestriction inflateLicenseRestrictionStruct(JSONObject jRestriction) throws JSONException {
			throw new JSONException("Input data does not contain supported license restriction data.");
		}
	
		/**
		 * Convert a license restriction object to a JSON structure.
		 * @param lRestriction license restriction object to be converted.
		 * @return JSON structure converted from the input license data.
		 * @throws JSONException when the structure cannot be built.
		 * @throws NullPointerException when lRestriction is null.
		 */
		public JSONObject deflateLicenseRestrictionStruct(LicenseRestriction lRestriction) throws JSONException {
			throw new JSONException("Input license restriction is not supported.");
		}

		/**
		 * Convert a JSON structure to license list object.
		 * @param jList JSON structure to be converted.
		 * @return license list object converted from the input structure.
		 * @throws JSONException thrown when the structure is invalid.
		 */
		public LicenseList inflateLicenseListStruct(JSONObject jList) throws JSONException {
			if (checkJsonObjectType(jList, LicenseList.TYPE_NAME)) {
				LicenseList lList = new LicenseList();
				JSONArray jArray = jList.getJSONArray("licenses");
				int len = jArray.length();
			
				for (int i = 0; i < len; i++) {
					lList.add(this.inflateLicenseStruct(jArray.getJSONObject(i)));
				}
			
				return lList;
			} else {
				throw new JSONException("Input data is not a license list object");
			}
		}
	
		/**
		 * Convert a license list object to a JSON structure.
		 * @param lList license list object to be converted.
		 * @return JSON structure converted from the specified object.
		 * @throws JSONException when the structure cannot be built.
		 */
		public JSONObject deflateLicenseListStruct(LicenseList lList) throws JSONException {
			if (lList != null) {
				JSONObject jList = new JSONObject();
				JSONArray jArray = new JSONArray();
			
				for (int i = 0; i < lList.size(); i++) {
					jArray.put(this.deflateLicenseStruct(lList.get(i)));
				}
			
				jList.put(TYPE_FIELD, LicenseList.TYPE_NAME);
				jList.put("licenses", jArray);

				return jList;
			} else {
				throw new NullPointerException();
			}
		}

		/**
		 * Convert a JSON structure to license cache object.
		 * @param jCache JSON structure to be converted.
		 * @return license cache object converted from the input structure.
		 * @throws JSONException thrown when the structure is invalid.
		 */
		public LicenseCache inflateLicenseCacheStruct(JSONObject jCache) throws JSONException {
			if (checkJsonObjectType(jCache, LicenseCache.TYPE_NAME)) {
				LicenseCache lCache = new LicenseCache();
				JSONArray jList = jCache.getJSONArray("licenses");
				int size = jList.length();
			
				for (int i = 0; i < size; i++) {
					lCache.add(this.inflateLicenseStruct(jList.getJSONObject(i)));
				}
			
				return lCache;
			} else {
				throw new JSONException("input data is not a license cache object");
			}
		}
	
		/**
		 * Convert a license cache object to a JSON structure.
		 * @param lCache license cache object to be converted.
		 * @return JSON structure converted from the specified object.
		 * @throws JSONException thrown when the structure cannot be built.
		 */
		public JSONObject deflateLicenseCacheStruct(LicenseCache lCache) throws JSONException {
			JSONObject jCache = new JSONObject();
			JSONArray jList = new JSONArray();
			int size = lCache.size();
			
			for (int i = 0; i < size; i++) {
				jList.put(this.deflateLicenseStruct(lCache.get(i)));
			}
			
			jCache.put(TYPE_FIELD, LicenseCache.TYPE_NAME);
			jCache.put("licenses", jList);

			return jCache;
		}

		/**
		 * Convert a JSON structure to action object.
		 * @param jAction JSON structure to be converted.
		 * @return action object converted from the input structure.
		 * @throws JSONException when the JSON object does not have proper structure.
		 */
		public Action inflateActionStruct(JSONObject jAction) throws JSONException {
			String type = getJsonObjectType(jAction);

			if (type.equals(CancelAction.TYPE_NAME)) {
				return new CancelAction();
			}
			else if (type.equals(PromptAction.TYPE_NAME)) {
				String title = jAction.getString("title");
				String summary = jAction.getString("summary");
				String description = jAction.getString("description");
				JSONArray jChoiceList = jAction.getJSONArray("choices");
				int length = jChoiceList.length();

				if (length > 0) {
					String[] choiceLabels = new String[length];
					Action[] choiceActions = new Action[length];
			
					for (int i = 0; i < length; i++) {
						JSONObject jChoice = jChoiceList.getJSONObject(i);
						JSONObject jChoiceAction = jChoice.getJSONObject("action");
						choiceLabels[i] = jChoice.getString("label");
						choiceActions[i] = this.inflateActionStruct(jChoiceAction);
					}

					return new PromptAction(title, summary, description, choiceLabels, choiceActions);
				} else {
					throw new JSONException("PromptAction choice list cannot be empty");
				}
			}
			else if (type.equals(DirectServerRequestAction.TYPE_NAME)) {
				JSONObject jRequest = jAction.getJSONObject("request");
				ServerRequest lRequest = this.inflateServerRequestStruct(jRequest);
				return new DirectServerRequestAction(lRequest);
			}
			else if (type.equals(DirectServerRequestAction.TYPE_NAME_COMPAT)) {
				JSONObject jRequest = jAction.getJSONObject("request");
				ServerRequest lRequest = this.inflateServerRequestStruct(jRequest);
				return new DirectServerRequestAction(lRequest);
			}
			else if (type.equals(WebFormServerRequestAction.TYPE_NAME)) {
				JSONObject jRequest = jAction.getJSONObject("request");
				ServerRequest lRequest = this.inflateServerRequestStruct(jRequest);
				return new WebFormServerRequestAction(lRequest);
			}
			else {
				throw new JSONException("Unknown action type");
			}
		}

		/**
		 * Convert an action object to a JSON structure.
		 * @param action action object to be converted.
		 * @return JSON structure converted from the specified object.
		 * @throws JSONException when the structure cannot be built.
		 */
		public JSONObject deflateActionStruct(Action lAction) throws JSONException {
			JSONObject jAction = new JSONObject();

			if (lAction instanceof CancelAction) {
				jAction.put(TYPE_FIELD, CancelAction.TYPE_NAME);
			}
			else if (lAction instanceof PromptAction) {
				PromptAction lPromptAction = (PromptAction) lAction;
				JSONArray jChoiceList = new JSONArray();
				
				for (int i = 0; i < lPromptAction.getChoiceCount(); i++) {
					JSONObject jChoice = new JSONObject();
					jChoice.put("label", lPromptAction.getChoiceLabel(i));
					jChoice.put("action", this.deflateActionStruct(lPromptAction.getChoiceAction(i)));
					jChoiceList.put(jChoice);
				}
					
				jAction.put(TYPE_FIELD, PromptAction.TYPE_NAME);
				jAction.put("title", lPromptAction.getTitle());
				jAction.put("summary", lPromptAction.getSummary());
				jAction.put("description", lPromptAction.getDescription());
				jAction.put("choices", jChoiceList);
			}
			else if (lAction instanceof DirectServerRequestAction) {
				DirectServerRequestAction lRequestAction = (DirectServerRequestAction) lAction;
				ServerRequest lRequest = lRequestAction.getServerRequest();
				jAction.put(TYPE_FIELD, DirectServerRequestAction.TYPE_NAME);
				jAction.put("request", this.deflateServerRequestStruct(lRequest));
			} 
			else if (lAction instanceof WebFormServerRequestAction) {
				WebFormServerRequestAction lRequestAction = (WebFormServerRequestAction) lAction;
				ServerRequest lRequest = lRequestAction.getServerRequest();
				jAction.put(TYPE_FIELD, WebFormServerRequestAction.TYPE_NAME);
				jAction.put("request", this.deflateServerRequestStruct(lRequest));
			}
			else {
				throw new JSONException("Unsupported action type");
			}

			return jAction;
		}

		/**
		 * Convert a JSON structure to server request object.
		 * @param jRequest JSON structure to be converted.
		 * @return server request object converted from the input structure.
		 * @throws JSONException when the JSON object does not have proper structure.
		 */
		public ServerRequest inflateServerRequestStruct(JSONObject jRequest) throws JSONException {
			if (checkJsonObjectType(jRequest, ServerRequest.TYPE_NAME)) {
				String method = jRequest.getString("method");
				JSONObject jParameters = jRequest.getJSONObject("parameters");
				Map<String,Object> parameters = new HashMap<String,Object>();
			
				for (Iterator keys = jParameters.keys(); keys.hasNext(); ) {
					String key = (String) keys.next();
					Object value = jParameters.get(key);
					parameters.put(key, value);
				}

				return new ServerRequest(method, parameters);
			} else {
				throw new JSONException("Input data is not a server request object");
			}
		}

		/**
		 * Convert a server request object to a JSON structure.
		 * @param lRequest server request object to be converted.
		 * @return JSON structure converted from the specified object.
		 * @throws JSONException thrown when the structure cannot be built.
		 */
		public JSONObject deflateServerRequestStruct(ServerRequest lRequest) throws JSONException {
			JSONObject jRequest = new JSONObject();
			JSONObject jParameters = new JSONObject();

			for (Iterator<String> i = lRequest.parameterIterator(); i.hasNext(); ) {
				String name = i.next();
				Object value = lRequest.getParameter(name);
				jParameters.put(name, value);
			}

			jRequest.put(TYPE_FIELD, "server-request");
			jRequest.put("method", lRequest.getMethod());
			jRequest.put("parameters", jParameters);

			return jRequest;
		}

		/**
		 * Convert a JSON structure to server response object.
		 * @param jResponse JSON structure to be converted.
		 * @return server response object converted from the input structure.
		 * @throws JSONException when the JSON object does not have proper structure.
		 */
		public ServerResponse inflateServerResponseStruct(JSONObject jResponse) throws JSONException {
			String type = getJsonObjectType(jResponse);
			
			if (type.equals("error-response")) {
				return new ErrorResponse(
					jResponse.getInt("error_code"),
					jResponse.getString("error_message"));
			}
			else if (type.equals("nonce-response")) {
				return new NonceResponse(
					jResponse.getString("nonce"));
			}
			else if (type.equals("action-response")) {
				return new ActionResponse(
					this.inflateActionStruct(jResponse.getJSONObject("action")));
			}
			else {
				throw new JSONException("");
			}
		}

		/**
 		 * Return the type of a given JSON object.
 		 * @param object JSON object whose type is to be checked.
 		 * @return type of the JSON object.
 		 * @throws JSONException when the structure is invalid.
 		 */
		protected final String getJsonObjectType(JSONObject object) throws JSONException {
			String dt = object.getString(TYPE_FIELD);

			if (dt != null) {
				return dt;
			} else {
				return "unknown";
			}
		}

		/**
		 * Check if the JSON object is of the given type.
		 * @param object JSON object whose type is checked.
		 * @param type data type the JSON object is supposed to be.
		 * @return true if object is of the given type, false otherwise.
 		 * @throws JSONException when the structure is invalid.
		 */
		protected final boolean checkJsonObjectType(JSONObject object, String type) throws JSONException {
			if (getJsonObjectType(object).equals(type)) {
				return true;
			} else {
				return false;
			}
		}

		/**
 	   * Return the license ID from a JSON object containing a license.
 	   * @param jLicense JSON object containing a license.
 	   * @return ID of the license contained in the JSON object.
 	   * @throws JSONException when the required information cannot be extracted.
 	   */
		private final String getJsonLicenseObjectId(JSONObject jLicense) throws JSONException {
			return jLicense.getString("id");
		}

		/**
 	   * Return the license restrictions from a JSON object containing a license.
 	   * @param jLicense JSON object containing a license.
 	   * @return license restrictions of the license contained in the JSON object, or null
 	   * when the license contains no restriction at all.
 	   * @throws JSONException when the required information cannot be extracted.
 	   */
		private final LicenseRestriction[] getJsonLicenseObjectRestrictions(JSONObject jLicense) throws JSONException {
			JSONArray jRestrictionList = jLicense.optJSONArray("restrictions");
			
			if (jRestrictionList != null) {
				int count = jRestrictionList.length();
				LicenseRestriction[] restrictions = new LicenseRestriction[count];

				for (int i = 0; i < count; i++) {
					JSONObject jRestriction = jRestrictionList.getJSONObject(i);
					LicenseRestriction lRestriction = this.inflateLicenseRestrictionStruct(jRestriction);
					restrictions[i] = lRestriction;
				}

				return restrictions;
			} else {
				return null;
			}
		}
	}
}

#endif	//USE_OPTUS_DRM
