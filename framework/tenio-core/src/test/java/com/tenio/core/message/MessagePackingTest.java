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
package com.tenio.core.message;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tenio.common.data.element.CommonObject;
import com.tenio.common.msgpack.MsgPackConverter;
import com.tenio.core.configuration.constant.CoreConstant;
import com.tenio.core.network.utility.MessagePackerUtitlity;

/**
 * @author kong
 */
public final class MessagePackingTest {

	private CommonObject __message;

	@BeforeEach
	public void initialize() {
		__message = CommonObject.newInstance();
		__message.put("string", "String");
		__message.put("integer", 1993);
		__message.put("float", 1.0);
		__message.put("boolean", true);
	}

	@Test
	public void messageSerializeAndUnserializeShouldReturnTrue() {
		// Convert MessageObject message to byte array
		var bytes = MsgPackConverter.serialize(__message);
		
		// Revert the byte array to MessageObject message
		assertEquals(__message, MsgPackConverter.unserialize(bytes));
	}

	@Test
	public void messagePackAndUnpackShouldReturnTrue() {
		// Convert MessageObject message to byte array
		var bytes = MsgPackConverter.serialize(__message);
		// Pack message with header-length value
		var packWithHeader = MessagePackerUtitlity.pack(bytes);
		// Slide message, keep only the message content
		var packWithContent = Arrays.copyOfRange(packWithHeader, CoreConstant.HEADER_BYTES, packWithHeader.length);
		
		// Revert the byte array to MessageObject message
		assertEquals(__message, MsgPackConverter.unserialize(packWithContent));
	}

}
