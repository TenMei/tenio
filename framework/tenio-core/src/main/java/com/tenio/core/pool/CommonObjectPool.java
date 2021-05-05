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
package com.tenio.core.pool;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import com.tenio.common.configuration.constant.CommonConstants;
import com.tenio.common.data.CommonObject;
import com.tenio.common.exception.NullElementPoolException;
import com.tenio.common.logger.ZeroLogger;
import com.tenio.common.pool.IElementsPool;

/**
 * The object pool mechanism for {@link CommonObject}.
 * 
 * @author kong
 * 
 */
@ThreadSafe
public final class CommonObjectPool extends ZeroLogger implements IElementsPool<CommonObject> {

	@GuardedBy("this")
	private CommonObject[] __pool;
	@GuardedBy("this")
	private boolean[] __used;

	public CommonObjectPool() {
		__pool = new CommonObject[CommonConstants.DEFAULT_NUMBER_ELEMENTS_POOL];
		__used = new boolean[CommonConstants.DEFAULT_NUMBER_ELEMENTS_POOL];

		for (int i = 0; i < __pool.length; i++) {
			__pool[i] = CommonObject.newInstance();
			__used[i] = false;
		}
	}

	@Override
	public synchronized CommonObject get() {
		for (int i = 0; i < __used.length; i++) {
			if (!__used[i]) {
				__used[i] = true;
				return __pool[i];
			}
		}
		// If we got here, then all the Elements are in use. We will
		// increase the number in our pool by @ADD_ELEMENT_POOL (arbitrary value for
		// illustration purposes).
		var oldUsed = __used;
		__used = new boolean[oldUsed.length + CommonConstants.ADDITIONAL_NUMBER_ELEMENTS_POOL];
		System.arraycopy(oldUsed, 0, __used, 0, oldUsed.length);

		var oldPool = __pool;
		__pool = new CommonObject[oldPool.length + CommonConstants.ADDITIONAL_NUMBER_ELEMENTS_POOL];
		System.arraycopy(oldPool, 0, __pool, 0, oldPool.length);

		for (int i = oldPool.length; i < __pool.length; i++) {
			__pool[i] = CommonObject.newInstance();
			__used[i] = false;
		}

		_info("MESSAGE OBJECT POOL", _buildgen("Increased the number of elements by ",
				CommonConstants.ADDITIONAL_NUMBER_ELEMENTS_POOL, " to ", __used.length));

		// and allocate the last old ELement
		__used[oldPool.length - 1] = true;
		return __pool[oldPool.length - 1];
	}

	@Override
	public synchronized void repay(CommonObject element) {
		boolean flagFound = false;
		for (int i = 0; i < __pool.length; i++) {
			if (__pool[i] == element) {
				__used[i] = false;
				// Clear object
				element.clear();
				flagFound = true;
				break;
			}
		}
		if (!flagFound) {
			var error = new NullElementPoolException(
					"Something went wrong, the element is not in use but had to be repaid.");
			_error(error);
			throw error;
		}
	}

	@Override
	public synchronized void cleanup() {
		for (int i = 0; i < __pool.length; i++) {
			__pool[i] = null;
		}
		__used = null;
		__pool = null;
	}

	@Override
	public synchronized int getPoolSize() {
		return (__pool.length == __used.length) ? __pool.length : -1;
	}

}
