package io.core9.plugin.filesmanager.handler;

import io.core9.plugin.database.mongodb.MongoDatabase;
import io.core9.plugin.database.repository.CrudRepository;
import io.core9.plugin.database.repository.NoCollectionNamePresentException;
import io.core9.plugin.database.repository.RepositoryFactory;
import io.core9.plugin.filesmanager.BucketConf;
import io.core9.plugin.filesmanager.FileRepository;
import io.core9.plugin.server.VirtualHost;
import io.core9.plugin.server.handler.Middleware;
import io.core9.plugin.server.request.Request;
import io.core9.plugin.server.vertx.VertxServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Map;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

import com.google.common.io.ByteStreams;

@PluginImplementation
public class StaticFilesHandlerImpl implements StaticFilesHandler {
	
	@InjectPlugin
	private FileRepository repository;
	
	private VertxServer server;
	
	@InjectPlugin
	private MongoDatabase database;
	
	private CrudRepository<BucketConf> config;
	
	@PluginLoaded
	public void onVertxServerLoaded(VertxServer server){
		this.server = server;;
	}
	
	@PluginLoaded
	public void onRepositoryFactoryLoaded(RepositoryFactory factory) throws NoCollectionNamePresentException {
		this.config = factory.getRepository(BucketConf.class);
	}
	
	@Override
	public void addNewBucketListener(final VirtualHost vhost, final String prefix, final String database, final String bucket) {
		server.use(vhost, prefix + "/.*", new Middleware() {

			@Override
			public void handle(Request request) {
				String filePath = request.getPath().replaceFirst(prefix, "");
				try {
					Map<String,Object> file = repository.getFileContentsByName(database, bucket, filePath);
					if(file == null){
						request.getResponse().setStatusCode(404);
						request.getResponse().setStatusMessage("File not found");
					}else{
						request.getResponse().putHeader("Content-Type", (String) file.get("ContentType"));
						InputStream result = (InputStream) file.get("stream");
						request.getResponse().sendBinary(ByteStreams.toByteArray(result));
						result.close();
					}
				} catch (IOException e) {
					request.getResponse().setStatusCode(404);
					request.getResponse().setStatusMessage("File not found");
				}
			}
		});
	}

	@Override
	public void execute() {
		if(server == null) return;
		// Adds the default /static/ listener on all vhosts
		server.use("/static/.*", new Middleware() {

			@Override
			public void handle(Request request) {
				String filePath = request.getPath().replaceFirst("/static", "");
				try {
					Map<String,Object> file = repository.getFileContentsByName(request.getVirtualHost(), filePath);
					if(file == null){
						request.getResponse().setStatusCode(404);
						request.getResponse().setStatusMessage("File not found");
					}else{
						request.getResponse().putHeader("Content-Type", (String) file.get("ContentType"));
						InputStream result = (InputStream) file.get("stream");
						request.getResponse().sendBinary(ByteStreams.toByteArray(result));
						result.close();
					}
				} catch (IOException e) {
					request.getResponse().setStatusCode(404);
					request.getResponse().setStatusMessage("File not found");
				}

			}
		});
	}

	@Override
	public void process(VirtualHost[] vhosts) {
		for(VirtualHost vhost : vhosts) {
			for(BucketConf conf : config.getAll(vhost)) {
				if(conf.getPrefix() != null && !conf.getPrefix().equals("")) {
					try {
						this.database.addDatabase(conf.getHost(), conf.getDatabase(), conf.getUsername(), conf.getPassword());
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
					this.addNewBucketListener(vhost, conf.getPrefix(), conf.getDatabase(), conf.getBucket());
				}
			}
		}
	}
}
