/*
 * Copyright 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ustyugov.actionbar;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;

public class ActionBarHelperHoneycomb extends ActionBarHelper {
    protected ActionBarHelperHoneycomb(Activity activity) {
        super(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
		mActivity.setTheme(prefs.getBoolean("DarkColors", false) ? android.R.style.Theme_Holo : android.R.style.Theme_Holo_Light);

        ActionBar actionBar = mActivity.getActionBar();
        if (actionBar != null) actionBar.setHomeButtonEnabled(true);
    }
    
    @Override
    public void setDisplayHomeAsUpEnabled(boolean enabled) {
    	ActionBar actionBar = mActivity.getActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(enabled);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    protected Context getActionBarThemedContext() {
        return mActivity;
    }
    
    @Override
    protected void onTitleChanged(CharSequence title, int color) {
    	ActionBar actionBar = mActivity.getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }
    
    @Override
    protected void onSubTitleChanged(CharSequence subtitle) {
        ActionBar actionBar = mActivity.getActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(subtitle);
        }
    }
}
