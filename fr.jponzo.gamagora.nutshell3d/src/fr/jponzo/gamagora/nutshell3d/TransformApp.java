package fr.jponzo.gamagora.nutshell3d;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.naming.OperationNotSupportedException;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;

import fr.jponzo.gamagora.nutshell3d.input.InputManager;
import fr.jponzo.gamagora.nutshell3d.input.KeyCode;
import fr.jponzo.gamagora.nutshell3d.material.impl.MaterialManager;
import fr.jponzo.gamagora.nutshell3d.material.interfaces.IMaterial;
import fr.jponzo.gamagora.nutshell3d.material.interfaces.ITexture;
import fr.jponzo.gamagora.nutshell3d.rendering.RenderingSystem;
import fr.jponzo.gamagora.nutshell3d.runtime.RuntimeSystem;
import fr.jponzo.gamagora.nutshell3d.scene.SceneManager;
import fr.jponzo.gamagora.nutshell3d.scene.impl.AbstractUpdator;
import fr.jponzo.gamagora.nutshell3d.scene.impl.Camera;
import fr.jponzo.gamagora.nutshell3d.scene.impl.Entity;
import fr.jponzo.gamagora.nutshell3d.scene.impl.Light;
import fr.jponzo.gamagora.nutshell3d.scene.impl.Mesh;
import fr.jponzo.gamagora.nutshell3d.scene.impl.MeshDef;
import fr.jponzo.gamagora.nutshell3d.scene.impl.Mirror;
import fr.jponzo.gamagora.nutshell3d.scene.impl.Transform;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ICamera;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ILight;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IMesh;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IMeshDef;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IMirror;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ITransform;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Mat4;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Matrices;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec3;

public class TransformApp {
	private static final String APP_NAME = "Nutshell3D App";
	private static final String RES_FOLDER_PATH = "D:\\JavaWorkspaces\\nutshell3dWorkspace\\fr.jponzo.gamagora.nutshell3d\\resources\\";
	private static int width = 800;
	private static int height = 600;

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
		transform.setLocalTranslate(Matrices.translation(0f, 0.5f, -5f));
		ICamera camera = new Camera(cameraEntity);
		camera.setWidth(width);
		camera.setHeight(height);
		camera.setNear(1);
		camera.setFar(100);
		camera.setFov(60f);
		camera.setViewport(
				new Rectangle(0, 0, 100, 100)
				);
		camera.setOrtho(false);
		IMaterial camMat = MaterialManager.getInstance().createMaterial(
				RES_FOLDER_PATH + "shaders\\idPostEffect.vert", 
				RES_FOLDER_PATH + "shaders\\idPostEffect.frag");
		camera.setMaterial(camMat);
		rootEntity.addChild(cameraEntity);
		new AbstractUpdator(cameraEntity) {
			private float moveSpeed = 2f;
			private float rotSpeed = (float) Math.PI / 3f;

			@Override
			public void update(long deltaTime) {
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
				if (InputManager.getInstance().getKey(KeyCode.Left)) {
					Mat4 localRotation = transform.getLocalRotate();
					Mat4 offsetRotation = Matrices.yRotation(rotSpeed * ((float) deltaTime / 1000f));
					transform.setLocalRotate(offsetRotation.multiply(localRotation));
				}
				if (InputManager.getInstance().getKey(KeyCode.Right)) {
					Mat4 localRotation = transform.getLocalRotate();
					Mat4 offsetRotation = Matrices.yRotation(-rotSpeed * ((float) deltaTime / 1000f));
					transform.setLocalRotate(offsetRotation.multiply(localRotation));
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
		transform.setLocalTranslate(Matrices.translation(2f, 2f, -2f));
		ILight light = new Light(lightEntity);
		light.setColor(
				new Color(255, 255, 255, 255)
				);
		light.setIntensity(10f);
		rootEntity.addChild(lightEntity);

		//Create Floor
		IEntity floorEntity = new Entity();
		transform = new Transform(floorEntity);
		transform.setLocalTranslate(Matrices.translation(0f, -1f, 0f));
		transform.setLocalRotate(Matrices.xRotation((float) (Math.PI / 2)));
		transform.setLocalScale(Matrices.scale(5f, 5f, 1f));
		IMeshDef floorMeshDef = new MeshDef();
		floorMeshDef.setPath(RES_FOLDER_PATH + "meshes\\square.mesh.csv");
		floorMeshDef.load();
		IMesh mirrorMesh = new Mesh(floorEntity, floorMeshDef);
		rootEntity.addChild(floorEntity);
		IMaterial mirrorMat = MaterialManager.getInstance().createMaterial(
				RES_FOLDER_PATH + "shaders\\basicColor.vert", 
				RES_FOLDER_PATH + "shaders\\\\basicColor.frag");
		mirrorMat.setVec3Param("mat_color", 1f, 1f, 0f);
		mirrorMesh.setMaterial(mirrorMat);

		//Create Box Mesh
		IEntity boxEntity = new Entity();
		transform = new Transform(boxEntity);
		IMeshDef boxMeshDef = new MeshDef();
		boxMeshDef.setPath(RES_FOLDER_PATH + "meshes\\Scifi_Box_01.obj");
		boxMeshDef.load();
		IMesh boxMesh = new Mesh(boxEntity, boxMeshDef);
		rootEntity.addChild(boxEntity);
		IMaterial boxMat = MaterialManager.getInstance().createMaterial(
				RES_FOLDER_PATH + "shaders\\basicLightTexNorm.vert", 
				RES_FOLDER_PATH + "shaders\\\\basicLightTexNorm.frag");
		ITexture nutDiffTex = MaterialManager.getInstance().createTexture(RES_FOLDER_PATH + "textures\\Scifi_Box_03_D.png");
		boxMat.setTexParam("mat_diffTexture", nutDiffTex);
		ITexture nutNormalTex = MaterialManager.getInstance().createTexture(RES_FOLDER_PATH + "textures\\Scifi_Box_01_N.png");
		boxMat.setTexParam("mat_normTexture", nutNormalTex);
		boxMesh.setMaterial(boxMat);
		new AbstractUpdator(boxEntity) {
			private float speed = (float) (Math.PI / 2);

			@Override
			public void update(long deltaTime) {
				ITransform transform = entity.getTransforms().get(0);
				Mat4 localRotation = transform.getLocalRotate();
				Mat4 offsetRotation = Matrices.yRotation(speed * ((float) deltaTime / 1000f));
				transform.setLocalRotate(offsetRotation.multiply(localRotation));
			}

			@Override
			public void init() {
			}
		};
	}
}
