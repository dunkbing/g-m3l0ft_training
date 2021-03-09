#if USE_IN_APP_BILLING || USE_BILLING
package APP_PACKAGE.billing.common;

/**
 * Exception thrown when encountering an invalid Base64 input character.
 *
 * @author nelson
 */
public class Base64DecoderException extends Exception {
  public Base64DecoderException() {
    super();
  }

  public Base64DecoderException(String s) {
    super(s);
  }

  private static final long serialVersionUID = 1L;
}
#endif //#if USE_IN_APP_BILLING || USE_BILLING
