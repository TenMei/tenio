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
package com.tenio.core.entities.implement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.tenio.core.configuration.defines.CoreMessageCode;
import com.tenio.core.entities.Player;
import com.tenio.core.entities.Room;
import com.tenio.core.entities.RoomState;
import com.tenio.core.entities.defines.RoomRemoveMode;
import com.tenio.core.entities.managers.PlayerManager;
import com.tenio.core.entities.settings.strategies.RoomCredentialValidatedStrategy;
import com.tenio.core.entities.settings.strategies.RoomPlayerSlotGeneratedStrategy;
import com.tenio.core.exceptions.PlayerJoinRoomException;
import com.tenio.core.network.entities.session.Session;

/**
 * @author kong
 */
// TODO: Add description
public final class RoomImpl implements Room {

	public static final int NIL_SLOT = -1;
	public static final int DEFAULT_SLOT = 0;

	private static AtomicLong __idCounter = new AtomicLong();

	private final long __id;
	private String __name;
	private String __password;

	private int __maxPlayers;
	private int __maxSpectators;
	private volatile int __playerCount;
	private volatile int __spectatorCount;

	private Player __owner;
	private PlayerManager __playerManager;

	private RoomRemoveMode __roomRemoveMode;
	private RoomCredentialValidatedStrategy __roomCredentialValidatedStrategy;
	private RoomPlayerSlotGeneratedStrategy __roomPlayerSlotGeneratedStrategy;

	private final Lock __switchPlayerLock;

	private final Map<String, Object> __properties;
	private RoomState __state;

	private volatile boolean __activated;

	private RoomImpl(String name) {
		this(name, null, null);
	}

	private RoomImpl(String name, RoomCredentialValidatedStrategy roomPasswordValidatedStrategy,
			RoomPlayerSlotGeneratedStrategy roomPlayerSlotGeneratedStrategy) {
		__id = __idCounter.getAndIncrement();
		__name = name;

		__password = null;

		__maxPlayers = 0;
		__maxSpectators = 0;
		__spectatorCount = 0;
		__playerCount = 0;

		__owner = null;
		__playerManager = null;

		__roomRemoveMode = RoomRemoveMode.DEFAULT;
		__roomCredentialValidatedStrategy = roomPasswordValidatedStrategy;
		__roomPlayerSlotGeneratedStrategy = roomPlayerSlotGeneratedStrategy;

		__switchPlayerLock = new ReentrantLock();

		__properties = new ConcurrentHashMap<String, Object>();
		__activated = false;
	}

	@Override
	public long getId() {
		return __id;
	}

	@Override
	public String getName() {
		return __name;
	}

	@Override
	public void setName(String name) throws RuntimeException {
		__roomCredentialValidatedStrategy.validateName(name);
		__name = name;
	}

	@Override
	public String getPassword() {
		return __password;
	}

	@Override
	public void setPassword(String password) throws RuntimeException {
		__roomCredentialValidatedStrategy.validatePassword(password);
		__password = password;
	}

	@Override
	public RoomState getState() {
		return __state;
	}

	@Override
	public void setState(RoomState state) {
		__state = state;
	}

	@Override
	public boolean isPublic() {
		return __password == null;
	}

	@Override
	public int getMaxPlayers() {
		return __maxPlayers;
	}

	@Override
	public void setMaxPlayers(int maxPlayers) {
		__maxPlayers = maxPlayers;
	}

	@Override
	public int getMaxSpectators() {
		return __maxSpectators;
	}

	@Override
	public void setMaxSpectators(int maxSpectators) {
		__maxPlayers = maxSpectators;
	}

	@Override
	public Player getOwner() {
		return __owner;
	}

	@Override
	public void setOwner(Player owner) {
		__owner = owner;
	}

	@Override
	public PlayerManager getPlayerManager() {
		return __playerManager;
	}

	@Override
	public void setPlayerManager(PlayerManager playerManager) {
		__playerManager = playerManager;
	}

	@Override
	public boolean isActivated() {
		return __activated;
	}

	@Override
	public void setActivated(boolean activated) {
		__activated = activated;
	}

	@Override
	public RoomRemoveMode getRoomRemoveMode() {
		return __roomRemoveMode;
	}

	@Override
	public void setRoomRemoveMode(RoomRemoveMode roomRemoveMode) {
		__roomRemoveMode = roomRemoveMode;
	}

	@Override
	public Object getProperty(String key) {
		return __properties.get(key);
	}

	@Override
	public boolean containsProperty(String key) {
		return __properties.containsKey(key);
	}

	@Override
	public void addProperty(String key, Object value) {
		__properties.put(key, value);
	}

	@Override
	public void removeProperty(String key) {
		__properties.remove(key);
	}

	@Override
	public int getCapacity() {
		return __maxPlayers + __maxSpectators;
	}

	@Override
	public void setCapacity(int maxPlayers, int maxSpectators) {
		__maxPlayers = maxPlayers;
		__maxSpectators = maxSpectators;
	}

	@Override
	public List<Player> getPlayersList() {
		var players = __playerManager.getAllPlayers().stream().filter(player -> !player.isSpectator())
				.collect(Collectors.toList());
		return new ArrayList<Player>(players);
	}

	@Override
	public List<Player> getSpectatorsList() {
		var spectators = __playerManager.getAllPlayers().stream().filter(player -> player.isSpectator())
				.collect(Collectors.toList());
		return new ArrayList<Player>(spectators);
	}

	@Override
	public int getPlayerCount() {
		return __playerCount;
	}

	@Override
	public int getSpectatorCount() {
		return __spectatorCount;
	}

	@Override
	public boolean containsPlayerName(String playerName) {
		return __playerManager.containsPlayerName(playerName);
	}

	@Override
	public boolean containsPlayer(Player player) {
		return __playerManager.containsPlayer(player);
	}

	@Override
	public Player getPlayerById(long playerId) {
		return __playerManager.getPlayerById(playerId);
	}

	@Override
	public Player getPlayerByName(String playerName) {
		return __playerManager.getPlayerByName(playerName);
	}

	@Override
	public Player getPlayerBySession(Session session) {
		return __playerManager.getPlayerBySession(session);
	}

	@Override
	public List<Player> getAllPlayersList() {
		return new ArrayList<Player>(__playerManager.getAllPlayers());
	}

	@Override
	public List<Session> getAllSessionList() {
		return new ArrayList<Session>(__playerManager.getAllSessions());
	}

	@Override
	public void addPlayer(Player player, boolean asSpectator, int targetSlot) {
		if (__playerManager.containsPlayer(player)) {
			throw new PlayerJoinRoomException(
					String.format("Unable to add player to room, the player: %s already joined room", player.getName()),
					CoreMessageCode.PLAYER_WAS_IN_ROOM);
		}

		boolean validated = false;
		if (asSpectator) {
			validated = getSpectatorCount() < getMaxSpectators();
		} else {
			validated = getPlayerCount() < getMaxPlayers();
		}

		if (!validated) {
			throw new PlayerJoinRoomException(String.format(
					"Unable to add player: %s to room, room is full with maximum player: %d, spectator: %d",
					player.getName(), getMaxPlayers(), getMaxSpectators()), CoreMessageCode.ROOM_IS_FULL);
		}

		__playerManager.addPlayer(player);

		if (asSpectator) {
			player.setSpectator(true);
		}

		__updateElementsCounter();

		if (asSpectator) {
			player.setPlayerSlotInCurrentRoom(RoomImpl.DEFAULT_SLOT);
		} else {
			if (targetSlot == RoomImpl.DEFAULT_SLOT) {
				player.setPlayerSlotInCurrentRoom(__roomPlayerSlotGeneratedStrategy.getFreePlayerSlotInRoom());
			} else {
				try {
					__roomPlayerSlotGeneratedStrategy.tryTakeSlot(targetSlot);
					player.setPlayerSlotInCurrentRoom(targetSlot);
				} catch (IllegalArgumentException e) {
					player.setPlayerSlotInCurrentRoom(RoomImpl.DEFAULT_SLOT);
					throw new PlayerJoinRoomException(String.format("Unable to set the target slot: %d for player: %s",
							targetSlot, player.getName()), CoreMessageCode.SLOT_UNAVAILABLE_IN_ROOM);
				}

			}
		}
	}

	@Override
	public void addPlayer(Player player, boolean asSpectator) {
		addPlayer(player, asSpectator, RoomImpl.DEFAULT_SLOT);
	}

	@Override
	public void addPlayer(Player player) {
		addPlayer(player, false);
	}

	@Override
	public void removePlayer(Player player) {
		__roomPlayerSlotGeneratedStrategy.freeSlotWhenPlayerLeft(player.getPlayerSlotInCurrentRoom());
		__playerManager.removePlayer(player);
		player.setCurrentRoom(null);
		__updateElementsCounter();
	}

	@Override
	public void switchPlayerToSpectator(Player player) {
		if (!__playerManager.containsPlayer(player)) {
			throw new PlayerJoinRoomException(String.format("Player %s was not in room", player.getName()),
					CoreMessageCode.PLAYER_WAS_NOT_IN_ROOM);
		}

		__switchPlayerLock.lock();
		try {
			if (getSpectatorCount() >= getMaxSpectators()) {
				throw new PlayerJoinRoomException("All spectator slots were already taken",
						CoreMessageCode.SWITCH_NO_SPECTATOR_SLOTS_AVAILABLE);
			}

			__roomPlayerSlotGeneratedStrategy.freeSlotWhenPlayerLeft(player.getPlayerSlotInCurrentRoom());
			player.setPlayerSlotInCurrentRoom(RoomImpl.DEFAULT_SLOT);
			player.setSpectator(true);

			__updateElementsCounter();
		} finally {
			__switchPlayerLock.unlock();
		}

	}

	@Override
	public void switchSpectatorToPlayer(Player player, int targetSlot) {
		if (!__playerManager.containsPlayer(player)) {
			throw new PlayerJoinRoomException(String.format("Player %s was not in room", player.getName()),
					CoreMessageCode.PLAYER_WAS_NOT_IN_ROOM);
		}

		__switchPlayerLock.lock();
		try {
			if (getPlayerCount() >= getMaxPlayers()) {
				throw new PlayerJoinRoomException("All player slots were already taken",
						CoreMessageCode.SWITCH_NO_PLAYER_SLOTS_AVAILABLE);
			}

			if (targetSlot == RoomImpl.DEFAULT_SLOT) {
				player.setPlayerSlotInCurrentRoom(__roomPlayerSlotGeneratedStrategy.getFreePlayerSlotInRoom());
				player.setSpectator(false);
			} else {
				try {
					__roomPlayerSlotGeneratedStrategy.tryTakeSlot(targetSlot);
					player.setPlayerSlotInCurrentRoom(targetSlot);
					player.setSpectator(false);
				} catch (IllegalArgumentException e) {
					throw new PlayerJoinRoomException(String.format("Unable to set the target slot: %d for player: %s",
							targetSlot, player.getName()), CoreMessageCode.SLOT_UNAVAILABLE_IN_ROOM);
				}
			}
		} finally {
			__switchPlayerLock.unlock();
		}

	}

	@Override
	public void switchSpectatorToPlayer(Player player) {
		switchSpectatorToPlayer(player, RoomImpl.DEFAULT_SLOT);
	}

	private void __updateElementsCounter() {
		__playerCount = getPlayersList().size();
		__spectatorCount = getSpectatorsList().size();
	}

	@Override
	public boolean isEmpty() {
		return __playerManager.getPlayerCount() == 0;
	}

	@Override
	public boolean isFull() {
		return __playerManager.getPlayerCount() == getCapacity();
	}

	@Override
	public boolean equals(Object room) {
		if (!(room instanceof Room)) {
			return false;
		} else {
			return getId() == ((Room) room).getId();
		}
	}

	// FIXME: Implement me
	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public RoomPlayerSlotGeneratedStrategy getPlayerSlotGeneratedStrategy() {
		return __roomPlayerSlotGeneratedStrategy;
	}

	@Override
	public void setPlayerSlotGeneratedStrategy(RoomPlayerSlotGeneratedStrategy roomPlayerSlotGeneratedStrategy) {
		__roomPlayerSlotGeneratedStrategy = roomPlayerSlotGeneratedStrategy;
	}

	@Override
	public RoomCredentialValidatedStrategy getRoomCredentialValidatedStrategy() {
		return __roomCredentialValidatedStrategy;
	}

	@Override
	public void setRoomCredentialValidatedStrategy(RoomCredentialValidatedStrategy roomCredentialValidatedStrategy) {
		__roomCredentialValidatedStrategy = roomCredentialValidatedStrategy;
	}

}
