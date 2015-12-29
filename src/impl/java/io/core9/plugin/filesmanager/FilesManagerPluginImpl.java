package io.core9.plugin.filesmanager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.io.ByteStreams;

import io.core9.plugin.database.repository.CrudRepository;
import io.core9.plugin.database.repository.NoCollectionNamePresentException;
import io.core9.plugin.database.repository.RepositoryFactory;
import io.core9.plugin.filesmanager.sync.FileHashSyncer;
import io.core9.plugin.filesmanager.sync.FileSyncer;
import io.core9.plugin.server.VirtualHost;
import io.core9.plugin.server.request.FileUpload;
import io.core9.plugin.server.request.Request;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

@PluginImplementation
public class FilesManagerPluginImpl implements FilesManagerPlugin {
	
	@InjectPlugin
	private FileRepository repository;
	
	private CrudRepository<BucketConf> buckets;
	
	@PluginLoaded
	public void onRepositoryFactoryLoaded(RepositoryFactory factory) throws NoCollectionNamePresentException {
		buckets = factory.getRepository(BucketConf.class);
	}

	@Override
	public String getControllerName() {
		return "files";
	}

	@Override
	public void handle(Request request) {
		System.out.println(request);
		String fileId = (String) request.getPathParams().get("type");
		Deque<String> bucketDeque = request.getQueryParams().get("bucket");
		if(bucketDeque != null) {
			BucketConf conf = buckets.read(request.getVirtualHost(), bucketDeque.getFirst());
			if(fileId == null) {
				process(request, conf);
			} else {
				process(request, fileId, conf);
			}
		} else {
			if(fileId == null) {
				process(request);
			} else {
				process(request, fileId);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void process(Request request) {
		switch(request.getMethod()) {
		case POST:
			request.getResponse().setEnded(true); // Temporarily block response
			request.getBodyAsMap().toBlocking().last();
			try {
			if(request.getContext("files") != null) {
				for(FileUpload file : (List<FileUpload>) request.getContext("files")) {
					String filename = file.getFilename();
					Map<String,Object> fileMap = new HashMap<String,Object>();
					fileMap.put("filename", filename.substring(filename.lastIndexOf('/') + 1));
					fileMap.put("contentType", file.getContentType());
					Map<String,Object> metadata = new HashMap<String,Object>();
					metadata.put("folder", filename.substring(0, filename.lastIndexOf('/') + 1));
					metadata.put("type", "File");
					fileMap.put("metadata", metadata);
					repository.addFile(request.getVirtualHost(), fileMap, new FileInputStream(file.getFilepath()));
					new File(file.getFilepath()).delete();
					request.getResponse().end();
				}
			} else {
				request.getResponse().sendJsonMap(repository.addFile(request.getVirtualHost(), request.getBodyAsMap().toBlocking().last(), this.getClass().getResourceAsStream("/dummy.txt")));
			}
			} catch (IOException e) {
				request.getResponse().setStatusCode(500);
				request.getResponse().end("500");
			};
			break;
		case GET:
		default:
			String folder = (String) request.getQueryParams().get("folder").getFirst();
			request.getResponse().sendJsonArray(repository.getFolderContents(request.getVirtualHost(), folder));
			break;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void process(Request request, BucketConf bucket) {
		switch(request.getMethod()) {
		case POST:
			request.getBody().subscribe((value) -> {
				try {
					if(request.getContext("files") != null) {
						for(FileUpload file : (List<FileUpload>) request.getContext("files")) {
							String filename = file.getFilename();
							Map<String,Object> fileMap = new HashMap<String,Object>();
							fileMap.put("filename", filename.substring(filename.lastIndexOf('/') + 1));
							fileMap.put("contentType", file.getContentType());
							Map<String,Object> metadata = new HashMap<String,Object>();
							metadata.put("folder", filename.substring(0, filename.lastIndexOf('/') + 1));
							metadata.put("type", "File");
							fileMap.put("metadata", metadata);
							repository.addFile(bucket.getDatabase(), bucket.getName(), fileMap, new FileInputStream(file.getFilepath()));
							new File(file.getFilepath()).delete();
						}
					} else {
						request.getResponse().sendJsonMap(repository.addFile(bucket.getDatabase(), bucket.getName(), request.getBodyAsMap().toBlocking().last(), this.getClass().getResourceAsStream("/dummy.txt")));
					}
				} catch (IOException e) {
					request.getResponse().setStatusCode(500);
					request.getResponse().end("500");
				}
			});
			break;
		case GET:
		default:
			String folder = (String) request.getQueryParams().get("folder").getFirst();
			request.getResponse().sendJsonArray(repository.getFolderContents(bucket.getDatabase(), bucket.getName(), folder));
			break;
		}
	}
	
	
	
	
	private void process(Request request, String fileId) {
		switch(request.getMethod()) {
		case DELETE:
			repository.removeFile(request.getVirtualHost(), fileId);
			request.getResponse().end("Success");
			break;
		case PUT:
			if(request.getQueryParams().get("contents") == null) {
				request.getResponse().sendJsonMap(repository.saveFile(request.getVirtualHost(), request.getBodyAsMap().toBlocking().last(), fileId));
			} else {
				// TODO Only supports textual updates, allow files as well
				repository.updateFileContents(request.getVirtualHost(), fileId, new ByteArrayInputStream(((String) request.getBodyAsMap().toBlocking().last().get("content")).getBytes()));
				request.getResponse().end("Success");
			}
			break;
		case GET:
		default:
			if(request.getQueryParams().get("contents") == null) {
				request.getResponse().sendJsonMap(repository.getFile(request.getVirtualHost(), fileId));
			} else {
				try {
					request.getResponse().sendBinary(ByteStreams.toByteArray(repository.getFileContents(request.getVirtualHost(), fileId)));
				} catch (IOException e) {
					request.getResponse().setStatusCode(404);
				}
			}
			break;
		}
	}
	
	private void process(Request request, String fileId, BucketConf bucket) {
		switch(request.getMethod()) {
		case DELETE:
			repository.removeFile(bucket.getDatabase(), bucket.getName(), fileId);
			request.getResponse().end("Success");
			break;
		case PUT:
			if(request.getQueryParams().get("contents") == null) {
				request.getResponse().sendJsonMap(repository.saveFile(bucket.getDatabase(), bucket.getName(), request.getBodyAsMap().toBlocking().last(), fileId));
			} else {
				// TODO Only supports textual updates, allow files as well
				repository.updateFileContents(bucket.getDatabase(), bucket.getName(), fileId, new ByteArrayInputStream(((String) request.getBodyAsMap().toBlocking().last().get("content")).getBytes()));
				request.getResponse().end("Success");
			}
			break;
		case GET:
		default:
			if(request.getQueryParams().get("contents") == null) {
				request.getResponse().sendJsonMap(repository.getFile(bucket.getDatabase(), bucket.getName(), fileId));
			} else {
				try {
					request.getResponse().sendBinary(ByteStreams.toByteArray(repository.getFileContents(bucket.getDatabase(), bucket.getName(), fileId)));
				} catch (IOException e) {
					request.getResponse().setStatusCode(404);
				}
			}
			break;
		}
	}

	@Override
	public void syncDirectory(VirtualHost vhost, String directory) {
		syncDirectory(vhost, directory, new FileHashSyncer());
	}
	
	@Override
	public void syncDirectory(VirtualHost vhost, String directory, FileSyncer sync) {
		Path dir = Paths.get(directory);
		try {
			sync.setVirtualHost(vhost).setFileRepostiroy(repository).setPath(directory);
			Files.walkFileTree(dir, sync);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}