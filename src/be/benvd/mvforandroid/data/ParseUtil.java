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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class ParseUtil {

	private static SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static Date getDateFromAPI(String dateString) {
		try {
			return apiFormat.parse(dateString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			Log.e("MVFA", "Exception in getDateFromAPI", e);
			return null;
		}
	}
}
