DELETE FROM DGB_SCHEMA_VERSIONS;
INSERT INTO DGB_SCHEMA_VERSIONS (
	SCHV_VERSION, SCHV_DESCRIPTION)
	VALUES (6, 'Extension Points');

CREATE TABLE DGB_EXTENSION_POINTS ( -- the extension points by location
	EXTPOINT_LOCATION VARCHAR(1000) NOT NULL,
	EXTPOINT_DESCRIPTION VARCHAR(2000),
	EXTPOINT_CREATED_BY VARCHAR(32),
	EXTPOINT_CREATED_AT $TIMESTAMP$,
	PRIMARY KEY (EXTPOINT_LOCATION)
);

CREATE TABLE DGB_EXTENSIONS ( -- the registerted extensions
	EXT_LOCATION VARCHAR(1000) NOT NULL,
	EXT_EXTPOINT_LOCATION VARCHAR(1000) NOT NULL,
	EXT_DESCRIPTION VARCHAR(2000),
	EXT_CREATED_BY VARCHAR(32),
	EXT_CREATED_AT $TIMESTAMP$,
	PRIMARY KEY (EXT_LOCATION, EXT_EXTPOINT_LOCATION)
);
