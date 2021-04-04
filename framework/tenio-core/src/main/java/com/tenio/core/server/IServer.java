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

import java.io.IOException;

import com.tenio.common.api.TaskApi;
import com.tenio.common.configuration.IConfiguration;
import com.tenio.core.api.MessageApi;
import com.tenio.core.api.PlayerApi;
import com.tenio.core.api.RoomApi;
import com.tenio.core.event.IEventManager;
import com.tenio.core.exception.DuplicatedUriAndMethodException;
import com.tenio.core.exception.NotDefinedSocketConnectionException;
import com.tenio.core.exception.NotDefinedSubscribersException;
import com.tenio.core.extension.IExtension;

/**
 * This class manages the workflow of the current server. The instruction's
 * orders are important, event subscribes must be set last and all configuration
 * values should be confirmed.
 * 
 * @author kong
 * 
 */
interface IServer {

	/**
	 * Start the server base on your own configurations
	 * 
	 * @param configuration, see {@link IConfiguration}
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws NotDefinedSocketConnectionException
	 * @throws NotDefinedSubscribersException
	 * @throws DuplicatedUriAndMethodException
	 */
	void start(final IConfiguration configuration) throws IOException, InterruptedException,
			NotDefinedSocketConnectionException, NotDefinedSubscribersException, DuplicatedUriAndMethodException;

	/**
	 * Shut down the server and close all services
	 */
	void shutdown();

	/**
	 * @return Returns your own implemented extension
	 */
	IExtension getExtension();

	/**
	 * Set your own extension for handling your own logic in-game
	 * 
	 * @param extension your own logic handling @see {@link IExtension}
	 */
	void setExtension(final IExtension extension);

	/**
	 * @return see {@link IEventManager}
	 */
	IEventManager getEventManager();

	/**
	 * @return see {@link PlayerApi}
	 */
	PlayerApi getPlayerApi();

	/**
	 * @return see {@link RoomApi}
	 */
	RoomApi getRoomApi();

	/**
	 * @return see {@link MessageApi}
	 */
	MessageApi getMessageApi();

	/**
	 * @return see {@link TaskApi}
	 */
	TaskApi getTaskApi();

}
