package io.core9.plugin.filesmanager;

import io.core9.core.plugin.Core9Plugin;
import io.core9.plugin.admin.AdminPlugin;
import io.core9.plugin.server.VirtualHost;

public interface FilesManagerPlugin extends Core9Plugin, AdminPlugin {

	/**
	 * Sychronize a directory with the File Repository
	 * @param directory
	 */
	void syncDirectory(VirtualHost vhost, String directory);
}
