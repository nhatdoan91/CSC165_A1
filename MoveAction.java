package myGameEngine;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;
import a1.MyGame;

public class MoveAction extends AbstractInputAction{ 

	//private Camera camera;
	private MyGame g;
	private char dir;
	public MoveAction(MyGame g, char dir)
	{ 
		this.dir=dir;
		this.g = g;
	}
	@Override
	public void performAction(float arg0, Event arg1) {
	// TODO Auto-generated method stub
		/*if(arg1.getComponent() == net.java.games.input.Component.Identifier.Key.W)
		{
			g.moveForward();
		}else if(arg1.getComponent() == net.java.games.input.Component.Identifier.Key.S)
		{
			g.moveBackward();
		}
		else if(arg1.getComponent() == net.java.games.input.Component.Identifier.Key.A)
		{
			g.moveLeft();
		}else if(arg1.getComponent() == net.java.games.input.Component.Identifier.Key.D)
		{
			g.moveRight();
		}*/
		g.moveAction(dir);
		
	}
}
