package com.lzy.sui.client.listener;

import com.lzy.sui.common.model.push.HostEntity;
import com.lzy.sui.common.model.push.HostEvent;
import com.lzy.sui.common.model.push.PushEvent;

public class HostListener implements Listener {

	@Override
	public void action(PushEvent event) {
		if (event instanceof HostEvent) {
			HostEntity hostEntity = gson.fromJson(event.getJson(), HostEntity.class);
			System.out.println(hostEntity);
		}
	}

}
