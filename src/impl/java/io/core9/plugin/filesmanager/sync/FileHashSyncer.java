package io.core9.plugin.filesmanager.sync;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import com.google.common.io.ByteStreams;

import io.core9.plugin.filesmanager.FileRepository;
import io.core9.plugin.server.VirtualHost;

public class FileHashSyncer implements FileSyncer {

	private FileRepository repository;
	private VirtualHost vhost;
	private String rootPath;

	@Override
	public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path path, IOException ex) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
		String strPath = path.toString().replace(rootPath, "");
		Map<String,Object> query = new HashMap<String,Object>();
		query.put("filename", strPath);
		Map<String,Object> file = repository.getFile(vhost, query);
		InputStream stream = Files.newInputStream(path);
		if(file == null) {
			insertFile(stream, strPath);
		} else {
			updateFile(stream, file, strPath);
		}
		stream.close();
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path path, IOException ex) throws IOException {
		System.err.println("ERROR: Cannot visit path: " + path);
		return FileVisitResult.CONTINUE;
	}
	
	/**
	 * Insert a new file in the repository
	 * @param stream
	 * @param strPath
	 * @throws IOException
	 */
	protected void insertFile(InputStream stream, String strPath) throws IOException {
		System.out.println("INSERTING: " + strPath);
		String folder = strPath.substring(0, strPath.lastIndexOf('/') + 1);
		String filePath = strPath.substring(strPath.lastIndexOf('/') + 1);
		if(!folder.equals("/")) {
			repository.ensureFolderExists(vhost, folder);
		}
		Map<String,Object> metadata = new HashMap<String,Object>();
		metadata.put("folder", folder);
		metadata.put("type", "File");
		Map<String,Object> file = new HashMap<String, Object>(); 
		file.put("filename", filePath);
		file.put("metadata", metadata);
		repository.addFile(vhost, file, stream);
	}
	
	/**
	 * Update an existing file
	 * @throws IOException 
	 */
	protected void updateFile(InputStream stream, Map<String,Object> file, String strPath) throws IOException {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			DigestInputStream dis = new DigestInputStream(stream, md);
			byte[] bin = ByteStreams.toByteArray(dis);
			Formatter formatter = new Formatter();
	        for (byte b : md.digest()) {
	            formatter.format("%02x", b);
	        }
	        String md5 = formatter.toString();
	        formatter.close();
	        if(!md5.equals(file.get("md5"))) {
	        	ByteArrayInputStream output = new ByteArrayInputStream(bin);
	        	System.out.println("UPDATING: " + strPath);
	        	repository.updateFileContents(vhost, (String) file.get("_id"), output);
	        }
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	@Override
	public FileSyncer setVirtualHost(VirtualHost vhost) {
		this.vhost = vhost;
		return this;
	}

	@Override
	public FileSyncer setFileRepostiroy(FileRepository repository) {
		this.repository = repository;
		return this;
	}

	@Override
	public FileSyncer setPath(String path) {
		this.rootPath = path;
		return this;
	}

	@Override
	public VirtualHost getVirtualHost() {
		return vhost;
	}

	@Override
	public FileRepository getFileRepository() {
		return repository;
	}

	@Override
	public String getPath() {
		return rootPath;
	}
}
