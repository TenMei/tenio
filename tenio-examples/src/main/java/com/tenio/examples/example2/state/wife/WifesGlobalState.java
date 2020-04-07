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
package com.tenio.examples.example2.state.wife;

import com.tenio.engine.fsm.entities.State;
import com.tenio.engine.fsm.entities.Telegram;
import com.tenio.examples.example2.constants.MessageType;
import com.tenio.examples.example2.entities.Wife;
import com.tenio.utils.MathUtility;

/**
 * This is a global state, which means the miner's wife will check it every
 * time.
 * 
 * @author kong
 *
 */
public final class WifesGlobalState extends State<Wife> {

	private static volatile WifesGlobalState __instance;

	private WifesGlobalState() {
	} // prevent creation manually

	// preventing Singleton object instantiation from outside
	// creates multiple instance if two thread access this method simultaneously
	public static WifesGlobalState getInstance() {
		if (__instance == null) {
			__instance = new WifesGlobalState();
		}
		return __instance;
	}

	@Override
	public void enter(Wife wife) {

	}

	@Override
	public void execute(Wife wife) {
		// 1 in 10 chance of needing the bathroom(provided she is not already
		// in the bathroom)
		if ((MathUtility.randFloat() < 0.1f) && !wife.getFSM().isInState(VisitBathroom.getInstance())) {
			wife.getFSM().changeState(VisitBathroom.getInstance());
		}
	}

	@Override
	public void exit(Wife wife) {

	}

	@Override
	public boolean onMessage(Wife wife, Telegram msg) {

		if (msg.getType() == MessageType.HI_HONEY_IM_HOME.get()) {
			System.out.println("\nMessage handled by " + wife.getName() + " at time: " + System.currentTimeMillis());

			wife.setMood(wife.getName() + ": Hi honey. Let me make you some of mah fine country stew");
			System.out.println("\n" + wife.getMood());

			wife.getFSM().changeState(CookStew.getInstance());

			return true;

		}

		return false;
	}

}
