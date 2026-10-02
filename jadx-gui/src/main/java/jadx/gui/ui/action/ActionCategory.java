package jadx.gui.ui.action;

import jadx.gui.utils.NLS;

public enum ActionCategory {
	MENU_TOOLBAR(NLS.str("action_category.menu_toolbar")),
	CODE_AREA(NLS.str("action_category.code_area")),
	PLUGIN_SCRIPT(NLS.str("action_category.plugin_script")),
	HEX_VIEWER_MENU(NLS.str("action_category.hex_viewer"));

	private final String name;

	ActionCategory(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
