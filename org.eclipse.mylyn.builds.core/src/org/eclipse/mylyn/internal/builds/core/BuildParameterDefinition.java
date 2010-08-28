/**
 * <copyright>
 * </copyright>
 *
 * $Id: BuildParameterDefinition.java,v 1.2 2010/08/28 03:38:02 spingel Exp $
 */
package org.eclipse.mylyn.internal.builds.core;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;

import org.eclipse.mylyn.builds.core.IBuildModel;
import org.eclipse.mylyn.builds.core.IBuildPlan;
import org.eclipse.mylyn.builds.core.IBuildServer;

import java.util.List;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Parameter Definition</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.mylyn.internal.builds.core.BuildParameterDefinition#getBuildPlanId <em>Build Plan Id</em>}</li>
 * <li>{@link org.eclipse.mylyn.internal.builds.core.BuildParameterDefinition#getBuildPlan <em>Build Plan</em>}</li>
 * </ul>
 * </p>
 * 
 * @see org.eclipse.mylyn.internal.builds.core.BuildPackage#getBuildParameterDefinition()
 * @model kind="class"
 * @generated
 */
public class BuildParameterDefinition extends ParameterDefinition {
	/**
	 * The default value of the '{@link #getBuildPlanId() <em>Build Plan Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @see #getBuildPlanId()
	 * @generated
	 * @ordered
	 */
	protected static final String BUILD_PLAN_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getBuildPlanId() <em>Build Plan Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @see #getBuildPlanId()
	 * @generated
	 * @ordered
	 */
	protected String buildPlanId = BUILD_PLAN_ID_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected BuildParameterDefinition() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return BuildPackage.Literals.BUILD_PARAMETER_DEFINITION;
	}

	/**
	 * Returns the value of the '<em><b>Build Plan Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Build Plan Id</em>' attribute isn't clear, there really should be more of a
	 * description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Build Plan Id</em>' attribute.
	 * @see #setBuildPlanId(String)
	 * @see org.eclipse.mylyn.internal.builds.core.BuildPackage#getBuildParameterDefinition_BuildPlanId()
	 * @model
	 * @generated
	 */
	public String getBuildPlanId() {
		return buildPlanId;
	}

	/**
	 * Sets the value of the '{@link org.eclipse.mylyn.internal.builds.core.BuildParameterDefinition#getBuildPlanId
	 * <em>Build Plan Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @param value
	 *            the new value of the '<em>Build Plan Id</em>' attribute.
	 * @see #getBuildPlanId()
	 * @generated
	 */
	public void setBuildPlanId(String newBuildPlanId) {
		String oldBuildPlanId = buildPlanId;
		buildPlanId = newBuildPlanId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET,
					BuildPackage.BUILD_PARAMETER_DEFINITION__BUILD_PLAN_ID, oldBuildPlanId, buildPlanId));
	}

	/**
	 * Returns the value of the '<em><b>Build Plan</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Build Plan</em>' reference isn't clear, there really should be more of a description
	 * here...
	 * </p>
	 * <!-- end-user-doc -->
	 * 
	 * @return the value of the '<em>Build Plan</em>' reference.
	 * @see org.eclipse.mylyn.internal.builds.core.BuildPackage#getBuildParameterDefinition_BuildPlan()
	 * @model type="org.eclipse.mylyn.internal.builds.core.IBuildPlan" transient="true" changeable="false"
	 *        volatile="true" derived="true"
	 * @generated
	 */
	public IBuildPlan getBuildPlan() {
		IBuildPlan buildPlan = basicGetBuildPlan();
		return buildPlan != null && ((EObject) buildPlan).eIsProxy() ? (IBuildPlan) eResolveProxy((InternalEObject) buildPlan)
				: buildPlan;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated NOT
	 */
	public IBuildPlan basicGetBuildPlan() {
		String buildPlanId = getBuildPlanId();
		if (buildPlanId == null) {
			return null;
		}

		IBuildPlan containingBuildPlan = getContainingBuildPlan();
		if (containingBuildPlan != null) {
			IBuildServer server = containingBuildPlan.getServer(); // TODO Consider derived EReference topLevelBuildPlan
			if (server instanceof EObject) {
				EObject container = ((EObject) server).eContainer(); // TODO Consider eOpposite for IBuildModel.plans, ...
				if (container instanceof IBuildModel) {
					List<IBuildPlan> plans = ((IBuildModel) container).getPlans();
					for (IBuildPlan plan : plans) {
						if (buildPlanId.equals(plan.getId())) {
							return plan;
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
		case BuildPackage.BUILD_PARAMETER_DEFINITION__BUILD_PLAN_ID:
			return getBuildPlanId();
		case BuildPackage.BUILD_PARAMETER_DEFINITION__BUILD_PLAN:
			if (resolve)
				return getBuildPlan();
			return basicGetBuildPlan();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
		case BuildPackage.BUILD_PARAMETER_DEFINITION__BUILD_PLAN_ID:
			setBuildPlanId((String) newValue);
			return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
		case BuildPackage.BUILD_PARAMETER_DEFINITION__BUILD_PLAN_ID:
			setBuildPlanId(BUILD_PLAN_ID_EDEFAULT);
			return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
		case BuildPackage.BUILD_PARAMETER_DEFINITION__BUILD_PLAN_ID:
			return BUILD_PLAN_ID_EDEFAULT == null ? buildPlanId != null : !BUILD_PLAN_ID_EDEFAULT.equals(buildPlanId);
		case BuildPackage.BUILD_PARAMETER_DEFINITION__BUILD_PLAN:
			return basicGetBuildPlan() != null;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy())
			return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (buildPlanId: "); //$NON-NLS-1$
		result.append(buildPlanId);
		result.append(')');
		return result.toString();
	}

} // BuildParameterDefinition
