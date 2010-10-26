/*******************************************************************************
 * Copyright (c) 2007, 2010 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.provisional.commons.ui.commands;

import org.eclipse.ui.texteditor.DeleteLineAction;

/**
 * Command handler for cut line command (to beginning)
 * 
 * @author David Green
 */
public class CutLineToBeginningHandler extends AbstractDeleteLineHandler {
	public CutLineToBeginningHandler() {
		super(DeleteLineAction.TO_BEGINNING, true);
	}
}
