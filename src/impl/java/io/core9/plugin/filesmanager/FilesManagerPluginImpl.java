package io.core9.plugin.filesmanager;

import io.core9.plugin.server.request.FileUpload;
import io.core9.plugin.server.request.Request;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

import com.google.common.io.ByteStreams;

@PluginImplementation
public class FilesManagerPluginImpl implements FilesManagerPlugin {
	
	@InjectPlugin
	private FileRepository repository;

	@Override
	public String getControllerName() {
		return "files";
	}

	@Override
	public void handle(Request request) {
		String type = (String) request.getParams().get("type");
		String id = (String) request.getParams().get("id");
		if(type == null) {
			process(request);
		} else if (id == null) {
			process(request, type);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void process(Request request) {
		switch(request.getMethod()) {
		case POST:
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
					}
				} else {
					request.getResponse().sendJsonMap(repository.addFile(request.getVirtualHost(), request.getBodyAsMap(), this.getClass().getResourceAsStream("/dummy.txt")));
				}
			} catch (IOException e) {
				request.getResponse().setStatusCode(500);
				request.getResponse().end("500");
			}
			break;
		case GET:
		default:
			String folder = (String) request.getParams().get("folder");
			request.getResponse().sendJsonArray(repository.getFolderContents(request.getVirtualHost(), folder));
			break;
		}
	}
	
	private void process(Request request, String fileId) {
		switch(request.getMethod()) {
		case DELETE:
			repository.removeFile(request.getVirtualHost(), fileId);
			request.getResponse().end();
			break;
		case PUT:
			if(request.getParams().get("contents") == null) {
				request.getResponse().sendJsonMap(repository.saveFile(request.getVirtualHost(), request.getBodyAsMap(), fileId));
			} else {
				// TODO Only supports textual updates, allow files as well
				repository.updateFileContents(request.getVirtualHost(), fileId, new ByteArrayInputStream(((String) request.getBodyAsMap().get("content")).getBytes()));
				
			}
			break;
		case GET:
		default:
			if(request.getParams().get("contents") == null) {
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
}