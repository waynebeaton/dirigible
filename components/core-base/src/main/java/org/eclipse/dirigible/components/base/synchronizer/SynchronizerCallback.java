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
package org.eclipse.dirigible.components.base.synchronizer;

import java.util.List;

import org.eclipse.dirigible.components.base.artefact.Artefact;
import org.eclipse.dirigible.components.base.artefact.ArtefactLifecycle;
import org.eclipse.dirigible.components.base.artefact.ArtefactState;
import org.eclipse.dirigible.components.base.artefact.topology.TopologyWrapper;

/**
 * The Interface SynchronizerCallback.
 *
 */
public interface SynchronizerCallback {
	
	/**
	 * Adds the error.
	 *
	 * @param error the error
	 */
	void addError(String error);
	
	/**
	 * Gets the errors.
	 *
	 * @return the errors
	 */
	List<String> getErrors();
	
	/**
	 * Register errors.
	 *
	 * @param synchronizer the synchronizer
	 * @param remained the remained
	 * @param lifecycle the lifecycle
	 * @param state the state
	 */
	void registerErrors(Synchronizer<? extends Artefact> synchronizer, List<TopologyWrapper<? extends Artefact>> remained, 
			String lifecycle, ArtefactState state);

	/**
	 * Register errors.
	 *
	 * @param synchronizer the synchronizer
	 * @param wrapper the wrapper
	 * @param lifecycle the lifecycle
	 * @param state the state
	 */
	void registerState(Synchronizer<? extends Artefact> synchronizer, TopologyWrapper<? extends Artefact> wrapper,
			String lifecycle, ArtefactState state);

}
