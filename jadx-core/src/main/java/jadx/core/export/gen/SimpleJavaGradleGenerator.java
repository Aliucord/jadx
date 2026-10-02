package jadx.core.export.gen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import jadx.api.ResourceFile;
import jadx.api.security.IJadxSecurity;
import jadx.api.security.SanitizeType;
import jadx.core.dex.nodes.RootNode;
import jadx.core.export.OutDirs;
import jadx.core.export.TemplateFile;
import jadx.core.utils.exceptions.JadxRuntimeException;

public class SimpleJavaGradleGenerator implements IExportGradleGenerator {
	private final RootNode root;
	private final File projectDir;
	private final List<ResourceFile> resources;

	private OutDirs outDirs;
	private File appDir;

	public SimpleJavaGradleGenerator(RootNode root, File projectDir, List<ResourceFile> resources) {
		this.root = root;
		this.projectDir = projectDir;
		this.resources = resources;
	}

	@Override
	public void init() {
		appDir = new File(projectDir, "app");
		File srcOutDir = new File(appDir, "src/main/java");
		File resOutDir = new File(appDir, "src/main/resources");
		outDirs = new OutDirs(srcOutDir, resOutDir);
	}

	@Override
	public void generateFiles() {
		try {
			saveSettingsGradle();
			saveBuildGradle();
		} catch (Exception e) {
			throw new JadxRuntimeException("Failed to generate gradle files", e);
		}
	}

	private void saveSettingsGradle() throws IOException {
		TemplateFile tmpl = loadGradleTemplate("/export/java/settings.gradle.kts.tmpl");
		tmpl.add("projectName", GradleGeneratorTools.guessProjectName(root));
		tmpl.save(new File(projectDir, "settings.gradle.kts"));
	}

	private void saveBuildGradle() throws IOException {
		TemplateFile tmpl = loadGradleTemplate("/export/java/build.gradle.kts.tmpl");
		tmpl.save(new File(appDir, "build.gradle.kts"));
	}

	private TemplateFile loadGradleTemplate(String templatePath) throws FileNotFoundException {
		TemplateFile tmpl = TemplateFile.fromResources(templatePath);
		IJadxSecurity security = root.getArgs().getSecurity();
		tmpl.setValueSanitizer(str -> security.sanitizeString(str, SanitizeType.GRADLE_KOTLIN));
		return tmpl;
	}

	@Override
	public OutDirs getOutDirs() {
		return outDirs;
	}
}
