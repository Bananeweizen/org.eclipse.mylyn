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
package org.eclipse.mylar.ui.internal;

import java.util.ArrayList;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylar.ui.MylarImages;
import org.eclipse.mylar.ui.MylarUiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.dialogs.IWorkingSetPage;


/**
 * @author sminto
 */
public class MylarWorkingSetPage extends WizardPage implements IWorkingSetPage {

	private Text workingSetNameText;
	private IWorkingSet workingSet;

	public MylarWorkingSetPage() {
		super("org.eclipse.mylar.monitor.workingSetPage", "Mylar Task Context Working Set", MylarImages.MYLAR);
		setDescription("Mylar Working Set - All of the files contained in the active taskscape"); 
	}

	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

		Label label= new Label(composite, SWT.WRAP);
		label.setText("Name"); 
		GridData gd= new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(gd);

		workingSetNameText= new Text(composite, SWT.SINGLE | SWT.BORDER);
		workingSetNameText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		workingSetNameText.setFocus();
		workingSetNameText.setEditable(false);
		workingSetNameText.setText("Mylar Task Context");

		label = new Label(composite, SWT.WRAP);
		label.setText("");
		gd= new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(gd);
		
		label = new Label(composite, SWT.WRAP);
		label.setText("NOTE: This working set is dynamically created by Mylar using your current task context.");
		label.setFont(UiUtil.BOLD);
		gd= new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(gd);

		label = new Label(composite, SWT.WRAP);
		label.setText("");
		gd= new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(gd);
		
		label = new Label(composite, SWT.WRAP);
		label.setText("PLEASE DO NOT ACTIVATE THIS WORKING SET AT ANYTIME.\nTHIS WORKING SET IS ONLY USEFUL FOR SEARCHING.");
		label.setFont(UiUtil.BOLD);
		gd= new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		label.setLayoutData(gd);
		
		Dialog.applyDialogFont(composite);
	}

	public IWorkingSet getSelection() {
		return workingSet;
	}

	public void setSelection(IWorkingSet workingSet) {
		// don't need to do anything here
	}

	public void finish() {
		String workingSetName= workingSetNameText.getText();
		ArrayList<IAdaptable> elements= new ArrayList<IAdaptable>(1);
		MylarWorkingSetUpdater.getElementsFromTaskscape(elements);
		if (workingSet == null) {
			IWorkingSetManager workingSetManager= MylarUiPlugin.getDefault().getWorkbench().getWorkingSetManager();
			if((workingSet = workingSetManager.getWorkingSet(workingSetName)) == null){
				workingSet= workingSetManager.createWorkingSet(workingSetName, elements.toArray(new IAdaptable[elements.size()]));
			}
		}
	}

	@Override
	public boolean isPageComplete() {
		String workingSetName= workingSetNameText.getText();
		IWorkingSetManager workingSetManager= MylarUiPlugin.getDefault().getWorkbench().getWorkingSetManager();
		if (workingSetManager.getWorkingSet(workingSetName) != null){
			setErrorMessage("Cannot create another Active Taskscape Working Set");
			return false;
		} else {
			return true;
		}
	}
	
	
}
