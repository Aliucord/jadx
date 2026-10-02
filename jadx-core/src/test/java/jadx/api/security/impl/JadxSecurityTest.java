package jadx.api.security.impl;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jadx.api.security.JadxSecurityFlag;
import jadx.api.security.SanitizeType;

import static org.assertj.core.api.Assertions.assertThat;

class JadxSecurityTest {
	private static final Logger LOG = LoggerFactory.getLogger(JadxSecurityTest.class);

	private final JadxSecurity security = new JadxSecurity(JadxSecurityFlag.all());

	@Test
	void sanitizeString() {
		verifySanitize("ab';c", "abc");
		verifySanitize("a/b\\c", "abc");
		verifySanitize("\"'{}[]:>?*|", "");
	}

	private void verifySanitize(String inputStr, String expectedStr) {
		String result = security.sanitizeString(inputStr, SanitizeType.GRADLE_GROOVY);
		LOG.debug("sanitize {}, result: {}", inputStr, result);
		assertThat(result)
				.describedAs("Sanitized string of %s expected to be %s, but got: %s", inputStr, expectedStr, result)
				.isEqualTo(expectedStr);
	}
}
