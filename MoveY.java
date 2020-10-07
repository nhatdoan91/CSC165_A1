package myGameEngine;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;
import a1.MyGame;

public class MoveY extends AbstractInputAction{ 

	private Camera camera;
	private MyGame g;
	private SceneNode sn;
	public MoveY(MyGame g,Camera c)
	{ 
		this.camera = c;
		this.g=g;
	}
	public MoveY(MyGame g,SceneNode sn) {
		this.sn=sn;
		this.g=g;
	}
	@Override
	public void performAction(float arg0, Event arg1) {
	// TODO Auto-generated method stub
		if(!g.isOnDolphin()) {
			Vector3f v = null;
			v = camera.getFd();
			if(arg1.getValue()<-0.5f)
			{
				v = camera.getFd();
			}
			else if(arg1.getValue()>0.5f)
			{
				v = (Vector3f)v.div(-1.0f);
			}else {
				v=null;
			}
			
			if(v!=null)
			{
				Vector3f p = camera.getPo();
				Vector3f p1 =(Vector3f) Vector3f.createFrom(0.01f*v.x(), 0.01f*v.y(), 0.01f*v.z());
				Vector3f p2 = (Vector3f) p.add((Vector3)p1);
				camera.setPo((Vector3f)Vector3f.createFrom(p2.x(),p2.y(),p2.z()));
			}
			g.checkAddPoint();
		}else {
			if(arg1.getValue()>0.5f)
			{
				g.moveBackward();
			}
			else if(arg1.getValue()<-0.5f) {
				g.moveForward();
			}
		}
		
	}
}