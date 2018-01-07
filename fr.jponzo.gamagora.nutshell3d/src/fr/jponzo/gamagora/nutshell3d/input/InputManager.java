package fr.jponzo.gamagora.nutshell3d.input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import fr.jponzo.gamagora.nutshell3d.rendering.RenderingSystem;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

public class InputManager {
	private static class SingletonWrapper {
		private final static InputManager instance = new InputManager();
	}

	public static InputManager getInstance() {
		return SingletonWrapper.instance;
	}

	private Map<KeyCode, Boolean> keyMap = Collections.synchronizedMap(new HashMap<KeyCode, Boolean>());
	private Map<KeyCode, Boolean> keyUpMap = Collections.synchronizedMap(new HashMap<KeyCode, Boolean>());
	private Map<KeyCode, Boolean> keyDownMap = Collections.synchronizedMap(new HashMap<KeyCode, Boolean>());
	private Map<KeyCode, Boolean> pendingKeyUpMap = Collections.synchronizedMap(new HashMap<KeyCode, Boolean>());
	private Map<KeyCode, Boolean> pendingKeyDownMap = Collections.synchronizedMap(new HashMap<KeyCode, Boolean>());
	private boolean isUpdating = false;

	public InputManager() {
		RenderingSystem.getInstance().getGlcanvas().addKeyListener(new KeyAdapter() {	
			@Override
			public void keyReleased(KeyEvent e) {
				KeyCode key = getKeyCodeFromAWTKeyCode(e.getKeyCode());
				keyMap.put(key, false);
				if (!isUpdating) {
					keyUpMap.put(key, true);
				} else {
					pendingKeyUpMap.put(key, true);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				KeyCode key = getKeyCodeFromAWTKeyCode(e.getKeyCode());
				keyMap.put(key, true);
				if (!isUpdating) {
					keyDownMap.put(key, true);
				} else {
					pendingKeyDownMap.put(key, true);
				}
			}
		});

		RenderingSystem.getInstance().getGlcanvas().addMouseListener(new MouseAdapter() {	
			@Override
			public void mouseReleased(MouseEvent e) {
				KeyCode key = getKeyCodeFromAWTMouseButtonNumber(e.getButton());
				keyMap.put(key, false);
				if (!isUpdating) {
					keyUpMap.put(key, true);
				} else {
					pendingKeyUpMap.put(key, true);
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				KeyCode key = getKeyCodeFromAWTMouseButtonNumber(e.getButton());
				keyMap.put(key, true);
				if (!isUpdating) {
					keyDownMap.put(key, true);
				} else {
					pendingKeyDownMap.put(key, true);
				}
			}
		});

		synchronized (keyMap) {
			synchronized (keyUpMap) {
				synchronized (keyDownMap) {
					for (KeyCode keyCode : KeyCode.values()) {
						keyMap.put(keyCode, false);
						keyUpMap.put(keyCode, false);
						keyDownMap.put(keyCode, false);
					}
				}
			}
		}

	}

	public boolean getKeyUp(KeyCode keyCode) {
		if (keyUpMap == null
				|| keyUpMap.get(keyCode) == null) {
			return false;
		}
		boolean value = keyUpMap.get(keyCode);
		keyUpMap.put(keyCode, false);
		return value;
	}

	public boolean getKeyDown(KeyCode keyCode) {
		if (keyDownMap == null
				|| keyDownMap.get(keyCode) == null) {
			return false;
		}
		boolean value = keyDownMap.get(keyCode);
		keyDownMap.put(keyCode, false);
		return value;
	}

	public boolean getKey(KeyCode keyCode) {
		if (keyMap == null
				|| keyMap.get(keyCode) == null) {
			return false;
		}
		return keyMap.get(keyCode);
	}

	public void onPreUpdatePass() {
		isUpdating = true;
	}

	public void onPostUpdatePass() {
		isUpdating = false;
		synchronized (keyMap) {
			synchronized (keyUpMap) {
				synchronized (keyDownMap) {
					for (KeyCode keyCode : KeyCode.values()) {
						keyUpMap.put(keyCode, pendingKeyUpMap.get(keyCode));
						pendingKeyUpMap.put(keyCode, false);
						keyDownMap.put(keyCode, pendingKeyDownMap.get(keyCode));
						pendingKeyDownMap.put(keyCode, false);
					}
				}
			}
		}
	}

	private KeyCode getKeyCodeFromAWTKeyCode(int keyCode) {
		switch (keyCode) {
		case KeyEvent.VK_A:
			return KeyCode.A;
		case KeyEvent.VK_B:
			return KeyCode.B;
		case KeyEvent.VK_C:
			return KeyCode.C;
		case KeyEvent.VK_D:
			return KeyCode.D;
		case KeyEvent.VK_E:
			return KeyCode.E;
		case KeyEvent.VK_F:
			return KeyCode.F;
		case KeyEvent.VK_G:
			return KeyCode.G;
		case KeyEvent.VK_H:
			return KeyCode.H;
		case KeyEvent.VK_I:
			return KeyCode.I;
		case KeyEvent.VK_J:
			return KeyCode.J;
		case KeyEvent.VK_K:
			return KeyCode.K;
		case KeyEvent.VK_L:
			return KeyCode.L;
		case KeyEvent.VK_M:
			return KeyCode.M;
		case KeyEvent.VK_N:
			return KeyCode.N;
		case KeyEvent.VK_O:
			return KeyCode.O;
		case KeyEvent.VK_P:
			return KeyCode.P;
		case KeyEvent.VK_Q:
			return KeyCode.Q;
		case KeyEvent.VK_R:
			return KeyCode.R;
		case KeyEvent.VK_S:
			return KeyCode.S;
		case KeyEvent.VK_T:
			return KeyCode.T;
		case KeyEvent.VK_U:
			return KeyCode.U;
		case KeyEvent.VK_V:
			return KeyCode.V;
		case KeyEvent.VK_W:
			return KeyCode.W;
		case KeyEvent.VK_X:
			return KeyCode.X;
		case KeyEvent.VK_Y:
			return KeyCode.Y;
		case KeyEvent.VK_Z:
			return KeyCode.Z;
		case KeyEvent.VK_UP:
			return KeyCode.Up;
		case KeyEvent.VK_DOWN:
			return KeyCode.Down;
		case KeyEvent.VK_LEFT:
			return KeyCode.Left;
		case KeyEvent.VK_RIGHT:
			return KeyCode.Right;
		case KeyEvent.VK_PAGE_UP:
			return KeyCode.PageUp;
		case KeyEvent.VK_PAGE_DOWN:
			return KeyCode.PageDown;
		case KeyEvent.VK_NUMPAD0:
			return KeyCode.Numpad0;
		case KeyEvent.VK_NUMPAD1:
			return KeyCode.Numpad1;
		case KeyEvent.VK_NUMPAD2:
			return KeyCode.Numpad2;
		case KeyEvent.VK_NUMPAD3:
			return KeyCode.Numpad3;
		case KeyEvent.VK_NUMPAD4:
			return KeyCode.Numpad4;
		case KeyEvent.VK_NUMPAD5:
			return KeyCode.Numpad5;
		case KeyEvent.VK_NUMPAD6:
			return KeyCode.Numpad6;
		case KeyEvent.VK_NUMPAD7:
			return KeyCode.Numpad7;
		case KeyEvent.VK_NUMPAD8:
			return KeyCode.Numpad8;
		case KeyEvent.VK_NUMPAD9:
			return KeyCode.Numpad9;
		}
		return null;
	}

	private KeyCode getKeyCodeFromAWTMouseButtonNumber(int button) {
		switch (button) {
		case 1:
			return KeyCode.Mouse1;
		case 2:
			return KeyCode.Mouse2;
		case 3:
			return KeyCode.Mouse3;
		case 4:
			return KeyCode.Mouse4;
		case 5:
			return KeyCode.Mouse5;
		case 6:
			return KeyCode.Mouse6;
		case 7:
			return KeyCode.Mouse7;
		case 8:
			return KeyCode.Mouse8;
		case 9:
			return KeyCode.Mouse9;
		case 10:
			return KeyCode.Mouse10;
		}
		return null;
	}
}
