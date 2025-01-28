package com.ans.cda.service.control;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.NamespaceContext;

/**
 * SimpleNamespaceContext
 * 
 * @author Nizar Ben Salem
 */
public class SimpleNamespaceContext implements NamespaceContext {

	/**
	 * prefixToUri
	 */
	private final Map<String, String> prefixToUri = new ConcurrentHashMap<>();

	/**
	 * SimpleNamespaceContext
	 */
	SimpleNamespaceContext() {
		// empty constructor
	}

	/**
	 * addNamespace
	 * 
	 * @param prefix
	 * @param uri
	 */
	public void addNamespace(final String prefix, final String uri) {
		prefixToUri.put(prefix, uri);
	}

	/**
	 * getNamespaceURI
	 */
	@Override
	public String getNamespaceURI(final String prefix) {
		return prefixToUri.getOrDefault(prefix, javax.xml.XMLConstants.NULL_NS_URI);
	}

	/**
	 * getPrefix
	 */
	@Override
	public String getPrefix(final String uri) {
		for (final Entry<String, String> entry : prefixToUri.entrySet()) {
			if (entry.getValue().equals(uri)) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * getPrefixes
	 */
	@Override
	public Iterator<String> getPrefixes(final String uri) {
		return prefixToUri.keySet().iterator();
	}
}