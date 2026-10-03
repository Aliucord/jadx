package jadx.plugins.kotlin.metadata.pass

import jadx.api.data.IRenameNode
import jadx.api.plugins.input.data.AccessFlags
import jadx.api.plugins.pass.JadxPassInfo
import jadx.api.plugins.pass.impl.OrderedJadxPassInfo
import jadx.api.plugins.pass.types.JadxDecompilePass
import jadx.core.deobf.NameMapper
import jadx.core.dex.attributes.AFlag
import jadx.core.dex.attributes.AttrNode
import jadx.core.dex.attributes.nodes.RenameReasonAttr
import jadx.core.dex.nodes.ClassNode
import jadx.core.dex.nodes.MethodNode
import jadx.core.dex.nodes.RootNode
import jadx.plugins.kotlin.metadata.KotlinMetadataOptions
import jadx.plugins.kotlin.metadata.utils.KmClassWrapper
import jadx.plugins.kotlin.metadata.utils.KmClassWrapper.Companion.getWrapper

class KotlinMetadataDecompilePass(
	private val options: KotlinMetadataOptions,
) : JadxDecompilePass {

	override fun getInfo(): JadxPassInfo = OrderedJadxPassInfo(
		"KotlinMetadataDecompile",
		"Use kotlin.Metadata annotation perform various renames",
	)
		.before("CodeRenameVisitor")

	override fun init(root: RootNode) {
	}

	override fun visit(cls: ClassNode): Boolean {
		cls.innerClasses.forEach(::visit)

		val wrapper = cls.getWrapper() ?: return false
		if (options.isMethodArgs) renameMethodArgs(wrapper)
		if (options.isFields.isUsed) renameFields(wrapper)
		if (options.isCompanion.isUsed) renameCompanion(wrapper)
		if (options.isDataClass) fixDataClass(wrapper)
		if (options.isToString.isUsed) renameToString(wrapper)
		if (options.isGetters.isUsed) renameGetters(wrapper)

		return false
	}

	override fun visit(mth: MethodNode?) {
		/* no op */
	}

	private fun renameMethodArgs(wrapper: KmClassWrapper) {
		val args = wrapper.getMethodArgs()
		args.forEach { (_, list) ->
			list.forEach { (rArg, alias) ->
				// TODO: comment not being added?
				RenameReasonAttr.forNode(rArg).append(METADATA_REASON).configureRenamed(rArg.name)
				rArg.name = alias
			}
		}
	}

	private fun renameFields(wrapper: KmClassWrapper) {
		val fields = wrapper.getFields()
		fields.forEach { (field, alias) ->
			field.renameWith(options.isFields.shouldRename, METADATA_REASON, alias)
		}
	}

	private fun renameCompanion(wrapper: KmClassWrapper) {
		val companion = wrapper.getCompanion()
		companion?.run {
			field.renameWith(options.isCompanion.shouldRename, METADATA_REASON, COMPANION_FIELD)
			cls.renameWith(options.isCompanion.shouldRename, METADATA_REASON, COMPANION_CLASS)

			if (hide) {
				field.add(AFlag.DONT_GENERATE)
				cls.add(AFlag.DONT_GENERATE)
				cls.add(AFlag.DONT_INLINE)
			}
		}
	}

	private fun fixDataClass(wrapper: KmClassWrapper) {
		val isData = wrapper.isDataClass()
		wrapper.cls.run {
			if (isData != accessFlags.isData) {
				accessFlags = accessFlags.run {
					if (isData) {
						add(AccessFlags.DATA)
					} else {
						remove(AccessFlags.DATA)
					}
				}
			}
		}
	}

	private fun renameToString(wrapper: KmClassWrapper) {
		val toString = wrapper.parseToString()
		toString?.run {
			clsAlias?.let { alias ->
				if (NameMapper.isValidIdentifier(alias)) {
					cls.renameWith(options.isToString.shouldRename, TO_STRING_REASON, alias, override = false)
				}
			}

			fields.forEach { (field, alias) ->
				if (NameMapper.isValidIdentifier(alias)) {
					field.renameWith(options.isToString.shouldRename, TO_STRING_REASON, alias)
				}
			}
		}
	}

	private fun renameGetters(wrapper: KmClassWrapper) {
		val getters = wrapper.getGetters()
		getters.forEach { (mth, alias) ->
			mth.renameWith(options.isGetters.shouldRename, GETTER_REASON, alias)
		}
	}

	private fun <T> T.renameWith(shouldRename: Boolean, reason: String, alias: String, override: Boolean = true) where T : AttrNode, T : IRenameNode {
		val renaming = shouldRename && AFlag.DONT_RENAME !in this
		val attr = RenameReasonAttr.forNode(this).append(reason)
		if (renaming) {
			attr.configureRenamed(null, override)
			this.rename(alias)
		} else {
			attr.configureComment(alias, override)
		}
	}

	companion object {
		private const val METADATA_REASON = "from kotlin metadata"
		private const val COMPANION_FIELD = "INSTANCE"
		private const val COMPANION_CLASS = "Companion"
		private const val TO_STRING_REASON = "from toString"
		private const val GETTER_REASON = "from getter"
	}
}
