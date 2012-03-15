/*
 * Juick
 * Copyright (C) 2008-2012, Ugnich Anton
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.ustyugov.juick.activitys;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.*;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.Calendar;
import java.util.List;
import net.ustyugov.actionbar.ActionBarActivity;
import net.ustyugov.juick.CheckUpdatesReceiver;
import net.ustyugov.juick.R;
import net.ustyugov.juick.Utils;
import net.ustyugov.juick.fragments.MessagesFragment;

/**
 *
 * @author Ugnich Anton
 */
public class MainActivity extends ActionBarActivity {
    public static final int ACTIVITY_SIGNIN = 2;
    public static final int ACTIVITY_PREFERENCES = 3;
    public static final int PENDINGINTENT_CONSTANT = 713242183;
    private SharedPreferences prefs;
    private String[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	items = getResources().getStringArray(R.array.messagesLists);
    	
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            Uri uri = intent.getData();
            if (uri != null && uri.getPathSegments().size() > 0 && parseUri(uri)) {
                return;
            }
        }

        if (!Utils.hasAuth(this)) {
            startActivityForResult(new Intent(this, SignInActivity.class), ACTIVITY_SIGNIN);
            return;
        }

        startCheckUpdates(this);
        setContentView(R.layout.messages);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    	View view = findViewById(R.id.background);
    	view.setBackgroundColor(sp.getBoolean("DarkColors", false) ? 0xFF000000 : 0xFFFFFFFF);
    	
    	update();
    }

    public static void startCheckUpdates(Context context) {
        Intent intent = new Intent(context, CheckUpdatesReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, PENDINGINTENT_CONSTANT, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int interval = Integer.parseInt(sp.getString("refresh", "5"));
        if (interval > 0) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, 5);
            am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), interval * 60000, sender);
        } else {
            am.cancel(sender);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTIVITY_SIGNIN) {
            if (resultCode == RESULT_OK) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            } else {
                finish();
            }
        } else if (requestCode == ACTIVITY_PREFERENCES) {
            if (resultCode == RESULT_OK) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case android.R.id.home:
        		createDialog();
        		return true;
            case R.id.menuitem_preferences:
                startActivityForResult(new Intent(this, PreferencesActivity.class), ACTIVITY_PREFERENCES);
                return true;
            case R.id.menuitem_newmessage:
                startActivity(new Intent(this, NewMessageActivity.class));
                return true;
            case R.id.menuitem_search:
                startActivity(new Intent(this, ExploreActivity.class));
                return true;
            case R.id.menuitem_refresh:
            	update();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void update() {
    	if (Utils.hasAuth(this)) {
    		int mode = prefs.getInt("ViewMode", 0);
        	setTitle(items[mode]);
            
            Bundle args = new Bundle();
            if (mode == 0) {
                args.putBoolean("home", true);
            } else if (mode == 2) {
                args.putBoolean("popular", true);
            } else if (mode == 3) {
                args.putBoolean("media", true);
            }
            
            MessagesFragment mf = new MessagesFragment();
            mf.setArguments(args);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.messagesfragment, mf);
            ft.commit();
        }
    }

    private boolean parseUri(Uri uri) {
        List<String> segs = uri.getPathSegments();
        if ((segs.size() == 1 && segs.get(0).matches("\\A[0-9]+\\z"))
                || (segs.size() == 2 && segs.get(1).matches("\\A[0-9]+\\z") && !segs.get(0).equals("places"))) {
            int mid = Integer.parseInt(segs.get(segs.size() - 1));
            if (mid > 0) {
                finish();
                Intent intent = new Intent(this, ThreadActivity.class);
                intent.setData(null);
                intent.putExtra("mid", mid);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            }
        } else if (segs.size() == 1 && segs.get(0).matches("\\A[a-zA-Z0-9\\-]+\\z")) {
            //TODO show user
        }
        return false;
    }
    
    private void createDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setItems(items, new DialogInterface.OnClickListener() {
    	    public void onClick(DialogInterface dialog, int item) {
    	        SharedPreferences.Editor editor = prefs.edit();
    	        editor.putInt("ViewMode", item);
    	        editor.commit();
    	        update();
    	    }
    	});
    	AlertDialog alert = builder.create();
    	alert.show();
    }
}
