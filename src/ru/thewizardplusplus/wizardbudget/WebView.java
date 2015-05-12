package ru.thewizardplusplus.wizardbudget;

import android.content.*;
import android.util.*;
import android.view.inputmethod.*;
import android.text.*;

public class WebView extends android.webkit.WebView {
	public WebView(Context context, AttributeSet attributes) {
		super(context, attributes);
	}

	@Override
	public android.view.inputmethod.InputConnection onCreateInputConnection(EditorInfo out_attributes) {
		return new InputConnection(this, false);
	}
}
