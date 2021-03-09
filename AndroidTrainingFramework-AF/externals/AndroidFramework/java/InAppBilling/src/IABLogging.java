package APP_PACKAGE.iab;

import android.text.TextUtils;
import android.util.Log;

import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class IABLogging
{
	
	SET_TAG("InAppBilling");
	
	private static IABLogging mThis;
	private int m_requestLogID = 100000;
	private Hashtable<String, RequestInfo> m_RequestTable = new Hashtable<String, RequestInfo>();
	
	public IABLogging () { 	}
	
	public static IABLogging getInstance ()
	{
		if (mThis == null)
			mThis = new IABLogging();
			
		return mThis;
	}
	
	public void LogInfo (int level, int status, String data)
	{
		try
		{
			JSONObject jObj		= new JSONObject();
			
			if (TextUtils.isEmpty(data))
				return;
			
			InAppBilling.c(OP_LOGGING_LOG_INFO, level, status, data);
		}
		catch(Exception e) {DBG_EXCEPTION(e);}
	}
	
	public void AppendLogRequestData (String url, String methodType, String requestName)
	{
		if (TextUtils.isEmpty(requestName)) return;
		m_requestLogID++;
		m_RequestTable.put(requestName, new RequestInfo(m_requestLogID, requestName, url, methodType));
	}
	
	
	public void AppendLogResponse (String data, String requestName)
	{
		if (TextUtils.isEmpty(requestName)) return;
		if (m_RequestTable.containsKey(requestName))
		{
			RequestInfo rInfo = m_RequestTable.get(requestName);
			rInfo.GenerateResponseJSONLog(data);
		}
	}
	
	public void ComputeTimeElapsed (String requestName)
	{
		if (TextUtils.isEmpty(requestName)) return;
		if (m_RequestTable.containsKey(requestName))
		{
			RequestInfo rInfo = m_RequestTable.get(requestName);
			rInfo.ComputeTimeElapsed();
		}
	}
	
	public double GetTimeElapsed (String requestName)
	{
		if (TextUtils.isEmpty(requestName)) return 0.0f;
		if (m_RequestTable.containsKey(requestName))
		{
			RequestInfo rInfo = m_RequestTable.get(requestName);
			rInfo.ComputeTimeElapsed();
			return rInfo.GetTimeElapsed();
		}
		return 0.0f;
	}
	
	public long GetRequestId (String requestName)
	{
		if (TextUtils.isEmpty(requestName)) return -1;
		if (m_RequestTable.containsKey(requestName))
		{
			RequestInfo rInfo = m_RequestTable.get(requestName);
			return rInfo.GetRequestId();
		}
		return -1;
	}
	
	public void RemoveRequestInfo (String requestName)
	{
		if (TextUtils.isEmpty(requestName)) return;
		if (m_RequestTable.containsKey(requestName))
		{
			m_RequestTable.remove(requestName);
		}
	}
	
	public void AppendParamsRequestHeaders (String paramName, String paramValue, String requestName)
	{
		if (TextUtils.isEmpty(requestName)) return;
		if (m_RequestTable.containsKey(requestName))
		{
			RequestInfo rInfo = m_RequestTable.get(requestName);
			rInfo.AppendParamsRequestHeaders(paramName, paramValue);
		}
	}
	
	public void AppendParamsRequestHeaders (String value, String requestName)
	{
		if (TextUtils.isEmpty(requestName)) return;
		if (m_RequestTable.containsKey(requestName))
		{
			RequestInfo rInfo = m_RequestTable.get(requestName);
			rInfo.AppendParamsRequestHeaders(value);
		}
	}
	
	public void AppendParamsRequestPayload (String paramName, String paramValue, String requestName)
	{
		if (TextUtils.isEmpty(requestName)) return;
		if (m_RequestTable.containsKey(requestName))
		{
			RequestInfo rInfo = m_RequestTable.get(requestName);
			rInfo.AppendParamsRequestPayload(paramName, paramValue);
		}
	}
	
	public void AppendParamsRequestPayload (String value, String requestName)
	{
		if (TextUtils.isEmpty(requestName)) return;
		if (m_RequestTable.containsKey(requestName))
		{
			RequestInfo rInfo = m_RequestTable.get(requestName);
			rInfo.AppendParamsRequestPayload(value);
		}
	}
	
	
	public void AppendParamsResponseHeaders (String paramName, String paramValue, String requestName)
	{
		if (TextUtils.isEmpty(requestName)) return;
		if (m_RequestTable.containsKey(requestName))
		{
			RequestInfo rInfo = m_RequestTable.get(requestName);
			rInfo.AppendParamsResponseHeaders(paramName, paramValue);
		}
	}
	
	public String GetRequestInfo(String requestName)
	{
		if (TextUtils.isEmpty(requestName)) return "";
		if (m_RequestTable.containsKey(requestName))
		{
			RequestInfo rInfo = m_RequestTable.get(requestName);
			rInfo.GenerateRequestJSONLog();
			return rInfo.GetRequestJSONData();
		}
		return "";
	}
	
	public String GetResponseInfo(String requestName)
	{
		if (TextUtils.isEmpty(requestName)) return "";
		if (m_RequestTable.containsKey(requestName))
		{
			RequestInfo rInfo = m_RequestTable.get(requestName);
			return rInfo.GetResponseJSONData();
		}
		return "";
	}
	
	class RequestInfo
	{
		private int m_requestLogID 	= 0;
		String m_requestName 		= null;
		String m_url				= null;
		String m_methodType			= null;
		long m_requestTimeStart 	= 0;
		long m_requestTimeEnd 		= 0;
		double m_requestTimeWait	= 0;
		
		StringBuffer m_ReqHeaders	= null;
		StringBuffer m_ReqPayload	= null;
		
		StringBuffer m_ResHeaders	= null;
		
		JSONObject m_JSONRequest		= null;	
		JSONObject m_JSONResponse	= null;	
		
		public RequestInfo() {}
		public RequestInfo (int requestId, String requestName)
		{
			m_requestLogID		= requestId;
			m_requestName		= requestName;
			m_requestTimeStart	= System.currentTimeMillis();
			
			m_ReqHeaders		= new StringBuffer();
			m_ReqPayload		= new StringBuffer();
			
			m_ResHeaders		= new StringBuffer();
			
			m_JSONRequest = new JSONObject();
			m_JSONResponse = new JSONObject();
		}
		
		public RequestInfo (int requestId, String requestName, String url, String methodType)
		{
			this(requestId, requestName);
			m_url			= url;
			m_methodType	= methodType;
		}
		
		public void setUrl (String url)
		{
			m_url = url;
		}
		
		private boolean IsValidRequest ()
		{
			if (m_requestLogID == 0)
			{
				ERR(TAG, "Trying to use invalid request");
			}
			return (m_requestLogID > 0);
		}
		
		public void setMethodType (String methodType)
		{
			m_methodType = methodType;
		}
		
		public void ComputeTimeElapsed ()
		{
			if (!IsValidRequest()) return;
			
			if (m_requestTimeWait == 0)
			{
				m_requestTimeEnd 	= System.currentTimeMillis();
				m_requestTimeWait	= ((double)(m_requestTimeEnd - m_requestTimeStart)) / 1000.0f;
			}
		}
		
		public double GetTimeElapsed ()
		{
			return m_requestTimeWait;
		}
		
		public long GetRequestId ()
		{
			return m_requestLogID;
		}
		
		public String GetRequestName ()
		{
			return m_requestName;
		}
		
		public void AppendParamsRequestHeaders (String paramName, String paramValue)
		{
			AppendParams(m_ReqHeaders, paramName, paramValue);
		}
		
		public void AppendParamsRequestHeaders (String value)
		{
			AppendParams(m_ReqHeaders, value);
		}
		
		public void AppendParamsRequestPayload (String paramName, String paramValue)
		{
			AppendParams(m_ReqPayload, paramName, paramValue);
		}
		
		public void AppendParamsRequestPayload (String value)
		{
			AppendParams(m_ReqPayload, value);
		}
		
		public void AppendParamsResponseHeaders (String paramName, String paramValue)
		{
			AppendParams(m_ResHeaders, paramName, paramValue);
		}
		
		private void AppendParams (StringBuffer result, String paramName, String paramValue)
		{
			if (!IsValidRequest()) return;
			if (TextUtils.isEmpty(paramName) || TextUtils.isEmpty(paramValue) )
				return;
				
			if (result.length() > 0)
				result.append("&");
				
			result.append(paramName);
			result.append("=");
			result.append(paramValue);
		}
		
		public void AppendParams (StringBuffer result, String value)
		{
			if (!IsValidRequest()) return;
			if (TextUtils.isEmpty(value))
				return;
				
			if (result.length() > 0)
				result.append("&");
				
			result.append(value);
		}
		
		public void GenerateRequestJSONLog ()
		{
			if (!IsValidRequest()) return;
			try
			{
				m_JSONRequest.put("requestID", m_requestLogID);
				m_JSONRequest.put("url", m_url);
				m_JSONRequest.put("payload", m_ReqPayload.toString());
				m_JSONRequest.put("methodType", m_methodType);
				m_JSONRequest.put("headers", m_ReqHeaders.toString());
				m_JSONRequest.put("requestType", m_requestName);
			}
			catch(Exception e) { DBG_EXCEPTION(e);}
		}
		
		public String GetRequestJSONData ()
		{
			if (!IsValidRequest()) return "";
			return m_JSONRequest.toString();
		}
		
		public void GenerateResponseJSONLog (String data)
		{
			try
			{
				m_JSONResponse.put("requestID", m_requestLogID);
				m_JSONResponse.put("raw_response", data);
				m_JSONResponse.put("server_headers", m_ResHeaders.toString());
				m_JSONResponse.put("response_type", m_requestName);
			}
			catch(Exception e) { DBG_EXCEPTION(e);}
		}
		
		public String GetResponseJSONData ()
		{
			if (!IsValidRequest()) return "";
			return m_JSONResponse.toString();
		}
	}
}
