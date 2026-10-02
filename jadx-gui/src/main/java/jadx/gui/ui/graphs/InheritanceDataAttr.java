package jadx.gui.ui.graphs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jadx.api.plugins.input.data.attributes.IJadxAttrType;
import jadx.api.plugins.input.data.attributes.IJadxAttribute;
import jadx.core.clsp.ClspClass;
import jadx.core.clsp.ClspGraph;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.nodes.RootNode;

public class InheritanceDataAttr implements IJadxAttribute {
	public static final IJadxAttrType<InheritanceDataAttr> INHERITANCE_DATA = IJadxAttrType.create("INHERITANCE_DATA");

	public static InheritanceDataAttr get(RootNode root) {
		InheritanceDataAttr existData = root.get(INHERITANCE_DATA);
		if (existData != null) {
			return existData;
		}
		InheritanceDataAttr inheritanceDataAttr = new InheritanceDataAttr(root.getClsp());
		root.addAttr(inheritanceDataAttr);
		return inheritanceDataAttr;
	}

	/**
	 * Maps class names to immediate super classes - this is equivalent to the .parents attribute of the
	 * ClspClasses in the nameMap
	 */
	private final Map<String, Set<String>> immediateSuperTypesCache;

	/**
	 * Maps class names to immediate subclasses and implementations
	 */
	private final Map<String, List<String>> immediateImplementsCache;

	private InheritanceDataAttr(ClspGraph clsp) {
		immediateSuperTypesCache = buildImmediateSuperTypesCache(clsp);
		immediateImplementsCache = buildImmediateImplementsCache(clsp);
	}

	/**
	 * Get the immediate parents for a class
	 */
	public Set<String> getParents(String clsName) {
		Set<String> parents = immediateSuperTypesCache.get(clsName);
		return parents == null ? Collections.emptySet() : parents;
	}

	/**
	 * Get direct implementations of a class
	 */
	public List<String> getChildren(String clsName) {
		List<String> list = immediateImplementsCache.get(clsName);
		return list == null ? Collections.emptyList() : list;
	}

	/**
	 * Propagate the immediateSuperTypesCache by traversing the nameMap
	 * and inspecting the parents of the ClspClasses
	 */
	private Map<String, Set<String>> buildImmediateSuperTypesCache(ClspGraph clspGraph) {
		Map<String, ClspClass> nameMap = clspGraph.getClsNameMap();
		Map<String, Set<String>> nametoSupertypesMap = new HashMap<>(nameMap.size());
		for (Map.Entry<String, ClspClass> entry : nameMap.entrySet()) {
			Set<String> supertypesSet = new HashSet<>();
			ClspClass cls = entry.getValue();
			addImmediateSuperTypes(clspGraph, cls, supertypesSet);
			nametoSupertypesMap.put(cls.getName(), supertypesSet);
		}
		return nametoSupertypesMap;
	}

	/**
	 * Propagate the immediate implements cache by reversing the immediateSuperTypesCache
	 */
	private Map<String, List<String>> buildImmediateImplementsCache(ClspGraph clsp) {
		Map<String, ClspClass> nameMap = clsp.getClsNameMap();
		Map<String, List<String>> map = new HashMap<>(nameMap.size());
		List<String> classes = new ArrayList<>(nameMap.keySet());
		Collections.sort(classes);
		for (String cls : classes) {
			for (String st : getParents(cls)) {
				map.computeIfAbsent(st, v -> new ArrayList<>()).add(cls);
			}
		}
		return map;
	}

	/**
	 * Add only the names of immediate super types of cls to result
	 */
	private void addImmediateSuperTypes(ClspGraph clspGraph, ClspClass cls, Set<String> result) {
		for (ArgType parentType : cls.getParents()) {
			if (parentType == null) {
				continue;
			}
			ClspClass parentCls = clspGraph.getClsDetails(parentType);
			if (parentCls != null) {
				// this should be equivalent to parentType.getObject()
				result.add(parentCls.getName());
			} else {
				// parent type is unknown
				result.add(parentType.getObject());
			}
		}
	}

	@Override
	public IJadxAttrType<InheritanceDataAttr> getAttrType() {
		return INHERITANCE_DATA;
	}
}
