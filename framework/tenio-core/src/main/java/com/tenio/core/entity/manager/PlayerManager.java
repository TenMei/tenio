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
package com.tenio.core.entity.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tenio.common.configuration.IConfiguration;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.core.api.PlayerApi;
import com.tenio.core.configuration.Sock;
import com.tenio.core.configuration.define.ConnectionType;
import com.tenio.core.configuration.define.CoreConfigurationType;
import com.tenio.core.configuration.define.CoreMessageCode;
import com.tenio.core.configuration.define.ExtEvent;
import com.tenio.core.configuration.define.InternalEvent;
import com.tenio.core.entity.AbstractPlayer;
import com.tenio.core.event.IEventManager;
import com.tenio.core.exception.DuplicatedPlayerException;
import com.tenio.core.exception.NullPlayerNameException;
import com.tenio.core.network.Connection;

/**
 * Manage all your players ({@link AbstractPlayer}) on the server. It is a
 * singleton pattern class, which can be called anywhere. But it's better that
 * you use the {@link PlayerApi} interface for easy management.
 * 
 * @see IPlayerManager
 * 
 * @author kong
 * 
 */
public final class PlayerManager extends AbstractLogger implements IPlayerManager {

	/**
	 * A map object to manage your players with the key must be a player's name
	 */
	private final Map<String, AbstractPlayer> __players = new HashMap<String, AbstractPlayer>();
	private final IEventManager __eventManager;
	private IConfiguration __configuration;
	private List<Sock> __socketPorts;
	private List<Sock> __webSocketPorts;
	private int __socketPortsSize;
	private int __webSocketPortsSize;

	public PlayerManager(IEventManager eventManager) {
		__eventManager = eventManager;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(IConfiguration configuration) {
		__configuration = configuration;
		__socketPorts = (List<Sock>) (__configuration.get(CoreConfigurationType.SOCKET_PORTS));
		__webSocketPorts = (List<Sock>) (__configuration.get(CoreConfigurationType.WEBSOCKET_PORTS));
		__socketPortsSize = __socketPorts.size();
		__webSocketPortsSize = __webSocketPorts.size();
	}

	@Override
	public int count() {
		synchronized (__players) {
			return __players.size();
		}
	}

	@Override
	public int countPlayers() {
		synchronized (__players) {
			return (int) __players.values().stream().filter(player -> !player.isNPC()).count();
		}
	}

	@Override
	public Map<String, AbstractPlayer> gets() {
		synchronized (__players) {
			return __players;
		}
	}

	@Override
	public void clear() {
		synchronized (__players) {
			__players.clear();
		}
	}

	@Override
	public boolean contain(final String name) {
		synchronized (__players) {
			return __players.containsKey(name);
		}
	}

	@Override
	public AbstractPlayer get(final String name) {
		synchronized (__players) {
			return __players.get(name);
		}
	}

	@Override
	public void add(final AbstractPlayer player, final Connection connection) {
		if (player.getName() == null) {
			// fire an event
			__eventManager.getExtension().emit(ExtEvent.PLAYER_LOGINED_FAILED, player,
					CoreMessageCode.PLAYER_INFO_IS_INVALID);
			var e = new NullPlayerNameException();
			error(e);
			throw e;
		}

		synchronized (__players) {
			if (__players.containsKey(player.getName())) {
				// fire an event
				__eventManager.getExtension().emit(ExtEvent.PLAYER_LOGINED_FAILED, player,
						CoreMessageCode.PLAYER_WAS_EXISTED);
				var e = new DuplicatedPlayerException();
				error(e, "player name: ", player.getName());
				throw e;
			}

			// add the main connection
			connection.setUsername(player.getName());
			int size = 0;
			if (connection.isType(ConnectionType.WEB_SOCKET)) {
				size = __webSocketPortsSize;
			} else {
				size = __socketPortsSize;
			}
			player.initializeConnections(size);
			player.setConnection(connection, 0);

			__players.put(player.getName(), player);

			// fire an event
			__eventManager.getExtension().emit(ExtEvent.PLAYER_LOGINED_SUCCESS, player);
		}

	}

	@Override
	public void add(final AbstractPlayer player) {
		synchronized (__players) {
			if (__players.containsKey(player.getName())) {
				// fire an event
				__eventManager.getExtension().emit(ExtEvent.PLAYER_LOGINED_FAILED, player,
						CoreMessageCode.PLAYER_WAS_EXISTED);
				var e = new DuplicatedPlayerException();
				error(e, "player name: ", player.getName());
				throw e;
			}

			__players.put(player.getName(), player);
			// fire an event
			__eventManager.getExtension().emit(ExtEvent.PLAYER_LOGINED_SUCCESS, player);
		}

	}

	@Override
	public void remove(final AbstractPlayer player) {
		if (player == null) {
			return;
		}

		synchronized (__players) {
			if (!__players.containsKey(player.getName())) {
				return;
			}

			// force player leave room, fire a logic event
			__eventManager.getInternal().emit(InternalEvent.PLAYER_WAS_FORCED_TO_LEAVE_ROOM, player);

			// remove all player's connections, player
			removeAllConnections(player);

			__players.remove(player.getName());
		}

	}

	@Override
	public void removeAllConnections(final AbstractPlayer player) {
		player.closeAllConnections();
	}

	@Override
	public void clean(final AbstractPlayer player) {
		if (player == null) {
			return;
		}

		synchronized (__players) {
			if (!__players.containsKey(player.getName())) {
				return;
			}

			__players.remove(player.getName());
		}

	}

}
