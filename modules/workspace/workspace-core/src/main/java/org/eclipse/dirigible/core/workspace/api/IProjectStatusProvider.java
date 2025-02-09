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
package org.eclipse.dirigible.core.workspace.api;

import java.io.IOException;

/**
 * The Interface IProjectStatusProvider.
 */
public interface IProjectStatusProvider {
	
	/**
	 * Gets the project status.
	 *
	 * @param workspace the workspace
	 * @param project the project
	 * @return the project status
	 */
	ProjectStatus getProjectStatus(String workspace, String project);
	
	/**
	 * Gets the project git folder.
	 *
	 * @param workspace the workspace
	 * @param project the project
	 * @return the project git folder
	 * @throws IOException 
	 */
	String getProjectGitFolder(String workspace, String project) throws IOException;

}
