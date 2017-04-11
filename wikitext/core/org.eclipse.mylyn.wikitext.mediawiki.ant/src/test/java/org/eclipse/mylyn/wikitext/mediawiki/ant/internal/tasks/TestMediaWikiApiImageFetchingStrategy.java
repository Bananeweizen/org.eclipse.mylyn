/*******************************************************************************
 * Copyright (c) 2017 Jeremie Bresson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jeremie Bresson - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.wikitext.mediawiki.ant.internal.tasks;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class TestMediaWikiApiImageFetchingStrategy extends MediaWikiApiImageFetchingStrategy {

	private final Map<String, String> serverContent;

	public TestMediaWikiApiImageFetchingStrategy(Map<String, String> serverContent) {
		checkNotNull(serverContent,
				"Please specify some server content for images used during the tests. See: TestMediaWikiApiImageFetchingStrategy#serverContent");
		this.serverContent = ImmutableMap.copyOf(serverContent);
	}

	@Override
	protected Reader createInputReader(URL apiUrl) throws UnsupportedEncodingException, IOException {
		String key = apiUrl.toString();
		if (!serverContent.containsKey(key)) {
			throw new IllegalStateException("Please define a server content used during the tests for the key: " + key);
		}
		String pageContent = serverContent.get(key);
		return new StringReader(pageContent);
	}
}
