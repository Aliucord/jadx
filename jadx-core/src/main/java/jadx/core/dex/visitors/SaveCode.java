package jadx.core.dex.visitors;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.ICodeInfo;
import jadx.api.JadxArgs;
import jadx.api.plugins.utils.ZipSecurity;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.RootNode;
import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.core.utils.files.FileUtils;

public class SaveCode {
	private static final Logger LOG = LoggerFactory.getLogger(SaveCode.class);

	private SaveCode() {
	}

	public static void save(File dir, ClassNode cls, ICodeInfo code, SaveToJar saveToJar) {
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
		String fileName = cls.getClassInfo().getAliasFullPath() + getFileExtension(cls.root());
		save(codeStr, dir, fileName, saveToJar);
	}

	public static void save(String code, File dir, String fileName, SaveToJar saveToJar) {
		if (!ZipSecurity.isValidZipEntryName(fileName)) {
			return;
		}
		if (saveToJar != null) {
			saveToJar.write(code, fileName);
		} else {
			save(code, new File(dir, fileName));
		}
	}

	public static void save(ICodeInfo codeInfo, File file) {
		save(codeInfo.getCodeStr(), file);
	}

	public static void save(String code, File file) {
		File outFile = FileUtils.prepareFile(file);
		try (PrintWriter out = new PrintWriter(outFile, "UTF-8")) {
			out.println(code);
		} catch (Exception e) {
			LOG.error("Save file error", e);
		}
	}

	public static String getFileExtension(RootNode root) {
		JadxArgs.OutputFormatEnum outputFormat = root.getArgs().getOutputFormat();
		switch (outputFormat) {
			case JAVA:
				return ".java";

			case JSON:
				return ".json";

			default:
				throw new JadxRuntimeException("Unknown output format: " + outputFormat);
		}
	}

	public static class SaveToJar implements Closeable {
		JarOutputStream jarOutputStream;

		public SaveToJar(File file) {
			try {
				jarOutputStream = new JarOutputStream(Files.newOutputStream(file.toPath()));
			} catch (IOException e) {
				LOG.error("Failed to create .jar file", e);
			}
		}

		public void write(String code, String path) {
			try {
				jarOutputStream.putNextEntry(new JarEntry(path.replace("\\", "/")));
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
}
