package myGameEngine;

import a1.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

public class RotateKeyboard extends AbstractInputAction{

	private MyGame g;
	private char c;
	public RotateKeyboard(MyGame g, char c) {
		this.g=g;
		this.c=c;
	}
	@Override
	public void performAction(float arg0, Event arg1) {
		// TODO Auto-generated method stub
		g.rotateKeyboard(c);
	}

}
