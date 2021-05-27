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
package com.tenio.core.network.entities.packet.policy;

import com.tenio.core.exceptions.PacketQueuePolicyViolationException;
import com.tenio.core.network.defines.ResponsePriority;
import com.tenio.core.network.entities.packet.Packet;
import com.tenio.core.network.entities.packet.PacketQueue;

/**
 * @author kong
 */
// TODO: Add description
public final class DefaultPacketQueuePolicy implements PacketQueuePolicy {

	private static final float THREE_QUARTERS_FULL = 75.0f;
	private static final float NINETY_PERCENT_FULL = 90.0f;

	public DefaultPacketQueuePolicy() {

	}

	@Override
	public void applyPolicy(PacketQueue packetQueue, Packet packet) {
		float percentageUsed = packetQueue.getPercentageUsed();

		if (percentageUsed >= THREE_QUARTERS_FULL && percentageUsed < NINETY_PERCENT_FULL) {
			if (packet.getPriority().getValue() < ResponsePriority.NORMAL.getValue()) {
				throw new PacketQueuePolicyViolationException(packet, percentageUsed);
			}
		} else if (percentageUsed >= NINETY_PERCENT_FULL) {
			if (packet.getPriority().getValue() < ResponsePriority.GUARANTEED.getValue()) {
				throw new PacketQueuePolicyViolationException(packet, percentageUsed);
			}
		}

	}

}