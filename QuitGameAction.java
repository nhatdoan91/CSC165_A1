package myGameEngine;
import a1.MyGame;
import ray.input.action.AbstractInputAction;
import ray.rage.game.*;
import net.java.games.input.Event;

public class QuitGameAction extends AbstractInputAction{

	private MyGame game;
	public QuitGameAction(MyGame g)
	{ 
		game = g;
	}
	@Override
	public void performAction(float arg0, Event arg1) {
		// TODO Auto-generated method stub
		System.out.println("shutdown requested");
		game.setState(Game.State.STOPPING);
	} 
}
