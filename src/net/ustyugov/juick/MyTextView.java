package net.ustyugov.juick;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.juick.android.api.JuickMessage;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Layout.Alignment;
import android.text.style.AlignmentSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class MyTextView  extends TextView {
	private JuickMessage msg;
	private String Replies;
	private Context context;
	TextLinkClickListener mListener;

	public MyTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public void setText(JuickMessage msg, boolean isFirst) {
		this.msg = msg;
		if (isFirst)
			setTextWithLinks(formatFirstMessageText(msg));
		else {
			if (msg.replies > 0)
				this.Replies = context.getString(R.string.Replies_) + " " + msg.replies;
			else
				this.Replies = context.getString(R.string.Reply);
			setTextWithLinks(formatMessageText(msg));
		}
	}
	
	public JuickMessage getMessage() {
		return msg;
	}
	
	public void setTextWithLinks(Spannable ssb) {
		ArrayList<Hyperlink> nameList = new ArrayList<Hyperlink>();
		ArrayList<Hyperlink> msgList = new ArrayList<Hyperlink>();
		ArrayList<Hyperlink> linkList = new ArrayList<Hyperlink>();
		ArrayList<Hyperlink> tagList = new ArrayList<Hyperlink>();
		getLinks(msgList, ssb, JuickMessagesAdapter.msgPattern);
		getLinks(nameList, ssb, JuickMessagesAdapter.usrPattern);
		getLinks(linkList, ssb, JuickMessagesAdapter.urlPattern);
		getLinks(tagList, ssb, JuickMessagesAdapter.tagPattern);

		for (Hyperlink link : nameList) {
			ssb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), link.start, link.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			ssb.setSpan(new StyleSpan(android.graphics.Typeface.SANS_SERIF.getStyle()), link.start, link.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			ssb.setSpan(link.span, link.start, link.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		for (Hyperlink link : tagList) {
			ssb.setSpan(new StyleSpan(android.graphics.Typeface.SANS_SERIF.getStyle()), link.start, link.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			ssb.setSpan(link.span, link.start, link.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		for (Hyperlink link : msgList) {
			ssb.setSpan(new StyleSpan(android.graphics.Typeface.SANS_SERIF.getStyle()), link.start, link.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			ssb.setSpan(link.span, link.start, link.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		for (Hyperlink link : linkList) {
			ssb.setSpan(new StyleSpan(android.graphics.Typeface.SANS_SERIF.getStyle()), link.start, link.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			ssb.setSpan(link.span, link.start, link.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		
		if (Replies != null) {
			int start = ssb.length() - Replies.length() - 1;
			int end = ssb.length();
			
			Hyperlink spec = new Hyperlink();
			spec.textSpan = Replies;
			spec.span = new InternalURLSpan(spec.textSpan.toString());
			spec.start = start;
			spec.end = end;
			ssb.setSpan(new StyleSpan(android.graphics.Typeface.SANS_SERIF.getStyle()), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			ssb.setSpan(spec.span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		setText(ssb);
	}

	public void setOnTextLinkClickListener(TextLinkClickListener newListener) {
		mListener = newListener;
	}

	private final void getLinks(ArrayList<Hyperlink> links, Spannable s, Pattern pattern) {
		if (pattern.equals(JuickMessagesAdapter.tagPattern)) {
			int end = s.toString().indexOf("\n");
			if (end != -1) s = new SpannableStringBuilder(s.subSequence(0, end));
		}
		
		Matcher m = pattern.matcher(s);

		while (m.find())
		{
			int start = m.start();
			int end = m.end();
        
			Hyperlink spec = new Hyperlink();

			spec.textSpan = s.subSequence(start, end);
			spec.span = new InternalURLSpan(spec.textSpan.toString());
			spec.start = start;
			spec.end = end;

			links.add(spec);
		}
	}
	
	private SpannableStringBuilder formatMessageText(JuickMessage jmsg) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        String name = '@' + jmsg.User.UName;
        String tags = jmsg.getTags() + " ";
        String txt = jmsg.Text;
        if (jmsg.Photo != null) {
            txt = jmsg.Photo + "\n" + txt;
        }
        if (jmsg.Video != null) {
            txt = jmsg.Video + "\n" + txt;
        }
        ssb.append(name + ' ' + tags + "\n" + txt);
        ssb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(0xFFC8934E), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(0xFF0000CC), name.length() + 1, name.length() + tags.length() + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        DateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm");
        df.setTimeZone(TimeZone.getDefault());
        String date = df.format(jmsg.Timestamp);
        ssb.append("\n" + date + " ");

        int padding = name.length() + 1 + tags.length() + 1 + txt.length() + 1;
        int end = padding + date.length() + 1;

        ssb.setSpan(new ForegroundColorSpan(0xFFAAAAAA), padding, padding + date.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        if (Replies != null) {
        	ssb.append("  " + Replies + " ");
        	end += 2 + Replies.length() + 1;
        }
        ssb.setSpan(new AlignmentSpan.Standard(Alignment.ALIGN_OPPOSITE), padding, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return ssb;
    }

    private SpannableStringBuilder formatFirstMessageText(JuickMessage jmsg) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        String tags = jmsg.getTags();
        if (tags.length() > 0) {
            tags += "\n";
        }
        String txt = jmsg.Text;
        if (jmsg.Photo != null) {
            txt = jmsg.Photo + "\n" + txt;
        }
        if (jmsg.Video != null) {
            txt = jmsg.Video + "\n" + txt;
        }
        ssb.append(tags + txt);

        return ssb;
    }

	public class InternalURLSpan extends ClickableSpan {
		private String clickedSpan;

		public InternalURLSpan (String clickedString) {
			clickedSpan = clickedString;
		}

		@Override
		public void onClick(View textView) {
			mListener.onTextLinkClick(MyTextView.this, clickedSpan);
		}
	}

	class Hyperlink {
		CharSequence textSpan;
		InternalURLSpan span;
		int start;
		int end;
	}
}
