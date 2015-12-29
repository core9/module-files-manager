package io.core9.plugin.filesmanager.sync;

import java.nio.file.FileVisitor;
import java.nio.file.Path;

import io.core9.plugin.filesmanager.FileRepository;
import io.core9.plugin.server.VirtualHost;

public interface FileSyncer extends FileVisitor<Path> {
	
	/**
	 * Set the vhost
	 * @param vhost
	 * @return
	 */
	FileSyncer setVirtualHost(VirtualHost vhost);
	
	/**
	 * Return the vhost
	 * @return
	 */
	VirtualHost getVirtualHost();
	
	/**
	 * Set the repo
	 * @param repository
	 * @return
	 */
	FileSyncer setFileRepostiroy(FileRepository repository);
	
	/**
	 * Return the repo
	 * @return
	 */
	FileRepository getFileRepository();
	
	/**
	 * Set the path
	 * @param path
	 * @return
	 */
	FileSyncer setPath(String path);
	
	/**
	 * Get the path
	 * @return
	 */
	String getPath();
}