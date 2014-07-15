package io.core9.plugin.filesmanager;

import io.core9.plugin.server.VirtualHost;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

@PluginImplementation
public class FileFeatureProcessorImpl implements FileFeatureProcessor {
	
	@InjectPlugin
	private FileRepository files;

	@Override
	public String getFeatureNamespace() {
		return "files";
	}

	@Override
	public String getProcessorAdminTemplateName() {
		return "files/featureprocessor/processor.tpl.html";
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleFeature(VirtualHost vhost, File repository, Map<String, Object> item) {
		try {
			Map<String, Object> entry = (Map<String, Object>) item.get("entry");
			Map<String, Object> metadata = (Map<String,Object>) entry.get("metadata");
			if(metadata != null && !metadata.get("folder").equals("/")) {
				files.ensureFolderExists(vhost, (String) metadata.get("folder"));
			}
			files.removeFile(vhost, (String) item.get("id"));
			files.addFile(vhost, entry, new FileInputStream(new File(repository, (String) item.get("id"))));
		}catch (IOException e) {
			e.printStackTrace();
		} 
	}

	@Override
	public void removeFeature(VirtualHost vhost, File repository, Map<String, Object> item) {
		files.removeFile(vhost, (String) item.get("id"));
	}

	@Override
	public boolean updateFeatureContent(VirtualHost vhost, File repository, Map<String, Object> item) {
		new File(repository, (String) item.get("id")).delete();
		if(item.get("id") != null) {
			if(item.get("remove") == null) {
				try {
					InputStream stream = files.getFileContents(vhost, (String) item.get("id"));
					if(stream == null) {
						return false;
					}
					Files.copy(stream, new File(repository, (String) item.get("id")).toPath()
					);
					return true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}
