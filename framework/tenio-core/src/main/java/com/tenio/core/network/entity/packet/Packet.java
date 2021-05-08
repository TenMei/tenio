package com.tenio.core.network.entity.packet;

import java.util.Collection;
import java.util.Map;

import com.tenio.core.network.define.ResponsePriority;
import com.tenio.core.network.define.TransportType;
import com.tenio.core.network.entity.session.Session;

public interface Packet {

	long getId();

	byte[] getData();

	void setData(byte[] binary);

	TransportType getTransportType();

	void setTransportType(TransportType type);

	ResponsePriority getPriority();

	void setPriority(ResponsePriority priority);
	
	boolean isEncrypted();
	
	void setEncrypted(boolean encrypted);

	Collection<Session> getRecipients();

	void setRecipients(Collection<Session> recipients);

	Session getSender();

	void setSender(Session session);

	Object getAttribute(String key);

	void setAttribute(String key, Object value);
	
	void setAttributes(Map<String, Object> attributes);

	long getCreatedTime();

	int getOriginalSize();

	boolean isTcp();

	boolean isUdp();

	boolean isWebSocket();

	byte[] getFragmentBuffer();

	void setFragmentBuffer(byte[] binary);

	boolean isFragmented();
	
	Packet clone();

}
