/*********************************************************************
 * Copyright (c) 2010 Sony Ericsson/ST Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      Sony Ericsson/ST Ericsson - initial API and implementation
 *      Tasktop Technologies - improvements
 *********************************************************************/
package org.eclipse.mylyn.internal.gerrit.core;

import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.internal.gerrit.core.client.GerritClient;
import org.eclipse.mylyn.internal.gerrit.core.client.GerritException;
import org.eclipse.mylyn.internal.gerrit.core.client.GerritHttpException;
import org.eclipse.mylyn.internal.gerrit.core.client.GerritLoginException;
import org.eclipse.mylyn.internal.gerrit.core.client.GerritSystemInfo;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;
import org.eclipse.osgi.util.NLS;

import com.google.gerrit.common.data.ChangeInfo;
import com.google.gerrit.common.data.GerritConfig;
import com.google.gwtorm.client.KeyUtil;
import com.google.gwtorm.server.StandardKeyEncoder;

/**
 * The Gerrit connector core.
 * 
 * @author Mikael Kober
 * @author Thomas Westling
 */
public class GerritConnector extends AbstractRepositoryConnector {

	static {
		KeyUtil.setEncoderImpl(new StandardKeyEncoder());
	}

	/**
	 * Prefix for task id in a task-url: http://[gerrit-repository]/#change,[task.id].
	 */
	public static final String CHANGE_PREFIX = "/#change,"; //$NON-NLS-1$

	/**
	 * Connector kind
	 */
	public static final String CONNECTOR_KIND = "org.eclipse.mylyn.gerrit"; //$NON-NLS-1$

	/**
	 * Label for the connector.
	 */
	public static final String CONNECTOR_LABEL = "Gerrit Code Review"; //$NON-NLS-1$

	private static final String KEY_REPOSITORY_CONFIG = CONNECTOR_KIND + ".config"; //$NON-NLS-1$

	private final GerritTaskDataHandler taskDataHandler = new GerritTaskDataHandler(this);

	private TaskRepositoryLocationFactory taskRepositoryLocationFactory = new TaskRepositoryLocationFactory();

	public GerritConnector() {
		if (GerritCorePlugin.getDefault() != null) {
			GerritCorePlugin.getDefault().setConnector(this);
		}
	}

	/**
	 * Not supported, yet.
	 */
	@Override
	public boolean canCreateNewTask(TaskRepository arg0) {
		return false;
	}

	@Override
	public boolean canCreateTaskFromKey(TaskRepository arg0) {
		return false;
	}

	public GerritClient getClient(TaskRepository repository) {
		return createClient(repository);
	}

	@Override
	public String getConnectorKind() {
		return CONNECTOR_KIND;
	}

	@Override
	public String getLabel() {
		return "Gerrit Code Review (supports 2.1.5 and later)";
	}

	/**
	 * Not supported, yet.
	 */
	@Override
	public String getRepositoryUrlFromTaskUrl(String url) {
		return null;
	}

	@Override
	public TaskData getTaskData(TaskRepository repository, String taskId, IProgressMonitor monitor)
			throws CoreException {
		return taskDataHandler.getTaskData(repository, taskId, monitor);
	}

	@Override
	public AbstractTaskDataHandler getTaskDataHandler() {
		return taskDataHandler;
	}

	@Override
	public String getTaskIdFromTaskUrl(String url) {
		// example: https://review.sonyericsson.net/#change,14175
		if ((url != null) && (url.length() > 0)) {
			int index = url.indexOf(CHANGE_PREFIX);
			if (index > 0) {
				return url.substring(index + CHANGE_PREFIX.length());
			}
		}
		return null;
	}

	@Override
	public ITaskMapping getTaskMapping(TaskData taskData) {
		return new TaskMapper(taskData);
	}

	public synchronized TaskRepositoryLocationFactory getTaskRepositoryLocationFactory() {
		return taskRepositoryLocationFactory;
	}

	@Override
	public String getTaskUrl(String repositoryUrl, String taskId) {
		// return null;
		String url = repositoryUrl + CHANGE_PREFIX + taskId;
		return ((repositoryUrl != null) && (taskId != null)) ? url : null;
	}

	@Override
	public boolean hasTaskChanged(TaskRepository repository, ITask task, TaskData taskData) {
		ITaskMapping taskMapping = getTaskMapping(taskData);
		Date repositoryDate = taskMapping.getModificationDate();
		Date localDate = task.getModificationDate();
		if (repositoryDate != null && repositoryDate.equals(localDate)) {
			return false;
		}
		return true;
	}

	@Override
	public IStatus performQuery(TaskRepository repository, IRepositoryQuery query, TaskDataCollector resultCollector,
			ISynchronizationSession session, IProgressMonitor monitor) {
		try {
			monitor.beginTask("Executing query", IProgressMonitor.UNKNOWN);
			GerritClient client = getClient(repository);
			client.refreshConfigOnce(monitor);
			List<ChangeInfo> result = null;
			if (GerritQuery.ALL_OPEN_CHANGES.equals(query.getAttribute(GerritQuery.TYPE))) {
				result = client.queryAllReviews(monitor);
			} else if (GerritQuery.MY_CHANGES.equals(query.getAttribute(GerritQuery.TYPE))) {
				result = client.queryMyReviews(monitor);
			} else if (GerritQuery.OPEN_CHANGES_BY_PROJECT.equals(query.getAttribute(GerritQuery.TYPE))) {
				String project = query.getAttribute(GerritQuery.PROJECT);
				result = client.queryByProject(monitor, project);
			}

			if (result != null) {
				for (ChangeInfo changeInfo : result) {
					TaskData taskData = taskDataHandler.createTaskData(repository, changeInfo.getId() + "", monitor); //$NON-NLS-1$
					taskData.setPartial(true);
					taskDataHandler.updateTaskData(taskData, changeInfo);
					resultCollector.accept(taskData);
				}
				return Status.OK_STATUS;
			} else {
				return new Status(IStatus.ERROR, GerritCorePlugin.PLUGIN_ID, NLS.bind("Unknows query type: {0}",
						query.getAttribute(GerritQuery.PROJECT)));
			}
		} catch (UnsupportedClassVersionError e) {
			return toStatus(repository, e);
		} catch (GerritException e) {
			return toStatus(repository, e);
		} finally {
			monitor.done();
		}
	}

	public synchronized void setTaskRepositoryLocationFactory(
			TaskRepositoryLocationFactory taskRepositoryLocationFactory) {
		this.taskRepositoryLocationFactory = taskRepositoryLocationFactory;
	}

	@Override
	public void updateRepositoryConfiguration(TaskRepository repository, IProgressMonitor monitor) throws CoreException {
		try {
			getClient(repository).refreshConfig(monitor);
		} catch (GerritException e) {
			throw toCoreException(repository, e);
		}
	}

	@Override
	public void updateTaskFromTaskData(TaskRepository taskRepository, ITask task, TaskData taskData) {
		TaskMapper mapper = (TaskMapper) getTaskMapping(taskData);
		mapper.applyTo(task);
	}

	public GerritSystemInfo validate(TaskRepository repository, IProgressMonitor monitor) throws CoreException {
		try {
			return createClient(repository).getInfo(Policy.backgroundMonitorFor(monitor));
		} catch (UnsupportedClassVersionError e) {
			throw toCoreException(repository, e);
		} catch (GerritException e) {
			throw toCoreException(repository, e);
		}
	}

	private GerritClient createClient(final TaskRepository repository) {
		GerritConfig config = GerritClient.configFromString(repository.getProperty(KEY_REPOSITORY_CONFIG));
		return new GerritClient(taskRepositoryLocationFactory.createWebLocation(repository), config) {
			@Override
			protected void configurationChanged(GerritConfig config) {
				repository.setProperty(KEY_REPOSITORY_CONFIG, GerritClient.configToString(config));
			}
		};

	}

	CoreException toCoreException(TaskRepository repository, GerritException e) {
		return new CoreException(toStatus(repository, e));
	}

	CoreException toCoreException(TaskRepository repository, UnsupportedClassVersionError e) {
		return new CoreException(toStatus(repository, e));
	}

	Status toStatus(TaskRepository repository, GerritException e) {
		String message;
		if (e instanceof GerritHttpException) {
			int code = ((GerritHttpException) e).getResponseCode();
			message = NLS.bind("Unexpected error: {1} ({0})", code, HttpStatus.getStatusText(code));
		} else if (e instanceof GerritLoginException) {
			message = "Login failed";
		} else if (e.getMessage() != null) {
			message = NLS.bind("Unexpected error: {0}", e.getMessage());
		} else {
			message = "Unexpected error while communicating with Gerrit";
		}
		return new Status(IStatus.ERROR, GerritCorePlugin.PLUGIN_ID, message, e);
	}

	Status toStatus(TaskRepository repository, UnsupportedClassVersionError e) {
		String message = NLS.bind("The Gerrit Connector requires at Java 1.6 or higer (installed version: {0})",
				System.getProperty("java.version"));
		return new Status(IStatus.ERROR, GerritCorePlugin.PLUGIN_ID, message, e);
	}

}
