#if USE_IN_APP_BILLING
package APP_PACKAGE.iab;

/**
 * Exception thrown when something went wrong with the iab.
 * An GMPException has an associated GMPResult (an error).
 * To get the GMPResult that caused this exception to be thrown,
 * call {@link #getResult()}.
 */
public class GMPException extends Exception {
    GMPResult mResult;

    public GMPException(GMPResult r) {
        this(r, null);
    }
    public GMPException(int response, String message) {
        this(new GMPResult(response, message));
    }
    public GMPException(GMPResult r, Exception cause) {
        super(r.getMessage(), cause);
        mResult = r;
    }
    public GMPException(int response, String message, Exception cause) {
        this(new GMPResult(response, message), cause);
    }

    /** Returns the GMPResult (error) for this exception */
    public GMPResult getResult() { return mResult; }
}
#endif //USE_IN_APP_BILLING