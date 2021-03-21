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
package com.tenio.core.entity;

import java.util.List;

import com.tenio.core.network.IConnection;

/**
 * A player is one of the base elements in your server. It is a representation
 * of one client in the server and helps that client and this server
 * communicates with each other. You can handle the message that sent from the
 * client or send a message back via a player's instance. Most important here, a
 * player should not be a part of your logic game. A player is better to work as
 * an inspector with 'control' power. For example, when a player joins one game,
 * you need to create for him a corresponding entity. Now, the play will control
 * that entity in the same way you control one chess in a board game. Something
 * like HP status, the number of manas, etc, should not be an attribute of a
 * player, it is a part of an entity's attributes. So, for some interrupt
 * accidents, your character (entity) is still alive and waiting for player
 * re-connect to control it. Look like a soul (player) with a body (entity).
 * 
 * @author kong
 * 
 */
public interface IPlayer {

	String getEntityId();

	void setEntityId(String entityId);

	String getName();

	/**
	 * Check the player's role
	 * 
	 * @return <b>true</b> if the player is a NPC (non player character), otherwise
	 *         return <b>false</b> (A NPC is a player without a connection).
	 */
	boolean isNPC();

	boolean hasConnection(final int index);

	IConnection getConnection(final int index);

	void initializeConnections(final int size);

	void setConnection(final IConnection connection, final int index);

	void closeConnection(int index);

	void closeAllConnections();

	IRoom getCurrentRoom();

	void setCurrentRoom(final IRoom room);

	/**
	 * @return the list of rooms that player has been in
	 */
	List<String> getTracedRoomsList();

	long getReaderTime();

	void setCurrentReaderTime();

	long getWriterTime();

	void setCurrentWriterTime();

	boolean isIgnoreTimeout();

	void setIgnoreTimeout(final boolean flagIgnoreTimeout);

	@Override
	String toString();

	@Override
	int hashCode();

	@Override
	boolean equals(Object obj);

}