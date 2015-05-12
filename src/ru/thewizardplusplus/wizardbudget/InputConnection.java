package ru.thewizardplusplus.wizardbudget;

import android.view.inputmethod.*;
import android.view.*;

public class InputConnection extends BaseInputConnection {
	public InputConnection(View target_view, boolean full_editor) {
		super(target_view, full_editor);
	}

	@Override
	public boolean deleteSurroundingText(int before_length, int after_length) {
		/* In Android 4+ Backspace key not call sendKeyEvent(..., KeyEvent.KEYCODE_DEL).
		 * Instead it call deleteSurroundingText(1, 0).
		 * Below first called manually for JavaScript "keydown" and "keyup" events.
		 */
		if (before_length == 1 && after_length == 0) {
			KeyEvent backspace_down = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
			boolean result_down = super.sendKeyEvent(backspace_down);

			KeyEvent backspace_up = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL);
			boolean result_up = super.sendKeyEvent(backspace_up);

			return result_down && result_up;
		}

		return super.deleteSurroundingText(before_length, after_length);
    }
}
