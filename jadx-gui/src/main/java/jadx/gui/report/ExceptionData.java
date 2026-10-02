package jadx.gui.report;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

public final class ExceptionData {
	private final Throwable exception;
	private final String githubProject;
	private final @Nullable IOException ioExc;

	ExceptionData(Throwable exception, String githubProject, @Nullable IOException ioExc) {
		this.exception = exception;
		this.githubProject = githubProject;
		this.ioExc = ioExc;
	}

	public Throwable getException() {
		return exception;
	}

	public @Nullable IOException getIOExc() {
		return ioExc;
	}

	public String getGithubProject() {
		return githubProject;
	}
}
