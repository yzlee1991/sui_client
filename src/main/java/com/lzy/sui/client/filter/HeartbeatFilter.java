package com.lzy.sui.client.filter;

import com.lzy.sui.common.abs.Filter;
import com.lzy.sui.common.model.ProtocolEntity;

public class HeartbeatFilter extends Filter{

	@Override
	public void handle(ProtocolEntity entity) {
		if(ProtocolEntity.Type.HEARTBEAT.equals(entity.getType())){
			System.out.println("HEARTBEAT  handling	"+entity);
			System.out.println("接收到来自："+entity.getSysUserName()+" 的心跳");
		}else {
			if(this.filter!=null){
				this.filter.handle(entity);
			}else{
				System.out.println("未知类型："+entity.getType());
			}
		}
	}

}
