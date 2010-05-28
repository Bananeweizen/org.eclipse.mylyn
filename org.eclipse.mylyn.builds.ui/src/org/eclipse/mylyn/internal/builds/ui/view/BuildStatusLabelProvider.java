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

package org.eclipse.mylyn.internal.builds.ui.view;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.mylyn.builds.core.IBuildPlan;
import org.eclipse.mylyn.internal.builds.ui.BuildImages;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.swt.graphics.Image;

/**
 * @author Steffen Pingel
 */
public class BuildStatusLabelProvider extends ColumnLabelProvider {

	@Override
	public Image getImage(Object element) {
		if (element instanceof IBuildPlan) {
			if (((IBuildPlan) element).getInfo() != null) {
				return CommonImages.getImage(BuildImages.STATUS_PASSED);
			} else {
				return CommonImages.getImage(BuildImages.STATUS_FAILED);
			}
		}
		return null;

	}

	@Override
	public String getText(Object element) {
		return ""; //$NON-NLS-1$
	}

}
