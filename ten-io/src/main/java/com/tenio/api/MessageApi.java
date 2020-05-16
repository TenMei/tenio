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
package com.tenio.api;

import com.tenio.api.pool.ArrayPool;
import com.tenio.api.pool.ObjectPool;
import com.tenio.configuration.constant.TEvent;
import com.tenio.entity.AbstractPlayer;
import com.tenio.entity.AbstractRoom;
import com.tenio.entity.element.TArray;
import com.tenio.entity.element.TObject;
import com.tenio.event.IEventManager;
import com.tenio.logger.AbstractLogger;
import com.tenio.network.Connection;
import com.tenio.pool.IElementPool;

/**
 * This class provides you a necessary interface for sending messages from the
 * server to clients. It uses a pooling mechanism to increase performance. For
 * creating a message, it's better that decrease this content as less as
 * possible (for fast transferring, save bandwidth, reduce risk, ...). For
 * example, a message should only hold an array, and the client will reference
 * the defined APIs to retrieve his desired values by the array's indexes.
 * 
 * @author kong
 * 
 */
public final class MessageApi extends AbstractLogger {

	private final IElementPool<TArray> __arrayPool = new ArrayPool();
	private final IElementPool<TObject> __objectPool = new ObjectPool();
	private final IEventManager __eventManager;

	public MessageApi(IEventManager eventManager) {
		__eventManager = eventManager;
	}

	/**
	 * Send a message for a connection
	 * 
	 * @param connection See {@link Connection}
	 * @param key        the key of message
	 * @param value      the value of message
	 */
	public void sendToConnection(Connection connection, String key, Object value) {
		var message = __objectPool.get();
		message.put(key, value);
		connection.send(message);
		__objectPool.repay(message);
		if (value instanceof TArray) {
			__arrayPool.repay((TArray) value);
		}
	}

	/**
	 * Send a message to a connection
	 * 
	 * Must use {@link #getArrayPack()} to create data array package for avoiding
	 * memory leak.
	 * 
	 * @param connection See {@link Connection}
	 * @param key        the key of message
	 * @param value      the value of message
	 * @param keyData    the key of message's data
	 * @param data       the main data of message, see: {@link TArray}
	 */
	public void sendToConnection(Connection connection, String key, Object value, String keyData, TArray data) {
		var message = __objectPool.get();
		message.put(key, value);
		message.put(keyData, data);
		connection.send(message);
		__objectPool.repay(message);
		__arrayPool.repay(data);
	}

	/**
	 * Send a message method to a player
	 * 
	 * @param player  See {@link AbstractPlayer}
	 * @param index   the index of connection in current player
	 * @param message the sending message
	 */
	private void __send(AbstractPlayer player, int index, TObject message) {
		player.setCurrentWriterTime(); // update time to check TIMEOUT
		// send to CLIENT (connection)
		if (player.hasConnection(index)) {
			player.getConnection(index).send(message);
		}
		debug("SENT", "", player.getName(), message.toString());
		__eventManager.getExternal().emit(TEvent.SEND_TO_PLAYER, player, index, message);
	}

	/**
	 * Send a message to player via his connection
	 * 
	 * @see #__send(AbstractPlayer, int, TObject)
	 * 
	 * @param key   the key of message
	 * @param value the value of message
	 */
	public void sendToPlayer(AbstractPlayer player, int index, String key, Object value) {
		var message = __objectPool.get();
		message.put(key, value);
		__send(player, index, message);
		__objectPool.repay(message);
		if (value instanceof TArray) {
			__arrayPool.repay((TArray) value);
		}
	}

	/**
	 * Send a message to a player
	 * 
	 * Must use {@link #getArrayPack()} to create data array package for avoiding
	 * memory leak.
	 * 
	 * @param player  the desired player
	 * @param index   the index of connection in current player
	 * @param key     the key of message
	 * @param value   the value of message
	 * @param keyData the key of message's data
	 * @param data    the message data, see: {@link TArray}
	 */
	public void sendToPlayer(AbstractPlayer player, int index, String key, Object value, String keyData, TArray data) {
		var message = __objectPool.get();
		message.put(key, value);
		message.put(keyData, data);
		__send(player, index, message);
		__objectPool.repay(message);
		__arrayPool.repay(data);
	}

	/**
	 * Send a message to all players of one room
	 * 
	 * @param room  the desired room
	 * @param index the index of connection in current player
	 * @param key   the key of message
	 * @param value the value of message
	 */
	public void sendToRoom(AbstractRoom room, int index, String key, Object value) {
		var message = __objectPool.get();
		message.put(key, value);
		for (var player : room.getPlayers().values()) {
			__send(player, index, message);
		}
		__objectPool.repay(message);
		if (value instanceof TArray) {
			__arrayPool.repay((TArray) value);
		}
	}

	/**
	 * Send a message to all players on one room
	 * 
	 * Must use {@link #getArrayPack()} to create data array package for avoiding
	 * memory leak.
	 * 
	 * @param room    the desired room
	 * @param index   the index of connection in current player
	 * @param key     the key of message
	 * @param value   the value of message
	 * @param keyData the key of message's data
	 * @param data    the message's data, see: {@link TArray}
	 */
	public void sendToRoom(AbstractRoom room, int index, String key, Object value, String keyData, TArray data) {
		var message = __objectPool.get();
		message.put(key, value);
		message.put(keyData, data);
		for (var player : room.getPlayers().values()) {
			__send(player, index, message);
		}
		__objectPool.repay(message);
		__arrayPool.repay(data);
	}

	/**
	 * Send a message to all players in one room except the desired player
	 * 
	 * @param player the desired player
	 * @param index  the index of connection in current player
	 * @param key    the key of message
	 * @param value  the value of message
	 */
	public void sendToRoomIgnorePlayer(AbstractPlayer player, int index, String key, Object value) {
		var room = player.getRoom();
		var message = __objectPool.get();
		message.put(key, value);
		for (var p : room.getPlayers().values()) {
			if (!p.equals(player)) {
				__send(p, index, message);
			}
		}
		__objectPool.repay(message);
		if (value instanceof TArray) {
			__arrayPool.repay((TArray) value);
		}
	}

	/**
	 * Send a message to all players in one room except the desired player
	 * 
	 * Must use {@link #getArrayPack()} to create data array package for avoiding
	 * memory leak.
	 * 
	 * @param player  the desired player
	 * @param index   the index of connection in current player
	 * @param key     the key of message
	 * @param value   the value of message
	 * @param keyData the key of message's data
	 * @param data    the message's data, see: {@link TArray}
	 */
	public void sendToRoomIgnorePlayer(AbstractPlayer player, int index, String key, Object value, String keyData,
			TArray data) {
		var room = player.getRoom();
		var message = __objectPool.get();
		message.put(key, value);
		message.put(keyData, data);
		for (var p : room.getPlayers().values()) {
			if (!p.equals(player)) {
				__send(p, index, message);
			}
		}
		__objectPool.repay(message);
		__arrayPool.repay(data);
	}

	/**
	 * @return a {@link TArray} object from the pooling mechanism
	 */
	public TArray getArrayPack() {
		return __arrayPool.get();
	}

}
