package io.core9.plugin.filesmanager.handler;

import io.core9.core.executor.Executor;
import io.core9.core.plugin.Core9Plugin;
import io.core9.plugin.server.VirtualHost;
import io.core9.plugin.server.VirtualHostProcessor;

public interface StaticFilesHandler extends Core9Plugin, Executor, VirtualHostProcessor {

	/**
	 * Adds a bucket to the web environment, this means all files in the bucket will be accessible
	 * @param vhost
	 * @param prefix
	 * @param database
	 * @param bucket
	 */
	void addNewBucketListener(VirtualHost vhost, String prefix, String database, String bucket);

}
