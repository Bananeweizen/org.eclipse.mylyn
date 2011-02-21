/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.gerrit.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.mylyn.internal.gerrit.core.client.GerritChange;
import org.eclipse.mylyn.internal.gerrit.core.client.GerritException;
import org.eclipse.mylyn.internal.gerrit.core.client.JSonSupport;
import org.eclipse.mylyn.reviews.core.model.IComment;
import org.eclipse.mylyn.reviews.core.model.IFileItem;
import org.eclipse.mylyn.reviews.core.model.IFileRevision;
import org.eclipse.mylyn.reviews.core.model.ILineLocation;
import org.eclipse.mylyn.reviews.core.model.ILineRange;
import org.eclipse.mylyn.reviews.core.model.IReview;
import org.eclipse.mylyn.reviews.core.model.IReviewItem;
import org.eclipse.mylyn.reviews.core.model.IReviewItemSet;
import org.eclipse.mylyn.reviews.core.model.ITopic;
import org.eclipse.mylyn.reviews.core.model.IUser;
import org.eclipse.mylyn.reviews.internal.core.model.ReviewsFactory;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.osgi.util.NLS;

import com.google.gerrit.common.data.AccountInfo;
import com.google.gerrit.common.data.AccountInfoCache;
import com.google.gerrit.common.data.ChangeDetail;
import com.google.gerrit.common.data.CommentDetail;
import com.google.gerrit.common.data.PatchScript;
import com.google.gerrit.common.data.PatchSetDetail;
import com.google.gerrit.prettify.common.SparseFileContent;
import com.google.gerrit.reviewdb.Account.Id;
import com.google.gerrit.reviewdb.Patch;
import com.google.gerrit.reviewdb.PatchLineComment;
import com.google.gerrit.reviewdb.PatchSet;

/**
 * @author Steffen Pingel
 */
public class GerritUtil {

	private static final ReviewsFactory FACTORY = ReviewsFactory.eINSTANCE;

	public static GerritChange getChange(TaskData taskData) {
		JSonSupport json = new JSonSupport();
		TaskAttribute attribute = taskData.getRoot().getAttribute(GerritTaskSchema.getDefault().OBJ_REVIEW.getKey());
		if (attribute != null) {
			return json.getGson().fromJson(attribute.getValue(), GerritChange.class);
		}
		return null;
	}

	public static IReview toReview(ChangeDetail detail) throws GerritException {
		IReview review = FACTORY.createReview();
		review.setId(detail.getChange().getId().get() + "");
		List<PatchSet> patchSets = detail.getPatchSets();
		for (PatchSet patchSet : patchSets) {
			IReviewItemSet itemSet = FACTORY.createReviewItemSet();
			itemSet.setName(NLS.bind("Patch Set {0}", patchSet.getPatchSetId()));
			itemSet.setId(patchSet.getPatchSetId() + "");
			itemSet.setAddedBy(createUser(patchSet.getUploader(), detail.getAccounts()));
			itemSet.setRevision(patchSet.getRevision().get());
			itemSet.setReview(review);
			review.getItems().add(itemSet);
		}
		return review;
	}

	public static List<IReviewItem> toReviewItems(PatchSetDetail detail,
			Map<Patch.Key, PatchScript> patchScriptByPatchKey) {
		List<IReviewItem> items = new ArrayList<IReviewItem>();
		for (Patch patch : detail.getPatches()) {
			IFileItem item = FACTORY.createFileItem();
			item.setName(patch.getFileName());
			items.add(item);

			if (patchScriptByPatchKey != null) {
				PatchScript patchScript = patchScriptByPatchKey.get(patch.getKey());
				if (patchScript != null) {
					CommentDetail commentDetail = patchScript.getCommentDetail();

					IFileRevision revisionA = FACTORY.createFileRevision();
					revisionA.setContent(patchScript.getA().asString());
					revisionA.setPath(patchScript.getA().getPath());
					revisionA.setRevision("Base");
					addComments(revisionA, commentDetail.getCommentsA(), commentDetail.getAccounts());
					item.setBase(revisionA);

					IFileRevision revisionB = FACTORY.createFileRevision();
					SparseFileContent target = patchScript.getB().apply(patchScript.getA(), patchScript.getEdits());
					revisionB.setContent(target.asString());
					revisionB.setPath(patchScript.getB().getPath());
					revisionB.setRevision(NLS.bind("Patch Set {0}", detail.getPatchSet().getPatchSetId()));
					addComments(revisionB, commentDetail.getCommentsB(), commentDetail.getAccounts());
					item.setTarget(revisionB);
				}
			}
		}
		return items;
	}

	private static void addComments(IFileRevision revision, List<PatchLineComment> comments,
			AccountInfoCache accountInfoCache) {
		if (comments == null) {
			return;
		}
		for (PatchLineComment comment : comments) {
			ILineRange line = FACTORY.createLineRange();
			line.setStart(comment.getLine());
			line.setEnd(comment.getLine());
			ILineLocation location = FACTORY.createLineLocation();
			location.getRanges().add(line);

			IUser author = GerritUtil.createUser(comment.getAuthor(), accountInfoCache);

			IComment topicComment = FACTORY.createComment();
			topicComment.setAuthor(author);
			topicComment.setCreationDate(comment.getWrittenOn());
			topicComment.setDescription(comment.getMessage());

			ITopic topic = FACTORY.createTopic();
			topic.setAuthor(author);
			topic.setCreationDate(comment.getWrittenOn());
			topic.setLocation(location);
			topic.setItem(revision);
			topic.setDescription(comment.getMessage());
			topic.getComments().add(topicComment);

			revision.getTopics().add(topic);
		}
	}

	public static IUser createUser(Id id, AccountInfoCache accountInfoCache) {
		AccountInfo info = accountInfoCache.get(id);
		IUser user = FACTORY.createUser();
		user.setDisplayName(info.getFullName());
		user.setId(Integer.toString(id.get()));
		return user;
	}

}
