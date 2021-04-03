/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

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
package com.tenio.core.server;

import com.tenio.common.configuration.IConfiguration;
import com.tenio.common.element.CommonObject;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.configuration.define.CoreMessageCode;
import com.tenio.core.configuration.define.ExtEvent;
import com.tenio.core.configuration.define.InternalEvent;
import com.tenio.core.entity.IPlayer;
import com.tenio.core.entity.manager.IPlayerManager;
import com.tenio.core.entity.manager.IRoomManager;
import com.tenio.core.event.IEventManager;
import com.tenio.core.event.ISubscriber;
import com.tenio.core.network.Connection;

/**
 * Handle the main logic of the server.
 * 
 * @author kong
 *
 */
final class InternalLogicManager extends AbstractLogger {

	private final IEventManager __eventManager;
	private final IPlayerManager __playerManager;
	private final IRoomManager __roomManager;

	public InternalLogicManager(IEventManager eventManager, IPlayerManager playerManager, IRoomManager roomManager) {
		__eventManager = eventManager;
		__playerManager = playerManager;
		__roomManager = roomManager;
	}

	/**
	 * Start handling
	 */
	public void init(IConfiguration configuration) {

		boolean keepPlayerOnDisconnect = configuration.getBoolean(CoreConfigurationType.KEEP_PLAYER_ON_DISCONNECT);
		
		__on(InternalEvent.CONNECTION_WAS_CLOSED, args -> {
			var connection = __getConnection(args[0]);

			if (connection != null) { // the connection has existed
				String playerName = connection.getPlayerName();
				if (playerName != null) { // the player maybe exist
					var player = __playerManager.get(playerName);
					if (player != null) { // the player has existed
						__eventManager.getExtension().emit(ExtEvent.DISCONNECT_PLAYER, player);
						__playerManager.removeAllConnections(player);
						if (!keepPlayerOnDisconnect) {
							__playerManager.clean(player);
						}
					}
				} else { // the free connection (without a corresponding player)
					__eventManager.getExtension().emit(ExtEvent.DISCONNECT_CONNECTION, connection);
				}
				connection.clean();
			}

			return null;
		});

		__on(InternalEvent.CONNECTION_MESSAGE_HANDLED_EXCEPTION, args -> {
			String channelId = __getString(args[0]);
			var connection = __getConnection(args[1]);
			var cause = __getThrowable(args[2]);

			if (connection != null) { // the old connection
				String username = connection.getPlayerName();
				if (username != null) { // the player maybe exist
					var player = __playerManager.get(username);
					if (player != null) { // the player has existed
						__exception(player, cause);
						return null;
					}
				}
			}
			// also catch the exception for "channel"
			__exception(channelId, cause);

			return null;
		});

		__on(InternalEvent.PLAYER_WAS_FORCED_TO_LEAVE_ROOM, args -> {
			var player = __getPlayer(args[0]);

			__roomManager.makePlayerLeaveRoom(player, true);

			return null;
		});

		__on(InternalEvent.CONNECTION_WAS_CLOSED_MANUALLY, args -> {
			String name = __getString(args[0]);

			var player = __playerManager.get(name);
			if (player != null) {
				__eventManager.getExtension().emit(ExtEvent.DISCONNECT_PLAYER, player);
			}

			return null;
		});

		__on(InternalEvent.MESSAGE_HANDLED_IN_CHANNEL, args -> {
			var index = __getInt(args[0]);
			var connection = __getConnection(args[1]);
			var message = __getMessageObject(args[2]);
			var tempConnection = __getConnection(args[3]);

			if (connection == null) {
				__createNewConnection(configuration, index, tempConnection, message);
			} else {
				var playerName = connection.getPlayerName();
				if (playerName != null) {
					var player = __playerManager.get(playerName);
					if (player != null) { // the player has existed
						__handle(player, index, message);
					} else {
						// Can handle free connection here
					}
				} else {
					// Can handle free connection here
				}
			}

			return null;
		});

	}

	private void __createNewConnection(final IConfiguration configuration, final int index,
			final Connection connection, final CommonObject message) {
		if (index == 0) { // is main connection
			// check reconnection request first
			var player = (IPlayer) __eventManager.getExtension().emit(ExtEvent.PLAYER_RECONNECT_REQUEST_HANDLE, connection,
					message);
			if (player != null) {
				connection.setPlayerName(player.getName());
				player.setConnection(connection, 0); // main connection
				__eventManager.getExtension().emit(ExtEvent.PLAYER_RECONNECT_SUCCESS, player);
			} else {
				// check the number of current players
				if (__playerManager.count() > configuration.getInt(CoreConfigurationType.MAX_NUMBER_PLAYERS)) {
					__eventManager.getExtension().emit(ExtEvent.CONNECTION_ESTABLISHED_FAILED, connection,
							CoreMessageCode.REACHED_MAX_CONNECTION);
					connection.close();
				} else {
					__eventManager.getExtension().emit(ExtEvent.CONNECTION_ESTABLISHED_SUCCESS, connection, message);
				}
			}

		} else {
			// the condition for creating sub-connection
			var player = (IPlayer) __eventManager.getExtension().emit(ExtEvent.ATTACH_CONNECTION_REQUEST_VALIDATE, index,
					message);

			if (player == null) {
				__eventManager.getExtension().emit(ExtEvent.ATTACH_CONNECTION_FAILED, index, message,
						CoreMessageCode.PLAYER_NOT_FOUND);
			} else if (!player.hasConnection(0)) {
				__eventManager.getExtension().emit(ExtEvent.ATTACH_CONNECTION_FAILED, index, message,
						CoreMessageCode.MAIN_CONNECTION_NOT_FOUND);
			} else {
				connection.setPlayerName(player.getName());
				player.setConnection(connection, index);
				__eventManager.getExtension().emit(ExtEvent.ATTACH_CONNECTION_SUCCESS, index, player);
			}
		}
	}

	private void __on(final InternalEvent event, ISubscriber sub) {
		__eventManager.getInternal().on(event, sub);
	}

	/**
	 * @param object the corresponding object
	 * @return a value in, see {@link CommonObject}
	 */
	private CommonObject __getMessageObject(Object object) {
		return (CommonObject) object;
	}

	/**
	 * @param object the corresponding object
	 * @return a value in ,see {@link Connection}
	 */
	private Connection __getConnection(Object object) {
		return (Connection) object;
	}

	/**
	 * @param object the corresponding object
	 * @return a value in, see {@link IPlayer}
	 */
	private IPlayer __getPlayer(Object object) {
		return (IPlayer) object;
	}

	/**
	 * @param object the corresponding object
	 * @return a value in, see {@link String}
	 */
	private String __getString(Object object) {
		return (String) object;
	}

	/**
	 * @param object the corresponding object
	 * @return a value in, see {@link Integer}
	 */
	private int __getInt(Object object) {
		return (int) object;
	}

	/**
	 * @param object the corresponding object
	 * @return value in, see {@link Throwable}
	 */
	private Throwable __getThrowable(Object object) {
		return (Throwable) object;
	}

	private void __handle(IPlayer player, int index, CommonObject message) {
		player.setCurrentReaderTime();
		__eventManager.getExtension().emit(ExtEvent.RECEIVED_MESSAGE_FROM_PLAYER, player, index, message);
	}

	private void __exception(IPlayer player, Throwable cause) {
		_error(cause, "player name: ", player.getName());
	}

	private void __exception(String identify, Throwable cause) {
		_error(cause, "identify: ", identify);
	}

}
