package APP_PACKAGE.iab.pdu;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;


//@see http://code.google.com/p/google-gson/
public class CommandRequest {
	public final String method;
	public final Parameter param;

	private CommandRequest(final String method, final Parameter param) {
		this.method = method;
		this.param = param;
	}

	public static CommandRequest makeRequest(final String method,
			final Parameter param) {
		return new CommandRequest(method, param);
	}

	public static class Parameter {
		public final String appid;
		public final List<String> product_id = new ArrayList<String>();
		public final String action;

		private Parameter(final String appid, String... pids) {
            this.appid = appid;
            this.action = null;
            if (pids != null) {
                for (String arg : pids) {
                    this.product_id.add(arg);
                }
            }
			
		}

		private Parameter(final String appid, final String action,
				String... pids) {
            this.appid = appid;
            this.action = action;
            if (pids != null) {
                for (String arg : pids) {
                    this.product_id.add(arg);
                }
            }
		}

		public static Parameter makeParam(final String appid, String... pids) {
			return new Parameter(appid, pids);
		}

		public static Parameter makeParam(final String appid,
				final String action, String pids[]) {
			return new Parameter(appid, action, pids);
		}
	}
}
