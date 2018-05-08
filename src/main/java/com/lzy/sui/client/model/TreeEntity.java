package com.lzy.sui.client.model;

/**
 * author :lzy date :2018年5月8日下午4:12:14
 */

public class TreeEntity {

	public enum Identity {
		USER, CORPSE
	}

	private enum TYPE {
		HOST, DISK, DIRECTORY, FILE,
	}

	private Identity identity;
	private String identityId;
	private String name;
	private long fileSize;
	private String filePath;
	private TYPE type;

	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public String getIdentityId() {
		return identityId;
	}

	public void setIdentityId(String identityId) {
		this.identityId = identityId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public TYPE getType() {
		return type;
	}

	public void setType(TYPE type) {
		this.type = type;
	}

}
