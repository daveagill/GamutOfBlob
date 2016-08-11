package week.of.awesome.framework;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.TimeUtils;

public abstract class StandardGameApp implements ApplicationListener {
	
	private static final float FIXED_TIMESTEP = 1f / 60f;
	private static final long FIXED_TIMESTEP_NANOS = (long)(FIXED_TIMESTEP * 1000000000L);
	
	
	private InputMultiplexer inputMultiplexer;
	private Services services = new Services();

	private GameState currentState;
	
	private long lastFrameTime;
	private long accumulatedTime;
	
	
	@Override
	public void create () {
		// setup input system
		inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(new ExitKeyInputProcessor());
		Gdx.input.setInputProcessor(inputMultiplexer);
		
		// setup graphics system
		services.gfx = new RenderService();
		services.gfxResources = new GraphicsResources();
		
		// setup audio system
		services.jukebox = new JukeboxService();
		services.sfxResources = new SoundResources();
		
		// initialise game
		currentState = setupGameStates();
		currentState.onEnter(services);
		inputMultiplexer.addProcessor(0, currentState.getInputProcessor());
		
		lastFrameTime = TimeUtils.nanoTime();
	}

	@Override
	public void render () {
		long time = TimeUtils.nanoTime();
		accumulatedTime += (time - lastFrameTime);
		lastFrameTime = time;
		
		float frameSimulationTime = 0;
		
		while (accumulatedTime >= FIXED_TIMESTEP_NANOS) {
			GameState nextState = currentState.update(FIXED_TIMESTEP);
			if (nextState != null) {
				currentState.onExit();
				nextState.onEnter(services);
				
				currentState = nextState;
				
				// swap the input processor for the new state's
				InputProcessor inputProcessor = nextState.getInputProcessor();
				inputMultiplexer.removeProcessor(0);
				inputMultiplexer.addProcessor(0, inputProcessor);
			}
			
			accumulatedTime -= FIXED_TIMESTEP_NANOS;
			frameSimulationTime += FIXED_TIMESTEP;
		}
		
		
		services.jukebox.update(frameSimulationTime);
		
		services.gfx.beginFrame();
		currentState.render(frameSimulationTime);
		services.gfx.endFrame();
	}
	


	@Override
	public void resize(int width, int height) {
		services.gfx.resizeViewport(width, height);
	}

	@Override
	public void pause() { }

	@Override
	public void resume() { }
	
	@Override
	public void dispose () {
		currentState.onExit();
		
		services.gfx.dispose();
		services.gfxResources.dispose();
		services.jukebox.dispose();
		services.sfxResources.dispose();
	}
	
	
	protected abstract GameState setupGameStates();
}
