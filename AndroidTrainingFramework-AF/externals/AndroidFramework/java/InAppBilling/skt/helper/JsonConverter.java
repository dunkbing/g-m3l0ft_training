
package APP_PACKAGE.iab.helper;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import APP_PACKAGE.iab.pdu.CommandRequest;
import APP_PACKAGE.iab.pdu.Response;
import APP_PACKAGE.iab.pdu.Response.Status;
import APP_PACKAGE.iab.pdu.VerifyReceipt;

public class JsonConverter implements Converter {

    @Override
    public String toJson(CommandRequest r) {
        JSONObject reqJson = new JSONObject();
        JSONObject param = new JSONObject();
        JSONArray ids = new JSONArray();
        String result = null;
        try {
            reqJson.put("method", r.method);
            param.put("appid", r.param.appid);
            for (String id : r.param.product_id) {
                ids.put(id);
            }
            param.put("product_id", ids);

            if (r.param.action != null) {
                param.put("action", r.param.action);
            }

            reqJson.put("param", param);

            result = reqJson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            return result;
        }
    }

    @Override
    public Response fromJson(String json) {
        Response response = null;
        JSONObject obj = null;
        JSONObject robj = null;
        List<Response.Product> products = null;
        Response.Result result = null;
        int count = 0;
        try {
            obj = new JSONObject(json);
            final String method = obj.getString("method");
            if (!obj.has("result")) { 
                return null;
            }
            
            robj = obj.getJSONObject("result");
            count = (robj.has("count") ? robj.getInt("count") : 0);

            if (count > 0) {
                products = new ArrayList<Response.Product>(count);
                JSONArray ar = robj.getJSONArray("product");
                for (int i = 0; i < count; i++) {
                    JSONObject o = ar.getJSONObject(i);
                    Response.Product p = new Response.Product();
                    p.appid = o.has("appid") ? o.getString("appid") : null;
                    p.endDate = o.has("endDate") ? o.getString("endDate") : null;
                    p.id = o.has("id") ? o.getString("id") : null;
                    p.kind = o.has("kind") ? o.getString("kind") : null;
                    p.name = o.has("name") ? o.getString("name") : null;
                    p.price = o.has("price") ? o.getDouble("price") : null;
                    p.purchasability = false;
                    if (o.has("purchasability")) {
                        p.purchasability = o.getBoolean("purchasability") ;
                    }
                    p.startDate = o.has("startDate") ? o.getString("startDate") : null;
                    if (o.has("status")) {
                        p.status = new Status(o.getString("code"), o.getString("message"));
                    }
                    p.type = o.has("type") ? o.getString("type") : null;
                    p.validity =  o.has("validity") ? o.getInt("validity") : -1;
                    products.add(p);
                }
            }

            result = new Response.Result(
                    (robj.has("code")? robj.getString("code") : null),
                    (robj.has("message") ? robj.getString("message") : null), 
                    (robj.has("txid") ? robj.getString("txid") : null),
                    (robj.has("receipt") ? robj.getString("receipt") : null), 
                    count, products);

            response = new Response(obj.getString("api_version"),
                    obj.getString("identifier"), method, result);

        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            return response;
        }
    }

    @Override
    public VerifyReceipt fromJson2VerifyReceipt(String json) {
        VerifyReceipt data = new VerifyReceipt();
        JSONObject obj;        
        try {
            
            obj = new JSONObject(json);
            data.status = obj.getInt("status");
            data.detail = obj.getString("detail");
            data.message = obj.getString("message");
            data.count = obj.getInt("count");
            
            if (data.count > 0) {
                data.product = new ArrayList<VerifyReceipt.Product>(data.count);
                JSONArray ar = obj.getJSONArray("product");
                for (int i=0, count=data.count; i < count; i++) {
                    JSONObject o = ar.getJSONObject(i);
                    VerifyReceipt.Product p = new VerifyReceipt.Product();
                    p.log_time = o.getString("log_time");
                    p.appid = o.getString("appid");
                    p.product_id = o.getString("product_id");
                    p.charge_amount = o.getDouble("charge_amount");
                    p.tid = o.getString("tid");
                    p.detail_pname = o.getString("detail_pname");
                    p.bp_info = o.getString("bp_info");
                    p.tcash_flag = o.getString("tcash_flag");
                    data.product.add(p);                    
                }                               
            }
            
        } catch (Exception e) {
            
        } finally {
            return data;
        }
    }
    
    
}
