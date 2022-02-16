package jadx.core.dex.visitors;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.ICodeInfo;
import jadx.api.JadxArgs;
import jadx.api.plugins.utils.ZipSecurity;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.core.utils.files.FileUtils;

public class SaveCode {
	private static final Logger LOG = LoggerFactory.getLogger(SaveCode.class);

	private SaveCode() {
	}

	@FunctionalInterface
	public interface SaveAction {
		void save(String code, File file);
	}

	public static void save(File dir, ClassNode cls, ICodeInfo code, SaveAction action) {
		if (cls.contains(AFlag.DONT_GENERATE)) {
			return;
		}
		if (code == null) {
			throw new JadxRuntimeException("Code not generated for class " + cls.getFullName());
		}
		if (code == ICodeInfo.EMPTY) {
			return;
		}
		String codeStr = code.getCodeStr();
		if (codeStr.isEmpty()) {
			return;
		}
		if (cls.root().getArgs().isSkipFilesSave()) {
			return;
		}
		String fileName = cls.getClassInfo().getAliasFullPath() + getFileExtension(cls);

		if (!ZipSecurity.isValidZipEntryName(fileName)) {
			return;
		}

		action.save(codeStr, new File(dir, fileName));
	}

	public static void saveToFile(String code, File file) {
		File outFile = FileUtils.prepareFile(file);
		try (PrintWriter out = new PrintWriter(outFile, "UTF-8")) {
			out.println(code);
		} catch (Exception e) {
			LOG.error("Save file error", e);
		}
	}

	public static class SaveToJar implements SaveAction, Closeable {
		JarOutputStream jarOutputStream;

		public SaveToJar(File file) {
			try {
				jarOutputStream = new JarOutputStream(new FileOutputStream(file));
			} catch (IOException e) {
				LOG.error("Failed to create .jar file", e);
			}
		}

		@Override
		public void save(String code, File file) {
			try {
				jarOutputStream.putNextEntry(new JarEntry(file.getPath()));

				jarOutputStream.write(code.getBytes(StandardCharsets.UTF_8));

				jarOutputStream.closeEntry();
			} catch (IOException e) {
				LOG.error("Save file error", e);
			}
		}

		@Override
		public void close() {
			try {
				jarOutputStream.close();
			} catch (IOException e) {
				LOG.error("Jar save error", e);
			}
		}
	}

	private static String getFileExtension(ClassNode cls) {
		JadxArgs.OutputFormatEnum outputFormat = cls.root().getArgs().getOutputFormat();
		switch (outputFormat) {
			case JAVA:
				return ".java";

			case JSON:
				return ".json";

			default:
				throw new JadxRuntimeException("Unknown output format: " + outputFormat);
		}
	}
}
