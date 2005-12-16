/*******************************************************************************
 * Copyright (c) 2004 - 2005 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.ui.internal.views;

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.mylar.core.IMylarContextListener;
import org.eclipse.mylar.core.MylarPlugin;
import org.eclipse.mylar.tasklist.IQueryHit;
import org.eclipse.mylar.tasklist.ITask;
import org.eclipse.mylar.tasklist.ui.IDynamicSubMenuContributor;
import org.eclipse.mylar.tasklist.ui.ITaskListElement;
import org.eclipse.mylar.tasklist.ui.views.TaskListView;
import org.eclipse.mylar.ui.MylarUiPlugin;
import org.eclipse.mylar.ui.actions.EditHighlightersAction;

/**
 * @author Mik Kersten
 */
public class TaskListHighlighterMenuContributor implements IDynamicSubMenuContributor {

	private static final String CHOOSE_HIGHLIGHTER = "Choose Highlighter";
	
	public MenuManager getSubMenuManager(TaskListView view, ITaskListElement selection) {
		final ITaskListElement selectedElement = selection;
		final TaskListView taskListView = view;
		final MenuManager subMenuManager = new MenuManager(CHOOSE_HIGHLIGHTER);
		for (Iterator<Highlighter> it = MylarUiPlugin.getDefault().getHighlighters().iterator(); it.hasNext();) {
          final Highlighter highlighter = it.next();
          if (selectedElement instanceof ITaskListElement) {
        	  Action action = new Action() {
            	  @Override
              		public void run() {
            		  	ITask task = null;
						if (selectedElement instanceof ITask) {
							task = (ITask) selectedElement;
						} else if (selectedElement instanceof IQueryHit) {
							if (((IQueryHit) selectedElement).getCorrespondingTask() != null) {
								task = ((IQueryHit) selectedElement).getCorrespondingTask();
							}
						}

//            		  	if (!task.isActive()) {
//	        	    		MessageDialog.openError(Workbench.getInstance()
//	        						.getActiveWorkbenchWindow().getShell(), "Mylar Highlighting",
//	        						"Please activate the task before setting a highlighter.");
//	        				return;
//	        	    	} else {
	        	    		MylarUiPlugin.getDefault().setHighlighterMapping(task.getHandleIdentifier(), highlighter.getName());
	        	    		taskListView.getViewer().refresh();
	        	    		MylarPlugin.getContextManager().notifyPostPresentationSettingsChange(IMylarContextListener.UpdateKind.HIGHLIGHTER);
//	        	    	}
            	  }
              };
              if (highlighter.isGradient()) {
                  action.setImageDescriptor(new HighlighterImageDescriptor(highlighter.getBase(), highlighter.getLandmarkColor()));
              } else {
            	  action.setImageDescriptor(new HighlighterImageDescriptor(highlighter.getLandmarkColor(), highlighter.getLandmarkColor()));
              }
              action.setText(highlighter.toString());
              subMenuManager.add(action);
          	}
		} 
		subMenuManager.add(new Separator());
		subMenuManager.add(new EditHighlightersAction());
		return subMenuManager;
  	}
}
