package myGameEngine;
import a1.MyGame;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;

public class JumpOnOffDolphin extends AbstractInputAction{
	
	private MyGame g;
	
	public JumpOnOffDolphin(MyGame g)
	{
		this.g=g;
	}

	@Override
	public void performAction(float arg0, Event arg1) {
		g.jumpDolphin();
	}		
}
