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
package com.tenio.core.server;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

import com.tenio.common.configuration.Configuration;
import com.tenio.common.configuration.constant.CommonConstant;
import com.tenio.common.loggers.SystemLogger;
import com.tenio.core.api.ServerApi;
import com.tenio.core.api.ServerApiImpl;
import com.tenio.core.bootstrap.EventHandler;
import com.tenio.core.configuration.defines.CoreConfigurationType;
import com.tenio.core.configuration.defines.InternalEvent;
import com.tenio.core.entities.managers.PlayerManager;
import com.tenio.core.entities.managers.RoomManager;
import com.tenio.core.entities.managers.implement.PlayerManagerImpl;
import com.tenio.core.entities.managers.implement.RoomManagerImpl;
import com.tenio.core.events.EventManager;
import com.tenio.core.events.implement.EventManagerImpl;
import com.tenio.core.monitoring.system.SystemInfo;
import com.tenio.core.network.NetworkService;
import com.tenio.core.network.NetworkServiceImpl;
import com.tenio.core.network.defines.data.HttpConfig;
import com.tenio.core.network.defines.data.SocketConfig;
import com.tenio.core.network.entities.packet.policy.PacketQueuePolicy;
import com.tenio.core.network.security.filter.ConnectionFilter;
import com.tenio.core.network.zero.codec.compression.BinaryPacketCompressor;
import com.tenio.core.network.zero.codec.decoder.BinaryPacketDecoder;
import com.tenio.core.network.zero.codec.encoder.BinaryPacketEncoder;
import com.tenio.core.network.zero.codec.encryption.BinaryPacketEncrypter;
import com.tenio.core.schedule.ScheduleService;
import com.tenio.core.schedule.ScheduleServiceImpl;
import com.tenio.core.server.services.InternalProcessorService;
import com.tenio.core.server.services.InternalProcessorServiceImpl;
import com.tenio.core.server.settings.ConfigurationAssessment;

/**
 * This class manages the workflow of the current server. The instruction's
 * orders are important, event subscribes must be set last and all configuration
 * values should be confirmed.
 * 
 * @author kong
 */
@ThreadSafe
public final class ServerImpl extends SystemLogger implements Server {

	private static Server __instance;

	private ServerImpl() {

		__eventManager = EventManagerImpl.newInstance();
		__roomManager = RoomManagerImpl.newInstance(__eventManager);
		__playerManager = PlayerManagerImpl.newInstance(__eventManager);
		__networkService = NetworkServiceImpl.newInstance(__eventManager);
		__internalProcessorService = InternalProcessorServiceImpl.newInstance(__eventManager);
		__scheduleService = ScheduleServiceImpl.newInstance(__eventManager);
		__serverApi = ServerApiImpl.newInstance(this);

		// print out the framework's preface
		for (var line : CommonConstant.CREDIT) {
			info("", "", line);
		}
	} // prevent creation manually

	// preventing Singleton object instantiation from outside
	// creates multiple instance if two thread access this method simultaneously
	public static Server getInstance() {
		if (__instance == null) {
			__instance = new ServerImpl();
		}
		return __instance;
	}

	private final EventManager __eventManager;
	private final RoomManager __roomManager;
	private final PlayerManager __playerManager;
	private final InternalProcessorService __internalProcessorService;
	private final ScheduleService __scheduleService;
	private final NetworkService __networkService;
	private final ServerApi __serverApi;

	private String __serverName;

	@Override
	public void start(Configuration configuration, EventHandler eventHandler)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {

		var ConfigsAssessment = ConfigurationAssessment.newInstance(__eventManager, configuration);
		ConfigsAssessment.assess();

		__serverName = configuration.getString(CoreConfigurationType.SERVER_NAME);

		// show system information
		var systemInfo = new SystemInfo();
		systemInfo.logSystemInfo();
		systemInfo.logNetCardsInfo();
		systemInfo.logDiskInfo();

		info("SERVER", __serverName, "Starting ...");

		// Put the current configurations to the logger
		info("CONFIGURATION", configuration.toString());

		__setupInternalProcessorService(configuration);
		__setupNetworkService(configuration);
		__setupScheduleService(configuration);

		__internalProcessorService.subscribe();

		eventHandler.initialize();

		__startServices();

		// collect all subscribers, listen all the events
		__eventManager.subscribe();

		info("SERVER", __serverName, "Started!");
	}

	private void __startServices() {
		__networkService.start();
		__scheduleService.start();
		__eventManager.getInternal().emit(InternalEvent.SERVER_STARTED, __serverName);
	}

	private void __setupScheduleService(Configuration configuration) {
		__scheduleService.setCcuScanInterval(configuration.getInt(CoreConfigurationType.INTERVAL_CCU_SCAN));
		__scheduleService.setDeadlockScanInterval(configuration.getInt(CoreConfigurationType.INTERVAL_DEADLOCK_SCAN));
		__scheduleService.setDisconnectedPlayerScanInterval(
				configuration.getInt(CoreConfigurationType.INTERVAL_DISCONNECTED_PLAYER_SCAN));
		__scheduleService
				.setRemovedRoomScanInterval(configuration.getInt(CoreConfigurationType.INTERVAL_REMOVED_ROOM_SCAN));
		__scheduleService
				.setSystemMonitoringInterval(configuration.getInt(CoreConfigurationType.INTERVAL_SYSTEM_MONITORING));
		__scheduleService
				.setTrafficCounterInterval(configuration.getInt(CoreConfigurationType.INTERVAL_TRAFFIC_COUNTER));

		__scheduleService.setPlayerManager(__playerManager);
		__scheduleService.setRoomManager(__roomManager);
	}

	@SuppressWarnings("unchecked")
	private void __setupNetworkService(Configuration configuration)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {

		var connectionFilterClazz = Class
				.forName(configuration.getString(CoreConfigurationType.CLASS_CONNECTION_FILTER));
		__networkService.setConnectionFilterClass((Class<? extends ConnectionFilter>) connectionFilterClazz);

		var httpConfig = (List<HttpConfig>) configuration.get(CoreConfigurationType.HTTP_CONFIGS);
		__networkService.setHttpPort(httpConfig.get(0).getPort());
		__networkService.setHttpPathConfigs(httpConfig.get(0).getPaths());

		__networkService.setSocketAcceptorBufferSize(
				configuration.getInt(CoreConfigurationType.NETWORK_PROP_SOCKET_ACCEPTOR_BUFFER_SIZE));
		__networkService.setSocketAcceptorWorkers(configuration.getInt(CoreConfigurationType.THREADS_SOCKET_ACCEPTOR));

		__networkService.setSocketConfigs((List<SocketConfig>) configuration.get(CoreConfigurationType.SOCKET_CONFIGS));

		__networkService.setSocketReaderBufferSize(
				configuration.getInt(CoreConfigurationType.NETWORK_PROP_SOCKET_READER_BUFFER_SIZE));
		__networkService.setSocketReaderWorkers(configuration.getInt(CoreConfigurationType.THREADS_SOCKET_READER));

		__networkService.setSocketWriterBufferSize(
				configuration.getInt(CoreConfigurationType.NETWORK_PROP_SOCKET_WRITER_BUFFER_SIZE));
		__networkService.setSocketWriterWorkers(configuration.getInt(CoreConfigurationType.THREADS_SOCKET_WRITER));

		__networkService
				.setWebsocketConsumerWorkers(configuration.getInt(CoreConfigurationType.THREADS_WEBSOCKET_CONSUMER));
		__networkService
				.setWebsocketProducerWorkers(configuration.getInt(CoreConfigurationType.THREADS_WEBSOCKET_PRODUCER));

		__networkService.setWebsocketReceiverBufferSize(
				configuration.getInt(CoreConfigurationType.NETWORK_PROP_WEBSOCKET_RECEIVER_BUFFER_SIZE));
		__networkService.setWebsocketSenderBufferSize(
				configuration.getInt(CoreConfigurationType.NETWORK_PROP_WEBSOCKET_SENDER_BUFFER_SIZE));
		__networkService
				.setWebsocketUsingSSL(configuration.getBoolean(CoreConfigurationType.NETWORK_PROP_WEBSOCKET_USING_SSL));

		var packetQueuePolicyClazz = Class
				.forName(configuration.getString(CoreConfigurationType.CLASS_PACKET_QUEUE_POLICY));
		__networkService.setPacketQueuePolicy((Class<? extends PacketQueuePolicy>) packetQueuePolicyClazz);
		__networkService.setPacketQueueSize(configuration.getInt(CoreConfigurationType.PROP_MAX_PACKET_QUEUE_SIZE));

		var binaryPacketCompressorClazz = Class
				.forName(configuration.getString(CoreConfigurationType.CLASS_PACKET_COMPRESSOR));
		var binaryPacketCompressor = (BinaryPacketCompressor) binaryPacketCompressorClazz.getDeclaredConstructor()
				.newInstance();
		var binaryPacketEncrypterClazz = Class
				.forName(configuration.getString(CoreConfigurationType.CLASS_PACKET_ENCRYPTER));
		var binaryPacketEncrypter = (BinaryPacketEncrypter) binaryPacketEncrypterClazz.getDeclaredConstructor()
				.newInstance();
		var binaryPacketEncoderClazz = Class
				.forName(configuration.getString(CoreConfigurationType.CLASS_PACKET_ENCODER));
		var binaryPacketEncoder = (BinaryPacketEncoder) binaryPacketEncoderClazz.getDeclaredConstructor().newInstance();
		var binaryPacketDecoderClazz = Class
				.forName(configuration.getString(CoreConfigurationType.CLASS_PACKET_DECODER));
		var binaryPacketDecoder = (BinaryPacketDecoder) binaryPacketDecoderClazz.getDeclaredConstructor().newInstance();

		binaryPacketEncoder.setCompressionThresholdBytes(
				configuration.getInt(CoreConfigurationType.NETWORK_PROP_PACKET_COMPRESSION_THRESHOLD_BYTES));
		binaryPacketEncoder.setCompressor(binaryPacketCompressor);
		binaryPacketEncoder.setEncrypter(binaryPacketEncrypter);

		binaryPacketDecoder.setCompressor(binaryPacketCompressor);
		binaryPacketDecoder.setEncrypter(binaryPacketEncrypter);
	}

	private void __setupInternalProcessorService(Configuration configuration) {
		__internalProcessorService
				.setMaxNumberPlayers(configuration.getInt(CoreConfigurationType.PROP_MAX_NUMBER_PLAYERS));
		__internalProcessorService.setPlayerManager(__playerManager);
		__internalProcessorService
				.setMaxRequestQueueSize(configuration.getInt(CoreConfigurationType.PROP_MAX_REQUEST_QUEUE_SIZE));
		__internalProcessorService
				.setThreadPoolSize(configuration.getInt(CoreConfigurationType.THREADS_INTERNAL_PROCESSOR));
	}

	@Override
	public void shutdown() {
		info("SERVER", __serverName, "Stopping ...");
		__shutdownServices();
		info("SERVER", __serverName, "Stopped!");
	}

	private void __shutdownServices() {
		__internalProcessorService.halt();
		__networkService.halt();
		__scheduleService.halt();
	}

	@Override
	public ServerApi getApi() {
		return __serverApi;
	}

}
