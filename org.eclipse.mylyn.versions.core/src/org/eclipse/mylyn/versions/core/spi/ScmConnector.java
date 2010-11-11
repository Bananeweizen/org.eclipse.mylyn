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

package org.eclipse.mylyn.versions.core.spi;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.versions.core.ChangeSet;
import org.eclipse.mylyn.versions.core.ScmArtifact;
import org.eclipse.mylyn.versions.core.ScmArtifactInfo;
import org.eclipse.mylyn.versions.core.ScmRepository;
import org.eclipse.team.core.history.IFileRevision;

/**
 * @author Steffen Pingel
 */
public abstract class ScmConnector {

	/**
	 * Lookup a remote or local resource.
	 */
	public abstract ScmArtifact getArtifact(ScmArtifactInfo resource, IProgressMonitor monitor) throws CoreException;

	/**
	 * Lookup a local resource.
	 */
	public abstract ScmArtifact getArtifact(IResource resource);

	public abstract ChangeSet getChangeset(ScmRepository repository, IFileRevision revision, IProgressMonitor monitor)
			throws CoreException;

	public abstract List<ChangeSet> getChangeSets(ScmRepository repository, IProgressMonitor monitor)
			throws CoreException;

	public abstract String getProviderId();

	public abstract List<ScmRepository> getRepositories(IProgressMonitor monitor) throws CoreException;

	public abstract ScmRepository getRepository(IResource resource, IProgressMonitor monitor) throws CoreException;

}
