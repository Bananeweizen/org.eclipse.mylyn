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

package org.eclipse.mylyn.internal.ccvs.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.mylyn.versions.core.spi.ScmResourceArtifact;

/**
 * @author Steffen Pingel
 */
public class CvsArtifact extends ScmResourceArtifact {

	public CvsArtifact(CvsConnector connector, IResource resource, String id) {
		super(connector, resource, id);
	}

}
