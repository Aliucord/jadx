package jadx.gui.utils.ui;

import java.awt.Window;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;

import jadx.gui.utils.UiUtils;

/**
 * Simple message box dialog with customizable action buttons
 */
public class ActionMessageBox {

	public static class Action {
		private final String name;
		private final Runnable action;

		public Action(String name, Runnable action) {
			this.name = name;
			this.action = action;
		}

		public Runnable getAction() {
			return action;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private final Window parent;
	private final String title;
	private final String msg;
	private final List<Action> actions;

	public ActionMessageBox(Window parent, String title, String msg, Action... actions) {
		this(parent, title, msg, Arrays.asList(actions));
	}

	public ActionMessageBox(Window parent, String title, String msg, List<Action> actions) {
		this.parent = parent;
		this.title = title;
		this.msg = msg;
		this.actions = actions;
	}

	/**
	 * Show message box.
	 *
	 * @return true if any action was executes, false otherwise
	 */
	public boolean show() {
		AtomicInteger result = new AtomicInteger(-1);
		UiUtils.uiRunAndWait(() -> result.set(
				JOptionPane.showOptionDialog(parent, msg, title,
						JOptionPane.DEFAULT_OPTION,
						JOptionPane.INFORMATION_MESSAGE,
						null,
						actions.toArray(),
						actions.get(0))));
		int r = result.get();
		if (r == JOptionPane.CLOSED_OPTION) {
			return false;
		}
		actions.get(r).getAction().run();
		return true;
	}
}
