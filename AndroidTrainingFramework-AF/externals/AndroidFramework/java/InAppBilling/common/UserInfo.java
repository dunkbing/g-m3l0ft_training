#if USE_IN_APP_BILLING || USE_BILLING
package APP_PACKAGE.billing.common;

public class UserInfo {
	
	private String password = null;
	private String cardHolder = null;
	private String cardNumber1 = null;
	private String cardNumber2 = null;
	private String cardNumber3 = null;
	private String cardNumber4 = null;
	private String expirationDateMonth = null;
	private String expirationDateYear = null;
	private String securityCode = null;
	private String email = null;
	
	#if SHENZHOUFU_STORE
	private String cardType = null;
	private String cardDenomination = null;
	private String cardNumber = null;
	private String cardPassword = null;

	/**
	 * @return the cardType
	 */
	public String getCardType() {
		return cardType;
	}
	/**
	 * @param cardType the cardType to set
	 */
	public void setCardType(String cardType) {
		this.cardType = cardType;
	}
	/**
	 * @return the cardDenomination
	 */
	public String getCardDenomination() {
		return cardDenomination;
	}
	/**
	 * @param cardDenomination the cardDenomination to set
	 */
	public void setCardDenomination(String cardDenomination) {
		this.cardDenomination = cardDenomination;
	}
	/**
	 * @return the cardNumber
	 */
	public String getShenzhoufuCardNumber() {
		return cardNumber;
	}
	/**
	 * @param cardNumber the cardNumber to set
	 */
	public void setShenzhoufuCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}
	/**
	 * @return the cardPassword
	 */
	public String getCardPassword() {
		return cardPassword;
	}
	/**
	 * @param cardPassword the cardPassword to set
	 */
	public void setCardPassword(String cardPassword) {
		this.cardPassword = cardPassword;
	}
	
	/**
	 * @return true if the card password is valid
	 */
	public boolean isValidShenzhoufuCardPassword() {
		return (cardPassword != null && cardPassword.length() > 3);
	}
	/**
	 * @return true if the credit card number has all the data filled it.
	 */
	public boolean isValidSehnzhoufuCardNumber() {
		if (getShenzhoufuCardNumber() == null || getShenzhoufuCardNumber().length() < 3)//check the size wanted for card number
			return false;
		return true;
	}
#endif

	
	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * @return true if the email has a proper structure <code>mailbox@domainName</code>
	  */
	public boolean isValidEmail() {
		if (email == null)
			return false;
		String parts [] = email.split("\\@");
		return (parts.length == 2);
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @return true if the password is valid
	 */
	public boolean isValidPassword() {
		return (password != null && password.length() > 3);
	}
	/**
	 * @return the cardHolder
	 */
	public String getCardHolder() {
		return cardHolder;
	}
	/**
	 * @param cardHolder the cardHolder to set
	 */
	public void setCardHolder(String cardHolder) {
		this.cardHolder = cardHolder;
	}
	/**
	 * @return true if the CardHolder is valid
	 */
	public boolean isValidCardHolder() {
		return (cardHolder != null && cardHolder.length() > 3);
	}
	/**
	 * @return the cardNumber String
	 */
	public String getCardNumber() {
		if (cardNumber1 == null || cardNumber2 == null || cardNumber3 == null || cardNumber4 == null)
			return null;
		return cardNumber1+cardNumber2+cardNumber3+cardNumber4;
	}
	
	public String getLastCardNumbers() {
		if (cardNumber4 == null)
			return null;
		return cardNumber4;
	}
	/**
	 * @return the cardNumber1
	 */
	public String getCardNumber1() {
		return cardNumber1;
	}
	/**
	 * @param cardNumber1 the cardNumber1 to set
	 */
	public void setCardNumber1(String cardNumber1) {
		this.cardNumber1 = cardNumber1;
	}
	/**
	 * @return the cardNumber1
	 */
	public String getCardNumber2() {
		return cardNumber2;
	}
	/**
	 * @param cardNumber2 the cardNumber2 to set
	 */
	public void setCardNumber2(String cardNumber2) {
		this.cardNumber2 = cardNumber2;
	}
	/**
	 * @return the cardNumber3
	 */
	public String getCardNumber3() {
		return cardNumber3;
	}
	/**
	 * @param cardNumber3 the cardNumber3 to set
	 */
	public void setCardNumber3(String cardNumber3) {
		this.cardNumber3 = cardNumber3;
	}
	/**
	 * @return the cardNumber4
	 */
	public String getCardNumber4() {
		return cardNumber4;
	}
	/**
	 * @param cardNumber4 the cardNumber4 to set
	 */
	public void setCardNumber4(String cardNumber4) {
		this.cardNumber4 = cardNumber4;
	}
	/**
	 * @return true if the credit card number has all the data filled it.
	 */
	public boolean isValidCardNumber() {
		if (getCardNumber() == null || getCardNumber().length() < 16)
			return false;
		return true;
	}
	/**
	 * @return the expirationDateMonth
	 */
	public String getExpirationDateMonth() {
		return expirationDateMonth;
	}
	/**
	 * @param expirationDateMonth the expirationDateMonth to set
	 */
	public void setExpirationDateMonth(String expirationDateMonth) {
		this.expirationDateMonth = expirationDateMonth;
	}
	/**
	 * @return the expirationDate
	 */
	public String getExpirationDate() {
		if (expirationDateMonth == null || expirationDateYear == null || expirationDateYear.length() < 4)
			return null;
		return expirationDateMonth+expirationDateYear.substring(2, 4);
	}
	/**
	 * @return the expirationDateYear
	 */
	public String getExpirationDateYear() {
		return expirationDateYear;
	}
	/**
	 * @param expirationDateYear the expirationDateYear to set
	 */
	public void setExpirationDateYear(String expirationDateYear) {
		this.expirationDateYear = expirationDateYear;
	}
	/**
	 * @return true if the Expiration Date is valid
	 */
	public boolean isValidExpirationDate() {
		if (getExpirationDate() == null || getExpirationDate().length() < 4)
			return false;
		return true;
	}
	/**
	 * @return the securityCode
	 */
	public String getSecurityCode() {
		return securityCode;
	}
	/**
	 * @param securityCode the securityCode to set
	 */
	public void setSecurityCode(String securityCode) {
		this.securityCode = securityCode;
	}
	/**
	 * @return true if the security code is valid
	 */
	public boolean isValidSecurityCode() {
		if (this.securityCode == null || securityCode.length() < 3)
			return false;
		return true;
	}
	/**
	 * @return true if the credit card has all the fields filled properly (Card number, date, secure code)
	 */
	public boolean isValidCreditCard() {
		return (isValidCardNumber() && isValidExpirationDate() && isValidSecurityCode());
	}
	/**
	 * @return true if all the info filled in the form is valid
	 */
	public boolean isValidFormInfo (){
		return (isValidCreditCard() && this.isValidEmail() && isValidCardHolder() && isValidPassword());
	}
	/**
	 * @return true if all the info filled in the form is valid
	 */
	public boolean isValidFormInfoNoPassword (){
		return (isValidCreditCard() && this.isValidEmail() && isValidCardHolder());
	}
}
#endif//#if USE_IN_APP_BILLING || USE_BILLING