package com.lzy.sui.client.listener;

import com.google.gson.Gson;
import com.lzy.sui.common.model.push.PushEvent;

public interface Listener {

	public void action(PushEvent event);
	
}
