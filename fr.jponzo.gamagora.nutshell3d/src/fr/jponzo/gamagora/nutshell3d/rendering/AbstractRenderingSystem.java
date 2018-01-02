package fr.jponzo.gamagora.nutshell3d.rendering;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;

import fr.jponzo.gamagora.nutshell3d.scene.SceneManager;

public abstract class AbstractRenderingSystem {
	protected GLCanvas glcanvas;

	protected AbstractRenderingSystem() {
		//getting the capabilities object of GL2 profile
		GLProfile profile = GLProfile.get(GLProfile.GL4);
		GLCapabilities capabilities = new GLCapabilities(profile);
		capabilities.setStencilBits(8);

		// The canvas
		this.glcanvas = new GLCanvas(capabilities);   
		glcanvas.setAutoSwapBufferMode(true);

		GLEventListener glEventListener = new GLEventListener() {

			@Override
			public void display(GLAutoDrawable drawable) {
				GL4 gl = drawable.getGL().getGL4();
				long time = System.currentTimeMillis();
				render(gl);
				long renderTime = System.currentTimeMillis() - time;
				System.out.println(renderTime);
			}

			@Override
			public void init(GLAutoDrawable drawable) {
				GL4 gl = drawable.getGL().getGL4();
				initialize(gl);
			}

			@Override
			public void dispose(GLAutoDrawable drawable) {
				// TODO Auto-generated method stub

			}

			@Override
			public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
				GL4 gl = drawable.getGL().getGL4();
				resize(gl, x, y, width, height);		
			}
		};
		glcanvas.addGLEventListener(glEventListener);
	}
	
	protected abstract void resize(GL4 gl, int x, int y, int width, int height);

	protected abstract void render(GL4 drawable);
	
	protected abstract void initialize(GL4 drawable);
}
