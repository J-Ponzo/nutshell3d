package fr.jponzo.gamagora.nutshell3d.runtime;

import fr.jponzo.gamagora.nutshell3d.input.InputManager;
import fr.jponzo.gamagora.nutshell3d.rendering.RenderingSystem;
import fr.jponzo.gamagora.nutshell3d.scene.SceneManager;

public class RuntimeSystem {
	private static class SingletonWrapper {
		private final static RuntimeSystem instance = new RuntimeSystem();
	}

	public static RuntimeSystem getInstance() {
		return SingletonWrapper.instance;
	}

	private int TARGET_FPS = 120;
	private long deltaTime = 0;
	private int targetFrameTime = 1000 / TARGET_FPS;

	private volatile boolean isStarted = false;
	private volatile boolean isRunning = false;
	private Thread thread;
	private long passNumber;

	public void start() {		
		if (!isStarted) {
			passNumber = 0;
			isRunning = false;
			isStarted = true;
			thread = new Thread(
					new Runnable() {

						@Override
						public void run() {

							initPass();
							while (isStarted) {
								if (isRunning) {
									try {
										mainPass();
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}
					}
					);
			thread.start();
		}
	}

	public void stop() {
		if (isStarted) {
			isRunning = false;
			isStarted = false;
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			passNumber = 0;
		}
	}

	public void play() {
		if (isStarted) {
			isRunning = true;
		}
	}

	public void pause() {
		if (isStarted) {
			isRunning = false;
		}
	}

	private void initPass(){
		SceneManager.getInstance().initPass();
	}
	
	private void mainPass() throws InterruptedException {
		long currentTime = System.currentTimeMillis();
		InputManager.getInstance().onPreUpdatePass();
		
		SceneManager.getInstance().computeWorldMatricesPass(deltaTime);
		SceneManager.getInstance().updatePass(deltaTime);
		
		InputManager.getInstance().onPostUpdatePass();
		RenderingSystem.getInstance().renderPass(SceneManager.getInstance().getRoot());
		deltaTime = System.currentTimeMillis() - currentTime;
		if (deltaTime < targetFrameTime) {
			Thread.sleep(targetFrameTime - deltaTime);
			deltaTime = targetFrameTime;
		}
	}

	public boolean isStarted() {
		return isStarted;
	}

	public boolean isRunning() {
		return isRunning;
	}
}
