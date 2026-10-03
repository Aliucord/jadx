package jadx.plugins.kotlin.metadata

import jadx.api.plugins.options.impl.BasePluginOptionsBuilder
import jadx.plugins.kotlin.metadata.KotlinMetadataPlugin.Companion.PLUGIN_ID

class KotlinMetadataOptions : BasePluginOptionsBuilder() {
	enum class Value {
		YES,
		COMMENT,
		NO,
		;

		inline val shouldRename get() = this == YES
		inline val isUsed get() = this != NO

		companion object {
			fun fromString(str: String): Value {
				return when (str.lowercase()) {
					"yes", "true", "rename" -> YES
					"comment" -> COMMENT
					"no", "false" -> NO
					else -> NO
				}
			}

			val default = YES
			val values = entries.toTypedArray()
		}
	}

	private fun valueOption(name: String) =
		enumOption(name, Value.values, Value::fromString)
			.defaultValue(Value.default)

	var isClassAlias: Value = Value.default
		private set
	var isMethodArgs: Boolean = true
		private set
	var isFields: Value = Value.default
		private set
	var isCompanion: Value = Value.default
		private set
	var isDataClass: Boolean = true
		private set
	var isToString: Value = Value.default
		private set
	var isGetters: Value = Value.default
		private set

	override fun registerOptions() {
		valueOption(CLASS_ALIAS_OPT)
			.description("rename class alias")
			.setter { isClassAlias = it }

		boolOption(METHOD_ARGS_OPT)
			.description("rename function arguments")
			.defaultValue(true)
			.setter { isMethodArgs = it }

		valueOption(FIELDS_OPT)
			.description("rename fields")
			.setter { isFields = it }

		valueOption(COMPANION_OPT)
			.description("rename companion object")
			.setter { isCompanion = it }

		boolOption(DATA_CLASS_OPT)
			.description("add data class modifier")
			.defaultValue(true)
			.setter { isDataClass = it }

		valueOption(TO_STRING_OPT)
			.description("rename fields using toString")
			.setter { isToString = it }

		valueOption(GETTERS_OPT)
			.description("rename simple getters to field names")
			.setter { isGetters = it }
	}

	fun isPreparePassNeeded(): Boolean = isClassAlias.isUsed

	fun isDecompilePassNeeded(): Boolean = isMethodArgs || isFields.isUsed || isCompanion.isUsed || isDataClass || isToString.isUsed || isGetters.isUsed

	companion object {
		const val CLASS_ALIAS_OPT = "$PLUGIN_ID.class-alias"
		const val METHOD_ARGS_OPT = "$PLUGIN_ID.method-args"
		const val FIELDS_OPT = "$PLUGIN_ID.fields"
		const val COMPANION_OPT = "$PLUGIN_ID.companion"
		const val DATA_CLASS_OPT = "$PLUGIN_ID.data-class"
		const val TO_STRING_OPT = "$PLUGIN_ID.to-string"
		const val GETTERS_OPT = "$PLUGIN_ID.getters"
	}
}
