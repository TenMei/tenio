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
package com.tenio.examples.example5;

import com.tenio.engine.ecs.ContextInfo;
import com.tenio.engine.heartbeat.ecs.ECSHeartBeat;
import com.tenio.examples.example5.components.GameComponents;
import com.tenio.examples.example5.context.GameContext;
import com.tenio.examples.example5.systems.InitializeSystem;
import com.tenio.examples.example5.systems.MoveSystem;
import com.tenio.examples.example5.systems.RenderSystem;
import com.tenio.examples.example5.systems.TeardownSystem;

/**
 * @author kong
 */
public class ECS extends ECSHeartBeat {
	
	public ECS(int cx, int cy) {
		super(cx, cy);
		
		ContextInfo info = new ContextInfo("Game", GameComponents.getComponentNames(), GameComponents.getComponentTypes(), GameComponents.getNumberComponents());
		GameContext context = new GameContext(info);
		
		addSystem(new InitializeSystem(context));
		addSystem(new MoveSystem(context));
		addSystem(new RenderSystem(context));
		addSystem(new TeardownSystem(context));
	}
	
}