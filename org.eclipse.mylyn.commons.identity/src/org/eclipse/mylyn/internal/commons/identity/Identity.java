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

package org.eclipse.mylyn.internal.commons.identity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.commons.identity.Account;
import org.eclipse.mylyn.commons.identity.IIdentity;
import org.eclipse.mylyn.commons.identity.IProfile;
import org.eclipse.mylyn.commons.identity.IProfileImage;
import org.eclipse.mylyn.commons.identity.spi.Profile;
import org.eclipse.mylyn.commons.identity.spi.ProfileImage;

/**
 * @author Steffen Pingel
 */
public class Identity implements IIdentity {

	private static final class FutureResult<T> implements Future<T> {

		private final T result;

		private FutureResult(T result) {
			this.result = result;
		}

		public boolean cancel(boolean mayInterruptIfRunning) {
			return true;
		}

		public boolean isCancelled() {
			return false;
		}

		public boolean isDone() {
			return true;
		}

		public T get() throws InterruptedException, ExecutionException {
			return result;
		}

		public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			return result;
		}
	}

	private static abstract class FutureJob<T> extends Job implements Future<T> {

		private boolean cancelled;

		private final AtomicReference<T> futureResult = new AtomicReference<T>();

		private final CountDownLatch resultLatch = new CountDownLatch(1);

		private final AtomicReference<Throwable> futureException = new AtomicReference<Throwable>();

		public FutureJob(String name) {
			super(name);
		}

		public boolean cancel(boolean mayInterruptIfRunning) {
			this.cancelled = true;
			return this.cancel();
		}

		public boolean isCancelled() {
			return this.cancelled;
		}

		public boolean isDone() {
			return getResult() != null;
		}

		public T get() throws InterruptedException, ExecutionException {
			resultLatch.await();
			return getFutureResult();
		}

		private T getFutureResult() throws ExecutionException {
			Throwable t = futureException.get();
			if (t != null) {
				throw new ExecutionException(t);
			}
			return futureResult.get();
		}

		public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			if (!resultLatch.await(timeout, unit)) {
				throw new TimeoutException();
			}
			return getFutureResult();
		}

		protected IStatus success(T result) {
			futureResult.set(result);
			resultLatch.countDown();
			return Status.OK_STATUS;
		}

		protected IStatus error(Throwable t) {
			futureException.set(t);
			resultLatch.countDown();
			if (t instanceof OperationCanceledException) {
				return Status.CANCEL_STATUS;
			}
			return Status.OK_STATUS;
		}
	}

	private final Set<Account> accounts;

	private final UUID kind;

	private List<ProfileImage> images;

	private final IdentityModel model;

	private Profile profile;

	public Identity(IdentityModel model) {
		this.model = model;
		this.kind = UUID.randomUUID();
		this.accounts = new CopyOnWriteArraySet<Account>();
	}

	public void addAccount(Account account) {
		accounts.add(account);
	}

	public String[] getAliases() {
		Set<String> aliases = new HashSet<String>(accounts.size());
		for (Account account : accounts) {
			aliases.add(account.getId());
		}
		return aliases.toArray(new String[aliases.size()]);
	}

	public UUID getId() {
		return kind;
	}

	public boolean is(String id) {
		return getAccountById(id) != null;
	}

	public boolean is(Account account) {
		return accounts.contains(account);
	}

	public void removeAccount(Account account) {
		accounts.remove(account);
	}

	public synchronized Future<IProfileImage> requestImage(final int preferredWidth, final int preferredHeight) {
		if (images != null) {
			for (final ProfileImage image : images) {
				if (image.getWidth() == preferredWidth && image.getHeight() == preferredHeight) {
					return new FutureResult<IProfileImage>(image);
				}
			}
		}
		FutureJob<IProfileImage> job = new FutureJob<IProfileImage>("Retrieving Image") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				ProfileImage image;
				try {
					image = model.getImage(Identity.this, preferredWidth, preferredHeight, monitor);
				} catch (Throwable t) {
					return error(t);
				}
				addImage(image);
				return success(image);
			}
		};
		job.schedule();
		return job;
	}

	protected synchronized void addImage(ProfileImage image) {
		if (images == null) {
			images = new ArrayList<ProfileImage>();
		}
		images.add(image);
	}

	public Future<IProfile> requestProfile() {
		// ignore
		return null;
	}

	public Account getAccountByKind(String kind) {
		if (kind == null) {
			return null;
		}
		for (Account account : accounts) {
			if (kind.equals(account.getKind())) {
				return account;
			}
		}
		return null;
	}

	public Account getAccountById(String id) {
		if (id == null) {
			return null;
		}
		for (Account account : accounts) {
			if (id.equals(account.getId())) {
				return account;
			}
		}
		return null;
	}

}
