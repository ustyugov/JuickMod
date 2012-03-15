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

import net.ustyugov.actionbar.ActionBarActivity;
import net.ustyugov.juick.R;
import net.ustyugov.juick.fragments.TagsFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

/**
 *
 * @author Ugnich Anton
 */
public class TagsActivity extends ActionBarActivity implements TagsFragment.TagsFragmentListener {

    private int uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDisplayHomeAsUpEnabled(true);

        uid = getIntent().getIntExtra("uid", 0);

        if (uid == 0) {
            setTitle(R.string.Popular_tags);
        }

        setContentView(R.layout.tags);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    	View view = findViewById(R.id.background);
    	view.setBackgroundColor(sp.getBoolean("DarkColors", false) ? 0xFF000000 : 0xFFFFFFFF);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        TagsFragment tf = new TagsFragment();
        Bundle args = new Bundle();
        args.putInt("uid", uid);
        tf.setArguments(args);
        ft.add(R.id.tagsfragment, tf);
        ft.commit();
    }

    public void onTagClick(String tag) {
        Intent i = new Intent();
        i.putExtra("tag", tag);
        setResult(RESULT_OK, i);
        finish();
    }

    public void onTagLongClick(String tag) {
        Intent i = new Intent(this, MessagesActivity.class);
        i.putExtra("tag", tag);
        i.putExtra("uid", uid);
        startActivity(i);
    }
}
