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
package org.eclipse.dirigible.commons.config;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration Facade class keeps all the configurations in the Dirigible
 * instance It has the default built in properties file - dirigible.properties
 * After the initialization, all the default properties are replaced with the
 * ones coming as: 1. System's properties 2. Environment variables This can be
 * triggered programmatically with update() method It supports also loading of
 * custom properties files from the class loader with load() for the modules and
 * also merge with a provided properties object with add() methods
 */
public class Configuration {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

	/**
	 * The Enum ConfigType.
	 */
	private enum ConfigType {
		
		/** The runtime. */
		RUNTIME, 
 /** The environment. */
 ENVIRONMENT, 
 /** The deployment. */
 DEPLOYMENT, 
 /** The module. */
 MODULE
	}

	/** The Constant RUNTIME_VARIABLES. */
	private static final Map<String, String> RUNTIME_VARIABLES = Collections.synchronizedMap(new HashMap<String, String>());
	
	/** The Constant ENVIRONMENT_VARIABLES. */
	private static final Map<String, String> ENVIRONMENT_VARIABLES = Collections.synchronizedMap(new HashMap<String, String>());
	
	/** The Constant DEPLOYMENT_VARIABLES. */
	private static final Map<String, String> DEPLOYMENT_VARIABLES = Collections.synchronizedMap(new HashMap<String, String>());
	
	/** The Constant MODULE_VARIABLES. */
	private static final Map<String, String> MODULE_VARIABLES = Collections.synchronizedMap(new HashMap<String, String>());

	/** The Constant CONFIG_FILE_PATH_DIRIGIBLE_PROPERTIES. */
	private static final String CONFIG_FILE_PATH_DIRIGIBLE_PROPERTIES = "/dirigible.properties";
	
	/** The Constant ERROR_MESSAGE_CONFIGURATION_DOES_NOT_EXIST. */
	private static final String ERROR_MESSAGE_CONFIGURATION_DOES_NOT_EXIST = "Configuration file {0} does not exist";

	/** The loaded. */
	public static boolean LOADED = false;

	static {
		loadDeploymentConfig(CONFIG_FILE_PATH_DIRIGIBLE_PROPERTIES);
		loadEnvironmentConfig();
		LOADED = true;
	}

	/**
	 * Load environment config.
	 */
	private static void loadEnvironmentConfig() {
		addConfigProperties(System.getenv(), ConfigType.ENVIRONMENT);
		addConfigProperties(System.getProperties(), ConfigType.ENVIRONMENT);
	}

	/**
	 * Load deployment config.
	 *
	 * @param path the path
	 */
	private static void loadDeploymentConfig(String path) {
		load(path, ConfigType.DEPLOYMENT);
	}

	/**
	 * Load module config.
	 *
	 * @param path the path
	 */
	public static void loadModuleConfig(String path) {
		load(path, ConfigType.MODULE);
	}

	/**
	 * Load.
	 *
	 * @param path the path
	 * @param type the type
	 */
	private static void load(String path, ConfigType type) {
		try {
			Properties custom = new Properties();
			InputStream in = Configuration.class.getResourceAsStream(path);
			if (in != null) {
				try {
					custom.load(in);
					switch (type) {
					case RUNTIME:
						addConfigProperties(custom, RUNTIME_VARIABLES);
						break;
					case ENVIRONMENT:
						addConfigProperties(custom, ENVIRONMENT_VARIABLES);
						break;
					case DEPLOYMENT:
						addConfigProperties(custom, DEPLOYMENT_VARIABLES);
						break;
					case MODULE:
						addConfigProperties(custom, MODULE_VARIABLES);
						break;
					default:
						break;
					}
				} finally {
					in.close();
				}
				if (logger.isDebugEnabled()) {logger.debug("Configuration loaded: " + path);}
			} else if (!path.equals(CONFIG_FILE_PATH_DIRIGIBLE_PROPERTIES)) {					
				throw new IOException(format(ERROR_MESSAGE_CONFIGURATION_DOES_NOT_EXIST, path));
			} else {
				if (logger.isDebugEnabled()) {logger.debug(format(ERROR_MESSAGE_CONFIGURATION_DOES_NOT_EXIST, path));}
			}
		} catch (IOException e) {
			if (logger.isErrorEnabled()) {logger.error(e.getMessage(), e);}
		}
	}

	/**
	 * Adds the config properties.
	 *
	 * @param properties the properties
	 * @param type the type
	 */
	private static void addConfigProperties(Map<String, String> properties, ConfigType type) {
		switch(type) {
		case RUNTIME:
			RUNTIME_VARIABLES.putAll(properties);
			break;
		case ENVIRONMENT:
			ENVIRONMENT_VARIABLES.putAll(properties);
			break;
		case DEPLOYMENT:
			DEPLOYMENT_VARIABLES.putAll(properties);
			break;
		case MODULE:
			MODULE_VARIABLES.putAll(properties);
			break;
		default:
			break;
		
		}
	}

	/**
	 * Adds the config properties.
	 *
	 * @param properties the properties
	 * @param type the type
	 */
	private static void addConfigProperties(Properties properties, ConfigType type) {
		switch(type) {
		case RUNTIME:
			addConfigProperties(properties, RUNTIME_VARIABLES);
			break;
		case ENVIRONMENT:
			addConfigProperties(properties, ENVIRONMENT_VARIABLES);
			break;
		case DEPLOYMENT:
			addConfigProperties(properties, DEPLOYMENT_VARIABLES);
			break;
		case MODULE:
			addConfigProperties(properties, MODULE_VARIABLES);
			break;
		default:
			break;
		}
	}

	/**
	 * Adds the config properties.
	 *
	 * @param properties the properties
	 * @param map the map
	 */
	private static void addConfigProperties(Properties properties, Map<String, String> map) {
		for (String property: properties.stringPropertyNames()) {
			map.put(property, properties.getProperty(property));
		}
	}

	/**
	 * Getter for the value of the property by its key.
	 *
	 * @param key the key
	 * @return the string
	 */
	public static String get(String key) {
		return get(key, null);
	}

	/**
	 * Getter for the value of the property by its key.
	 *
	 * @param key          the key
	 * @param defaultValue the default value
	 * @return the string
	 */
	public static String get(String key, String defaultValue) {
		String value = null;
		if (RUNTIME_VARIABLES.containsKey(key)) {
			value = RUNTIME_VARIABLES.get(key);
		} else if (ENVIRONMENT_VARIABLES.containsKey(key)) {
			value = ENVIRONMENT_VARIABLES.get(key);
		} else if (DEPLOYMENT_VARIABLES.containsKey(key)) {
			value = DEPLOYMENT_VARIABLES.get(key);
		} else if (MODULE_VARIABLES.containsKey(key)) {
			value = MODULE_VARIABLES.get(key);
		}
		return (value != null) ? value : defaultValue;
	}

	/**
	 * Setter for the property's key and value.
	 *
	 * @param key   the key
	 * @param value the value
	 */
	public static void set(String key, String value) {
		RUNTIME_VARIABLES.put(key, value);
	}

	/**
	 * Setter for the property's key and value. Sets the new value, only if the key value is null.
	 *
	 * @param key   the key
	 * @param value the value
	 */
	public static void setIfNull(String key, String value) {
		if (get(key) == null) {
			set(key, value);
		}
	}

	/**
	 * Remove property.
	 *
	 * @param key the key
	 */
	public static void remove(String key) {
		RUNTIME_VARIABLES.remove(key);
	}

	/**
	 * Getter for all the keys.
	 *
	 * @return the keys
	 */
	public static String[] getKeys() {
		Set<String> keys = new HashSet<String>();
		keys.addAll(RUNTIME_VARIABLES.keySet());
		keys.addAll(ENVIRONMENT_VARIABLES.keySet());
		keys.addAll(DEPLOYMENT_VARIABLES.keySet());
		keys.addAll(MODULE_VARIABLES.keySet());
		return keys.toArray(new String[] {});
	}

	/**
	 * Update the properties values from the System's properties and from the
	 * Environment if any.
	 */
	public static void update() {
		loadEnvironmentConfig();
	}

	/**
	 * Checks if is anonymous mode enabled.
	 *
	 * @return true, if is anonymous mode enabled
	 */
	public static boolean isAnonymousModeEnabled() {
		try {
			Class.forName("org.eclipse.dirigible.runtime.anonymous.AnonymousAccess");
		} catch (ClassNotFoundException e) {
			return false;
		}
		return !isProtectedModeEnabled();
	}

	/**
	 * Checks if is protected mode enabled.
	 *
	 * @return true, if is protected mode enabled
	 */
	private static boolean isProtectedModeEnabled() {
		return isKeycloakModeEnabled() || isOAuthAuthenticationEnabled() || isJwtModeEnabled();
	}
	/**
	 * Checks if is anonymous user enabled.
	 *
	 * @return true, if is anonymous user enabled
	 */
	public static boolean isAnonymousUserEnabled() {
		try {
			Class.forName("org.eclipse.dirigible.anonymous.AnonymousUser");
		} catch (ClassNotFoundException e) {
			return false;
		}
		return !isProtectedModeEnabled();
	}

	/**
	 * Checks if the OAuth authentication is enabled.
	 *
	 * @return true, if the OAuth authentication is enabled
	 */
	public static boolean isOAuthAuthenticationEnabled() {
		return Boolean.parseBoolean(get("DIRIGIBLE_OAUTH_ENABLED", Boolean.FALSE.toString()));
	}

	/**
	 * Checks if the Keycloak authentication is enabled.
	 *
	 * @return true, if the Keycloak authentication is enabled
	 */
	public static boolean isKeycloakModeEnabled() {
		return Boolean.parseBoolean(get("DIRIGIBLE_KEYCLOAK_ENABLED", Boolean.FALSE.toString()));
	}

	/**
	 * Checks if is JWT mode enabled.
	 *
	 * @return true, if is JWT mode enabled
	 */
	public static boolean isJwtModeEnabled() {
		boolean enabled = false;
		if (isOAuthAuthenticationEnabled()) {
			try {
				Class.forName("org.eclipse.dirigible.jwt.JwtAccess");
				enabled = true;
			} catch (ClassNotFoundException e) {
				// Do nothing
			}
		}
		return enabled;
	}

	/**
	 * Checks if productive iframe is enabled.
	 *
	 * @return true, if productive iframe is enabled
	 */
	public static boolean isProductiveIFrameEnabled() {
		return Boolean.parseBoolean(Configuration.get("DIRIGIBLE_PRODUCTIVE_IFRAME_ENABLED", Boolean.TRUE.toString()));
	}

	/**
	 * Checks if Web IDE Terminal is enabled.
	 *
	 * @return true, if Web IDE Terminal is enabled
	 */
	public static boolean isTerminalEnabled() {
		return Boolean.parseBoolean(Configuration.get("DIRIGIBLE_TERMINAL_ENABLED", Boolean.TRUE.toString()));
	}

	/**
	 * Setter as a System's property.
	 *
	 * @param key   the key
	 * @param value the value
	 */
	public static void setSystemProperty(String key, String value) {
		System.setProperty(key, value);
	}
	
	/**
	 * Getter for the configurations set programmatically at runtime.
	 *
	 * @return the map of the runtime variables
	 */
	public static Map<String, String> getRuntimeVariables() {
		return new HashMap<String, String>(RUNTIME_VARIABLES);
	}
	
	/**
	 * Getter for the configurations from the environment.
	 *
	 * @return the map of the variables from the environment
	 */
	public static Map<String, String> getEnvironmentVariables() {
		return new HashMap<String, String>(ENVIRONMENT_VARIABLES);
	}
	
	/**
	 * Getter for the configurations from the dirigible.properties files
	 * 
	 * @return the map of the variables from the dirigible.properties files
	 */
	public static Map<String, String> getDeploymentVariables() {
		return new HashMap<String, String>(DEPLOYMENT_VARIABLES);
	}
	
	/**
	 * Getter for the configurations from the module's dirigible-*.properties files
	 * 
	 * @return the map of the variables from the module's dirigible-*.properties files
	 */
	public static Map<String, String> getModuleVariables() {
		return new HashMap<String, String>(MODULE_VARIABLES);
	}
	
	/** The Constant CONFIGURATION_PARAMETERS. */
	private static final String[] CONFIGURATION_PARAMETERS = new String[] {
			"DIRIGIBLE_ANONYMOUS_USER_NAME_PROPERTY_NAME",
			"DIRIGIBLE_BRANDING_NAME",
			"DIRIGIBLE_BRANDING_BRAND",
			"DIRIGIBLE_BRANDING_ICON",
			"DIRIGIBLE_BRANDING_WELCOME_PAGE_DEFAULT",
			"DIRIGIBLE_GIT_ROOT_FOLDER",
			"DIRIGIBLE_REGISTRY_EXTERNAL_FOLDER",
			"DIRIGIBLE_REGISTRY_IMPORT_WORKSPACE",
			"DIRIGIBLE_REPOSITORY_PROVIDER",
			"DIRIGIBLE_REPOSITORY_DATABASE_DATASOURCE_NAME",
			"DIRIGIBLE_REPOSITORY_LOCAL_ROOT_FOLDER",
			"DIRIGIBLE_REPOSITORY_LOCAL_ROOT_FOLDER_IS_ABSOLUTE",
			"DIRIGIBLE_MASTER_REPOSITORY_PROVIDER",
			"DIRIGIBLE_MASTER_REPOSITORY_ROOT_FOLDER",
			"DIRIGIBLE_MASTER_REPOSITORY_ZIP_LOCATION",
			"DIRIGIBLE_MASTER_REPOSITORY_JAR_PATH",
			"DIRIGIBLE_REPOSITORY_SEARCH_ROOT_FOLDER",
			"DIRIGIBLE_REPOSITORY_SEARCH_ROOT_FOLDER_IS_ABSOLUTE",
			"DIRIGIBLE_REPOSITORY_SEARCH_INDEX_LOCATION",
			"DIRIGIBLE_DATABASE_PROVIDER",
			"DIRIGIBLE_DATABASE_DEFAULT_SET_AUTO_COMMIT",
			"DIRIGIBLE_DATABASE_DEFAULT_MAX_CONNECTIONS_COUNT",
			"DIRIGIBLE_DATABASE_DEFAULT_WAIT_TIMEOUT",
			"DIRIGIBLE_DATABASE_DEFAULT_WAIT_COUNT",
			"DIRIGIBLE_DATABASE_CUSTOM_DATASOURCES",
			"DIRIGIBLE_DATABASE_DATASOURCE_NAME_DEFAULT",
			"DIRIGIBLE_DATABASE_NAMES_CASE_SENSITIVE",
			"DIRIGIBLE_DATABASE_DERBY_ROOT_FOLDER_DEFAULT",
			"DIRIGIBLE_DATABASE_H2_ROOT_FOLDER_DEFAULT",
			"DIRIGIBLE_DATABASE_H2_DRIVER",
			"DIRIGIBLE_DATABASE_H2_URL",
			"DIRIGIBLE_DATABASE_H2_USERNAME",
			"DIRIGIBLE_DATABASE_H2_PASSWORD",
			"DIRIGIBLE_DATABASE_TRANSFER_BATCH_SIZE",
			"DIRIGIBLE_PERSISTENCE_CREATE_TABLE_ON_USE",
			"DIRIGIBLE_MONGODB_CLIENT_URI",
			"DIRIGIBLE_MONGODB_DATABASE_DEFAULT",
			"DIRIGIBLE_SCHEDULER_MEMORY_STORE",
			"DIRIGIBLE_SCHEDULER_DATASOURCE_TYPE",
			"DIRIGIBLE_SCHEDULER_DATASOURCE_NAME",
			"DIRIGIBLE_SCHEDULER_DATABASE_DELEGATE",
			"DIRIGIBLE_SCHEDULER_LOGS_RETANTION_PERIOD",
			"DIRIGIBLE_SCHEDULER_EMAIL_SENDER",
			"DIRIGIBLE_SCHEDULER_EMAIL_RECIPIENTS",
			"DIRIGIBLE_SCHEDULER_EMAIL_SUBJECT_ERROR",
			"DIRIGIBLE_SCHEDULER_EMAIL_SUBJECT_NORMAL",
			"DIRIGIBLE_SCHEDULER_EMAIL_TEMPLATE_ERROR",
			"DIRIGIBLE_SCHEDULER_EMAIL_TEMPLATE_NORMAL",
			"DIRIGIBLE_SCHEDULER_EMAIL_URL_SCHEME",
			"DIRIGIBLE_SCHEDULER_EMAIL_URL_HOST",
			"DIRIGIBLE_SCHEDULER_EMAIL_URL_PORT",
			"DIRIGIBLE_SYNCHRONIZER_IGNORE_DEPENDENCIES",
			"DIRIGIBLE_SYNCHRONIZER_EXCLUDE_PATHS",
			"DIRIGIBLE_HOME_URL",
			"DIRIGIBLE_JOB_EXPRESSION_BPM",
			"DIRIGIBLE_JOB_EXPRESSION_DATA_STRUCTURES",
			"DIRIGIBLE_JOB_EXPRESSION_EXTENSIONS",
			"DIRIGIBLE_JOB_EXPRESSION_JOBS",
			"DIRIGIBLE_JOB_EXPRESSION_MESSAGING",
			"DIRIGIBLE_JOB_EXPRESSION_MIGRATIONS",
			"DIRIGIBLE_JOB_EXPRESSION_ODATA",
			"DIRIGIBLE_JOB_EXPRESSION_PUBLISHER",
			"DIRIGIBLE_JOB_EXPRESSION_SECURITY",
			"DIRIGIBLE_JOB_EXPRESSION_REGISTRY",
			"DIRIGIBLE_JOB_DEFAULT_TIMEOUT",
			"DIRIGIBLE_CMS_PROVIDER",
			"DIRIGIBLE_CMS_ROLES_ENABLED",
			"DIRIGIBLE_CMS_INTERNAL_ROOT_FOLDER",
			"DIRIGIBLE_CMS_INTERNAL_ROOT_FOLDER_IS_ABSOLUTE",
			"DIRIGIBLE_CMS_MANAGED_CONFIGURATION_JNDI_NAME",
			"DIRIGIBLE_CMS_MANAGED_CONFIGURATION_AUTH_METHOD",
			"DIRIGIBLE_CMS_MANAGED_CONFIGURATION_NAME",
			"DIRIGIBLE_CMS_MANAGED_CONFIGURATION_KEY",
			"DIRIGIBLE_CMS_MANAGED_CONFIGURATION_DESTINATION",
			"DIRIGIBLE_CONNECTIVITY_CONFIGURATION_JNDI_NAME",
			"DIRIGIBLE_CMS_DATABASE_DATASOURCE_TYPE",
			"DIRIGIBLE_CMS_DATABASE_DATASOURCE_NAME",
			"DIRIGIBLE_BPM_PROVIDER",
			"DIRIGIBLE_FLOWABLE_DATABASE_DRIVER",
			"DIRIGIBLE_FLOWABLE_DATABASE_URL",
			"DIRIGIBLE_FLOWABLE_DATABASE_USER",
			"DIRIGIBLE_FLOWABLE_DATABASE_PASSWORD",
			"DIRIGIBLE_FLOWABLE_DATABASE_DATASOURCE_NAME",
			"DIRIGIBLE_FLOWABLE_DATABASE_SCHEMA_UPDATE",
			"DIRIGIBLE_FLOWABLE_USE_SYSTEM_DATASOURCE",
			"DIRIGIBLE_MESSAGING_USE_DEFAULT_DATABASE",
			"DIRIGIBLE_KAFKA_BOOTSTRAP_SERVER",
			"DIRIGIBLE_KAFKA_ACKS",
			"DIRIGIBLE_KAFKA_KEY_SERIALIZER",
			"DIRIGIBLE_KAFKA_VALUE_SERIALIZER",
			"DIRIGIBLE_KAFKA_AUTOCOMMIT_ENABLED",
			"DIRIGIBLE_KAFKA_AUTOCOMMIT_INTERVAL",
			"DIRIGIBLE_JAVASCRIPT_ENGINE_TYPE_DEFAULT",
			"DIRIGIBLE_JAVASCRIPT_GRAALVM_DEBUGGER_PORT",
			"DIRIGIBLE_JAVASCRIPT_GRAALVM_ALLOW_HOST_ACCESS",
			"DIRIGIBLE_JAVASCRIPT_GRAALVM_ALLOW_CREATE_THREAD",
			"DIRIGIBLE_JAVASCRIPT_GRAALVM_ALLOW_CREATE_PROCESS",
			"DIRIGIBLE_JAVASCRIPT_GRAALVM_ALLOW_IO",
			"DIRIGIBLE_JAVASCRIPT_GRAALVM_COMPATIBILITY_MODE_NASHORN",
			"DIRIGIBLE_JAVASCRIPT_GRAALVM_COMPATIBILITY_MODE_MOZILLA",
			"DIRIGIBLE_OPERATIONS_LOGS_ROOT_FOLDER_DEFAULT",
			"DIRIGIBLE_THEME_DEFAULT",
			"DIRIGIBLE_GENERATE_PRETTY_NAMES",
			"DIRIGIBLE_OAUTH_ENABLED",
			"DIRIGIBLE_OAUTH_AUTHORIZE_UR",
			"DIRIGIBLE_OAUTH_TOKEN_URL",
			"DIRIGIBLE_OAUTH_CLIENT_ID",
			"DIRIGIBLE_OAUTH_CLIENT_SECRET",
			"DIRIGIBLE_OAUTH_VERIFICATION_KEY",
			"DIRIGIBLE_OAUTH_APPLICATION_NAME",
			"DIRIGIBLE_OAUTH_APPLICATION_HOST",
			"DIRIGIBLE_OAUTH_ISSUER",
			"DIRIGIBLE_PRODUCT_NAME",
			"DIRIGIBLE_PRODUCT_VERSION",
			"DIRIGIBLE_PRODUCT_REPOSITORY",
			"DIRIGIBLE_PRODUCT_COMMIT_ID",
			"DIRIGIBLE_PRODUCT_TYPE",
			"DIRIGIBLE_INSTANCE_NAME",
			"DIRIGIBLE_SPARK_CLIENT_URI",
			"DIRIGIBLE_EXEC_COMMAND_LOGGING_ENABLED",
			"DIRIGIBLE_TERMINAL_ENABLED",
			"DIRIGIBLE_MAIL_CONFIG_PROVIDER",
			"DIRIGIBLE_MAIL_USERNAME",
			"DIRIGIBLE_MAIL_PASSWORD",
			"DIRIGIBLE_MAIL_TRANSPORT_PROTOCOL",
			"DIRIGIBLE_MAIL_SMTPS_HOST",
			"DIRIGIBLE_MAIL_SMTPS_PORT",
			"DIRIGIBLE_MAIL_SMTPS_AUTH",
			"DIRIGIBLE_MAIL_SMTP_HOST",
			"DIRIGIBLE_MAIL_SMTP_PORT",
			"DIRIGIBLE_MAIL_SMTP_AUTH",
			"DIRIGIBLE_KEYCLOAK_ENABLED",
			"DIRIGIBLE_KEYCLOAK_REALM",
			"DIRIGIBLE_KEYCLOAK_AUTH_SERVER_URL",
			"DIRIGIBLE_KEYCLOAK_SSL_REQUIRED",
			"DIRIGIBLE_KEYCLOAK_CLIENT_ID",
			"DIRIGIBLE_KEYCLOAK_CONFIDENTIAL_PORT",
			"DIRIGIBLE_CSV_DATA_MAX_COMPARE_SIZE",
			"DIRIGIBLE_CSV_DATA_BATCH_SIZE",
			"DIRIGIBLE_DESTINATION_CLIENT_ID",
			"DIRIGIBLE_DESTINATION_CLIENT_SECRET",
			"DIRIGIBLE_DESTINATION_URL",
			"DIRIGIBLE_DESTINATION_URI"
	};

	/**
	 * Gets the os.
	 *
	 * @return the os
	 */
	public static String getOS() {
		return System.getProperty("os.name").toLowerCase();
	}

	/**
	 * Checks if is OS windows.
	 *
	 * @return true, if is OS windows
	 */
	public static boolean isOSWindows() {
		return (getOS().indexOf("win") >= 0);
	}

	/**
	 * Checks if is OS mac.
	 *
	 * @return true, if is OS mac
	 */
	public static boolean isOSMac() {
		return (getOS().indexOf("mac") >= 0);
	}

	/**
	 * Checks if is osunix.
	 *
	 * @return true, if is osunix
	 */
	public static boolean isOSUNIX() {
		return (getOS().indexOf("nix") >= 0 || getOS().indexOf("nux") >= 0 || getOS().indexOf("aix") > 0);
	}

	/**
	 * Checks if is OS solaris.
	 *
	 * @return true, if is OS solaris
	 */
	public static boolean isOSSolaris() {
		return (getOS().indexOf("sunos") >= 0);
	}
	
	/**
	 * Gets the configuration parameters.
	 *
	 * @return the configuration parameters
	 */
	public static String[] getConfigurationParameters() {
		return CONFIGURATION_PARAMETERS.clone();
	}

}
