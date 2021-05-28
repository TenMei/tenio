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
package com.tenio.example.example1;

import com.tenio.common.data.implement.ZeroObjectImpl;
import com.tenio.core.entities.data.ServerMessage;
import com.tenio.example.client.ClientUtility;
import com.tenio.example.client.SocketListener;
import com.tenio.example.client.TCP;
import com.tenio.example.server.SharedKey;

/**
 * This class shows how a client communicates with the server:<br>
 * 1. Create connections.<br>
 * 2. Send a login request.<br>
 * 3. Receive messages via TCP connection from the server.<br>
 * 4. Be logout by server. <br>
 * <br>
 * <b>[NOTE]</b> The client test is also available on <b>C++</b> and
 * <b>JavaScript</b> language, please see the <b>README.md</b> for more details
 */
public final class TestClientLogin implements SocketListener {

	private static final int SOCKET_PORT = 8032;

	/**
	 * The entry point
	 */
	public static void main(String[] args) {
		new TestClientLogin();
	}

	private TCP __tcp;
	private String __name;

	public TestClientLogin() {
		// create a new TCP object and listen for this port
		__tcp = new TCP(SOCKET_PORT);
		__tcp.receive(this);

		__name = ClientUtility.generateRandomString(5);

		// send a login request
		var data = ZeroObjectImpl.newInstance();
		data.putString(SharedKey.KEY_PLAYER_LOGIN, __name);
		__tcp.send(ServerMessage.newInstance().setData(data));

		System.err.println("Login Request -> " + data.toString());

	}

	@Override
	public void onReceivedTCP(ServerMessage message) {
		System.out.println("[RECV FROM SERVER TCP] -> " + message.getData().toString());

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		var data = ZeroObjectImpl.newInstance();
		data.putString(SharedKey.KEY_CLIENT_SERVER_ECHO, "Hello from client");
		__tcp.send(ServerMessage.newInstance().setData(data));
	}

}
