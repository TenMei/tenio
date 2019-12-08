/*
The MIT License

Copyright (c) 2016-2019 kong <congcoi123@gmail.com>

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
package com.tenio.examples.example2;

import com.tenio.AbstractApp;
import com.tenio.configuration.constant.TEvent;
import com.tenio.entities.element.TObject;
import com.tenio.examples.example2.entities.Inspector;
import com.tenio.examples.server.Configuration;
import com.tenio.extension.AbstractExtensionHandler;
import com.tenio.extension.IExtension;
import com.tenio.net.Connection;

/**
 * This class is used to send to clients all the daily life of our miner and his
 * wife
 * 
 * @author kong
 *
 */
public final class TestServerFSM extends AbstractApp {

	/**
	 * The entry point
	 */
	public static void main(String[] args) {
		TestServerFSM game = new TestServerFSM();
		game.setup();
	}

	@Override
	public IExtension getExtension() {
		return new Extenstion();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Configuration getConfiguration() {
		return new Configuration("TenIOConfig.xml");
	}

	/**
	 * Your own logic handler
	 */
	private final class Extenstion extends AbstractExtensionHandler implements IExtension {

		@Override
		public void init() {

			on(TEvent.CONNECTION_SUCCESS, (source, args) -> {
				Connection connection = (Connection) args[0];
				TObject message = (TObject) args[1];

				info("CONNECTION", connection.getAddress());

				// allow a connection login to the server
				String username = message.getString("u");
				_playerApi.login(new Inspector(username), connection);

				return null;
			});

			on(TEvent.DISCONNECT_CONNECTION, (source, args) -> {
				Connection connection = (Connection) args[0];

				info("DISCONNECT CONNECTION", connection.getAddress());

				return null;
			});

			on(TEvent.PLAYER_IN_SUCCESS, (source, args) -> {
				// Login successful
				Inspector player = (Inspector) args[0];

				info("PLAYER IN", player.getName());

				return null;
			});

			on(TEvent.PLAYER_TIMEOUT, (source, args) -> {
				Inspector player = (Inspector) args[0];

				info("PLAYER TIMEOUT", player.getName());

				return null;
			});

			on(TEvent.DISCONNECT_PLAYER, (source, args) -> {
				Inspector player = (Inspector) args[0];

				info("DISCONNECT PLAYER", player.getName());

				return null;
			});
			
			// create a life-cycle first
			_heartbeatApi.create("daily-life", new LifeCycle());

		}

	}

}