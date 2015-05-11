/******************************************************************************* 
 * Copyright (c) 2015 SAP and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   SAP - initial API and implementation
 *******************************************************************************/

package org.eclipse.dirigible.ide.repository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.dirigible.repository.ext.conf.IConfigurationStore;

public class RepositoryGlobalPreferenceStore extends
		AbstractRepositoryPreferenceStore {

	private static final long serialVersionUID = 4602966805779348296L;


	public RepositoryGlobalPreferenceStore(String path, String name) {
		super(path, name);
	}

	@Override
	protected byte[] loadSettings(IConfigurationStore configurationStorage)
			throws IOException {
		byte[] bytes = configurationStorage.getGlobalSettingsAsBytes(getPath(), getName());
		return bytes;
	}


	@Override
	protected void saveSettingd(ByteArrayOutputStream baos) throws IOException {
		getConfigurationStore().setGlobalSettingsAsBytes(getPath(), getName(), baos.toByteArray());
	}


}
