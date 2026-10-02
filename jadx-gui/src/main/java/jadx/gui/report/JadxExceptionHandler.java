package jadx.gui.report;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.core.utils.Utils;
import jadx.gui.ui.MainWindow;
import jadx.plugins.tools.JadxPluginsTools;
import jadx.plugins.tools.data.JadxPluginMetadata;

import static jadx.plugins.tools.JadxExternalPluginsLoader.JADX_PLUGIN_CLASSLOADER_PREFIX;

public class JadxExceptionHandler implements Thread.UncaughtExceptionHandler {
	private static final Logger LOG = LoggerFactory.getLogger(JadxExceptionHandler.class);

	public static final String MAIN_PROJECT_STRING = "skylot/jadx";

	public static void register(MainWindow mainWindow) {
		Thread.setDefaultUncaughtExceptionHandler(new JadxExceptionHandler(mainWindow));
	}

	private final MainWindow mainWindow;

	private JadxExceptionHandler(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		LOG.error("Exception was thrown", ex);
		ExceptionData excData = buildExceptionData(ex);
		if (excData.getIOExc() != null) {
			IOExceptionMessageBox.show(mainWindow, excData);
		} else {
			ExceptionDialog.show(mainWindow, excData);
		}
	}

	private ExceptionData buildExceptionData(Throwable ex) {
		String projectName = null;
		IOException ioExc = null;
		Throwable curExc = ex;
		do {
			if (curExc instanceof IOException) {
				ioExc = (IOException) curExc;
			}
			if (projectName == null) {
				projectName = searchPluginInStackTrace(curExc);
			}
			curExc = curExc.getCause();
		} while (curExc != null);
		return new ExceptionData(ex, Utils.getOrElse(projectName, MAIN_PROJECT_STRING), ioExc);
	}

	private @Nullable String searchPluginInStackTrace(Throwable curExc) {
		for (StackTraceElement stackTraceElement : curExc.getStackTrace()) {
			String classLoaderName = stackTraceElement.getClassLoaderName();
			if (classLoaderName != null && classLoaderName.startsWith(JADX_PLUGIN_CLASSLOADER_PREFIX)) {
				String jarName = classLoaderName.substring(JADX_PLUGIN_CLASSLOADER_PREFIX.length());
				String pluginProject = resolvePluginByJarName(jarName);
				LOG.debug("Report exception in plugin: {}", pluginProject);
				return pluginProject;
			}
		}
		return null;
	}

	private String resolvePluginByJarName(String jarName) {
		for (JadxPluginMetadata jadxPluginMetadata : JadxPluginsTools.getInstance().getInstalled()) {
			if (jadxPluginMetadata.getPath().equals(jarName)) {
				String githubProject = getGithubProject(jadxPluginMetadata.getLocationId());
				return githubProject != null ? githubProject : "";
			}
		}
		return "";
	}

	private static @Nullable String getGithubProject(String locationId) {
		if (locationId.startsWith("github:")) {
			return locationId.substring("github:".length()).replace(':', '/');
		}
		return null;
	}
}
