/*******************************************************************************
 * Copyright (c) 2013, 2015 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.wikitext.html.internal;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.eclipse.mylyn.wikitext.html.HtmlLanguage;
import org.eclipse.mylyn.wikitext.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.parser.markup.MarkupLanguage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class HtmlLanguageIntegrationTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void defaultMarkupLanguageHasImageSupport() {
		MarkupLanguage sourceLanguage = getDefaultMarkupLanguage("Source HTML");
		MarkupLanguage targetLanguage = getDefaultMarkupLanguage("Target HTML");
		StringWriter stringWriter = new StringWriter();
		MarkupParser markupParser = getMarkupParser(sourceLanguage, targetLanguage, stringWriter);

		markupParser.parse("Text Before Image<img src=\"/favicon.ico\"/>Text After Image", false);

		assertEquals("Text Before Image<img border=\"0\" src=\"/favicon.ico\"/>Text After Image",
				stringWriter.toString());
	}

	@Test
	public void emitImageWithoutImageSupport() {
		MarkupLanguage sourceLanguage = getDefaultMarkupLanguage("Source HTML");
		MarkupLanguage targetLanguageWithNoImageSupport = getMarkupLanguageWithoutImageSupport(
				"Target HTML With No Images");
		StringWriter stringWriter = new StringWriter();
		MarkupParser markupParser = getMarkupParser(sourceLanguage, targetLanguageWithNoImageSupport, stringWriter);

		markupParser.parse("Text Before Image<img src=\"/favicon.ico\"/>Text After Image", false);

		assertEquals("Text Before ImageText After Image", stringWriter.toString());
	}

	private MarkupLanguage getDefaultMarkupLanguage(String name) {
		return HtmlLanguage.builder().name(name).create();
	}

	private MarkupParser getMarkupParser(MarkupLanguage sourceLanguage, MarkupLanguage targetLanguage,
			StringWriter stringWriter) {
		return new MarkupParser(sourceLanguage, targetLanguage.createDocumentBuilder(stringWriter));
	}

	private MarkupLanguage getMarkupLanguageWithoutImageSupport(String name) {
		return HtmlLanguage.builder().name(name).setSupportsImages(false).create();
	}
}
