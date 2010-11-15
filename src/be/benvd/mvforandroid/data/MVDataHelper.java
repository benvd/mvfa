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

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class MVDataHelper {

	public static final String PRICE_PLAN_TOPUP_AMOUNT = "price_plan_topup_amount";
	public static final String PRICE_PLAN_DATA_AMOUNT = "price_plan_data_amount";
	public static final String PRICE_PLAN_SMS_AMOUNT = "price_plan_sms_amount";
	public static final String PRICE_PLAN_NAME = "price_plan_name";

	/**
	 * Returns the GET response of the given url.
	 * 
	 * @throws IOException
	 * @return The response of the given URL. If no response was found, null is returned.
	 */
	public static String getResponse(String username, String password, String url) throws IOException {
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

}
