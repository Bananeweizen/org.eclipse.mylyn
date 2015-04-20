/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.ide.ui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.context.ui.AbstractFocusViewAction;
import org.eclipse.mylyn.context.ui.InterestFilter;
import org.eclipse.mylyn.internal.ide.ui.IdeUiBridgePlugin;
import org.eclipse.mylyn.internal.ide.ui.MarkerInterestFilter;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.views.markers.ExtendedMarkersView;
import org.eclipse.ui.internal.views.markers.MarkersTreeViewer;

/**
 * @author Mik Kersten
 * @since 3.0
 */
public abstract class AbstractFocusMarkerViewAction extends AbstractFocusViewAction {

	private StructuredViewer cachedViewer = null;

	public AbstractFocusMarkerViewAction() {
		super(new MarkerInterestFilter(), true, true, false);
	}

	public AbstractFocusMarkerViewAction(InterestFilter interestFilter, boolean manageViewer, boolean manageFilters,
			boolean manageLinking) {
		super(interestFilter, manageFilters, manageFilters, manageLinking);
	}

	/**
	 * @deprecated This functionality is no longer supported
	 */
	@Deprecated
	protected void updateMarkerViewLabelProvider(StructuredViewer viewer) {
	}

	/**
	 * HACK: changing accessibility
	 */
	@Override
	public List<StructuredViewer> getViewers() {
		List<StructuredViewer> viewers = new ArrayList<StructuredViewer>();
		if (cachedViewer == null) {
			try {
				IViewPart viewPart = super.getPartForAction();
				if (viewPart != null) {
					// NOTE: following code is Eclipse 3.4 specific
					Class<?> clazz = ExtendedMarkersView.class;
					Field field = clazz.getDeclaredField("viewer"); //$NON-NLS-1$
					field.setAccessible(true);
					cachedViewer = (MarkersTreeViewer) field.get(viewPart);
				}
			} catch (Exception e) {
				StatusHandler.log(new Status(IStatus.ERROR, IdeUiBridgePlugin.ID_PLUGIN,
						"Could not get problems view viewer", e)); //$NON-NLS-1$
			}
		}
		if (cachedViewer != null) {
			viewers.add(cachedViewer);
		}
		return viewers;
	}
}
