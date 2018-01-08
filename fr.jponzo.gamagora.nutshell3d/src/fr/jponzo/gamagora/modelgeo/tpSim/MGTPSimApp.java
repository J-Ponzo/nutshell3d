package fr.jponzo.gamagora.modelgeo.tpSim;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import com.jogamp.opengl.awt.GLCanvas;

import fr.jponzo.gamagora.modelgeo.MutableMeshDef;
import fr.jponzo.gamagora.modelgeo.Sphere;
import fr.jponzo.gamagora.modelgeo.tp5.ICurve;
import fr.jponzo.gamagora.nutshell3d.input.InputManager;
import fr.jponzo.gamagora.nutshell3d.input.KeyCode;
import fr.jponzo.gamagora.nutshell3d.material.impl.MaterialManager;
import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterial;
import fr.jponzo.gamagora.nutshell3d.rendering.RenderingSystem;
import fr.jponzo.gamagora.nutshell3d.runtime.RuntimeSystem;
import fr.jponzo.gamagora.nutshell3d.scene.SceneManager;
import fr.jponzo.gamagora.nutshell3d.scene.impl.AbstractUpdator;
import fr.jponzo.gamagora.nutshell3d.scene.impl.Camera;
import fr.jponzo.gamagora.nutshell3d.scene.impl.Entity;
import fr.jponzo.gamagora.nutshell3d.scene.impl.Light;
import fr.jponzo.gamagora.nutshell3d.scene.impl.Mesh;
import fr.jponzo.gamagora.nutshell3d.scene.impl.MeshDef;
import fr.jponzo.gamagora.nutshell3d.scene.impl.Transform;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ICamera;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ILight;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IMesh;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IMeshDef;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ITransform;
import fr.jponzo.gamagora.nutshell3d.utils.io.IOUtils;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Mat4;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Matrices;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec3;

public class MGTPSimApp {
	private static final String APP_NAME = "Nutshell3D App";
	private static int width = 800;
	private static int height = 600;

	private static float gridDisc = 100f;
	private static float[][][] voxGrid;
	private static MutableMeshDef meshDef;
	private static int selectedSphere = 0;
	private static boolean isInterOp = false;

	public static void main(String[] args) throws InterruptedException, OperationNotSupportedException {

		//creating frame
		final Frame frame = new Frame (APP_NAME);

		//adding canvas to frame
		GLCanvas glCanvas = RenderingSystem.getInstance().getGlcanvas();
		frame.add(glCanvas);

		//Configure frame
		frame.setSize( width, height);
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter() {				
			@Override
			public void windowClosing(WindowEvent e) {
				// Use a dedicate thread to run the stop() to ensure that the
				// animator stops before program exits.
				new Thread() {
					@Override
					public void run() {
						System.exit(0);
					}
				}.start();
			}
		});

		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				width = e.getComponent().getWidth();
				height = e.getComponent().getHeight();

				ICamera camera = SceneManager.getInstance().getActiveCamera();
				if (camera != null) {
					float w = ((float) camera.getViewport().width) / 100f;
					float h = ((float) camera.getViewport().height) / 100f;
					camera.setWidth((int) ((float)width * w));
					camera.setHeight((int) ((float)height * h));
				}
			}
		});

		configureScene();

		SceneManager.getInstance().initPass();

		RenderingSystem.getInstance().setBatchDrawCalls(false);

		RuntimeSystem.getInstance().start();

		RuntimeSystem.getInstance().play();
	}

	private static void configureScene() throws OperationNotSupportedException {
		//Create root
		IEntity rootEntity = new Entity();
		ITransform transform = new Transform(rootEntity);
		SceneManager.getInstance().setRoot(rootEntity);

		//Create Camera
		IEntity cameraEntity = new Entity();
		transform = new Transform(cameraEntity);
		transform.setLocalTranslate(Matrices.translation(0f, 0f, -30));
		ICamera camera = new Camera(cameraEntity);
		camera.setWidth(width);
		camera.setHeight(height);
		camera.setNear(0.001f);
		camera.setFar(100);
		camera.setFov(60f);
		camera.setViewport(
				new Rectangle(0, 0, 100, 100)
				);
		camera.setOrtho(false);
		IMaterial camMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\idPostEffect.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\idPostEffect.frag");
		camera.setMaterial(camMat);
		rootEntity.addChild(cameraEntity);
		new AbstractUpdator(cameraEntity) {
			private float moveSpeed = 10f;
			private float rotSpeed = (float) Math.PI / 3f;

			@Override
			public void update(long deltaTime) {
				//Move
				ITransform transform = entity.getTransforms().get(0);
				if (InputManager.getInstance().getKey(KeyCode.R)) {
					Mat4 localTranslation = transform.getLocalTranslate();
					Vec3 move = transform.getUp().multiply(moveSpeed * ((float) deltaTime / 1000f));
					Mat4 offsetTranslation = Matrices.translation(move.getX(), move.getY(), move.getZ());
					transform.setLocalTranslate(offsetTranslation.multiply(localTranslation));
				}
				if (InputManager.getInstance().getKey(KeyCode.F)) {
					Mat4 localTranslation = transform.getLocalTranslate();
					Vec3 move = transform.getUp().multiply(-moveSpeed * ((float) deltaTime / 1000f));
					Mat4 offsetTranslation = Matrices.translation(move.getX(), move.getY(), move.getZ());
					transform.setLocalTranslate(offsetTranslation.multiply(localTranslation));
				}
				if (InputManager.getInstance().getKey(KeyCode.Z)) {
					Mat4 localTranslation = transform.getLocalTranslate();
					Vec3 move = transform.getFwd().multiply(moveSpeed * ((float) deltaTime / 1000f));
					Mat4 offsetTranslation = Matrices.translation(move.getX(), move.getY(), move.getZ());
					transform.setLocalTranslate(offsetTranslation.multiply(localTranslation));
				}
				if (InputManager.getInstance().getKey(KeyCode.S)) {
					Mat4 localTranslation = transform.getLocalTranslate();
					Vec3 move = transform.getFwd().multiply(-moveSpeed * ((float) deltaTime / 1000f));
					Mat4 offsetTranslation = Matrices.translation(move.getX(), move.getY(), move.getZ());
					transform.setLocalTranslate(offsetTranslation.multiply(localTranslation));
				}
				if (InputManager.getInstance().getKey(KeyCode.Q)) {
					Mat4 localTranslation = transform.getLocalTranslate();
					Vec3 move = transform.getRight().multiply(moveSpeed * ((float) deltaTime / 1000f));
					Mat4 offsetTranslation = Matrices.translation(move.getX(), move.getY(), move.getZ());
					transform.setLocalTranslate(offsetTranslation.multiply(localTranslation));
				}
				if (InputManager.getInstance().getKey(KeyCode.D)) {
					Mat4 localTranslation = transform.getLocalTranslate();
					Vec3 move = transform.getRight().multiply(-moveSpeed * ((float) deltaTime / 1000f));
					Mat4 offsetTranslation = Matrices.translation(move.getX(), move.getY(), move.getZ());
					transform.setLocalTranslate(offsetTranslation.multiply(localTranslation));
				}

				//Turn
				Mat4 localRotation = transform.getLocalRotate();
				Mat4 offsetHRotation = Mat4.MAT4_IDENTITY;
				Mat4 offsetVRotation = Mat4.MAT4_IDENTITY;
				if (InputManager.getInstance().getKey(KeyCode.Left)) {
					offsetHRotation = Matrices.yRotation(rotSpeed * ((float) deltaTime / 1000f));
				}
				if (InputManager.getInstance().getKey(KeyCode.Right)) {
					offsetHRotation = Matrices.yRotation(-rotSpeed * ((float) deltaTime / 1000f));
				}
				if (InputManager.getInstance().getKey(KeyCode.Up)) {
					offsetVRotation = Matrices.rotate(-rotSpeed * ((float) deltaTime / 1000f), transform.getRight());
				}
				if (InputManager.getInstance().getKey(KeyCode.Down)) {
					offsetVRotation = Matrices.rotate(rotSpeed * ((float) deltaTime / 1000f), transform.getRight());
				}
				transform.setLocalRotate(offsetHRotation.multiply(offsetVRotation).multiply(localRotation));

				//Discretisation
				if (InputManager.getInstance().getKeyDown(KeyCode.PageUp)) {
					gridDisc++;
					updateMesh();
				}
				if (InputManager.getInstance().getKeyDown(KeyCode.PageDown)) {
					if (gridDisc > 1) {
						gridDisc--;
					}
					updateMesh();
				}
			}


			@Override
			public void init() {
			}
		};
		SceneManager.getInstance().setActiveCamera(camera);

		//Create Light
		IEntity lightEntity = new Entity();
		transform = new Transform(lightEntity);
		transform.setLocalTranslate(Matrices.translation(0f, 0f, 0f));
		ILight light = new Light(lightEntity);
		light.setAlbedo(
				new Color(255, 255, 255, 255)
				);
		light.setIntensity(100f);
		cameraEntity.addChild(lightEntity);

		meshDef = new MutableMeshDef();
		meshDef.setPath(IOUtils.RES_FOLDER_PATH + "meshes\\bunny.off");
		meshDef.load();
		
		updateMesh();
	}
	
	private static void updateMesh() {
		System.out.println("Update");
	}
}
