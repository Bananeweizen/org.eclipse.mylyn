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

package org.eclipse.mylyn.builds.core;

/**
 * @author Steffen Pingel
 */
// FIXME rename class to something less confusing
public enum BuildStatus {

	// FIXME rename FAILED to FAILURE
	FAILED, UNSTABLE, SUCCESS, DISABLED, ABORTED;

}
