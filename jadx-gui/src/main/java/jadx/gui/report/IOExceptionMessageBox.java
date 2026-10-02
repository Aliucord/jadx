package jadx.gui.report;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;

import jadx.gui.ui.MainWindow;
import jadx.gui.utils.NLS;
import jadx.gui.utils.UiUtils;
import jadx.gui.utils.ui.ActionMessageBox;
import jadx.gui.utils.ui.ActionMessageBox.Action;

public class IOExceptionMessageBox {

	public static void show(MainWindow mainWindow, ExceptionData excData) {
		IOException ioExc = excData.getIOExc();
		if (ioExc == null) {
			return;
		}
		String message;
		if (ioExc instanceof FileNotFoundException || ioExc instanceof NoSuchFileException) {
			message = String.format(NLS.str("io_error_dialog.file_not_found"), ioExc.getMessage());
		} else if (ioExc instanceof AccessDeniedException) {
			message = String.format(NLS.str("io_error_dialog.access_denied"), ioExc.getMessage());
		} else {
			message = ioExc.getClass().getSimpleName() + ": " + ioExc.getMessage();
		}
		List<Action> actions = new ArrayList<>(2);
		actions.add(new Action(NLS.str("io_error_dialog.report"), () -> ExceptionDialog.show(mainWindow, excData)));
		actions.add(new Action(NLS.str("io_error_dialog.close"), UiUtils.EMPTY_RUNNABLE));
		new ActionMessageBox(mainWindow, NLS.str("io_error_dialog.title"), message, actions).show();
	}
}
