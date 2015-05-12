package ru.thewizardplusplus.wizardbudget;

import android.view.inputmethod.*;
import android.view.*;

public class InputConnection extends BaseInputConnection implements android.view.inputmethod.InputConnection {
	public InputConnection(View target_view, boolean full_editor) {
		super(target_view, full_editor);
	}

	@Override
	public boolean deleteSurroundingText(int before_length, int after_length) {
		// in latest Android deleteSurroundingText(1, 0) will be called for Backspace
		if (before_length == 1 && after_length == 0) {
			KeyEvent backspace_down = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
			KeyEvent backspace_up = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL);
			return super.sendKeyEvent(backspace_down) && super.sendKeyEvent(backspace_up);
		}

		return super.deleteSurroundingText(before_length, after_length);
    }
}
