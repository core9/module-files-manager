package io.core9.plugin.filesmanager.sync;

import io.core9.plugin.filesmanager.FileRepository;
import io.core9.plugin.server.VirtualHost;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
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

public class FileHashSyncer implements FileVisitor<Path> {

	private FileRepository repository;
	private VirtualHost vhost;
	private String rootPath;

	public FileHashSyncer(VirtualHost vhost, FileRepository repository, String path) {
		this.vhost = vhost;
		this.rootPath = path;
		this.repository = repository;
	}

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
		if(file == null) {
			insertFile(Files.newInputStream(path), strPath);
		} else {
			updateFile(Files.newInputStream(path), file, strPath);
		}
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
	private void insertFile(InputStream stream, String strPath) throws IOException {
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
		stream.close();
	}
	
	/**
	 * Update an existing file
	 * @throws IOException 
	 */
	private void updateFile(InputStream stream, Map<String,Object> file, String strPath) throws IOException {
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
}
