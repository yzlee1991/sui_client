package com.lzy.sui.client.filter;

import com.lzy.sui.common.abs.AbstractSocketHandle;
import com.lzy.sui.common.abs.Filter;
import com.lzy.sui.common.model.Conversation;
import com.lzy.sui.common.model.ProtocolEntity;
import com.lzy.sui.common.utils.CommonUtils;
import com.sun.org.apache.xml.internal.security.utils.Base64;

public class ResponseFilter extends Filter {

	@Override
	public void handle(ProtocolEntity entity) {
		try {
			if (ProtocolEntity.Type.RESPONSE.equals(entity.getType())) {
				System.out.println("ResponseFilter  handling " + entity);
				Conversation.Data data=Conversation.MAP.get(entity.getConversationId());
				data.setEntity(entity);
				String lock=data.getLock();
				synchronized (lock) {
					lock.notify();
				}
			} else {
				if (this.filter != null) {
					this.filter.handle(entity);
				} else {
					System.out.println("未知类型：" + entity.getType());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
