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

import net.ustyugov.juick.R;
import net.ustyugov.simplemenu.SimpleMenu;
import net.ustyugov.simplemenu.SimpleMenuItem;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.InflateException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ActionBarHelperBase extends ActionBarHelper {
    private static final String MENU_RES_NAMESPACE = "http://schemas.android.com/apk/res/android";
    private static final String MENU_ATTR_ID = "id";
    private static final String MENU_ATTR_SHOW_AS_ACTION = "showAsAction";

    protected Set<Integer> mActionItemIds = new HashSet<Integer>();

    protected ActionBarHelperBase(Activity activity) {
        super(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
		mActivity.setTheme(prefs.getBoolean("DarkColors", false) ? R.style.AppThemeDark : R.style.AppThemeLight);
        mActivity.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        mActivity.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.actionbar_compat);
        setupActionBar();

        SimpleMenu menu = new SimpleMenu(mActivity);
        mActivity.onCreatePanelMenu(Window.FEATURE_OPTIONS_PANEL, menu);
        mActivity.onPrepareOptionsMenu(menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (mActionItemIds.contains(item.getItemId())) {
                addActionItemCompatFromMenuItem(item);
            }
        }
    }

    private void setupActionBar() {
        final ViewGroup actionBarCompat = getActionBarCompat();
        if (actionBarCompat == null) {
            return;
        }
        
     // Add Home button
        SimpleMenu tempMenu = new SimpleMenu(mActivity);
        SimpleMenuItem homeItem = new SimpleMenuItem(tempMenu, android.R.id.home, 0, "");
        homeItem.setIcon(R.drawable.ic_launcher);
        addActionItemCompatFromMenuItem(homeItem);
        
     // Add Title and SubTitle
        LinearLayout.LayoutParams springLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        springLayoutParams.weight = 1;
        
        LinearLayout layout = new LinearLayout(mActivity, null);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(springLayoutParams);
        
        TextView titleText = new TextView(mActivity, null, R.attr.actionbarCompatTitleStyle);
        titleText.setText(mActivity.getTitle());
        layout.addView(titleText);
        
        TextView subTitleText = new TextView(mActivity, null, R.attr.actionbarCompatSubTitleStyle);
        layout.addView(subTitleText);

        actionBarCompat.addView(layout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Hides on-screen action items from the options menu.
        for (Integer id : mActionItemIds) {
            menu.findItem(id).setVisible(false);
        }
        return true;
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        TextView titleView = (TextView) mActivity.findViewById(R.id.actionbar_compat_title);
        if (titleView != null) {
            titleView.setText(title);
        }
        updateTitles();
    }
    
    @Override
    protected void onSubTitleChanged(CharSequence subtitle) {
        TextView subTitleView = (TextView) mActivity.findViewById(R.id.actionbar_compat_sub_title);
        if (subTitleView != null) {
            subTitleView.setText(subtitle);
        }
        updateTitles();
    }
    
    private void updateTitles() {
    	TextView subTitleView = (TextView) mActivity.findViewById(R.id.actionbar_compat_sub_title);
        if (subTitleView != null) {
        	if (subTitleView.getText().length() < 1) subTitleView.setVisibility(View.GONE);
        	else subTitleView.setVisibility(View.VISIBLE);
        }
    }

    public MenuInflater getMenuInflater(MenuInflater superMenuInflater) {
        return new WrappedMenuInflater(mActivity, superMenuInflater);
    }

    private ViewGroup getActionBarCompat() {
        return (ViewGroup) mActivity.findViewById(R.id.actionbar_compat);
    }

    private View addActionItemCompatFromMenuItem(final MenuItem item) {
        final int itemId = item.getItemId();

        final ViewGroup actionBar = getActionBarCompat();
        if (actionBar == null) {
            return null;
        }

        // Create the button
        ImageButton actionButton = new ImageButton(mActivity, null,
                itemId == android.R.id.home
                        ? R.attr.actionbarCompatItemHomeStyle
                        : R.attr.actionbarCompatItemStyle);
        actionButton.setLayoutParams(new ViewGroup.LayoutParams(
                (int) mActivity.getResources().getDimension(
                        itemId == android.R.id.home
                                ? R.dimen.actionbar_compat_button_home_width
                                : R.dimen.actionbar_compat_button_width),
                ViewGroup.LayoutParams.FILL_PARENT));
        if (itemId == R.id.menu_refresh) {
            actionButton.setId(R.id.actionbar_compat_item_refresh);
        }
        actionButton.setImageDrawable(item.getIcon());
        actionButton.setScaleType(ImageView.ScaleType.CENTER);
        actionButton.setContentDescription(item.getTitle());
        actionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mActivity.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, item);
            }
        });

        actionBar.addView(actionButton);

        if (item.getItemId() == R.id.menu_refresh) {
            // Refresh buttons should be stateful, and allow for indeterminate progress indicators,
            // so add those.
            ProgressBar indicator = new ProgressBar(mActivity, null, R.attr.actionbarCompatProgressIndicatorStyle);

            final int buttonWidth = mActivity.getResources().getDimensionPixelSize(R.dimen.actionbar_compat_button_width);
            final int buttonHeight = mActivity.getResources().getDimensionPixelSize(R.dimen.actionbar_compat_height);
            final int progressIndicatorWidth = buttonWidth / 2;

            LinearLayout.LayoutParams indicatorLayoutParams = new LinearLayout.LayoutParams(progressIndicatorWidth, progressIndicatorWidth);
            indicatorLayoutParams.setMargins(
                    (buttonWidth - progressIndicatorWidth) / 2,
                    (buttonHeight - progressIndicatorWidth) / 2,
                    (buttonWidth - progressIndicatorWidth) / 2,
                    0);
            indicator.setLayoutParams(indicatorLayoutParams);
            indicator.setVisibility(View.GONE);
            indicator.setId(R.id.actionbar_compat_item_refresh_progress);
            actionBar.addView(indicator);
        }

        return actionButton;
    }

    private class WrappedMenuInflater extends MenuInflater {
        MenuInflater mInflater;

        public WrappedMenuInflater(Context context, MenuInflater inflater) {
            super(context);
            mInflater = inflater;
        }

        @Override
        public void inflate(int menuRes, Menu menu) {
            loadActionBarMetadata(menuRes);
            mInflater.inflate(menuRes, menu);
        }

        private void loadActionBarMetadata(int menuResId) {
            XmlResourceParser parser = null;
            try {
                parser = mActivity.getResources().getXml(menuResId);

                int eventType = parser.getEventType();
                int itemId;
                int showAsAction;

                boolean eof = false;
                while (!eof) {
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if (!parser.getName().equals("item")) {
                                break;
                            }

                            itemId = parser.getAttributeResourceValue(MENU_RES_NAMESPACE,
                                    MENU_ATTR_ID, 0);
                            if (itemId == 0) {
                                break;
                            }

                            showAsAction = parser.getAttributeIntValue(MENU_RES_NAMESPACE,
                                    MENU_ATTR_SHOW_AS_ACTION, -1);
                            if (showAsAction == MenuItem.SHOW_AS_ACTION_ALWAYS ||
                                    showAsAction == MenuItem.SHOW_AS_ACTION_IF_ROOM) {
                                mActionItemIds.add(itemId);
                            }
                            break;

                        case XmlPullParser.END_DOCUMENT:
                            eof = true;
                            break;
                    }

                    eventType = parser.next();
                }
            } catch (XmlPullParserException e) {
                throw new InflateException("Error inflating menu XML", e);
            } catch (IOException e) {
                throw new InflateException("Error inflating menu XML", e);
            } finally {
                if (parser != null) {
                    parser.close();
                }
            }
        }

    }

	@Override
	public void setDisplayHomeAsUpEnabled(boolean enabled) { }
}
