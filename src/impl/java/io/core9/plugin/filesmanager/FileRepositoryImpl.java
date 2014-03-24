package io.core9.plugin.filesmanager;

import io.core9.plugin.database.mongodb.MongoDatabase;
import io.core9.plugin.server.VirtualHost;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

@PluginImplementation
public class FileRepositoryImpl implements FileRepository {
	
	private final String BUCKET = "static";

	@InjectPlugin
	private MongoDatabase database;

	@Override
	public List<Map<String, Object>> getFolderContents(VirtualHost vhost, String folder) {
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("metadata.folder", folder);
		return this.database.queryStaticFiles((String) vhost.getContext("database"), BUCKET, query);
	}
	
	@Override
	public Map<String,Object> getFile(VirtualHost vhost, String fileId) {
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("_id", fileId);
		return this.database.queryStaticFile((String) vhost.getContext("database"), BUCKET, query);
	}

	@Override
	public Map<String, Object> addFile(VirtualHost vhost, Map<String,Object> file, InputStream stream) throws IOException {
		return this.database.addStaticFile((String) vhost.getContext("database"), BUCKET, file, stream);
	}

	@Override
	public Map<String, Object> saveFile(VirtualHost vhost, Map<String, Object> file, String fileId) {
		return this.database.saveStaticFile((String) vhost.getContext("database"), BUCKET, file, fileId);
	}

	@Override
	public void removeFile(VirtualHost vhost, String fileId) {
		this.database.removeStaticFile((String) vhost.getContext("database"), BUCKET, fileId);
	}

	@Override
	public InputStream getFileContents(VirtualHost vhost, String fileId) {
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("_id", fileId);
		return this.database.getStaticFile((String) vhost.getContext("database"), BUCKET, query);
	}
	
	@Override
	public Map<String,Object> getFileContentsByName(VirtualHost vhost, String filename) {
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("filename", filename);
		
		Map<String,Object> result = new HashMap<String,Object>();
		result.put("stream", database.getStaticFile((String) vhost.getContext("database"), BUCKET, query));
		result.put("ContentType", database.queryStaticFile((String) vhost.getContext("database"), BUCKET, query).get("contentType"));
		return result;
	}

	@Override
	public void updateFileContents(VirtualHost vhost, String fileId, InputStream stream) {
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("_id", fileId);
		this.database.saveStaticFileContents((String) vhost.getContext("database"), BUCKET, query, stream);		
	}

	@Override
	public void ensureFolderExists(VirtualHost vhost, String folderpath) {
		if(!folderpath.equals("/")) {
			if(folderpath.endsWith("/")) {
				folderpath = folderpath.substring(0, folderpath.length() -1);
			}
			Map<String,Object> query = new HashMap<String,Object>();
			query.put("filename", folderpath);
			Map<String,Object> folder = this.database.queryStaticFile((String) vhost.getContext("database"), BUCKET, query);
			if(folder == null || folder.get("contentType").equals("inode/directory")) {
				ensureFolderExists(vhost, folderpath.substring(0, folderpath.lastIndexOf('/') + 1));
				Map<String,Object> file = new HashMap<String,Object>();
				Map<String,Object> metadata = new HashMap<String,Object>();
				metadata.put("folder", folderpath.substring(0, folderpath.lastIndexOf('/') +1 ));
				metadata.put("type", "Directory");
				file.put("filename", folderpath.substring(folderpath.lastIndexOf('/') + 1));
				file.put("contentType", "inode/directory");
				file.put("metadata", metadata);
				try {
					this.addFile(vhost, file, this.getClass().getResourceAsStream("/dummy.txt"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
