/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-FileCopyrightText: 2022 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.dirigible.engine.wiki.synchronizer;

import org.eclipse.dirigible.commons.config.Configuration;
import org.eclipse.dirigible.core.scheduler.api.IJobDefinitionProvider;
import org.eclipse.dirigible.core.scheduler.api.ISchedulerCoreService;
import org.eclipse.dirigible.core.scheduler.service.definition.JobDefinition;

/**
 * The Class WikiSynchronizerJobDefinitionProvider.
 */
public class WikiSynchronizerJobDefinitionProvider implements IJobDefinitionProvider {

	/** The Constant DIRIGIBLE_JOB_EXPRESSION_WIKI. */
	private static final String DIRIGIBLE_JOB_EXPRESSION_WIKI = "DIRIGIBLE_JOB_EXPRESSION_WIKI";
	
	/** The Constant DIRIGIBLE_INTERNAL_WIKI_SYNCHRONIZER_JOB. */
	private static final String DIRIGIBLE_INTERNAL_WIKI_SYNCHRONIZER_JOB = "dirigible-internal-wiki-synchronizer-job";
	
	/** The Constant WIKI_SYNCHRONIZER_JOB. */
	static final String WIKI_SYNCHRONIZER_JOB = "Wiki Synchronizer Job";

	/**
	 * Gets the job definition.
	 *
	 * @return the job definition
	 */
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.dirigible.core.scheduler.api.IJobDefinitionProvider#getJobDefinition()
	 */
	@Override
	public JobDefinition getJobDefinition() {
		JobDefinition jobDefinition = new JobDefinition();
		jobDefinition.setName(DIRIGIBLE_INTERNAL_WIKI_SYNCHRONIZER_JOB);
		jobDefinition.setGroup(ISchedulerCoreService.JOB_GROUP_INTERNAL);
		jobDefinition.setClazz(WikiSynchronizerJob.class.getCanonicalName());
		jobDefinition.setDescription(WIKI_SYNCHRONIZER_JOB);
		String expression = Configuration.get(DIRIGIBLE_JOB_EXPRESSION_WIKI, "0/53 * * * * ?");
		jobDefinition.setExpression(expression);
		jobDefinition.setSingleton(true);
		return jobDefinition;
	}

}
