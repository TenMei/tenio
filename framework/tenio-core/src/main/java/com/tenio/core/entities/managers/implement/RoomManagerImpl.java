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
package com.tenio.core.entities.managers.implement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.tenio.core.entities.Player;
import com.tenio.core.entities.Room;
import com.tenio.core.entities.defines.results.RoomCreatedResult;
import com.tenio.core.entities.implement.RoomImpl;
import com.tenio.core.entities.managers.RoomManager;
import com.tenio.core.entities.settings.InitialRoomSetting;
import com.tenio.core.event.implement.EventManager;
import com.tenio.core.exceptions.AddedDuplicatedRoomException;
import com.tenio.core.exceptions.CreatedRoomException;
import com.tenio.core.manager.AbstractManager;

public final class RoomManagerImpl extends AbstractManager implements RoomManager {

	private static final int DEFAULT_MAX_ROOMS = 100;

	private final Map<Long, Room> __roomByIds;
	private int __maxRooms;

	public static RoomManager newInstance(EventManager eventManager) {
		return new RoomManagerImpl(eventManager);
	}

	private RoomManagerImpl(EventManager eventManager) {
		super(eventManager);

		__roomByIds = new ConcurrentHashMap<Long, Room>();
		__maxRooms = DEFAULT_MAX_ROOMS;
	}

	@Override
	public void setMaxRooms(int maxRooms) {
		__maxRooms = maxRooms;
	}

	@Override
	public int getMaxRooms() {
		return __maxRooms;
	}

	@Override
	public void addRoom(Room room) {
		if (containsRoomId(room.getId())) {
			throw new AddedDuplicatedRoomException(room);
		}
		__roomByIds.put(room.getId(), room);
	}

	@Override
	public Room createRoomWithOwner(InitialRoomSetting roomSetting, Player player) {
		int roomCount = getRoomCount();
		if (roomCount >= getMaxRooms()) {
			throw new CreatedRoomException(
					String.format("Unable to create new room, reached limited the maximum room number: %d", roomCount),
					RoomCreatedResult.REACHED_MAX_ROOMS);
		}

		Room newRoom = RoomImpl.newInstance();
		newRoom.setPlayerSlotGeneratedStrategy(roomSetting.getRoomPlayerSlotGeneratedStrategy());
		newRoom.setRoomCredentialValidatedStrategy(roomSetting.getRoomCredentialValidatedStrategy());
		newRoom.setRoomRemoveMode(roomSetting.getRoomRemoveMode());
		newRoom.setName(roomSetting.getName());
		newRoom.setPassword(roomSetting.getPassword());
		newRoom.setActivated(roomSetting.isActivated());
		newRoom.setCapacity(roomSetting.getMaxPlayers(), roomSetting.getMaxSpectators());
		newRoom.setOwner(player);
		newRoom.setPlayerManager(PlayerManagerImpl.newInstance(__eventManager));

		addRoom(newRoom);

		return newRoom;
	}

	@Override
	public boolean containsRoomId(long roomId) {
		return __roomByIds.containsKey(roomId);
	}

	@Override
	public boolean containsRoomName(String roomName) {
		return __roomByIds.values().stream().filter(room -> room.getName().equals(roomName)).findFirst().isPresent();
	}

	@Override
	public Room getRoomById(long roomId) {
		return __roomByIds.get(roomId);
	}

	@Override
	public List<Room> getRoomListByName(String roomName) {
		var rooms = __roomByIds.values().stream().filter(room -> room.getName().equals(roomName))
				.collect(Collectors.toList());
		return new ArrayList<Room>(rooms);
	}

	@Override
	public List<Room> getRoomList() {
		return new ArrayList<Room>(__roomByIds.values());
	}

	@Override
	public void removeRoomById(long roomId) {
		__roomByIds.remove(roomId);
	}

	@Override
	public void changeRoomName(Room room, String roomName) {
		room.setName(roomName);
	}

	@Override
	public void changeRoomPassword(Room room, String roomPassword) {
		room.setPassword(roomPassword);
	}

	@Override
	public void changeRoomCapacity(Room room, int maxPlayers, int maxSpectators) {
		if (maxPlayers <= room.getPlayerCount()) {
			throw new IllegalArgumentException(String.format(
					"Unable to assign the new max player number: %d, because it's less than the current number of players: %d",
					maxPlayers, room.getPlayerCount()));
		}
		if (maxSpectators <= room.getSpectatorCount()) {
			throw new IllegalArgumentException(String.format(
					"Unable to assign the new max spectator number: %d, because it's less than the current number of spectator: %d",
					maxSpectators, room.getSpectatorCount()));
		}

		room.setCapacity(maxPlayers, maxSpectators);
	}

	@Override
	public int getRoomCount() {
		return __roomByIds.size();
	}

}
