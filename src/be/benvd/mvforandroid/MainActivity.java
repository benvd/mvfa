package be.benvd.mvforandroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {

	private MVDataService dataService;

	/**
	 * Called when the MVDataService is bound to / unbound from the Activity.
	 */
	private ServiceConnection onService = new ServiceConnection() {
		public void onServiceConnected(ComponentName className,
				IBinder rawBinder) {
			Log.d("MVFA", "Service bound to activity.");
			dataService = ((MVDataService.LocalBinder) rawBinder).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.d("MVFA", "Service disconnected.");
			dataService = null;
		}
	};

	/**
	 * Callback for the MVDataService.
	 */
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateTextView();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Binding the Activity to our MVDataService
		bindService(new Intent(this, MVDataService.class), onService,
				BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Unbind the Service
		unbindService(onService);
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(receiver, new IntentFilter(MVDataService.UPDATE));
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}

		return false;
	}

	private void updateTextView() {
		TextView text = (TextView) findViewById(R.id.text);
		text.setText(text.getText() + "\n" + dataService.getData());
	}
}