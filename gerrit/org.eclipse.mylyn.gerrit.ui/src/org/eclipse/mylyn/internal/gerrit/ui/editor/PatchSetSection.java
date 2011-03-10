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

package org.eclipse.mylyn.internal.gerrit.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.mylyn.internal.gerrit.core.GerritConnector;
import org.eclipse.mylyn.internal.gerrit.core.GerritTaskSchema;
import org.eclipse.mylyn.internal.gerrit.core.GerritUtil;
import org.eclipse.mylyn.internal.gerrit.core.client.GerritChange;
import org.eclipse.mylyn.internal.gerrit.core.client.GerritClient;
import org.eclipse.mylyn.internal.gerrit.core.client.GerritException;
import org.eclipse.mylyn.internal.gerrit.core.client.GerritPatchSetContent;
import org.eclipse.mylyn.internal.gerrit.ui.GerritUiPlugin;
import org.eclipse.mylyn.internal.gerrit.ui.operations.AbandonDialog;
import org.eclipse.mylyn.internal.gerrit.ui.operations.PublishDialog;
import org.eclipse.mylyn.internal.gerrit.ui.operations.RestoreDialog;
import org.eclipse.mylyn.internal.gerrit.ui.operations.SubmitDialog;
import org.eclipse.mylyn.internal.reviews.ui.annotations.ReviewCompareAnnotationModel;
import org.eclipse.mylyn.internal.reviews.ui.operations.ReviewCompareEditorInput;
import org.eclipse.mylyn.reviews.core.model.IFileItem;
import org.eclipse.mylyn.reviews.core.model.IReviewItem;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.google.gerrit.common.data.ChangeDetail;
import com.google.gerrit.common.data.PatchSetDetail;
import com.google.gerrit.common.data.PatchSetPublishDetail;
import com.google.gerrit.reviewdb.ApprovalCategory;
import com.google.gerrit.reviewdb.Patch;
import com.google.gerrit.reviewdb.PatchSet;
import com.google.gerrit.reviewdb.PatchSet.Id;

/**
 * @author Steffen Pingel
 */
public class PatchSetSection extends AbstractGerritSection {

	private class GetPatchSetContentJob extends Job {

		private GerritPatchSetContent patchSetContent;

		private final PatchSetDetail patchSetDetail;

		private final TaskRepository repository;

		public GetPatchSetContentJob(TaskRepository repository, PatchSetDetail patchSetDetail) {
			super("Caching Patch Set Content");
			this.repository = repository;
			this.patchSetDetail = patchSetDetail;
		}

		public GerritPatchSetContent getPatchSetContent() {
			return patchSetContent;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			GerritConnector connector = (GerritConnector) TasksUi.getRepositoryConnector(repository.getConnectorKind());
			GerritClient client = connector.getClient(repository);
			try {
				int reviewId = patchSetDetail.getInfo().getKey().getParentKey().get();
				patchSetContent = client.getPatchSetContent(
						reviewId + "", patchSetDetail.getPatchSet().getId().get(), monitor); //$NON-NLS-1$
			} catch (OperationCanceledException e) {
				return Status.CANCEL_STATUS;
			} catch (GerritException e) {
				return new Status(IStatus.ERROR, GerritUiPlugin.PLUGIN_ID, "Review retrieval failed", e);
			}
			return Status.OK_STATUS;
		}
	}

	private Composite composite;

	private final List<Job> jobs;

	private FormToolkit toolkit;

	public PatchSetSection() {
		setPartName("Patch Sets");
		jobs = new ArrayList<Job>();
	}

	@Override
	public void dispose() {
		for (Job job : jobs) {
			job.cancel();
		}
		super.dispose();
	}

	public String getTextClientText(final PatchSetDetail patchSetDetail) {
		int numComments = getNumComments(patchSetDetail);
		if (numComments > 0) {
			return NLS.bind("{0} Comments", numComments);
		} else {
			return " ";
		}
	}

	@Override
	public void initialize(AbstractTaskEditorPage taskEditorPage) {
		super.initialize(taskEditorPage);
	}

	private Composite createActions(final ChangeDetail changeDetail, final PatchSetDetail patchSetDetail,
			final PatchSetPublishDetail publishDetail, Composite composite) {
		Composite buttonComposite = new Composite(composite, SWT.NONE);
		RowLayout layout = new RowLayout();
		layout.center = true;
		layout.spacing = 10;
		buttonComposite.setLayout(layout);

		boolean canPublish = getTaskData().getAttributeMapper().getBooleanValue(
				getTaskData().getRoot().getAttribute(GerritTaskSchema.getDefault().CAN_PUBLISH.getKey()));
		boolean canSubmit = false;
		if (changeDetail.getCurrentActions() != null) {
			canSubmit = changeDetail.getCurrentActions().contains(ApprovalCategory.SUBMIT);
		}

		if (canPublish) {
			Button publishButton = toolkit.createButton(buttonComposite, "Publish Comments...", SWT.PUSH);
			publishButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					doPublish(publishDetail);
				}
			});
		}

		if (canSubmit) {
			Button submitButton = toolkit.createButton(buttonComposite, "Submit", SWT.PUSH);
			submitButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					doSubmit(patchSetDetail.getPatchSet());
				}
			});
		}

		if (changeDetail != null && changeDetail.isCurrentPatchSet(patchSetDetail)) {
			if (changeDetail.canAbandon()) {
				Button abondonButton = toolkit.createButton(buttonComposite, "Abandon...", SWT.PUSH);
				abondonButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						doAbandon(patchSetDetail.getPatchSet());
					}
				});
			} else if (changeDetail.canRestore()) {
				Button restoreButton = toolkit.createButton(buttonComposite, "Restore...", SWT.PUSH);
				restoreButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						doRestore(patchSetDetail.getPatchSet());
					}
				});
			}
		}
		return buttonComposite;
	}

	private void createSubSection(final ChangeDetail changeDetail, final PatchSetDetail patchSetDetail,
			final PatchSetPublishDetail publishDetail, Section section) {
		int style = ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT
				| ExpandableComposite.LEFT_TEXT_CLIENT_ALIGNMENT;
		if (changeDetail.isCurrentPatchSet(patchSetDetail)) {
			style |= ExpandableComposite.EXPANDED;
		}
		final Section subSection = toolkit.createSection(composite, style);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(subSection);
		subSection.setText(NLS.bind("Patch Set {0}", patchSetDetail.getPatchSet().getId().get()));

		String message = getTextClientText(patchSetDetail);
		addTextClient(toolkit, subSection, message);

		if (subSection.isExpanded()) {
			createSubSectionContents(changeDetail, patchSetDetail, publishDetail, subSection);
		}
		subSection.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				if (subSection.getClient() == null) {
					createSubSectionContents(changeDetail, patchSetDetail, publishDetail, subSection);
				}
			}
		});
	}

	private int getNumComments(PatchSetDetail patchSetDetail) {
		int numComments = 0;
		for (Patch patch : patchSetDetail.getPatches()) {
			numComments += patch.getCommentCount();
		}
		return numComments;
	}

	private void subSectionExpanded(final PatchSetDetail patchSetDetail, final Section composite, final Viewer viewer) {
		final Label progressLabel = (Label) composite.getTextClient();
		final String message = "  Caching contents...";
		progressLabel.setText(message);
		progressLabel.setVisible(true);

		final GetPatchSetContentJob job = new GetPatchSetContentJob(getTaskEditorPage().getTaskRepository(),
				patchSetDetail);
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (getControl() != null && !getControl().isDisposed()) {
							if (event.getResult().isOK()) {
								GerritPatchSetContent content = job.getPatchSetContent();
								if (content != null && content.getPatchScriptByPatchKey() != null) {
									List<IReviewItem> items = GerritUtil.toReviewItems(patchSetDetail,
											content.getPatchScriptByPatchKey());
									viewer.setInput(items);
								}
							}

							progressLabel.setText("  " + getTextClientText(patchSetDetail));
							progressLabel.setVisible(!composite.isExpanded());
							getTaskEditorPage().reflow();
						}
					}
				});
			}
		});
		jobs.add(job);
		job.schedule();
	}

	@Override
	protected Control createContent(FormToolkit toolkit, Composite parent) {
		this.toolkit = toolkit;

		composite = toolkit.createComposite(parent);
		GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 5).applyTo(composite);

		GerritChange change = GerritUtil.getChange(getTaskData());
		if (change != null) {
			for (PatchSetDetail patchSetDetail : change.getPatchSetDetails()) {
				Id patchSetId = patchSetDetail.getPatchSet().getId();
				PatchSetPublishDetail publishDetail = change.getPublishDetailByPatchSetId().get(patchSetId);
				createSubSection(change.getChangeDetail(), patchSetDetail, publishDetail, getSection());
			}
		}

		return composite;
	}

	protected void doAbandon(PatchSet patchSet) {
		AbandonDialog dialog = new AbandonDialog(getShell(), getTask(), patchSet);
		openOperationDialog(dialog);
	}

	protected void doPublish(PatchSetPublishDetail publishDetail) {
		PublishDialog dialog = new PublishDialog(getShell(), getTask(), publishDetail);
		openOperationDialog(dialog);
	}

	protected void doRestore(PatchSet patchSet) {
		RestoreDialog dialog = new RestoreDialog(getShell(), getTask(), patchSet);
		openOperationDialog(dialog);
	}

	protected void doSubmit(PatchSet patchSet) {
		SubmitDialog dialog = new SubmitDialog(getShell(), getTask(), patchSet);
		openOperationDialog(dialog);
	}

	@Override
	protected boolean shouldExpandOnCreate() {
		return true;
	}

	void createSubSectionContents(final ChangeDetail changeDetail, final PatchSetDetail patchSetDetail,
			PatchSetPublishDetail publishDetail, Section subSection) {
		Composite composite = toolkit.createComposite(subSection);
		GridLayoutFactory.fillDefaults().applyTo(composite);
		subSection.setClient(composite);

		TableViewer viewer = new TableViewer(composite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL | SWT.VIRTUAL);
		GridDataFactory.fillDefaults().grab(true, true).hint(500, SWT.DEFAULT).applyTo(viewer.getControl());
		viewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {
				// ignore					
			}

			public Object[] getElements(Object inputElement) {
				return ((List) inputElement).toArray();
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// ignore
			}
		});
		viewer.setLabelProvider(new DelegatingStyledCellLabelProvider(new ReviewItemLabelProvider()));
		viewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				IFileItem item = (IFileItem) selection.getFirstElement();
				ReviewCompareAnnotationModel model = new ReviewCompareAnnotationModel(item, null);
				CompareConfiguration configuration = new CompareConfiguration();
				CompareUI.openCompareEditor(new ReviewCompareEditorInput(item, model, configuration));
			}
		});

		List<IReviewItem> items = GerritUtil.toReviewItems(patchSetDetail, null);
		viewer.setInput(items);

		Composite actionComposite = createActions(changeDetail, patchSetDetail, publishDetail, composite);

		subSectionExpanded(patchSetDetail, subSection, viewer);

		getTaskEditorPage().reflow();
	}

}
