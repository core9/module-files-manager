package io.core9.plugin.filesmanager;

import java.util.HashMap;
import java.util.Map;

import io.core9.plugin.database.repository.AbstractCrudEntity;
import io.core9.plugin.database.repository.Collection;
import io.core9.plugin.database.repository.CrudEntity;

@Collection("configuration")
public class BucketConf extends AbstractCrudEntity implements CrudEntity {
	
	private String prefix;
	private String host;
	private String database;
	private String username;
	private String password;
	private String bucket;

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	@SuppressWarnings("serial")
	@Override
	public Map<String,Object> retrieveDefaultQuery() {
		return new HashMap<String,Object>(){{
			this.put("configtype", "bucket");
		}};
	}
}