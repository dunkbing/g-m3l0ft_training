package APP_PACKAGE.iab;

/**
 * Represents the result of an in-app billing operation.
 * A result is composed of a response code (an integer) and possibly a
 * message (String). You can get those by calling
 * {@link #getResponse} and {@link #getMessage()}, respectively. You
 * can also inquire whether a result is a success or a failure by
 * calling {@link #isSuccess()} and {@link #isFailure()}.
*/
public class GMPResult {
	int mResponse;
	String mMessage;

	public GMPResult(int response, String message) 
	{
		mResponse = response;
        if (message == null || message.trim().length() == 0) {
            mMessage = GMPUtils.getInstance().getResponseDesc(response);
        }
        else {
            mMessage = message + " (response: " + GMPUtils.getInstance().getResponseDesc(response) + ")";
        }
	}
	
	public int getResponse() { return mResponse; }
	public String getMessage() { return mMessage; }
	public boolean isSuccess() { return mResponse == GMPUtils.BILLING_RESPONSE_RESULT_OK; }
	public boolean isFailure() { return !isSuccess(); }
	public boolean isOwned()   { return mResponse == GMPUtils.BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED; }
	public String toString() { return "GMPResult: " + getMessage(); }
}