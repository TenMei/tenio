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
package com.tenio.core.event.extension;

import javax.annotation.concurrent.ThreadSafe;

import com.tenio.core.configuration.define.ZeroEvent;
import com.tenio.core.event.Subscriber;

/**
 * An object which creates a mapping between an event type with a subscriber.
 * 
 * @author kong
 * 
 */
@ThreadSafe
public final class ExtEventSubscriber {

	/**
	 * @see ZeroEvent
	 */
	private final ZeroEvent __event;
	/**
	 * @see Subscriber
	 */
	private final Subscriber __subscriber;

	public static ExtEventSubscriber newInstance(ZeroEvent event, Subscriber subscriber) {
		return new ExtEventSubscriber(event, subscriber);
	}

	private ExtEventSubscriber(ZeroEvent event, Subscriber subscriber) {
		__event = event;
		__subscriber = subscriber;
	}

	/**
	 * @return see {@link ZeroEvent}
	 */
	public ZeroEvent getEvent() {
		return __event;
	}

	/**
	 * @param event the comparison event value
	 * @return Return <b>true</b> if they are equal, <b>false</b> otherwise
	 */
	public boolean hasEvent(ZeroEvent event) {
		return __event == event;
	}

	/**
	 * @return see {@link Subscriber}
	 */
	public Subscriber getSubscriber() {
		return __subscriber;
	}

}
