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
package net.ustyugov.juick;

import android.net.Uri;
import com.juick.android.api.JuickMessage;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.regex.Pattern;

import net.ustyugov.juick.activitys.MessagesActivity;
import net.ustyugov.juick.activitys.ThreadActivity;

import org.json.JSONArray;

/**
 *
 * @author Ugnich Anton
 */
public class JuickMessagesAdapter extends ArrayAdapter<JuickMessage> implements TextLinkClickListener {

    public static final int TYPE_THREAD = 1;
    public static Pattern urlPattern = Pattern.compile("((?<=\\A)|(?<=\\s))(ht|f)tps?://[a-z0-9\\-\\.]+[a-z]{2,}/?[^\\s\\n]*", Pattern.CASE_INSENSITIVE);
    public static Pattern msgPattern = Pattern.compile("#[0-9]+");
    public static Pattern tagPattern = Pattern.compile("\\*[\\w\\?!-.@_|]+");
    public static Pattern usrPattern = Pattern.compile("@[a-zA-Z0-9\\-]{2,16}");
    
    private Context context;
    private int type;
//    private boolean allItemsEnabled = true;

    public JuickMessagesAdapter(Context context, int type) {
        super(context, R.layout.listitem_juickmessage);
        this.context = context;
        this.type = type;
    }

    public int parseJSON(String jsonStr) {
        try {
            JSONArray json = new JSONArray(jsonStr);
            int cnt = json.length();
            for (int i = 0; i < cnt; i++) {
                add(JuickMessage.parseJSON(json.getJSONObject(i)));
            }
            return cnt;
        } catch (Exception e) {
            Log.e("initOpinionsAdapter", e.toString());
        }

        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        JuickMessage jmsg = getItem(position);
        View v = convertView;

        if (jmsg.User != null && jmsg.Text != null) {
            if (v == null || !(v instanceof LinearLayout)) {
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.listitem_juickmessage, null);
            }
            
            MyTextView t = (MyTextView) v.findViewById(R.id.text);
            if (type == TYPE_THREAD && jmsg.RID == 0) {
                t.setText(jmsg, true);
            } else {
                t.setText(jmsg, false);
            }
            MovementMethod m = t.getMovementMethod();
            if ((m == null) || !(m instanceof LinkMovementMethod)) {
                if (t.getLinksClickable()) {
                    t.setMovementMethod(LinkMovementMethod.getInstance());
                }
            }
            t.setOnTextLinkClickListener(this);

            /*
            ImageView i = (ImageView) v.findViewById(R.id.icon);
            if (jmsg.User != null && jmsg.User.Avatar != null) {
            i.setImageDrawable((Drawable) jmsg.User.Avatar);
            } else {
            i.setImageResource(R.drawable.ic_user_32);
            }
             */

        } else {
            if (v == null || !(v instanceof TextView)) {
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.preference_category, null);
            }

            if (jmsg.Text != null) {
                ((TextView) v).setText(jmsg.Text);
            } else {
                ((TextView) v).setText("");
            }
        }

        return v;
    }

    @Override
    public boolean isEnabled(int position) {
        JuickMessage jmsg = getItem(position);
        return (jmsg != null && jmsg.User != null && jmsg.MID > 0);
    }

    public void addDisabledItem(String txt, int position) {
//        allItemsEnabled = false;
        JuickMessage jmsg = new JuickMessage();
        jmsg.Text = txt;
        insert(jmsg, position);
    }

    @Override
    public void onTextLinkClick(MyTextView t, String s) {
    	JuickMessage jmsg = t.getMessage();
		if (s.length() > 1) {
			if (s.startsWith("@")) {
				Intent i = new Intent(context, MessagesActivity.class);
                i.putExtra("uid", jmsg.User.UID);
                i.putExtra("uname", s.substring(1));
                context.startActivity(i);
			} else if (s.startsWith("#")) {
				try {
					int mid = Integer.parseInt(s.substring(1));
					Intent intent = new Intent(context, ThreadActivity.class);
	                intent.putExtra("mid", mid);
	                context.startActivity(intent);
				} catch (NumberFormatException nfe) { }
			} else if (s.startsWith("*")) {
				Intent i = new Intent(context, MessagesActivity.class);
		        i.putExtra("tag", s.substring(1));
		        context.startActivity(i);
			} else {
				try{
					Uri uri = Uri.parse(s);
					if (uri != null) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(uri);
						context.startActivity(intent);
					}
				} catch (ActivityNotFoundException e) {
					if (type == TYPE_THREAD) {
						Intent i = new Intent("com.juick.android.REPLY");
						i.putExtra("rid", jmsg.RID);
						i.putExtra("text", jmsg.Text);
						context.sendBroadcast(i);
					} else {
						Intent i = new Intent(context, ThreadActivity.class);
						i.putExtra("mid", jmsg.MID);
						context.startActivity(i);
					}
				}
			}
		}
	}
}
