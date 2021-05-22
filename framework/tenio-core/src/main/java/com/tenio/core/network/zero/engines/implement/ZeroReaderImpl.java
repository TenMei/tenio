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
package com.tenio.core.network.zero.engines.implement;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import com.tenio.core.events.EventManager;
import com.tenio.core.exceptions.ServiceRuntimeException;
import com.tenio.core.network.entities.session.Session;
import com.tenio.core.network.statistics.NetworkReaderStatistic;
import com.tenio.core.network.zero.engines.ZeroReader;
import com.tenio.core.network.zero.engines.listeners.ZeroAcceptorListener;
import com.tenio.core.network.zero.engines.listeners.ZeroReaderListener;
import com.tenio.core.network.zero.engines.listeners.ZeroWriterListener;

/**
 * @author kong
 */
// TODO: Add description
public final class ZeroReaderImpl extends AbstractZeroEngine implements ZeroReader, ZeroReaderListener {

	private ZeroAcceptorListener __zeroAcceptorListener;
	private ZeroWriterListener __zeroWriterListener;
	private Selector __readableSelector;
	private NetworkReaderStatistic __networkReaderStatistic;
	private ByteBuffer __readerBuffer;

	public static ZeroReader newInstance(EventManager eventManager) {
		return new ZeroReaderImpl(eventManager);
	}

	private ZeroReaderImpl(EventManager eventManager) {
		super(eventManager);

		setName("reader");
	}

	private void __initializeSelector() throws ServiceRuntimeException {
		try {
			__readableSelector = Selector.open();
		} catch (IOException e) {
			throw new ServiceRuntimeException(e.getMessage());
		}
	}

	private void __initializeBuffer() {
		__readerBuffer = ByteBuffer.allocate(getMaxBufferSize());
	}

	private void __readableLoop() {
		try {
			__zeroAcceptorListener.handleAcceptableChannels();
			__readIncomingSocketData();
			Thread.sleep(5L);
		} catch (InterruptedException e) {
			error(e);
		}
	}

	private void __readIncomingSocketData() {
		SocketChannel socketChannel = null;
		DatagramChannel datagramChannel = null;
		SelectionKey selectionKey = null;

		try {
			// blocks until at least one channel is ready for the events you registered for
			int readyKeyCount = __readableSelector.selectNow();

			if (readyKeyCount == 0) {
				return;
			}

			// readable selector was registered by OP_READ interested only socket channels,
			// but in some cases, we can received "can writable" signal from those sockets
			Set<SelectionKey> readyKeys = __readableSelector.selectedKeys();
			Iterator<SelectionKey> keyIterators = readyKeys.iterator();

			while (keyIterators.hasNext()) {
				selectionKey = keyIterators.next();

				if (selectionKey.isValid()) {
					SelectableChannel channel = selectionKey.channel();
					// we already registered 2 types of channels for this selector and need to
					// separate the processes
					if (channel instanceof SocketChannel) {
						socketChannel = (SocketChannel) channel;
						__readTcpData(socketChannel, selectionKey);
					} else if (channel instanceof DatagramChannel) {
						datagramChannel = (DatagramChannel) channel;
						__readUpdData(datagramChannel, selectionKey);
					}
				}

				keyIterators.remove();
			}

			// FIXME: Handles necessary exceptions for stopping the process in appropriate
			// cases
		} catch (ClosedSelectorException e2) {
			error(e2, "Selector is closed!");
		} catch (CancelledKeyException e3) {
			error(e3, "Cancelled key");
		} catch (IOException e4) {
			error(e4, "I/O reading/selection error: ", e4.getMessage());
		} catch (Exception e5) {
			error(e5, "Generic reading/selection error: ", e5.getMessage());
		}

	}

	private void __readTcpData(SocketChannel socketChannel, SelectionKey selectionKey) {
		// retrieves session by its socket channel
		Session session = getSessionManager().getSessionBySocket(socketChannel);

		if (session == null) {
			debug("READ CHANNEL", "Reader handle a null session with the socket channel: ", socketChannel.toString());
			return;
		}

		// when a socket channel is writable, should make it highest priority
		// manipulation
		if (selectionKey.isWritable()) {
			// should continue put this session for sending all left packets first
			__zeroWriterListener.continueWriteInterestOp(session);
			// now we should set it back to interest in OP_READ
			selectionKey.interestOps(SelectionKey.OP_READ);
		}

		if (selectionKey.isReadable()) {
			// prepares the buffer first
			__readerBuffer.clear();
			// reads data from socket and write them to buffer
			int byteCount = -1;
			try {
				byteCount = socketChannel.read(__readerBuffer);
			} catch (IOException e) {
				error(e, "An exception was occured on channel: ", socketChannel.toString());
				getSocketIOHandler().sessionException(session, e);
			}
			// no left data is available, should close the connection
			if (byteCount == -1) {
				__closeTcpConnection(socketChannel);
			} else if (byteCount > 0) {
				// update statistic data
				session.addReadBytes(byteCount);
				__networkReaderStatistic.updateReadBytes(byteCount);
				// ready to read data from buffer
				__readerBuffer.flip();
				// reads data from buffer and transfers them to the next process
				byte[] binary = new byte[__readerBuffer.limit()];
				__readerBuffer.get(binary);

				getSocketIOHandler().sessionRead(session, binary);
			}

		}
	}

	private void __closeTcpConnection(SelectableChannel channel) {
		SocketChannel socketChannel = (SocketChannel) channel;
		getSocketIOHandler().channelInactive(socketChannel);
		if (!socketChannel.socket().isClosed()) {
			try {
				socketChannel.socket().shutdownInput();
				socketChannel.socket().shutdownOutput();
				socketChannel.close();
			} catch (IOException e) {
				error(e, "Error on closing socket channel: ", socketChannel.toString());
			}
		}

	}

	private void __readUpdData(DatagramChannel datagramChannel, SelectionKey selectionKey) {
		// retrieves session by its datagram channel, hence we are using only one
		// datagram channel for all sessions, we use incoming request remote address to
		// distinguish them
		Session session = getSessionManager()
				.getSessionByDatagram((InetSocketAddress) datagramChannel.socket().getRemoteSocketAddress());

		if (selectionKey.isWritable()) {
			// should continue put this session for sending all left packets first
			__zeroWriterListener.continueWriteInterestOp(session);
			// now we should set it back to interest in OP_READ
			selectionKey.interestOps(SelectionKey.OP_READ);
		}

		if (selectionKey.isReadable()) {
			// prepares the buffer first
			__readerBuffer.clear();
			// reads data from socket and write them to buffer
			int byteCount = -1;
			try {
				byteCount = datagramChannel.read(__readerBuffer);
			} catch (IOException e) {
				if (session == null) {
					error(e, "An exception was occured on channel: ", datagramChannel.toString());
					getDatagramIOHandler().channelException(datagramChannel, e);
				} else {
					getDatagramIOHandler().sessionException(session, e);
				}
			}
			// update statistic data
			__networkReaderStatistic.updateReadBytes(byteCount);
			// ready to read data from buffer
			__readerBuffer.flip();
			// reads data from buffer and transfers them to the next process
			byte[] binary = new byte[__readerBuffer.limit()];
			__readerBuffer.get(binary);

			if (session == null) {
				getDatagramIOHandler().channelRead(datagramChannel, binary);
			} else {
				session.addReadBytes(byteCount);
				getDatagramIOHandler().sessionRead(session, binary);
			}
		}

	}

	@Override
	public void acceptDatagramChannel(DatagramChannel datagramChannel) throws ClosedChannelException {
		datagramChannel.register(__readableSelector, SelectionKey.OP_READ);
	}

	@Override
	public SelectionKey acceptSocketChannel(SocketChannel socketChannel) throws ClosedChannelException {
		return socketChannel.register(__readableSelector, SelectionKey.OP_READ);
	}

	@Override
	public void setZeroAcceptorListener(ZeroAcceptorListener zeroAcceptorListener) {
		__zeroAcceptorListener = zeroAcceptorListener;
	}

	@Override
	public void setZeroWriterListener(ZeroWriterListener zeroWriterListener) {
		__zeroWriterListener = zeroWriterListener;
	}

	@Override
	public NetworkReaderStatistic getNetworkReaderStatistic() {
		return __networkReaderStatistic;
	}

	@Override
	public void wakeup() {
		__readableSelector.wakeup();
	}

	@Override
	public void setNetworkReaderStatistic(NetworkReaderStatistic networkReaderStatistic) {
		__networkReaderStatistic = networkReaderStatistic;
	}

	@Override
	public void onInitialized() {
		__initializeSelector();
		__initializeBuffer();
	}

	@Override
	public void onStarted() {
		// do nothing
	}

	@Override
	public void onResumed() {
		// do nothing
	}

	@Override
	public void onRunning() {
		__readableLoop();
	}

	@Override
	public void onPaused() {
		// do nothing
	}

	@Override
	public void onHalted() {
		try {
			Thread.sleep(500L);
			__readableSelector.close();
		} catch (IOException | InterruptedException e) {
			error(e, "Exception while closing selector");
		}
	}

	@Override
	public void onDestroyed() {
		__readableSelector = null;
	}

}
