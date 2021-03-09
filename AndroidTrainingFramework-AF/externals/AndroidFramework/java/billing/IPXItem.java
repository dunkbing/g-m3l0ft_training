package APP_PACKAGE.billing;

#if USE_BILLING_FOR_CHINA
public class IPXItem {

	private String shortCode;

	private String keyword;

	private int price;

	private int count;

	public String getShortCode() {
		return shortCode;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
#endif
