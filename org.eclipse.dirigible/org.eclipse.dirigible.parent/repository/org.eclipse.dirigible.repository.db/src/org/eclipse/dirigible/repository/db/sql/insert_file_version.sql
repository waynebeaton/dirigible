INSERT INTO DGB_FILE_VERSIONS (
    FV_FILE_PATH, FV_VERSION, FV_CONTENT, FV_TYPE, FV_CONTENT_TYPE, FV_CREATED_BY, FV_CREATED_AT)
	VALUES (?, ?, ?, ?, ?, ?, $CURRENT_TIMESTAMP$)
