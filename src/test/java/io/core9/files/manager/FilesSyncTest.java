package io.core9.files.manager;

import java.net.UnknownHostException;

import io.core9.core.PluginRegistry;
import io.core9.core.PluginRegistryImpl;
import io.core9.core.boot.BootstrapFramework;
import io.core9.plugin.database.mongodb.MongoDatabase;
import io.core9.plugin.database.mongodb.MongoDatabaseImpl;
import io.core9.plugin.filesmanager.FilesManagerPlugin;
import io.core9.plugin.filesmanager.FilesManagerPluginImpl;
import io.core9.plugin.server.VirtualHost;

public class FilesSyncTest {

	public static void main(String[] args) throws UnknownHostException {
		VirtualHost vhost = new VirtualHost(null);
		vhost.putContext("database", "test");
		BootstrapFramework.run();
		PluginRegistry registry = PluginRegistryImpl.getInstance();
		
		MongoDatabase db = (MongoDatabase) registry.getPlugin(MongoDatabaseImpl.class);
		db.addDatabase("test", "", "");
		
		FilesManagerPlugin fm = (FilesManagerPlugin) registry.getPlugin(FilesManagerPluginImpl.class);
		fm.syncDirectory(vhost, FilesSyncTest.class.getResource("/files").getFile());
	}
}
