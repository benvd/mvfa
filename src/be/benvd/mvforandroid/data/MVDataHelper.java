/*
	Copyright (C) 2010 Ben Van Daele (vandaeleben@gmail.com)

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.benvd.mvforandroid.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class MVDataHelper {

	/**
	 * Returns the GET response of the given url.
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 * 
	 * @return The response of the given URL. If no response was found, null is returned.
	 */
	public static String getResponse(String username, String password, String url) throws ClientProtocolException,
			IOException {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.getCredentialsProvider().setCredentials(new AuthScope(null, -1),
				new UsernamePasswordCredentials(username + ":" + password));
		HttpGet httpget = new HttpGet(url);
		HttpResponse response = httpclient.execute(httpget);

		if (response.getEntity() != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuilder sb = new StringBuilder();

			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();

			return sb.toString();
		}

		return null;
	}

	/**
	 * Returns fake data.
	 */
	public static String getTestResponse(String username, String password, String url) throws ClientProtocolException,
			IOException {
		if (url.equals(MVDataService.URL_CREDIT)) {
			return "{\"valid_until\": \"2010-07-21 21:38:00\", \"sms\": " + new Random().nextInt(1000) + ", \"data\": "
					+ new Random().nextInt(1073741824)
					+ ", \"is_expired\": false, \"price_plan\": \"Classic\",  \"credits\": \""
					+ new Random().nextInt(15) + ".37\"}";
		} else if (url.equals(MVDataService.URL_USAGE)) {
			return "[    {        \"is_data\": true,         \"start_timestamp\": \"2010-07-17 11:21:16\",         \"balance\": \"8.100000\",         \"duration_call\": 9,         \"to\": \"web.be\",         \"is_sms\": false,         \"timestamp\": 1279358476,         \"price\": \"0.000000\",         \"duration_connection\": 3004,         \"duration_human\": \"50:04\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 11:21:25\"    },     {        \"is_data\": false,         \"start_timestamp\": \"2010-07-17 03:25:08\",         \"balance\": \"8.100000\",         \"duration_call\": 22,         \"to\": \"0498441877\",         \"is_sms\": false,         \"timestamp\": 1279329908,         \"price\": \"0.240000\",         \"duration_connection\": "
					+ new Random().nextInt(100)
					+ ",         \"duration_human\": \"0:11\",         \"is_incoming\": false,         \"is_voice\": true,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 03:25:30\"    },     {        \"is_data\": false,         \"start_timestamp\": \"2010-07-17 03:09:58\",         \"balance\": \"8.340000\",         \"duration_call\": 2,         \"to\": \"0498441877\",         \"is_sms\": true,         \"timestamp\": 1279328998,         \"price\": \"0.000000\",         \"duration_connection\": 1,         \"duration_human\": \"0:01\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 03:10:00\"    },     {        \"is_data\": false,         \"start_timestamp\": \"2010-07-17 03:05:03\",         \"balance\": \"8.340000\",         \"duration_call\": 1,         \"to\": \"0498441877\",         \"is_sms\": true,         \"timestamp\": 1279328703,         \"price\": \"0.000000\",         \"duration_connection\": 1,         \"duration_human\": \"0:01\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 03:05:04\"    },     {        \"is_data\": false,         \"start_timestamp\": \"2010-07-17 03:04:41\",         \"balance\": \"0.000000\",         \"duration_call\": 1,         \"to\": \"0498441877\",         \"is_sms\": true,         \"timestamp\": 1279328681,         \"price\": \"0.000000\",         \"duration_connection\": 1,         \"duration_human\": \"0:01\",         \"is_incoming\": true,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 03:04:42\"    },     {        \"is_data\": false,         \"start_timestamp\": \"2010-07-17 03:04:04\",         \"balance\": \"8.340000\",         \"duration_call\": 2,         \"to\": \"0498441877\",         \"is_sms\": true,         \"timestamp\": 1279328644,         \"price\": \"0.000000\",         \"duration_connection\": 1,         \"duration_human\": \"0:01\",         \"is_incoming\": false,         \"is_voice\": false,         \"is_mms\": false,         \"end_timestamp\": \"2010-07-17 03:04:06\"    }]";
		} else if (url.equals(MVDataService.URL_TOPUPS)) {
			return "[    {        \"status\": \"Top-up done\",         \"amount\": \""
					+ new Random().nextInt(40)
					+ "\",         \"amount_ex_vat\": \"12.40\",         \"executed_on\": \"2010-06-21 21:38:33\",         \"method\": \"Ogone\",         \"payment_received_on\": \"2010-06-21 21:38:32\"    },     {        \"status\": \"Top-up done\",         \"amount\": \"15.00\",         \"amount_ex_vat\": \"12.40\",         \"executed_on\": \"2010-05-01 11:28:36\",         \"method\": \"PayPal\",         \"payment_received_on\": \"2010-05-01 11:24:01\"    },     {        \"status\": \"Top-up done\",         \"amount\": \"15.00\",         \"amount_ex_vat\": \"12.40\",         \"executed_on\": \"2010-03-17 19:07:46\",         \"method\": \"PayPal\",         \"payment_received_on\": \"2010-03-17 19:07:41\"    },     {        \"status\": \"Top-up done\",         \"amount\": \"15.00\",         \"amount_ex_vat\": \"12.40\",         \"executed_on\": \"2010-02-15 12:42:52\",         \"method\": \"PayPal\",         \"payment_received_on\": \"2010-02-15 12:42:40\"    },     {        \"status\": \"Top-up done\",         \"amount\": \"15.00\",         \"amount_ex_vat\": \"12.40\",         \"executed_on\": \"2010-01-07 14:49:00\",         \"method\": \"PayPal\",         \"payment_received_on\": \"2010-01-07 14:48:51\"    },     {        \"status\": \"Top-up done\",         \"amount\": \"15.00\",         \"amount_ex_vat\": \"12.40\",         \"executed_on\": \"2009-12-04 00:45:43\",         \"method\": \"PayPal\",         \"payment_received_on\": \"2009-12-04 00:45:30\"    },     {        \"status\": \"Top-up done\",         \"amount\": \"15.00\",         \"amount_ex_vat\": \"12.40\",         \"executed_on\": \"2009-10-28 10:23:30\",         \"method\": \"PayPal\",         \"payment_received_on\": \"2009-10-28 10:23:17\"    },     {        \"status\": \"Top-up done\",         \"amount\": \"15.00\",         \"amount_ex_vat\": \"12.40\",         \"executed_on\": \"2009-09-28 08:03:00\",         \"method\": \"PayPal\",         \"payment_received_on\": \"2009-09-28 08:03:00\"    },     {        \"status\": \"Top-up done\",         \"amount\": \"15.00\",         \"amount_ex_vat\": \"12.40\",         \"executed_on\": \"2009-08-27 22:35:39\",         \"method\": \"PayPal\",         \"payment_received_on\": \"2009-08-27 22:35:39\"    },     {        \"status\": \"Top-up done\",         \"amount\": \"15.00\",         \"amount_ex_vat\": \"12.40\",         \"executed_on\": \"2009-07-27 13:17:45\",         \"method\": \"PayPal\",         \"payment_received_on\": \"2009-07-27 13:17:45\"    }]";
		} else {
			return "";
		}
	}

}
