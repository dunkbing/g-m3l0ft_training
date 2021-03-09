package APP_PACKAGE.iab.helper;

import APP_PACKAGE.iab.pdu.CommandRequest;
import APP_PACKAGE.iab.pdu.Response;
import APP_PACKAGE.iab.pdu.VerifyReceipt;

public interface Converter {
    public String toJson(final CommandRequest r);

    public Response fromJson(final String json);

    public VerifyReceipt fromJson2VerifyReceipt(final String json);
}
