package a1;
import myGameEngine.*;
import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Random;

import com.jogamp.nativewindow.util.Point;

import net.java.games.input.Controller;
import myGameEngine.MoveY;
import myGameEngine.MoveAction;
import myGameEngine.QuitGameAction;
import ray.input.GenericInputManager;
import ray.input.InputManager;
import ray.input.action.Action;
import ray.rage.Engine;
import ray.rage.asset.texture.Texture;
import ray.rage.game.Game;
import ray.rage.game.VariableFrameRateGame;
import ray.rage.rendersystem.RenderSystem;
import ray.rage.rendersystem.RenderWindow;
import ray.rage.rendersystem.Renderable;
import ray.rage.rendersystem.Renderable.DataSource;
import ray.rage.rendersystem.Renderable.Primitive;
import ray.rage.rendersystem.gl4.GL4RenderSystem;
import ray.rage.rendersystem.shader.GpuShaderProgram;
import ray.rage.rendersystem.states.FrontFaceState;
import ray.rage.rendersystem.states.RenderState;
import ray.rage.rendersystem.states.TextureState;
import ray.rage.scene.Camera;
import ray.rage.scene.Camera.Frustum.Projection;
import ray.rage.scene.Entity;
import ray.rage.scene.Light;
import ray.rage.scene.ManualObject;
import ray.rage.scene.ManualObjectSection;
import ray.rage.scene.Node;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneNode;
import ray.rage.scene.controllers.RotationController;
import ray.rage.util.BufferUtil;
import ray.rml.Angle;
import ray.rml.Degreef;
import ray.rml.Matrix3;
import ray.rml.Matrix3f;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class MyGame extends VariableFrameRateGame {

	// to minimize variable allocation in update()
	GL4RenderSystem rs;
	float elapsTime = 0.0f;
	String elapsTimeStr, counterStr, dispStr,displayWin;
	int elapsTimeSec, counter = 0,point = 0,visitedCount=0,numberOfPlanets=4;
	private InputManager im;
	private Action quitGameAction,moveForward,moveBackward,moveRight,moveLeft;
	private Camera camera;
	private SceneNode rootNode,dolphinN,cameraNode,earthN,marsN,jupiterN,posN;
	private boolean isOnDolphin=true,boolWin=false;
	private Entity dolphinE,earthE,marsE,jupiterE;
	Angle myAngle = Degreef.createFrom(1.0f);
	String[] visitedList = new String[numberOfPlanets];
	Vector3f previousDolphinNodeVector;
    public MyGame() {
        super();
		System.out.println("Game Started!!!");
    }

    public static void main(String[] args) {
        Game game = new MyGame();
        try {
            game.startup();
            game.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            game.shutdown();
            game.exit();
        }
    }
	
	@Override
	protected void setupWindow(RenderSystem rs, GraphicsEnvironment ge) {
		rs.createRenderWindow(new DisplayMode(1000, 700, 24, 60), false);
	}

    @Override
    protected void setupCameras(SceneManager sm, RenderWindow rw) {
        rootNode = sm.getRootSceneNode();
        camera = sm.createCamera("MainCamera", Projection.PERSPECTIVE);
        rw.getViewport(0).setCamera(camera);
		
		camera.setRt((Vector3f)Vector3f.createFrom(1.0f, 0.0f, 0.0f));
		camera.setUp((Vector3f)Vector3f.createFrom(0.0f, 1.0f, 0.0f));
		camera.setFd((Vector3f)Vector3f.createFrom(0.0f, 0.0f, -1.0f));
		
		camera.setPo((Vector3f)Vector3f.createFrom(0.0f, 0.0f, 0.0f));

        cameraNode = rootNode.createChildSceneNode(camera.getName() + "Node");
        cameraNode.attachObject(camera);
        //
    }
	
    @Override
    protected void setupScene(Engine eng, SceneManager sm) throws IOException {
    	
    	setupInputs();
    	ManualObject pos = makeCharacter(eng,sm);
    	posN =sm.getRootSceneNode().createChildSceneNode("PosNode"); 
    	
    	ManualObject myXAxis = makeXAxis(eng,sm);
    	SceneNode myXAxisN =sm.getRootSceneNode().createChildSceneNode("XAxisNode");
    	
    	myXAxisN.scale(0.1f,0.1f,0.1f);
    	myXAxisN.attachObject(myXAxis);
    	myXAxis.setPrimitive(Primitive.LINES);
    	
    	posN.scale(.5f,.5f,.5f);
    	posN.attachObject(pos);
    	posN.setLocalPosition(randomFloat(),randomFloat(),randomFloat());
    	pos.setPrimitive(Primitive.TRIANGLES);
    	
    	earthE = sm.createEntity("myEarth", "earth.obj");
    	earthE.setPrimitive(Primitive.TRIANGLES);
    	earthN = sm.getRootSceneNode().createChildSceneNode(earthE.getName() + "Node");
    	earthN.scale(1f,1f,1f);
    	earthN.setLocalPosition(randomFloat(),randomFloat(),randomFloat());
    	earthN.attachObject(earthE);
    	
    	marsE = sm.createEntity("myMars", "sphere.obj");
    	marsE.setPrimitive(Primitive.TRIANGLES);
    	marsN = sm.getRootSceneNode().createChildSceneNode(marsE.getName() + "Node");
    	marsN.scale(1f,1f,1f);
    	marsN.setLocalPosition(randomFloat(),randomFloat(),randomFloat());
    	marsN.attachObject(marsE);  
    	
    	jupiterE = sm.createEntity("myJupiter", "sphere.obj");
    	jupiterE.setPrimitive(Primitive.TRIANGLES);
    	jupiterN = sm.getRootSceneNode().createChildSceneNode(jupiterE.getName() + "Node");
    	jupiterN.scale(1f,1f,1f);
    	jupiterN.setLocalPosition(randomFloat(),randomFloat(),randomFloat());
    	jupiterN.attachObject(jupiterE);
    	
    	texturePlanets(eng, sm);
    	dolphinE = sm.createEntity("myDolphin", "dolphinHighPoly.obj");
        dolphinE.setPrimitive(Primitive.TRIANGLES);
        dolphinN = sm.getRootSceneNode().createChildSceneNode(dolphinE.getName() + "Node");
        dolphinN.attachObject(dolphinE);
        
        dolphinN.setLocalPosition(0f,0f,0f);

       
        sm.getAmbientLight().setIntensity(new Color(.1f, .1f, .1f));
		Light plight = sm.createLight("testLamp1", Light.Type.POINT);
		plight.setAmbient(new Color(.43f, .43f, .43f));
        plight.setDiffuse(new Color(.97f, .97f, .97f));
		plight.setSpecular(new Color(1.0f, 1.0f, 1.0f));
        plight.setRange(25f);
		SceneNode plightNode = sm.getRootSceneNode().createChildSceneNode("plightNode");
        plightNode.attachObject(plight);
          
        //cameraNode = dolphinN.createChildSceneNode("cameraChildNode");
        cameraNode.setLocalPosition(dolphinN.getLocalPosition().x(),dolphinN.getLocalPosition().y()+0.5f,dolphinN.getLocalPosition().z()-1f);
        
        camera.setMode('c');
        isOnDolphin = false;
        camera.setFd((Vector3f) Vector3f.createFrom(1f,0f,1f));
        //camera.setPo((Vector3f) Vector3f.createFrom(dolphinN.getLocalPosition().x()-1f,dolphinN.getLocalPosition().y()+0.5f,dolphinN.getLocalPosition().z()-1f));
        
        RotationController rc = new RotationController(Vector3f.createUnitVectorY(), .02f);
        rc.addNode(posN);
        sm.addController(rc);
    }

    @Override
    protected void update(Engine engine) {
		// build and set HUD
		rs = (GL4RenderSystem) engine.getRenderSystem();
		elapsTime += engine.getElapsedTimeMillis();
		elapsTimeSec = Math.round(elapsTime/1000.0f);
		elapsTimeStr = Integer.toString(elapsTimeSec);
		counterStr = Integer.toString(counter);
		dispStr = "Time = " + elapsTimeStr + "   Score of visiting planets = " + point;
		
		if(boolWin)
		{
			rs.setHUD(displayWin, 15, 15);
		}
		else {
			rs.setHUD(dispStr, 15, 15);
		}
		im.update(elapsTime);
	}
    protected void setupInputs() {
    	im = new GenericInputManager();
       	quitGameAction = (Action) new QuitGameAction(this);
       	moveForward = (Action) new MoveAction(this,'f');
       	moveBackward = (Action) new MoveAction(this,'b');
       	moveRight = (Action) new MoveAction(this,'r');
       	moveLeft = (Action) new MoveAction(this,'l');
    	if(im.getFirstGamepadName()!=null)
    	{
        	String gamePadName = im.getFirstGamepadName();
        	System.out.println(gamePadName);
        	im.associateAction(gamePadName,net.java.games.input.Component.Identifier.Button._12,quitGameAction,InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
        	im.associateAction(gamePadName,net.java.games.input.Component.Identifier.Axis.Z,new RotateGamePadZAxis(this), InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        	im.associateAction(gamePadName,net.java.games.input.Component.Identifier.Axis.RZ,new RotateGamePadZRodation(this), InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        	
        	if(isOnDolphin()) {
        		im.associateAction(gamePadName,net.java.games.input.Component.Identifier.Axis.Y,new MoveY(this,this.camera), InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        		im.associateAction(gamePadName,net.java.games.input.Component.Identifier.Axis.X,new MoveX(this,this.camera),InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);	
        	}else
        	{
        		im.associateAction(gamePadName,net.java.games.input.Component.Identifier.Axis.Y,new MoveY(this,this.dolphinN), InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        		im.associateAction(gamePadName,net.java.games.input.Component.Identifier.Axis.X,new MoveX(this,this.dolphinN),InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        	}
    	}
    	String keyboardName = im.getKeyboardName();
    	im.associateAction(keyboardName,net.java.games.input.Component.Identifier.Key.ESCAPE,quitGameAction,InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    	im.associateAction(keyboardName,net.java.games.input.Component.Identifier.Key.W,moveForward,InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	im.associateAction(keyboardName,net.java.games.input.Component.Identifier.Key.S,moveBackward,InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	im.associateAction(keyboardName,net.java.games.input.Component.Identifier.Key.D,moveRight,InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	im.associateAction(keyboardName,net.java.games.input.Component.Identifier.Key.A,moveLeft,InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	im.associateAction(keyboardName,net.java.games.input.Component.Identifier.Key.LEFT,new RotateKeyboard(this,'l'),InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	im.associateAction(keyboardName,net.java.games.input.Component.Identifier.Key.RIGHT,new RotateKeyboard(this,'r'),InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	im.associateAction(keyboardName,net.java.games.input.Component.Identifier.Key.UP,new RotateKeyboard(this,'u'),InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	im.associateAction(keyboardName,net.java.games.input.Component.Identifier.Key.DOWN,new RotateKeyboard(this,'d'),InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    	im.associateAction(keyboardName,net.java.games.input.Component.Identifier.Key.SPACE,(Action)new JumpOnOffDolphin(this),InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
    	     
    }
    protected void texturePlanets(Engine eng, SceneManager sm)throws IOException {
    	
    	Texture tex1 =eng.getTextureManager().getAssetByPath("blue.jpeg");
    	TextureState texState1 = (TextureState)sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
    	texState1.setTexture(tex1);
    	FrontFaceState faceState1 = (FrontFaceState) sm.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
    	jupiterE.setRenderState(texState1);
    	jupiterE.setRenderState(faceState1);
    	
    	Texture tex =eng.getTextureManager().getAssetByPath("moon.jpeg");
    	TextureState texState = (TextureState)sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
    	texState.setTexture(tex);
    	FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
    	
    	// marsE.setDataSource(DataSource.INDEX_BUFFER);
    	marsE.setRenderState(texState);
    	marsE.setRenderState(faceState);
    }
    protected ManualObject makeXAxis(Engine eng, SceneManager sm)throws IOException {
    	
    	ManualObject myXAxis = sm.createManualObject("XAxis");
    	ManualObjectSection myCharacterSection = myXAxis.createManualSection("XAxisSection");
    	myXAxis.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
		float[] vertices = new float[] {0f,0f,0f,1000f,0f,0f,
										0f,0f,0f,0f,1000f,0f,
										0f,0f,0f,0f,0f,1000f
										};
		float[] texcoords = new float[] {0.0f,0.0f,1.0f,0.0f,
										0.0f,0.0f,1.0f,0.0f,
										0.0f,0.0f,1.0f,0.0f};
		float[] normals = new float[] {0f,0f,1f,
										0f,1f,0f,
										1f,0f,0f};
		int[] indices = new int[] {0,1,2,3,4,5};
		FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
		FloatBuffer texBuf  = BufferUtil.directFloatBuffer(texcoords);
		FloatBuffer normBuf = BufferUtil.directFloatBuffer(normals);
		IntBuffer indexBuf  = BufferUtil.directIntBuffer(indices);
		
		myCharacterSection.setVertexBuffer(vertBuf);
		myCharacterSection.setTextureCoordsBuffer(texBuf);
		myCharacterSection.setNormalsBuffer(normBuf);
		myCharacterSection.setIndexBuffer(indexBuf);
		Texture tex =eng.getTextureManager().getAssetByPath("default.png");
    	TextureState texState = (TextureState)sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
    	texState.setTexture(tex);
    	FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
    	
    	myXAxis.setDataSource(DataSource.INDEX_BUFFER);
    	myXAxis.setRenderState(texState);
    	myXAxis.setRenderState(faceState);
    	return myXAxis;
    	
    }
    protected ManualObject makeCharacter(Engine eng, SceneManager sm)throws IOException {
    	ManualObject myCharacter = sm.createManualObject("Poseidon");
    	ManualObjectSection myCharacterSection = myCharacter.createManualSection("PoseidonSection");
		myCharacter.setGpuShaderProgram(sm.getRenderSystem().getGpuShaderProgram(GpuShaderProgram.Type.RENDERING));
    	
		float[] vertices = new float[]
				{1.0f,1.0f,1.0f,-1.0f,1.0f,1.0f,1.0f,1.0f,-1.0f, //front left
				 -1.0f,1.0f,-1.0f,1.0f,1.0f,-1.0f,-1.0f,1.0f,1.0f, // front right
				 -1.0f,1.0f,1.0f,-1.0f,-1.0f,1.0f,-1.0f,1.0f,-1.0f, // right left
				 -1.0f,-1.0f,-1.0f,-1.0f,1.0f,-1.0f,-1.0f,-1.0f,1.0f, // right right
				 -1.0f,-1.0f,1.0f,1.0f,-1.0f,1.0f,-1.0f,-1.0f,-1.0f, // back left
				 1.0f,-1.0f,-1.0f,-1.0f,-1.0f,-1.0f,1.0f,-1.0f,1.0f, // back right
				 1.0f,-1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,-1.0f,-1.0f, // left left
				 1.0f,1.0f,-1.0f,1.0f,-1.0f,-1.0f,1.0f,1.0f,1.0f, // left right
				 1.0f,1.0f,1.0f,-1.0f,-1.0f,-1.0f,-1.0f,1.0f,1.0f, // top left
				 -1.0f,-1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,-1.0f,1.0f, // top right
				 1.0f,1.0f,-1.0f,-1.0f,-1.0f,-1.0f,-1.0f,1.0f,-1.0f, // bottom left
				 -1.0f,-1.0f,-1.0f,1.0f,1.0f,-1.0f,1.0f,-1.0f,-1.0f, // bottom right
				};
		float[] texcoords = new float[] {
				0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,
				1.0f,1.0f,1.0f,0.0f,0.0f,1.0f,
				0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,
				1.0f,1.0f,1.0f,0.0f,0.0f,1.0f,
				0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,
				1.0f,1.0f,1.0f,0.0f,0.0f,1.0f,
				0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,
				1.0f,1.0f,1.0f,0.0f,0.0f,1.0f,
				0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,
				1.0f,1.0f,1.0f,0.0f,0.0f,1.0f,
				0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,
				1.0f,1.0f,1.0f,0.0f,0.0f,1.0f
		};
		float[] normals = new float[]
				{
				0.0f,1.0f,0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,0.0f,
				0.0f,1.0f,0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,0.0f,	
				0.0f,0.0f,-1.0f,0.0f,0.0f,-1.0f,0.0f,0.0f,-1.0f,
				0.0f,0.0f,-1.0f,0.0f,0.0f,-1.0f,0.0f,0.0f,-1.0f,
				0.0f,-1.0f,0.0f,0.0f,-1.0f,0.0f,0.0f,-1.0f,0.0f,
				0.0f,-1.0f,0.0f,0.0f,-1.0f,0.0f,0.0f,-1.0f,0.0f,
				1.0f,0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,0.0f,0.0f,
				1.0f,0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,0.0f,0.0f,
				0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,
				0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,
				0.0f,0.0f,-1.0f,0.0f,0.0f,-1.0f,0.0f,0.0f,-1.0f,
				0.0f,0.0f,-1.0f,0.0f,0.0f,-1.0f,0.0f,0.0f,-1.0f
				};
		int[] indices = new int[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31};
		
		FloatBuffer vertBuf = BufferUtil.directFloatBuffer(vertices);
		FloatBuffer texBuf  = BufferUtil.directFloatBuffer(texcoords);
		FloatBuffer normBuf = BufferUtil.directFloatBuffer(normals);
		IntBuffer indexBuf  = BufferUtil.directIntBuffer(indices);
		
		myCharacterSection.setVertexBuffer(vertBuf);
		myCharacterSection.setTextureCoordsBuffer(texBuf);
		myCharacterSection.setNormalsBuffer(normBuf);
		myCharacterSection.setIndexBuffer(indexBuf);
		Texture tex =eng.getTextureManager().getAssetByPath("hexagons.jpeg");
    	TextureState texState = (TextureState)sm.getRenderSystem().createRenderState(RenderState.Type.TEXTURE);
    	texState.setTexture(tex);
    	FrontFaceState faceState = (FrontFaceState) sm.getRenderSystem().createRenderState(RenderState.Type.FRONT_FACE);
    	
    	myCharacter.setDataSource(DataSource.INDEX_BUFFER);
    	myCharacter.setRenderState(texState);
    	myCharacter.setRenderState(faceState);
    	return myCharacter;
    }
    protected float randomFloat() {
    	Random rd= new Random();
    	return (float)0f+ rd.nextFloat()*10f;
    }
    public void cameraSetPos(Vector3f v) {
    	if( v!=null)
		{
    		Vector3f p = camera.getPo();
    		Vector3f p1 =(Vector3f) Vector3f.createFrom(0.01f*v.x(), 0.01f*v.y(), 0.01f*v.z());
    		Vector3f p2 = (Vector3f) p.add((Vector3)p1);
    		camera.setPo((Vector3f)Vector3f.createFrom(p2.x(),p2.y(),p2.z()));
		}
    }
    public void moveForward() {
    	if(!isOnDolphin)
    	{
    		Vector3f v =  camera.getFd();
    		cameraSetPos(v);
    	}
    	else 
    	{
    		dolphinN.moveForward(0.01f);
    	}
    }
    public void moveBackward() {
    	if(!isOnDolphin)
    	{
    		Vector3f v =  (Vector3f) camera.getFd().div(-1.0f);
    		cameraSetPos(v);
    	}
    	else 
    	{
    		dolphinN.moveBackward(0.01f);
    	}
    }
    public void moveRight() {
    	if(!isOnDolphin)
    	{
    		Vector3f v =  camera.getRt();
    		cameraSetPos(v);
    	}
    	else 
    	{
    		dolphinN.moveLeft(0.01f);
    	}
    }
    public void moveLeft() {
    	if(!isOnDolphin)
    	{
    		Vector3f v =  (Vector3f) camera.getRt().div(-1.0f);
    		cameraSetPos(v);
    	}
    	else 
    	{
    		dolphinN.moveRight(0.01f);
    	}
    }
    public void moveActionGamePad() {
    	
    }
    public void moveAction(char dir) {
    	if(!isOnDolphin)
    	{
    		Vector3f v = null;
    		if(dir=='f')
    		{
    			v = camera.getFd();
    		}else if(dir=='b')
    		{
    			v = camera.getFd();
    			v=(Vector3f) v.div(-1.0f);
    		}else if(dir=='l')
    		{
    			v = camera.getRt();
    			v=(Vector3f) v.div(-1.0f);
    		}else if(dir=='r')
    		{
    			v = camera.getRt();
    		}
    		if( v!=null)
    		{
    			Vector3f p = camera.getPo();
    			Vector3f p1 =(Vector3f) Vector3f.createFrom(0.01f*v.x(), 0.01f*v.y(), 0.01f*v.z());
    			Vector3f p2 = (Vector3f) p.add((Vector3)p1);
    			camera.setPo((Vector3f)Vector3f.createFrom(p2.x(),p2.y(),p2.z()));
    		}
    		checkAddPoint();
    	}else
    	{
    		
    		if(dir=='f')
    		{
    			dolphinN.moveForward(0.01f);
    		}else if(dir=='b')
    		{
    			dolphinN.moveBackward(0.01f);
    		}else if(dir=='r')
    		{
    			dolphinN.moveLeft(0.01f);
    		}else if(dir=='l')
    		{
    			dolphinN.moveRight(0.01f);
    		}
    	}
    }
    public boolean isOnDolphin() {
    	return isOnDolphin;
    }
    public void jumpDolphin() {
    	if(isOnDolphin)
    	{
    		camera.setMode('c');
    		dolphinN.detachChild(cameraNode);
    		isOnDolphin=false;
    		//cameraNode.setLocalPosition(dolphinN.getLocalPosition().x()-1f,dolphinN.getLocalPosition().y()+0.5f,dolphinN.getLocalPosition().z()-1f);
    		camera.setPo((Vector3f) Vector3f.createFrom(dolphinN.getLocalPosition().x()-1f,dolphinN.getLocalPosition().y()+0.5f,dolphinN.getLocalPosition().z()-1f));
    	}else
    	{
    		camera.setMode('n');
    		isOnDolphin=true;
    		//cameraNode.setLocalPosition(dolphinN.getLocalPosition().x()-1f,dolphinN.getLocalPosition().y()+0.5f,dolphinN.getLocalPosition().z()-1f);
    		dolphinN.attachChild(cameraNode);
    	}
    }
    private void addToList(String in) {
    			visitedList[visitedCount]=in;
    			visitedCount++;
    }
    private boolean isVisited(SceneNode n, Camera c,String s) {
    	for(int i=0; i< visitedList.length; i++)
    	{
    			if(s==visitedList[i])
        			return true; 
    	}
    	
    	return false;
    }
    private boolean isClose(SceneNode n, Camera c) {
    	if(!isVisited(n,c,n.getName())) {
    		float distance = (float) Math.sqrt(Math.pow(n.getLocalPosition().x()-c.getPo().x(),2)+Math.pow(n.getLocalPosition().y()- c.getPo().y(),2)+Math.pow(n.getLocalPosition().z()- c.getPo().z(),2));
    		if(distance <3f)
    		{
            	if(n.getName()!=posN.getName())
    			addToList(n.getName());
            	return true;
    		}

    	}
    	
    	return false;
    }
    public void checkAddPoint() {
    	if(isClose(earthN, camera)||isClose(marsN, camera)||isClose(jupiterN, camera)) {
    		point++;
    	}
    }
    public boolean checkWin() {
    	if(point==(numberOfPlanets-1))
    	{
    		if(isClose(posN,camera))
        	{
    			displayWin = "You Won The Game";
    			System.out.println("Test You Won");
    			boolWin = true;
        		return true;
        	}
    	}
    	return false;
    }
    public void rotateDown() {
    	if(isOnDolphin())
    	{
    		Vector3 nodeSide = Vector3f.createFrom(camera.getRt().x()*-1.0f,camera.getRt().y()*-1.0f,camera.getRt().z()*-1.0f);
    		Matrix3 matRot = Matrix3f.createRotationFrom(Degreef.createFrom(-1.0f), nodeSide);
    		dolphinN.setLocalRotation(matRot.mult(dolphinN.getWorldRotation()));
    	}
    	else {
    		camera.setFd((Vector3f) camera.getFd().rotate(myAngle, Vector3f.createFrom(camera.getRt().x(),camera.getRt().y(),camera.getRt().z())));
    		camera.setRt((Vector3f) camera.getRt().rotate(myAngle, Vector3f.createFrom(camera.getRt().x(),camera.getRt().y(),camera.getRt().z())));
    		camera.setUp((Vector3f) camera.getUp().rotate(myAngle, Vector3f.createFrom(camera.getRt().x(),camera.getRt().y(),camera.getRt().z())));

    	}
    }
    public void rotateUp() {
    	if(isOnDolphin())
    	{
    		Vector3 nodeSide = Vector3f.createFrom(camera.getRt().x(),camera.getRt().y(),camera.getRt().z());
    		Matrix3 matRot = Matrix3f.createRotationFrom(Degreef.createFrom(-1.0f), nodeSide);
    		dolphinN.setLocalRotation(matRot.mult(dolphinN.getWorldRotation()));
    	}
    	else {
    		camera.setFd((Vector3f) camera.getFd().rotate(myAngle, Vector3f.createFrom(camera.getRt().x()*-1.0f,camera.getRt().y()*-1.0f,camera.getRt().z()*-1.0f)));
    		camera.setRt((Vector3f) camera.getRt().rotate(myAngle, Vector3f.createFrom(camera.getRt().x()*-1.0f,camera.getRt().y()*-1.0f,camera.getRt().z()*-1.0f)));
    		camera.setUp((Vector3f) camera.getUp().rotate(myAngle, Vector3f.createFrom(camera.getRt().x()*-1.0f,camera.getRt().y()*-1.0f,camera.getRt().z()*-1.0f)));
    	}
    }
    public void rotateRight() {
    	if(isOnDolphin())
    	{
    		Vector3 worldUp = Vector3f.createFrom(0.0f, 1.0f, 0.0f);
    		Matrix3 matRot = Matrix3f.createRotationFrom(Degreef.createFrom(-1.0f), worldUp);
    		dolphinN.setLocalRotation(matRot.mult(dolphinN.getWorldRotation()));
    	}
    	else {
    		camera.setFd((Vector3f) camera.getFd().rotate(myAngle, Vector3f.createFrom(0.0f, 1.0f, 0.0f)));
    		camera.setRt((Vector3f) camera.getRt().rotate(myAngle, Vector3f.createFrom(0.0f, 1.0f, 0.0f)));
    		camera.setUp((Vector3f) camera.getUp().rotate(myAngle, Vector3f.createFrom(0.0f, 1.0f, 0.0f)));
    	}
    }
    public void rotateLeft() {
    	if(isOnDolphin())
    	{
    		Vector3 worldUp = Vector3f.createFrom(0.0f, -1.0f, 0.0f);
    		Matrix3 matRot = Matrix3f.createRotationFrom(Degreef.createFrom(-1.0f), worldUp);
    		dolphinN.setLocalRotation(matRot.mult(dolphinN.getWorldRotation()));
    	}
    	else {
    		camera.setFd((Vector3f) camera.getFd().rotate(myAngle, Vector3f.createFrom(0.0f, -1.0f, 0.0f)));
    		camera.setRt((Vector3f) camera.getRt().rotate(myAngle, Vector3f.createFrom(0.0f, -1.0f, 0.0f)));
    		camera.setUp((Vector3f) camera.getUp().rotate(myAngle, Vector3f.createFrom(0.0f, 1.0f, 0.0f)));
    	}
    }
    public void rotateKeyboard(char c) {
    	if(c=='u')
    	{
    		rotateUp();
    	}else if(c=='d')
    	{
    		rotateDown();
    	}else if(c=='r') {
    		rotateRight();
    	}else if(c=='l') {
    		rotateLeft();
    	}
    }
    @Override
    public void keyPressed(KeyEvent e) {
        Entity dolphin = getEngine().getSceneManager().getEntity("myDolphin");
        switch (e.getKeyCode()) {
            case KeyEvent.VK_L:
                dolphin.setPrimitive(Primitive.LINES);
                break;
            case KeyEvent.VK_T:
                dolphin.setPrimitive(Primitive.TRIANGLES);
                break;
            case KeyEvent.VK_P:
                dolphin.setPrimitive(Primitive.POINTS);
                break;
			case KeyEvent.VK_C:
				counter++;
				break;
        }
        super.keyPressed(e);
    }
}
