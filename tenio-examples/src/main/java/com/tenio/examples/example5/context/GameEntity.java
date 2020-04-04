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
package com.tenio.examples.example5.context;

import com.tenio.engine.ecs.ContextInfo;
import com.tenio.engine.ecs.Entity;
import com.tenio.engine.ecs.pool.ComponentPool;
import com.tenio.examples.example5.components.GameComponents;
import com.tenio.examples.example5.components.Position;

/**
 * @author kong
 */
public class GameEntity extends Entity {

	private ComponentPool[] __componentPools;

	public GameEntity() {
		super();
	}
	
	public void setInfo(ContextInfo contextInfo) {
		super.setInfo(contextInfo);
		__componentPools = new ComponentPool[getContextInfo().getNumberComponents()];
		for (int i = 0; i < getContextInfo().getNumberComponents(); i++) {
			if (getContextInfo().getComponentTypes()[i] != null) {
				__componentPools[i] = new ComponentPool(getContextInfo().getComponentTypes()[i]);
			}
		}
	}

	public boolean isAnimation() {
		return hasComponent(GameComponents.ANIMATION);
	}

	public GameEntity setAnimation(boolean value) {
		if (value != hasComponent(GameComponents.ANIMATION)) {
			if (value) {
				addComponent(GameComponents.ANIMATION, __componentPools[GameComponents.ANIMATION].get());
			} else {
				__componentPools[GameComponents.ANIMATION].repay(getComponent(GameComponents.ANIMATION));
				removeComponent(GameComponents.ANIMATION);
			}
		}
		return this;
	}

	public boolean isMotion() {
		return hasComponent(GameComponents.MOTION);
	}

	public GameEntity setMotion(boolean value) {
		if (value != hasComponent(GameComponents.MOTION)) {
			if (value) {
				addComponent(GameComponents.MOTION, __componentPools[GameComponents.MOTION].get());
			} else {
				__componentPools[GameComponents.MOTION].repay(getComponent(GameComponents.MOTION));
				removeComponent(GameComponents.MOTION);
			}
		}
		return this;
	}

	public boolean hasPosition() {
		return hasComponent(GameComponents.POSITION);
	}

	public GameEntity addPosition(float x, float y) {
		var component = (Position) __componentPools[GameComponents.POSITION].get();
		component.x = x;
		component.y = y;
		addComponent(GameComponents.POSITION, component);
		return this;
	}

	public GameEntity replacePosition(float x, float y) {
		var component = (Position) __componentPools[GameComponents.POSITION].get();
		component.x = x;
		component.y = y;
		replaceComponent(GameComponents.POSITION, component);
		return this;
	}

	public GameEntity removePosition() {
		__componentPools[GameComponents.POSITION].repay(getComponent(GameComponents.POSITION));
		removeComponent(GameComponents.POSITION);
		return this;
	}

}