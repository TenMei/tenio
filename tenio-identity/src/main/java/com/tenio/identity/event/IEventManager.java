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
package com.tenio.identity.event;

import com.tenio.identity.event.external.TEventManager;
import com.tenio.identity.event.internal.LEventManager;

/**
 * Manage all events in the server
 * 
 * @author kong
 *
 */
public interface IEventManager {

	/**
	 * @return see {@link TEventManager}
	 */
	public TEventManager getExternal();

	/**
	 * @return see {@link LEventManager}
	 */
	public LEventManager getInternal();

	/**
	 * Collect all subscribers and these corresponding events.
	 */
	public void subscribe();

	/**
	 * Clear all subscribers and these corresponding events.
	 */
	public void clear();

}