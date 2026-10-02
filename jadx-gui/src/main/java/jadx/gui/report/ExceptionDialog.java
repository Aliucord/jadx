package jadx.gui.report;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.JadxDecompiler;
import jadx.cli.config.JadxConfigAdapter;
import jadx.commons.app.JadxSystemInfo;
import jadx.core.utils.Utils;
import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.gui.settings.JadxSettings;
import jadx.gui.settings.JadxSettingsData;
import jadx.gui.ui.MainWindow;
import jadx.gui.utils.LafManager;
import jadx.gui.utils.Link;
import jadx.gui.utils.NLS;
import jadx.gui.utils.TextStandardActions;
import jadx.gui.utils.UiUtils;

public class ExceptionDialog extends JDialog {
	private static final Logger LOG = LoggerFactory.getLogger(ExceptionDialog.class);

	private static final String FMT_DETAIL_LENGTH = "-13";

	static void show(@Nullable MainWindow mainWindow, ExceptionData data) {
		UiUtils.uiRun(() -> new ExceptionDialog(mainWindow, data));
	}

	private ExceptionDialog(@Nullable MainWindow mainWindow, ExceptionData data) {
		super(mainWindow, NLS.str("error.dialog.jadx_error_title"));
		JPanel titlePanel = new JPanel(new BorderLayout());
		JLabel titleLabel = new JLabel("<html><h2>" + NLS.str("error.dialog.exception_title") + "</h2></html>");
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titlePanel.add(titleLabel, BorderLayout.CENTER);

		Map<String, String> details = new LinkedHashMap<>();
		details.put("Jadx version", JadxDecompiler.getVersion());
		details.put("Java version", JadxSystemInfo.JAVA_VER);
		details.put("Java VM", String.format("%s %s",
				System.getProperty("java.vm.vendor", "?"), System.getProperty("java.vm.name", "?")));
		details.put("Platform", String.format("%s (%s %s)",
				JadxSystemInfo.OS_NAME, JadxSystemInfo.OS_VERSION, JadxSystemInfo.OS_ARCH));
		details.put("Max heap size", String.format("%d MB", Runtime.getRuntime().maxMemory() / (1024 * 1024)));

		try {
			details.put("Command line", ProcessHandle.current().info().commandLine().orElse(""));
		} catch (Throwable t) {
			LOG.error("Failed to get program command line", t);
		}
		String stackTrace = Utils.getFullStackTrace(data.getException());
		Link issueLink = buildNewIssueLink(data, details, stackTrace);

		JTextArea messageArea = new JTextArea();
		TextStandardActions.attach(messageArea);
		messageArea.setEditable(false);
		if (mainWindow != null) {
			messageArea.setFont(mainWindow.getSettings().getCodeFont());
		}
		messageArea.setForeground(Color.BLACK);
		messageArea.setBackground(Color.WHITE);

		StringBuilder detailsTextBuilder = new StringBuilder();
		details.forEach((key, value) -> detailsTextBuilder.append(String.format("%" + FMT_DETAIL_LENGTH + "s: %s\n", key, value)));
		messageArea.setText(detailsTextBuilder + "\n" + stackTrace);

		JScrollPane messageAreaScroller = new JScrollPane(messageArea);
		messageAreaScroller.setMinimumSize(new Dimension(600, 400));
		messageAreaScroller.setPreferredSize(new Dimension(600, 400));

		JButton exitButton = new JButton(NLS.str("error.dialog.terminate"));
		exitButton.addActionListener(event -> System.exit(1));
		JButton closeButton = new JButton(NLS.str("common_dialog.close"));
		closeButton.addActionListener(event -> {
			setVisible(false);
			dispose();
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		if (issueLink != null) {
			buttonPanel.add(issueLink);
		}
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(exitButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPanel.add(closeButton);

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout(5, 5));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		contentPanel.add(titlePanel, BorderLayout.PAGE_START);
		contentPanel.add(messageAreaScroller, BorderLayout.CENTER);
		contentPanel.add(buttonPanel, BorderLayout.PAGE_END);
		getContentPane().add(contentPanel);
		pack();

		javax.swing.SwingUtilities.invokeLater(() -> messageAreaScroller.getVerticalScrollBar().setValue(0));

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		int x = (screenSize.width - getWidth()) / 2;
		int y = (screenSize.height - getHeight()) / 2;
		setLocation(x, y);

		getRootPane().registerKeyboardAction(event -> setVisible(false),
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}

	private static @Nullable Link buildNewIssueLink(ExceptionData data, Map<String, String> details, String stackTrace) {
		String project = data.getGithubProject();
		if (project.isEmpty()) {
			return null;
		}
		Throwable ex = data.getException();
		String issueTitle;
		try {
			issueTitle = URLEncoder.encode(ex.toString(), StandardCharsets.UTF_8);
		} catch (Exception e) {
			LOG.error("URL encoding of title failed", e);
			issueTitle = ex.getClass().getSimpleName();
		}

		String message = "Please describe what you did before the error occurred.\n\n"
				+ "**IMPORTANT!** If the error occurs with a specific APK file please attach or provide link to apk file!\n\n";

		StringBuilder detailsIssueBuilder = new StringBuilder();
		details.forEach((key, value) -> detailsIssueBuilder.append(String.format("* %s: %s\n", key, value)));

		String body = String.format("%s%s\n```\n%s\n```", message, detailsIssueBuilder, stackTrace);

		String issueBody;
		try {
			issueBody = URLEncoder.encode(body, StandardCharsets.UTF_8);
		} catch (Exception e) {
			LOG.error("URL encoding of body failed", e);
			issueBody = "Please copy the displayed text in the Jadx error dialog and paste it here";
		}
		String url = String.format("https://github.com/%s/issues/new?labels=bug&title=%s&body=%s",
				project, issueTitle, issueBody);
		return new Link("<html><u><b>" + NLS.str("error.dialog.new_github_issue") + "</b></u></html>", url);
	}

	public static void throwTestException() {
		try {
			throw new RuntimeException("Inner exception message");
		} catch (Exception e) {
			throw new JadxRuntimeException("Outer exception message", e);
		}
	}

	public static void showTestExceptionDialog() {
		try {
			throwTestException();
		} catch (Exception e) {
			ExceptionData excData = new ExceptionData(e, JadxExceptionHandler.MAIN_PROJECT_STRING, null);
			ExceptionDialog.show(null, excData);
		}
	}

	public static void main(String[] args) {
		JadxConfigAdapter<JadxSettingsData> configAdapter = JadxSettings.buildConfigAdapter();
		configAdapter.useConfigRef("");
		JadxSettingsData settingsData = configAdapter.load();
		if (settingsData != null) {
			JadxSettings settings = new JadxSettings(configAdapter);
			settings.loadSettingsData(settingsData);
			LafManager.init(settings);
		}
		showTestExceptionDialog();
	}
}
