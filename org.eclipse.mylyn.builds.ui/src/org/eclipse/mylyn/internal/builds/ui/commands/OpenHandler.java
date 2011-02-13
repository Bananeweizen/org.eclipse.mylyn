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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.builds.core.IBuild;
import org.eclipse.mylyn.builds.core.IBuildElement;
import org.eclipse.mylyn.builds.ui.BuildsUiConstants;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.builds.ui.BuildsUiInternal;
import org.eclipse.mylyn.internal.builds.ui.BuildsUiPlugin;
import org.eclipse.mylyn.internal.builds.ui.editor.BuildEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Steffen Pingel
 */
public class OpenHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPage page = window.getActivePage();
		if (page == null) {
			throw new ExecutionException("No active workbench window"); //$NON-NLS-1$
		}

		List<IBuildElement> elements = BuildsUiInternal.getElements(event);
		openBuildElements(page, elements);

		return null;
	}

	public static List<IEditorPart> openBuildElements(IWorkbenchPage page, List<?> elements) {
		if (elements.size() > 0) {
			List<IEditorPart> parts = new ArrayList<IEditorPart>(elements.size());
			Object item = elements.get(0);
			BuildEditorInput input = null;
			if (item instanceof IBuild) {
				input = new BuildEditorInput((IBuild) item);
			}
			// TODO bug 324364
//			if (item instanceof IBuildPlan) {
//				input = new BuildEditorInput((IBuildPlan) item);
//			}
			if (input != null) {
				try {
					IEditorPart part = page.openEditor(input, BuildsUiConstants.ID_EDITOR_BUILDS);
					parts.add(part);
				} catch (PartInitException e) {
					StatusHandler.log(new Status(IStatus.ERROR, BuildsUiPlugin.ID_PLUGIN,
							"Unexpected error while opening build", e)); //$NON-NLS-1$
				}
			}
			return parts;
		}
		return Collections.emptyList();
	}

}
