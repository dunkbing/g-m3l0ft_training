
package APP_PACKAGE.iab.pdu;

import java.util.List;

public class VerifyReceipt {
    public int status;
    public String detail;
    public String message;
    public int count;
    public List<Product> product;

    public static class Product {
        public String log_time;
        public String appid;
        public String product_id;
        public double charge_amount;
        public String tid;
        public String detail_pname;
        public String bp_info;
        public String tcash_flag;

        @Override
        public String toString() {
            StringBuffer b = new StringBuffer();
            b.append("log_time:" + log_time + "\n")
                    .append("appid:" + appid + "\n")
                    .append("product_id:" + product_id + "\n")
                    .append("charge_amount:" + charge_amount + "\n")
                    .append("tid:" + tid + "\n")
                    .append("detail_pname:" + detail_pname + "\n")
                    .append("bp_info:" + bp_info + "\n")
                    .append("tcash_flag:" + tcash_flag + "\n");
            return b.toString();
        }

    }

    @Override
    public String toString() {
        StringBuffer b = new StringBuffer("[VerifyReceipt]\n");
        b.append("status:" + status + "\n")
                .append("detail:" + detail + "\n")
                .append("message:" + message + "\n")
                .append("count:" + count + "\n");

        if (this.product != null) {
            for (Product p : this.product) {
                b.append("{prodcut}\n").append(p.toString()).append("\n");
            }
        }
        return b.toString();
    }

}
