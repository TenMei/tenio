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
package com.tenio.core.bootstrap.handler;

import java.util.Optional;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tenio.core.bootstrap.annotation.AutowiredAcceptNull;
import com.tenio.core.bootstrap.annotation.Component;
import com.tenio.core.configuration.define.ZeroEvent;
import com.tenio.core.event.Subscriber;
import com.tenio.core.exception.ExtensionValueCastException;
import com.tenio.core.extension.AbstractExtensionHandler;
import com.tenio.core.extension.event.EventHttpRequestHandle;
import com.tenio.core.extension.event.EventHttpRequestValidate;
import com.tenio.core.network.define.RestMethod;

/**
 * @author kong
 */
@Component
public final class HttpEventHandler extends AbstractExtensionHandler {

	@AutowiredAcceptNull
	private EventHttpRequestHandle __eventHttpRequestHandle;

	@AutowiredAcceptNull
	private EventHttpRequestValidate __eventHttpRequestValidate;

	public void initialize() {
		Optional<EventHttpRequestHandle> eventHttpRequestHandleOp = Optional.ofNullable(__eventHttpRequestHandle);
		Optional<EventHttpRequestValidate> eventHttpRequestValidateOp = Optional
				.ofNullable(__eventHttpRequestValidate);

		eventHttpRequestValidateOp.ifPresent(new Consumer<EventHttpRequestValidate>() {

			@Override
			public void accept(EventHttpRequestValidate event) {
				_on(ZeroEvent.HTTP_REQUEST_VALIDATE, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						RestMethod method = _getRestMethod(params[0]);
						HttpServletRequest request = _getHttpServletRequest(params[1]);
						HttpServletResponse response = _getHttpServletResponse(params[2]);

						return event.handle(method, request, response);
					}
				});
			}
		});

		eventHttpRequestHandleOp.ifPresent(new Consumer<EventHttpRequestHandle>() {

			@Override
			public void accept(EventHttpRequestHandle event) {
				_on(ZeroEvent.HTTP_REQUEST_HANDLE, new Subscriber() {

					@Override
					public Object dispatch(Object... params) throws ExtensionValueCastException {
						RestMethod method = _getRestMethod(params[0]);
						HttpServletRequest request = _getHttpServletRequest(params[1]);
						HttpServletResponse response = _getHttpServletResponse(params[2]);

						event.handle(method, request, response);

						return null;
					}
				});
			}
		});
	}

}
