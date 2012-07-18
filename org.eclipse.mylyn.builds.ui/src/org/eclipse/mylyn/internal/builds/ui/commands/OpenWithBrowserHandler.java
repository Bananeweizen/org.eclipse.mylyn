/*******************************************************************************
 * Copyright (c) 2010 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.builds.ui.commands;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.mylyn.builds.core.IBuildElement;
import org.eclipse.mylyn.commons.workbench.browser.BrowserUtil;
import org.eclipse.mylyn.internal.builds.ui.BuildsUiInternal;

/**
 * @author Steffen Pingel
 */
public class OpenWithBrowserHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		List<IBuildElement> elements = BuildsUiInternal.getElements(event);
		if (elements.size() > 0) {
			Object item = elements.get(0);
			if (item instanceof IBuildElement) {
				IBuildElement element = (IBuildElement) item;
				BrowserUtil.openUrl(element.getUrl(), BrowserUtil.NO_RICH_EDITOR);
			}
		}
		return null;
	}

}
