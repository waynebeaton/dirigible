/*******************************************************************************
 * Copyright (c) 2015 SAP and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * SAP - initial API and implementation
 *******************************************************************************/

package org.eclipse.dirigible.runtime.js;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.script.Bindings;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.dirigible.repository.api.ICommonConstants;
import org.eclipse.dirigible.repository.api.IRepository;
import org.eclipse.dirigible.repository.logging.Logger;
import org.eclipse.dirigible.runtime.scripting.AbstractScriptExecutor;
import org.eclipse.dirigible.runtime.scripting.IJavaScriptEngineExecutor;
import org.eclipse.dirigible.runtime.scripting.IJavaScriptExecutor;
import org.eclipse.dirigible.runtime.scripting.Module;
import org.mozilla.javascript.ScriptableObject;

public class JavaScriptExecutor extends AbstractScriptExecutor implements IJavaScriptExecutor {

	private static final String JS_TYPE_INTERNAL = "IJavaScriptEngineExecutor";

	private static final Logger logger = Logger.getLogger(JavaScriptExecutor.class);

	private IRepository repository;
	private String[] rootPaths;

	public JavaScriptExecutor(IRepository repository, String... rootPaths) {
		super();
		logger.debug("entering: constructor()");
		this.repository = repository;
		this.rootPaths = rootPaths;
		if ((this.rootPaths == null) || (this.rootPaths.length == 0)) {
			this.rootPaths = new String[] { null, null };
		}
		logger.debug("exiting: constructor()");
	}

	@Override
	public IRepository getRepository() {
		return repository;
	}

	@Override
	public String[] getRootPaths() {
		return rootPaths;
	}

	@Override
	public Object executeServiceModule(HttpServletRequest request, HttpServletResponse response, Object input, String module,
			Map<Object, Object> executionContext) throws IOException {

		if (module == null) {
			throw new IOException("The module name for execution cannot be null");
		}

		if ((module.endsWith(ICommonConstants.ARTIFACT_EXTENSION.JSON) || module.endsWith(ICommonConstants.ARTIFACT_EXTENSION.ENTITY)
				|| module.endsWith(ICommonConstants.ARTIFACT_EXTENSION.SWAGGER))) {
			// *.json, *.swagger and *.entity files are returned back as raw content
			Module scriptingModule = retrieveModule(this.repository, module, null, this.rootPaths);
			byte[] result = scriptingModule.getContent();
			return new String(result, StandardCharsets.UTF_8);
		}

		// support for path parameters
		if (!module.endsWith(ICommonConstants.ARTIFACT_EXTENSION.JAVASCRIPT)) {
			String ends = "." + ICommonConstants.ARTIFACT_EXTENSION.JAVASCRIPT;
			int index = module.indexOf(ends);
			if (index > 0) {
				String pathInfo = module.substring(index + ends.length() + 1);
				module = module.substring(0, (index + ends.length()));
				request.setAttribute("path", pathInfo);
			}
		}

		IJavaScriptEngineExecutor javascriptEngineExecutor = null;

		// lookup for externally provided engine executor - e.g. test framework
		if (request != null) {
			javascriptEngineExecutor = (IJavaScriptEngineExecutor) request.getAttribute(JS_TYPE_INTERNAL);
		}

		if (javascriptEngineExecutor == null) {
			try {
				if (request != null) {
					String engine = request.getParameter(IJavaScriptEngineExecutor.JS_ENGINE_TYPE);
					String userAgent = request.getHeader("User-Agent");
					if (IJavaScriptEngineExecutor.JS_TYPE_NASHORN.equalsIgnoreCase(engine)) {
						javascriptEngineExecutor = JavaScriptActivator.createExecutor(IJavaScriptEngineExecutor.JS_TYPE_NASHORN, this);
					} else
						// if(userAgent.contains("Chrome")) {
						if (IJavaScriptEngineExecutor.JS_TYPE_V8.equalsIgnoreCase(engine)) {
						javascriptEngineExecutor = JavaScriptActivator.createExecutor(IJavaScriptEngineExecutor.JS_TYPE_V8, this);
					} else {
						// Hard-coded defaults to Rhino until Nashorn incompatibilities get solved
						javascriptEngineExecutor = JavaScriptActivator.createExecutor(IJavaScriptEngineExecutor.JS_TYPE_RHINO, this);
					}
				} else {
					// TODO: Jobs and Listeners only with Rhino for now - to be defined non-request configuration
					javascriptEngineExecutor = JavaScriptActivator.createExecutor(IJavaScriptEngineExecutor.JS_TYPE_RHINO, this);
				}
			} catch (Throwable t) {
				logger.error(t.getMessage());
				throw new IOException(t);
			}
		}
		return javascriptEngineExecutor.executeServiceModule(request, response, input, module, executionContext);

	}

	@Override
	public void beforeExecution(HttpServletRequest request, HttpServletResponse response, String module, Object context) {
	}

	@Override
	protected void registerDefaultVariable(Object scope, String name, Object value) {
		if (scope instanceof ScriptableObject) {
			ScriptableObject local = (ScriptableObject) scope;
			local.put(name, local, value);
		} else if (scope instanceof Bindings) {
			Bindings local = (Bindings) scope;
			local.put(name, value);
		}
	}

	@Override
	protected String getModuleType(String path) {
		return ICommonConstants.ARTIFACT_TYPE.SCRIPTING_SERVICES;
	}

}
