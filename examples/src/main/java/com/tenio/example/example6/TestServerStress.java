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
package com.tenio.example.example6;

import com.tenio.common.configuration.Configuration;
import com.tenio.common.data.element.CommonObjectArray;
import com.tenio.common.utility.MathUtility;
import com.tenio.core.AbstractApp;
import com.tenio.core.configuration.define.ExtensionEvent;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.IExtension;
import com.tenio.example.server.TestConfiguration;

/**
 * This class shows how a server handle 1000 players and communications
 * 
 * @author kong
 *
 */
public final class TestServerStress extends AbstractApp {

	/**
	 * The entry point
	 */
	public static void main(String[] params) {
		var game = new TestServerStress();
		game.start();
	}

	@Override
	public IExtension getExtension() {
		return new Extenstion();
	}

	@Override
	public TestConfiguration getConfiguration() {
		return new TestConfiguration("TenIOConfig.xml");
	}

	@Override
	public void onStarted(IExtension extension, Configuration configuration) {

	}

	@Override
	public void onShutdown() {

	}

	/**
	 * Your own logic handler class
	 */
	private final class Extenstion extends AbstractExtensionHandler implements IExtension {

		private static final int CONVERT_TO_MB = 1024 * 1024;

		@Override
		public void initialize(Configuration configuration) {
			_on(ExtensionEvent.CONNECTION_ESTABLISHED_SUCCESS, params -> {
				var connection = _getConnection(params[0]);
				var message = _getCommonObject(params[1]);

				// Allow the connection login into server (become a player)
				String username = message.getString("u");
				// Should confirm that credentials by data from database or other services, here
				// is only for testing
				_playerApi.login(new PlayerStress(username), connection);

				return null;
			});

			_on(ExtensionEvent.PLAYER_LOGINED_SUCCESS, params -> {
				// The player has login successful
				var player = (PlayerStress) _getPlayer(params[0]);
				player.setIgnoreTimeout(true);

				// Now you can send messages to the client
				// Sending, the data need to be packed
				var data = _messageApi.getMessageObjectArray();
				_messageApi.sendToPlayer(player, PlayerStress.MAIN_CHANNEL, "p", player.getName(), "d",
						data.put("H").put("3").put("L").put("O").put(true)
								.put(CommonObjectArray.newInstance().put("Sub").put("Value").put(100)));

				return null;
			});

			_on(ExtensionEvent.RECEIVED_MESSAGE_FROM_PLAYER, params -> {
				var player = (PlayerStress) _getPlayer(params[0]);

				var pack = __getSortRandomNumberArray();
				// Sending, the data need to be packed
				var data = _messageApi.getMessageObjectArray();
				for (int i = 0; i < pack.length; i++) {
					data.put(pack[i]);
				}

				_messageApi.sendToPlayer(player, PlayerStress.MAIN_CHANNEL, "p", player.getName(), "d", data);

				return null;
			});

			_on(ExtensionEvent.FETCHED_CCU_NUMBER, params -> {
				var ccu = _getInteger(params[0]);

				_info("FETCHED_CCU_NUMBER", ccu);

				return null;
			});

			_on(ExtensionEvent.FETCHED_BANDWIDTH_INFO, params -> {
				long lastReadThroughput = _getLong(params[0]);
				long lastWriteThroughput = _getLong(params[1]);
				long realWriteThroughput = _getLong(params[2]);
				long currentReadBytes = _getLong(params[3]);
				long currentWrittenBytes = _getLong(params[4]);
				long realWrittenBytes = _getLong(params[5]);
				String name = _getString(params[6]);

				var bandwidth = String.format(
						"name=%s;lastReadThroughput=%dKB/s;lastWriteThroughput=%dKB/s;realWriteThroughput=%dKB/s;currentReadBytes=%dKB;currentWrittenBytes=%dKB;realWrittenBytes=%dKB",
						name, lastReadThroughput, lastWriteThroughput, realWriteThroughput, currentReadBytes,
						currentWrittenBytes, realWrittenBytes);

				_info("FETCHED_BANDWIDTH_INFO", bandwidth);

				return null;
			});

			_on(ExtensionEvent.MONITORING_SYSTEM, params -> {
				double cpuUsage = _getDouble(params[0]);
				long totalMemory = _getLong(params[1]);
				long usedMemory = _getLong(params[2]);
				long freeMemory = _getLong(params[3]);
				int countRunningThreads = _getInteger(params[4]);

				var info = String.format(
						"cpuUsage=%.2f%%;totalMemory=%.3fMB;usedMemory=%.3fMB;freeMemory=%.3fMB;runningThreads=%d",
						(float) cpuUsage * 100, (float) totalMemory / CONVERT_TO_MB, (float) usedMemory / CONVERT_TO_MB,
						(float) freeMemory / CONVERT_TO_MB, countRunningThreads);

				_info("MONITORING_SYSTEM", info);

				return null;
			});

		}

		private int[] __getSortRandomNumberArray() {
			int[] arr = new int[10];
			for (int i = 0; i < arr.length; i++) {
				// storing random integers in an array
				arr[i] = MathUtility.randInt(0, 100);
			}
			// bubble sort
			int n = arr.length;
			int temp = 0;
			for (int i = 0; i < n; i++) {
				for (int j = 1; j < (n - i); j++) {
					if (arr[j - 1] > arr[j]) {
						// swap elements
						temp = arr[j - 1];
						arr[j - 1] = arr[j];
						arr[j] = temp;
					}
				}
			}

			return arr;
		}

	}

}
