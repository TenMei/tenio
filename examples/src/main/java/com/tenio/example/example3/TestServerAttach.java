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
package com.tenio.example.example3;

import java.io.IOException;

import org.json.simple.JSONObject;

import com.tenio.common.configuration.IConfiguration;
import com.tenio.common.element.CommonObject;
import com.tenio.common.utility.StringUtility;
import com.tenio.core.AbstractApp;
import com.tenio.core.configuration.define.ExtEvent;
import com.tenio.core.configuration.define.RestMethod;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.IExtension;
import com.tenio.example.server.Configuration;

/**
 * This class shows how a server handle messages that came from a client
 * 
 * @author kong
 *
 */
public final class TestServerAttach extends AbstractApp {

	/**
	 * The entry point
	 */
	public static void main(String[] params) {
		var game = new TestServerAttach();
		game.start();
	}

	@Override
	public IExtension getExtension() {
		return new Extenstion();
	}

	@Override
	public Configuration getConfiguration() {
		return new Configuration("TenIOConfig.attach.xml");
	}

	@Override
	public void onStarted(IExtension extension, IConfiguration configuration) {

	}

	@Override
	public void onShutdown() {

	}

	/**
	 * Your own logic handler class
	 */
	private final class Extenstion extends AbstractExtensionHandler implements IExtension {

		@SuppressWarnings("unchecked")
		@Override
		public void initialize(IConfiguration configuration) {
			_on(ExtEvent.CONNECTION_ESTABLISHED_SUCCESS, params -> {
				var connection = _getConnection(params[0]);
				var message = _getCommonObject(params[1]);

				// Allow the connection login into server (become a player)
				var playerName = message.getString("u");
				// Should confirm that credentials by data from database or other services, here
				// is only for testing
				_playerApi.login(new PlayerAttach(playerName), connection);

				return null;
			});

			_on(ExtEvent.PLAYER_LOGINED_SUCCESS, params -> {
				// The player has login successful
				var player = (PlayerAttach) _getPlayer(params[0]);

				// Now you can allow the player make a UDP connection request
				_messageApi.sendToPlayer(player, PlayerAttach.MAIN_CHANNEL, "c", "udp");

				return null;
			});

			_on(ExtEvent.RECEIVED_MESSAGE_FROM_PLAYER, params -> {
				var player = (PlayerAttach) _getPlayer(params[0]);
				int index = _getInteger(params[1]);

				_messageApi.sendToPlayer(player, index, "response", StringUtility.strgen("echo from server > ", index,
						" > ", player.getConnection(index).getAddress()));

				return null;
			});

			_on(ExtEvent.ATTACH_CONNECTION_REQUEST_VALIDATE, params -> {
				var message = _getCommonObject(params[1]);
				var playerName = message.getString("u");

				// It should be ...
				// 1. check if player has sub connection
				// 2. confirm with player's name and main connection

				// But now temporary returns a player by his name
				return _playerApi.get(playerName);
			});

			_on(ExtEvent.ATTACH_CONNECTION_SUCCESS, params -> {
				var player = (PlayerAttach) _getPlayer(params[1]);

				_messageApi.sendToPlayer(player, PlayerAttach.MAIN_CHANNEL, "c", "udp-done");

				return null;
			});

			_on(ExtEvent.ATTACH_CONNECTION_FAILED, params -> {
				// do nothing but need to be declared
				return null;
			});

			_on(ExtEvent.HTTP_REQUEST_VALIDATE, params -> {
				var method = _getRestMethod(params[0]);
				// var request = _getHttpServletRequest(params[1]);
				var response = _getHttpServletResponse(params[2]);

				if (method.equals(RestMethod.DELETE)) {
					var json = new JSONObject();
					json.putAll(CommonObject.newInstance().add("status", "failed").add("message", "not supported"));
					try {
						response.getWriter().println(json.toString());
					} catch (IOException e) {
						_error(e, "request");
					}
					return response;
				}

				return null;
			});

			_on(ExtEvent.HTTP_REQUEST_HANDLE, params -> {
				// var method = _getRestMethod(params[0]);
				// var request = _getHttpServletRequest(params[1]);
				var response = _getHttpServletResponse(params[2]);

				var json = new JSONObject();
				json.putAll(CommonObject.newInstance().add("status", "ok").add("message", "handler"));
				try {
					response.getWriter().println(json.toString());
				} catch (IOException e) {
					_error(e, "handler");
				}

				return null;
			});

		}

	}

}
