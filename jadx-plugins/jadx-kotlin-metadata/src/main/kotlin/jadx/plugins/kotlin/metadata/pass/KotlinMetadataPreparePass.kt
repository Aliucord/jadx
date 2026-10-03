package jadx.plugins.kotlin.metadata.pass

import jadx.api.plugins.pass.JadxPassInfo
import jadx.api.plugins.pass.impl.OrderedJadxPassInfo
import jadx.api.plugins.pass.types.JadxPreparePass
import jadx.core.dex.attributes.AFlag
import jadx.core.dex.attributes.nodes.RenameReasonAttr
import jadx.core.dex.nodes.RootNode
import jadx.plugins.kotlin.metadata.KotlinMetadataOptions
import jadx.plugins.kotlin.metadata.utils.KotlinMetadataUtils
import javax.management.Query.attr

class KotlinMetadataPreparePass(
	private val options: KotlinMetadataOptions,
) : JadxPreparePass {

	override fun getInfo(): JadxPassInfo = OrderedJadxPassInfo(
		"KotlinMetadataPrepare",
		"Use kotlin.Metadata annotation to rename class & package",
	)
		.before("RenameVisitor")

	override fun init(root: RootNode) {
		if (options.isClassAlias.isUsed) {
			for (cls in root.classes) {
				val renaming = options.isClassAlias.shouldRename && AFlag.DONT_RENAME !in cls
				// rename class & package
				val kotlinCls = KotlinMetadataUtils.getAlias(cls, renaming)
				if (kotlinCls != null && renaming) {
					cls.rename(kotlinCls.name)
					cls.packageNode.rename(kotlinCls.pkg)
				}
			}
		}
	}
}
