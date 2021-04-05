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
package com.tenio.core.extension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tenio.common.api.TaskApi;
import com.tenio.common.element.CommonObject;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.core.api.MessageApi;
import com.tenio.core.api.PlayerApi;
import com.tenio.core.api.RoomApi;
import com.tenio.core.configuration.define.ExtEvent;
import com.tenio.core.configuration.define.RestMethod;
import com.tenio.core.entity.IPlayer;
import com.tenio.core.entity.IRoom;
import com.tenio.core.event.ISubscriber;
import com.tenio.core.exception.ExtensionValueCastException;
import com.tenio.core.network.IConnection;
import com.tenio.core.server.Server;

/**
 * This class provides you all the necessary APIs for your own logic game
 * handling. The entry point class must implement the {@link IExtension}
 * interface. After that, you can create your desired number of handled logic
 * classes. These logic instances in the entry point object will be handled from
 * up to bottom. It works like a chain, you try to add a new value into an
 * object in one first handler and in the last handler, that value can be
 * retrieved for another purpose. Notice that, one event can be handled multiple
 * times in different classes.
 * 
 * @author kong
 * 
 */
public abstract class AbstractExtensionHandler extends AbstractLogger {

	private final Server __server = Server.getInstance();

	/**
	 * @see MessageApi
	 */
	protected final MessageApi _messageApi = __server.getMessageApi();
	/**
	 * @see PlayerApi
	 */
	protected final PlayerApi _playerApi = __server.getPlayerApi();
	/**
	 * @see RoomApi
	 */
	protected final RoomApi _roomApi = __server.getRoomApi();
	/**
	 * @see TaskApi
	 */
	protected final TaskApi _taskApi = __server.getTaskApi();

	/**
	 * Handle your own logic with the corresponding event type
	 * 
	 * @param event      the type of this current event. All the supported type can
	 *                   be found in {@link ExtEvent}
	 * @param subscriber your own subscriber-class handler
	 */
	protected void _on(ExtEvent event, ISubscriber subscriber) {
		__server.getEventManager().getExtension().on(event, subscriber);
	}

	/**
	 * @param object the corresponding object
	 * @return a value in {@link CommonObject} type
	 * @throws ExtensionValueCastException
	 */
	protected CommonObject _getCommonObject(Object object) throws ExtensionValueCastException {
		if (object instanceof CommonObject) {
			return (CommonObject) object;
		}
		throw new ExtensionValueCastException(object.toString());
	}

	/**
	 * @param object the corresponding object
	 * @return a value in {@link IConnection} type
	 * @throws ExtensionValueCastException
	 */
	protected IConnection _getConnection(Object object) throws ExtensionValueCastException {
		if (object instanceof IConnection) {
			return (IConnection) object;
		}
		throw new ExtensionValueCastException(object.toString());
	}

	/**
	 * @param <T>    the corresponding return type
	 * @param object the corresponding object
	 * @return a value in {@link IPlayer} type
	 * @throws ExtensionValueCastException
	 */
	protected IPlayer _getPlayer(Object object) throws ExtensionValueCastException {
		if (object instanceof IPlayer) {
			return (IPlayer) object;
		}
		throw new ExtensionValueCastException(object.toString());
	}

	/**
	 * @param <T>    the corresponding return type
	 * @param object the corresponding object
	 * @return a value in {@link IRoom} type
	 * @throws ExtensionValueCastException
	 */
	protected IRoom _getRoom(Object object) throws ExtensionValueCastException {
		if (object instanceof IRoom) {
			return (IRoom) object;
		}
		throw new ExtensionValueCastException(object.toString());
	}

	/**
	 * @param object the corresponding object
	 * @return a value in {@link RestMethod} type
	 * @throws ExtensionValueCastException
	 */
	protected RestMethod _getRestMethod(Object object) throws ExtensionValueCastException {
		if (object instanceof RestMethod) {
			return (RestMethod) object;
		}
		throw new ExtensionValueCastException(object.toString());
	}

	/**
	 * @param object the corresponding object
	 * @return a value in {@link HttpServletRequest} type
	 * @throws ExtensionValueCastException
	 */
	protected HttpServletRequest _getHttpServletRequest(Object object) throws ExtensionValueCastException {
		if (object instanceof HttpServletRequest) {
			return (HttpServletRequest) object;
		}
		throw new ExtensionValueCastException(object.toString());
	}

	/**
	 * @param object the corresponding object
	 * @return a value in {@link HttpServletResponse} type
	 * @throws ExtensionValueCastException
	 */
	protected HttpServletResponse _getHttpServletResponse(Object object) throws ExtensionValueCastException {
		if (object instanceof HttpServletResponse) {
			return (HttpServletResponse) object;
		}
		throw new ExtensionValueCastException(object.toString());
	}

	/**
	 * @param object the corresponding object
	 * @return a value in {@link Throwable} type
	 * @throws ExtensionValueCastException
	 */
	protected Throwable _getThrowable(Object object) throws ExtensionValueCastException {
		if (object instanceof Throwable) {
			return (Throwable) object;
		}
		throw new ExtensionValueCastException(object.toString());
	}

	/**
	 * @param object the corresponding object
	 * @return a value in {@link Boolean} type
	 * @throws ExtensionValueCastException
	 */
	protected boolean _getBoolean(Object object) throws ExtensionValueCastException {
		if (object instanceof Boolean) {
			return (boolean) object;
		}
		throw new ExtensionValueCastException(object.toString());
	}

	/**
	 * @param object the corresponding object
	 * @return value in {@link String} type
	 * @throws ExtensionValueCastException
	 */
	protected String _getString(Object object) throws ExtensionValueCastException {
		if (object instanceof String) {
			return (String) object;
		}
		throw new ExtensionValueCastException(object.toString());
	}

	/**
	 * @param object the corresponding object
	 * @return a value in {@link Integer} type
	 * @throws ExtensionValueCastException
	 */
	protected int _getInteger(Object object) throws ExtensionValueCastException {
		if (object instanceof Integer) {
			return (int) object;
		}
		throw new ExtensionValueCastException(object.toString());
	}

	/**
	 * @param object the corresponding object
	 * @return a value in {@link Long} type
	 * @throws ExtensionValueCastException
	 */
	protected long _getLong(Object object) throws ExtensionValueCastException {
		if (object instanceof Long) {
			return (long) object;
		}
		throw new ExtensionValueCastException(object.toString());
	}

	/**
	 * @param object the corresponding object
	 * @return a value in {@link Double} type
	 * @throws ExtensionValueCastException
	 */
	protected double _getDouble(Object object) throws ExtensionValueCastException {
		if (object instanceof Double) {
			return (double) object;
		}
		throw new ExtensionValueCastException(object.toString());
	}

}
