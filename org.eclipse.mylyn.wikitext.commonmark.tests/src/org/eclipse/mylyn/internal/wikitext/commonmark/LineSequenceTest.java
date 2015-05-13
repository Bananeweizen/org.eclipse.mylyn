/*******************************************************************************
 * Copyright (c) 2015 David Green.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.commonmark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;

public class LineSequenceTest {

	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	@Test
	public void createRequiresContent() {
		thrown.expect(NullPointerException.class);
		LineSequence.create((String) null);
	}

	@Test
	public void transform() {
		LineSequence lineSequence = LineSequence.create("one").transform(new Function<Line, Line>() {

			@Override
			public Line apply(Line input) {
				throw new UnsupportedOperationException();
			}
		});
		assertNotNull(lineSequence);
		assertEquals(TransformLineSequence.class, lineSequence.getClass());
	}

	@Test
	public void empty() {
		assertNoLinesRemain(LineSequence.create(""));
	}

	@Test
	public void oneLine() {
		assertOneLine(LineSequence.create("a"));
	}

	@Test
	public void twoLines() {
		assertTwoLines(LineSequence.create("abc\r\ndefg"));
	}

	@Test
	public void toStringTest() {
		assertEquals("LineSequence{currentLine=Line{lineNumber=0, offset=0, text=a}, nextLine=null}",
				LineSequence.create("a\n").toString());
		assertEquals(
				"LineSequence{currentLine=Line{lineNumber=0, offset=0, text=a}, nextLine=Line{lineNumber=1, offset=2, text=b}}",
				LineSequence.create("a\nb").toString());
	}

	@Test
	public void iteratorWithPredicate() {
		assertFalse(LineSequence.create("").with(Predicates.<Line> alwaysTrue()).iterator().hasNext());
		assertFalse(LineSequence.create("a").with(Predicates.<Line> alwaysFalse()).iterator().hasNext());

		List<String> strings = new ArrayList<>();
		for (Line line : LineSequence.create("a\nb\nc\na").with(new Predicate<Line>() {

			@Override
			public boolean apply(Line input) {
				return !input.getText().equals("c");
			}
		})) {
			strings.add(line.getText());
		}
		assertEquals(ImmutableList.of("a", "b"), strings);
	}

	@Test
	public void withPredicate() {
		LineSequence originalLineSequence = LineSequence.create("one\ntwo\nthree\nfour");
		LineSequence lineSequence = originalLineSequence.with(Predicates.not(new Predicate<Line>() {

			@Override
			public boolean apply(Line input) {
				return input.getText().equals("three");
			}
		}));
		assertEquals("one", lineSequence.getCurrentLine().getText());
		lineSequence.advance();
		assertEquals("two", lineSequence.getCurrentLine().getText());
		lineSequence.advance();
		assertNull(lineSequence.getCurrentLine());
		lineSequence.advance();
		assertNull(lineSequence.getCurrentLine());
		assertEquals("three", originalLineSequence.getCurrentLine().getText());
	}

	@Test
	public void advance() {
		assertAdvance(LineSequence.create("one"));
	}

	@Test
	public void advanceWithCount() {
		LineSequence lineSequence = LineSequence.create("one\ntwo\nthree");
		lineSequence.advance(0);
		assertEquals("one", lineSequence.getCurrentLine().getText());
		lineSequence.advance(2);
		assertEquals("three", lineSequence.getCurrentLine().getText());
	}

	@Test
	public void advanceWithCountNegative() {
		LineSequence lineSequence = LineSequence.create("one");
		thrown.expect(IllegalArgumentException.class);
		lineSequence.advance(-1);
	}

	@Test
	public void lookAhead() {
		assertLookAhead(LineSequence.create("a\nb\nc"));
	}

	@Test
	public void lookAheadFailsFast() {
		assertLookAheadFailsFast(LineSequence.create("a\nb\nc"));
	}

	private void assertLookAheadFailsFast(LineSequence lineSequence) {
		LineSequence lookAhead = lineSequence.lookAhead();
		lineSequence.advance();
		thrown.expect(IllegalStateException.class);
		lookAhead.advance();
	}

	private void assertAdvance(LineSequence lineSequence) {
		lineSequence.advance();
		assertNoLinesRemain(lineSequence);
		lineSequence.advance();
		assertNoLinesRemain(lineSequence);
		lineSequence.advance();
		assertNoLinesRemain(lineSequence);
	}

	private void assertNoLinesRemain(LineSequence lineSequence) {
		assertNull(lineSequence.getCurrentLine());
		assertNull(lineSequence.getNextLine());
	}

	private void assertLookAhead(LineSequence lineSequence) {
		lineSequence.advance();
		assertEquals("b", lineSequence.getCurrentLine().getText());
		LineSequence lookAhead = lineSequence.lookAhead();
		assertEquals(lineSequence.getCurrentLine(), lookAhead.getCurrentLine());
		lookAhead.advance();
		assertEquals("b", lineSequence.getCurrentLine().getText());
		assertEquals("c", lookAhead.getCurrentLine().getText());
		LineSequence lookAhead2 = lookAhead.lookAhead();
		assertNotNull(lookAhead2);
		assertNotSame(lookAhead, lookAhead2);
		assertNotSame(lookAhead2, lookAhead.lookAhead());
		lookAhead.advance();
		assertEquals("c", lookAhead2.getCurrentLine().getText());
		assertNoLinesRemain(lookAhead);
		assertNoLinesRemain(lookAhead.lookAhead());
		assertEquals("b", lineSequence.getCurrentLine().getText());
		lineSequence.advance();
		assertEquals("c", lineSequence.getCurrentLine().getText());
	}

	private void assertTwoLines(LineSequence lineSequence) {
		Line currentLine = lineSequence.getCurrentLine();
		assertNotNull(currentLine);
		assertEquals("abc", currentLine.getText());
		assertEquals(0, currentLine.getOffset());
		assertEquals(0, currentLine.getLineNumber());
		assertSame(currentLine, lineSequence.getCurrentLine());
		Line nextLine = lineSequence.getNextLine();
		assertNotNull(nextLine);
		assertEquals("defg", nextLine.getText());
		assertEquals(5, nextLine.getOffset());
		assertEquals(1, nextLine.getLineNumber());
		assertSame(nextLine, lineSequence.getNextLine());

		lineSequence.advance();

		assertNotSame(currentLine, lineSequence.getCurrentLine());
		assertNotNull(lineSequence.getCurrentLine());
		assertEquals("defg", lineSequence.getCurrentLine().getText());
		assertNull(lineSequence.getNextLine());

		lineSequence.advance();

		assertNoLinesRemain(lineSequence);
	}

	private void assertOneLine(LineSequence lineSequence) {
		Line currentLine = lineSequence.getCurrentLine();
		assertNotNull(currentLine);
		assertEquals("a", currentLine.getText());
		assertSame(currentLine, lineSequence.getCurrentLine());
		assertNull(lineSequence.getNextLine());
		lineSequence.advance();
		assertNoLinesRemain(lineSequence);
	}
}
