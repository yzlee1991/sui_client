package com.lzy.sui.client.model;

import javafx.concurrent.Task;

public abstract class TableTask extends Task<Void>{

	private String src;

	private String fileName;

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
