/*******************************************************************************
 * Copyright (c) 2018 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Kevin de Vlaming - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.creole.tests.documentbuilder;

import org.eclipse.mylyn.wikitext.parser.Attributes;
import org.eclipse.mylyn.wikitext.parser.DocumentBuilder.BlockType;
import org.eclipse.mylyn.wikitext.parser.DocumentBuilder.SpanType;
import org.eclipse.mylyn.wikitext.parser.ImageAttributes;

/**
 * @see http://www.wikicreole.org/wiki/Elements
 * @author Kevin de Vlaming
 */
public class CreoleDocumentBuilderTableTest extends AbstractCreoleDocumentBuilderTest {

	public void testTableWithEmptyCells() {
		assertTableRow("| |content| |\n\n", BlockType.TABLE_CELL_NORMAL);
	}

	public void testTableWithEmptyHeaderCells() {
		assertTableRow("|= |=content|= |\n\n", BlockType.TABLE_CELL_HEADER);
	}

	public void testTableWithLineBreaks() {
		assertTableRow("| |abc\\\\\\\\def| |\n\n", BlockType.TABLE_CELL_NORMAL, () -> {
			builder.characters("abc");
			builder.lineBreak();
			builder.lineBreak();
			builder.characters("def");
		});
	}

	public void testTableWithLink() {
		builder.beginDocument();
		builder.beginBlock(BlockType.TABLE, new Attributes());

		builder.beginBlock(BlockType.TABLE_ROW, new Attributes());

		builder.beginBlock(BlockType.TABLE_CELL_NORMAL, new Attributes());
		builder.link("http://example.com/", "link");
		builder.endBlock();

		builder.beginBlock(BlockType.TABLE_CELL_NORMAL, new Attributes());
		builder.beginSpan(SpanType.LINK, new Attributes());
		builder.characters("http://example.com");
		builder.endSpan();
		builder.endBlock();

		builder.endBlock();
		builder.endBlock();
		builder.endDocument();

		String markup = out.toString();

		assertEquals("|[[http://example.com/|link]]|[[http://example.com]]|\n\n", markup);
	}

	public void testTableWithImage() {
		builder.beginDocument();
		builder.beginBlock(BlockType.TABLE, new Attributes());

		builder.beginBlock(BlockType.TABLE_ROW, new Attributes());

		builder.beginBlock(BlockType.TABLE_CELL_NORMAL, new Attributes());
		builder.image(new ImageAttributes(), "/path/to/img.jpg");
		builder.endBlock();

		builder.beginBlock(BlockType.TABLE_CELL_NORMAL, new Attributes());
		ImageAttributes attr = new ImageAttributes();
		attr.setAlt("Alt text");
		builder.image(attr, "/path/to/img.jpg");
		builder.endDocument();
		builder.endBlock();

		builder.endBlock();
		builder.endBlock();
		builder.endDocument();

		String markup = out.toString();

		assertEquals("|{{/path/to/img.jpg}}|{{/path/to/img.jpg|Alt text}}|\n\n", markup);
	}

	private void assertTableRow(String expectedMarkup, BlockType cellType) {
		assertTableRow(expectedMarkup, cellType, () -> builder.characters("content"));
	}

	private void assertTableRow(String expectedMarkup, BlockType cellType, Runnable cellContentProvider) {
		builder.beginDocument();
		builder.beginBlock(BlockType.TABLE, new Attributes());

		builder.beginBlock(BlockType.TABLE_ROW, new Attributes());

		builder.beginBlock(cellType, new Attributes());
		builder.endBlock();

		builder.beginBlock(cellType, new Attributes());
		cellContentProvider.run();
		builder.endBlock();

		builder.beginBlock(cellType, new Attributes());
		builder.endBlock();

		builder.endBlock();
		builder.endBlock();
		builder.endDocument();

		String markup = out.toString();

		assertEquals(expectedMarkup, markup);
	}

}
