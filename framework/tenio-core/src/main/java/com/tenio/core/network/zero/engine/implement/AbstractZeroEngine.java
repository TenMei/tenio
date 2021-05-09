package com.tenio.core.network.zero.engine.implement;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.tenio.common.configuration.Configuration;
import com.tenio.common.logger.SystemLogger;
import com.tenio.common.utility.StringUtility;
import com.tenio.core.network.entity.session.SessionManager;
import com.tenio.core.network.zero.engine.ZeroEngine;
import com.tenio.core.network.zero.handler.DatagramIOHandler;
import com.tenio.core.network.zero.handler.SocketIOHandler;

public abstract class AbstractZeroEngine extends SystemLogger implements ZeroEngine, Runnable {

	private static final int DEFAULT_NUMBER_WORKERS = 5;

	private volatile int __id;
	private String __name;

	private ExecutorService __executor;
	private int __executorSize;

	private Configuration __configuration;

	private SocketIOHandler __socketIOHandler;
	private DatagramIOHandler __datagramIOHandler;
	private SessionManager __sessionManager;

	private volatile boolean __activated;

	public AbstractZeroEngine() {
		__executorSize = DEFAULT_NUMBER_WORKERS;
		__activated = false;
		__id = 0;
	}

	private void __initializeWorkers() {
		__executor = Executors.newFixedThreadPool(__executorSize);
		for (int i = 0; i < __executorSize; i++) {
			__executor.execute(this);
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				if (__executor != null && !__executor.isShutdown()) {
					__stop();
				}
			}
		});
	}

	private void __stop() {
		pause();
		onStopped();
		__executor.shutdown();
		while (true) {
			try {
				if (__executor.awaitTermination(5, TimeUnit.SECONDS)) {
					break;
				}
			} catch (InterruptedException e) {
				error(e);
			}
		}
		info("ENGINE STOPPED", buildgen("engine-", getName(), "-", __id));
		destroy();
		onDestroyed();
		info("ENGINE DESTROYED", buildgen("engine-", getName(), "-", __id));
	}

	@Override
	public void run() {
		__id++;
		info("ENGINE START", buildgen("engine-", getName(), "-", __id));
		__setThreadName();

		while (__activated) {
			onRunning();
		}

		info("ENGINE STOPPING", buildgen("engine-", getName(), "-", __id));
	}

	private void __setThreadName() {
		Thread.currentThread().setName(StringUtility.strgen("engine-", getName(), "-", __id));
	}

	@Override
	public void setConfiguration(Configuration configuration) {
		__configuration = configuration;
	}

	@Override
	public Configuration getConfiguration() {
		return __configuration;
	}

	@Override
	public void setSocketIOHandler(SocketIOHandler socketIOHandler) {
		__socketIOHandler = socketIOHandler;
	}

	@Override
	public SocketIOHandler getSocketIOHandler() {
		return __socketIOHandler;
	}

	@Override
	public void setDatagramIOHandler(DatagramIOHandler datagramIOHandler) {
		__datagramIOHandler = datagramIOHandler;
	}

	@Override
	public DatagramIOHandler getDatagramIOHandler() {
		return __datagramIOHandler;
	}

	@Override
	public void setSessionManager(SessionManager sessionManager) {
		__sessionManager = sessionManager;
	}

	@Override
	public SessionManager getSessionManager() {
		return __sessionManager;
	}

	@Override
	public void initialize() {
		__initializeWorkers();
		onInitialized();
	}

	@Override
	public void start() {
		__activated = true;
		onStarted();
	}

	@Override
	public void resume() {
		__activated = true;
		onResumed();
	}

	@Override
	public void pause() {
		__activated = false;
		onPaused();
	}

	@Override
	public void stop() {
		__stop();
	}

	@Override
	public void destroy() {
		__executor = null;
	}

	@Override
	public String getName() {
		return __name;
	}

	@Override
	public void setName(String name) {
		__name = name;
	}

}
