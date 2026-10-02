package jadx.gui;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jadx.api.plugins.utils.CommonFileUtils;
import jadx.core.utils.exceptions.JadxRuntimeException;
import jadx.gui.utils.LangLocale;
import jadx.gui.utils.NLS;

import static java.nio.file.Paths.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class TestI18n {
	private static final String DEFAULT_LANG_FILE = "Messages_en_US.properties";

	private static Path i18nPath;
	private static Path refPath;
	private static Path guiJavaPath;

	@BeforeAll
	public static void init() {
		i18nPath = get("src/main/resources/i18n");
		assertThat(i18nPath).exists();
		refPath = i18nPath.resolve(DEFAULT_LANG_FILE);
		assertThat(refPath).exists();
		guiJavaPath = get("src/main/java");
		assertThat(guiJavaPath).exists();
	}

	@Test
	public void verifyLocales() {
		for (LangLocale lang : NLS.getLangLocales()) {
			Locale locale = lang.get();
			System.out.println("Language: " + locale.getLanguage() + " - " + locale.getDisplayLanguage()
					+ ", country: " + locale.getCountry() + " - " + locale.getDisplayCountry()
					+ ", language tag: " + locale.toLanguageTag());
		}
	}

	@Test
	public void filesExactlyMatch() throws IOException {
		List<String> reference = Files.readAllLines(refPath)
				.stream()
				.map(TestI18n::getPrefix)
				.collect(Collectors.toList());
		try (Stream<Path> list = Files.list(i18nPath)) {
			list.filter(p -> !p.equals(refPath))
					.forEach(path -> compareToReference(path, reference));
		}
	}

	/**
	 * Extract prefix: 'key='
	 */
	private static String getPrefix(String line) {
		if (line.isBlank()) {
			return "";
		}
		int sep = line.indexOf('=');
		if (sep == -1) {
			return line;
		}
		if (line.startsWith("#")) {
			fail(DEFAULT_LANG_FILE + " shouldn't contain commented values: " + line);
		}
		return line.substring(0, sep + 1);
	}

	private void compareToReference(Path path, List<String> reference) {
		try {
			List<String> lines = Files.readAllLines(path);
			for (int i = 0; i < reference.size(); i++) {
				String prefix = reference.get(i);
				if (prefix.isEmpty()) {
					continue;
				}
				if (i >= lines.size()) {
					fail("File '" + path.getFileName() + "' contains unexpected lines at end");
				}
				String line = lines.get(i);
				if (!trimComment(line).startsWith(prefix)) {
					failLine(path, i + 1);
				}
				if (line.startsWith("#")) {
					int sep = line.indexOf('=');
					if (line.substring(sep + 1).isBlank()) {
						fail("File '" + path.getFileName() + "' has empty ref text at line " + (i + 1) + ": " + line);
					}
				}
			}
			if (lines.size() != reference.size()) {
				failLine(path, reference.size());
			}
		} catch (IOException e) {
			fail("Process error ", e);
		}
	}

	private static String trimComment(String string) {
		return string.startsWith("#") ? string.substring(1) : string;
	}

	private void failLine(Path path, int line) {
		fail("I18n file: " + path.getFileName() + " and " + DEFAULT_LANG_FILE + " differ in line " + line);
	}

	/**
	 * Temporary solution to allow use I18N strings in plugins until proper API implemented
	 */
	private static final List<String> EXCLUDED_KEYS = Arrays.asList(
			// keys from `jadx-script-kotlin`
			"file.save",
			"tree.input_scripts",
			"popup.new_script",
			"popup.add_scripts",
			"script.log",
			"script.format",
			"script.check");

	/**
	 * All keys should be used in code and all keys in code should exist in default lang file
	 */
	@Test
	public void keyUsage() throws IOException {
		Set<String> codeKeys = collectKeysFromCode();
		Properties properties = new Properties();
		try (Reader reader = Files.newBufferedReader(i18nPath.resolve(DEFAULT_LANG_FILE))) {
			properties.load(reader);
		}
		Set<String> keys = new HashSet<>();
		for (Object keyObj : properties.keySet()) {
			keys.add((String) keyObj);
		}
		EXCLUDED_KEYS.forEach(keys::remove);

		List<String> errors = new ArrayList<>();
		for (String codeKey : codeKeys) {
			if (!keys.contains(codeKey)) {
				errors.add(String.format("Key '%s' not found in NLS strings", codeKey));
			}
		}
		for (String key : keys) {
			if (!codeKeys.contains(key)) {
				errors.add(String.format("Key '%s' not used in code", key));
			}
		}
		if (!errors.isEmpty()) {
			fail("NLS key usage errors:\n " + StringUtils.join(errors, "\n "));
		}
	}

	private static Set<String> collectKeysFromCode() throws IOException {
		Set<String> keys = new HashSet<>();
		try (Stream<Path> walk = Files.walk(guiJavaPath)) {
			walk.filter(TestI18n::filterCodeFiles)
					.forEach(codeFile -> {
						try {
							for (String line : Files.readAllLines(codeFile)) {
								processCodeLine(codeFile, line, keys);
							}
						} catch (Exception e) {
							throw new JadxRuntimeException("Failed to process file: " + codeFile, e);
						}
					});
		}
		return keys;
	}

	private static boolean filterCodeFiles(Path filePath) {
		String ext = CommonFileUtils.getFileExtension(filePath.getFileName().toString());
		return Objects.equals(ext, "java") || Objects.equals(ext, "kt");
	}

	private static final Pattern NLS_STR_USAGE = Pattern.compile("NLS\\.str\\(\"([\\w._]*)\"[,)]");

	private static void processCodeLine(Path p, String line, Set<String> keys) {
		if (line.contains("NLS.str(")) {
			boolean find = false;
			Matcher matcher = NLS_STR_USAGE.matcher(line);
			while (matcher.find()) {
				String key = matcher.group(1);
				keys.add(key);
				find = true;
			}
			if (!find) {
				throw new JadxRuntimeException("NLS.str() should be used with constant string key, but got: "
						+ line.substring(line.indexOf("NLS.str("))
						+ ", file: " + p);
			}
		}
		if (line.startsWith("import static jadx.gui.utils.NLS.str;")) {
			throw new JadxRuntimeException("NLS.str() method import is forbidden, file: " + p);
		}
	}
}
