/*
The MIT License

Copyright (c) 2016-2021 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.tenio.core.network;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.BindException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tenio.common.data.element.CommonObject;
import com.tenio.common.logger.pool.ElementsPool;
import com.tenio.common.msgpack.ByteArrayInputStream;
import com.tenio.core.configuration.Configuration;
import com.tenio.core.event.EventManager;
import com.tenio.core.event.implement.EventManagerImpl;
import com.tenio.core.network.netty.NettyWebSocketService;
import com.tenio.core.pool.ByteArrayInputStreamPool;
import com.tenio.core.pool.CommonObjectPool;

/**
 * @author kong
 */
public final class NetworkTest {

	private Network __network;
	private ElementsPool<CommonObject> __msgObjectPool;
	private ElementsPool<ByteArrayInputStream> __byteArrayPool;
	private EventManager __eventManager;
	private Configuration __configuration;

	@BeforeEach
	public void initialize() throws IOException, InterruptedException {
		__network = new NettyWebSocketService();
		__eventManager = new EventManagerImpl();
		__msgObjectPool = new CommonObjectPool();
		__byteArrayPool = new ByteArrayInputStreamPool();
		__configuration = new Configuration("TenIOConfig.example.xml");
		__network.start(__eventManager, __configuration, __msgObjectPool, __byteArrayPool);
	}

	@Test
	public void bindPortAlreadyInUseShouldReturnErrorMessage() {
		assertThrows(BindException.class, () -> {
			__network.start(__eventManager, __configuration, __msgObjectPool, __byteArrayPool);
		});
	}

	@AfterEach
	public void tearDown() {
		__network.shutdown();
		__eventManager.clear();
	}

}
