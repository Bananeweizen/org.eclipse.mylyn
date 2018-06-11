/*******************************************************************************
 * Copyright (c) 2017 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.wikitext.toolkit;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.common.io.Resources;

/**
 * @since 3.0
 */
public class TestResources {

	public static String load(Class<?> relativeToClass, String path) {
		try {
			URL url = relativeToClass.getResource(path);
			checkNotNull(url, "Resource %s not found relative to %s", path, relativeToClass.getName());
			return Resources.toString(url, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
