/*******************************************************************************
 * Copyright (c) 2007, 2013 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.wikitext.internal.parser.html;

import org.eclipse.mylyn.wikitext.internal.parser.html.AbstractSaxHtmlParser;
import org.eclipse.mylyn.wikitext.internal.parser.html.XHtmlParser;

public class XHtmlParserTest extends HtmlParserTest {
	@Override
	protected AbstractSaxHtmlParser createParser() {
		return new XHtmlParser();
	}
}
