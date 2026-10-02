package jadx.gui.cache.code;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import jadx.gui.utils.NLS;

public enum CodeCacheMode {
	MEMORY(NLS.str("preferences.codeCacheMode.memory"), NLS.str("preferences.codeCacheMode.memory.desc")),
	DISK_WITH_CACHE(NLS.str("preferences.codeCacheMode.diskWithCache"), NLS.str("preferences.codeCacheMode.diskWithCache.desc")),
	DISK(NLS.str("preferences.codeCacheMode.disk"), NLS.str("preferences.codeCacheMode.disk.desc"));

	private final String label;
	private final String desc;

	CodeCacheMode(String label, String desc) {
		this.label = label;
		this.desc = desc;
	}

	public String getLocalizedName() {
		return label;
	}

	public String getDesc() {
		return desc;
	}

	@Override
	public String toString() {
		return getLocalizedName();
	}

	public static String buildToolTip() {
		return Stream.of(values())
				.map(v -> v.getLocalizedName() + " - " + v.getDesc())
				.collect(Collectors.joining("\n"));
	}
}
