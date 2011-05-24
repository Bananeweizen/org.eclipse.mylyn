/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.builds.ui.navigator;

import org.eclipse.mylyn.internal.provisional.commons.ui.actions.PropertiesAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

/**
 * @author Steffen Pingel
 */
public class BuildNavigatorActionProvider extends CommonActionProvider {

	private PropertiesAction propertiesAction;

	public BuildNavigatorActionProvider() {
	}

	@Override
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);

		this.propertiesAction = new PropertiesAction();
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);

		actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, propertiesAction);
	}

}
