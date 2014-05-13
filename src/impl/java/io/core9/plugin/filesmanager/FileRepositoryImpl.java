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
		return getFolderContents((String) vhost.getContext("database"), BUCKET, folder);
	}
	
	@Override
	public List<Map<String, Object>> getFolderContents(String db, String bucket, String folder) {
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("metadata.folder", folder);
		return this.database.queryStaticFiles(db, bucket, query);
	}
	
	@Override
	public Map<String,Object> getFile(VirtualHost vhost, Map<String,Object> query) {
		return this.database.queryStaticFile((String) vhost.getContext("database"), BUCKET, query);
	}
	
	@Override
	public Map<String,Object> getFile(VirtualHost vhost, String fileId) {
		return this.getFile((String) vhost.getContext("database"), BUCKET, fileId);
	}
	
	@Override
	public Map<String, Object> getFile(String db, String bucket, String fileId) {
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("_id", fileId);
		return this.database.queryStaticFile(db, bucket, query);
	}

	@Override
	public Map<String, Object> addFile(VirtualHost vhost, Map<String,Object> file, InputStream stream) throws IOException {
		return this.database.addStaticFile((String) vhost.getContext("database"), BUCKET, file, stream);
	}
	
	@Override
	public Map<String, Object> addFile(String db, String bucket, Map<String,Object> file, InputStream stream) throws IOException {
		return this.database.addStaticFile(db, bucket, file, stream);
	}

	@Override
	public Map<String, Object> saveFile(VirtualHost vhost, Map<String, Object> file, String fileId) {
		return saveFile((String) vhost.getContext("database"), BUCKET, file, fileId);
	}
	
	@Override
	public Map<String, Object> saveFile(String db, String bucket, Map<String, Object> file, String fileId) {
		return this.database.saveStaticFile(db, bucket, file, fileId);
	}

	@Override
	public void removeFile(VirtualHost vhost, String fileId) {
		removeFile((String) vhost.getContext("database"), BUCKET, fileId);
	}
	
	@Override
	public void removeFile(String db, String bucket, String fileId) {
		this.database.removeStaticFile(db, bucket, fileId);
	}

	@Override
	public InputStream getFileContents(VirtualHost vhost, String fileId) {
		return this.getFileContents((String) vhost.getContext("database"), BUCKET, fileId);
	}
	
	@Override
	public InputStream getFileContents(String db, String bucket, String fileId) {
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("_id", fileId);
		return this.database.getStaticFile(db, bucket, query);
	}
	
	@Override
	public Map<String,Object> getFileContentsByName(VirtualHost vhost, String filename) {
		return getFileContentsByName((String) vhost.getContext("database"), BUCKET, filename);
	}
	
	@Override
	public Map<String,Object> getFileContentsByName(String db, String bucket, String filename) {
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("filename", filename);
		
		Map<String,Object> result = new HashMap<String,Object>();

		try {
			result.put("stream", database.getStaticFile(db, bucket, query));
			result.put("ContentType", database.queryStaticFile(db, bucket, query).get("contentType"));
			return result;
		} catch (Exception e) {

		}
		return null;
	}

	@Override
	public void updateFileContents(VirtualHost vhost, String fileId, InputStream stream) {
		this.updateFileContents((String) vhost.getContext("database"), BUCKET, fileId, stream);		
	}
	
	@Override
	public void updateFileContents(String db, String bucket, String fileId, InputStream stream) {
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("_id", fileId);
		this.database.saveStaticFileContents(db, bucket, query, stream);
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
