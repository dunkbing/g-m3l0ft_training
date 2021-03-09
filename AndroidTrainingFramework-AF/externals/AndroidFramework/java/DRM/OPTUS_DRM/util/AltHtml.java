#if USE_OPTUS_DRM
package com.msap.store.drm.android.util;

import android.text.*;
import android.text.style.*;
import android.view.*;
import android.util.*;

/**
 * This class wraps the default HTML to spanned text converter to enable
 * developers to intercept a link opening.
 *
 * The default Html class can parse some HTML text into the corresponding 
 * spanned text, but whenever the links on it is clicked, the Android 
 * system will open the link directly. There are no way for the apps to 
 * intercept these events directly using Html class.
 *
 * Therefore, this class is written to postprocess any parsed output from
 * Html class so that link clicking events are handled by a given handler.
 *
 * @author Edison Chan
 */
public class AltHtml {
	/**
	 * Return displayable style text from the provided HTML string. However, when
	 * a link in the result is clicked, the listener is invoked, instead of opening
	 * the URL directly.
	 * @param source HTML string to be converted.
	 * @return spanned text for display.
	 */
	public static Spanned fromHtml(String source, OnClickListener listener) {
		return adaptLink(Html.fromHtml(source), listener);
	}

	/**
	 * Return displayable style text from the provided HTML string. However, when
	 * a link in the result is clicked, the listener is invoked, instead of opening
	 * the URL directly.
	 * @param source HTML string to be converted.
	 * @param imageGetter class that downloads images in the HTML string.
	 * @param tagHandler class that handles unrecognized tag in the HTML string.
	 * @return spanned text for display.
	 */
	public static Spanned fromHtml(String source, Html.ImageGetter imageGetter, Html.TagHandler tagHandler, OnClickListener listener) {
		return adaptLink(Html.fromHtml(source, imageGetter, tagHandler), listener);
	}

	/**
	 * Returns an HTML representation of the provided spanned text.
	 * @param text Spanned text to be converted to HTML.
	 * @return HTML representation of the spanned text.
	 */
	public static String toHtml(Spanned text) {
		// TODO: implement this method
		return null;
	}

	/**
	 * Make a copy of a spanned text, with all URLSpan replaced by 
	 * AltHtmlLinkSpan which calls AltHtmlClickListener rather than opening
	 * the URL directly.
	 * @param spanned spanned text to be processed.
	 * @param listener listener for the click events of the links in the spanned text.
	 * @return spanned string with URLSpan replaced.
	 */
	private static Spannable adaptLink(Spanned spanned, OnClickListener listener) {
		// Note that we do not directly instantiate a new SpannableString object 
		// from a Spanned object. It is tested that SpannableString will copy spans
		// found in a Spanned object on Froyo, but it is not something guarentee.
		// Therefore, we only make sure that result contains the same text as the
		// incoming Spanned object and manually copy all Spans over.

		SpannableString result = new SpannableString(spanned.toString());
		Object[] list = spanned.getSpans(0, spanned.length(), Object.class);
		Object span = null;
		Object span2 = null;
		int start = 0;
		int end = 0;
		int flags = 0;

		for (int i = 0; i < list.length; i++) {
			span = list[i];
			start = spanned.getSpanStart(span);
			end = spanned.getSpanEnd(span);
			flags = spanned.getSpanFlags(span);

			if (span instanceof URLSpan) {
				span = new AltURLSpan(((URLSpan) span).getURL(), listener);
			}

			result.setSpan(span, start, end, flags);
		}

		return result;
	}

	/**
	 * Make a copy of a spanned text, with all AltHtmlLinkSpan reverted back to
	 * URLSpan so that Html class can recognize the link.
	 * @param spanned spanned text to be processed.
	 * @return spanned string with AltHtmlLinkSpan reverted back to URLSpan.
	 */
	private static String revertLink(Spanned spanned) {
		// TODO: implement this method
		return null;
	}

	/**
	 * OnClickListener is used to receive a notification whenever a link
	 * in a spannable text is clicked.
	 * @author Edison Chan
	 */
	public static interface OnClickListener {
		/**
		 * Indicate that a link with the given URL is clicked on the given view.
		 * @param view view where the click occurs.
		 * @param url URL of the link clicked.
		 */
		public void onLinkClick(View view, String url);
	}

	/**
	 * An alternative implementation of clickable span we substitute URLSpan with.
	 * This class accepts a listener object during construction, and the listener
	 * will be called when the span is clicked.
	 * @author Edison Chan
	 */
	static class AltURLSpan extends ClickableSpan {
		private OnClickListener listener;
		private String url;

		/**
		 * Construct a new AltHtmlLinkSpan.
		 * @param url URL of the link
		 * @param listener listener to be called when the link is clicked.
		 */
		AltURLSpan(String url, OnClickListener listener) {
			this.url = url;
			this.listener = listener;
		}

		/**
		 * Return the URL of the link.
		 * @return URL of this link.
		 */
		public String getURL() {
			return this.url;
		}

		/**
		 * Call the listener to notify of link clicks.
		 * @param widget Widget where the link is clicked.
		 */
		public void onClick(View widget) {
			this.listener.onLinkClick(widget, this.url);
		}
	}
};

#endif	//USE_OPTUS_DRM
