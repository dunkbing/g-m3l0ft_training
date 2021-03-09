package APP_PACKAGE.iab.helper;

import com.google.gson.Gson;
import APP_PACKAGE.iab.pdu.CommandRequest;
import APP_PACKAGE.iab.pdu.Response;
import APP_PACKAGE.iab.pdu.VerifyReceipt;

public class GsonConverter implements Converter {
	private final Gson mGson = new Gson();
	
	@Override
	public String toJson(CommandRequest r) {
		return mGson.toJson(r);
	}

	@Override
	public Response fromJson(String json) {		
		return mGson.fromJson(json, Response.class);
	}

    @Override
    public VerifyReceipt fromJson2VerifyReceipt(String json) { 
        return mGson.fromJson(json, VerifyReceipt.class);
    }

}
