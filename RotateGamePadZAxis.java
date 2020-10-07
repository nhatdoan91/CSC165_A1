package myGameEngine;

import a1.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class RotateGamePadZAxis extends AbstractInputAction{

	private MyGame g;
	public RotateGamePadZAxis(MyGame g) {
		this.g=g;
	}
	@Override
	public void performAction(float arg0, Event arg1) {
		// TODO Auto-generated method stub
		if(arg1.getValue()<-0.5f)
		{
			g.rotateLeft();
		}else if(arg1.getValue()>0.5f) {
			g.rotateRight();
		}
	}

}