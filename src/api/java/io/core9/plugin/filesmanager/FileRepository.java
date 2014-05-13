package io.core9.plugin.filesmanager;

import io.core9.core.plugin.Core9Plugin;
import io.core9.plugin.server.VirtualHost;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;


public interface FileRepository extends Core9Plugin {
	
	
	List<Map<String, Object>> getFolderContents(VirtualHost vhost, String folder);

	Map<String, Object> getFile(VirtualHost vhost, String fileId);
	
	InputStream getFileContents(VirtualHost vhost, String fileId);

	Map<String, Object> addFile(VirtualHost vhost, Map<String, Object> file, InputStream stream) throws IOException;

	Map<String, Object> saveFile(VirtualHost virtualHost, Map<String, Object> bodyAsMap, String fileId);
	
	void ensureFolderExists(VirtualHost vhost, String folderpath);

	void removeFile(VirtualHost virtualHost, String fileId);

	void updateFileContents(VirtualHost vhost, String fileId, InputStream stream);

	Map<String, Object> getFileContentsByName(String database, String bucket, String filename);
	
	Map<String, Object> getFileContentsByName(VirtualHost vhost, String filename);

	Map<String, Object> getFile(VirtualHost vhost, Map<String, Object> query);
}
