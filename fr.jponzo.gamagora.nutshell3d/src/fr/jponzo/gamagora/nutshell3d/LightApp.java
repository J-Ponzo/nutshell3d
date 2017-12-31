package fr.jponzo.gamagora.nutshell3d;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.naming.OperationNotSupportedException;

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
import fr.jponzo.gamagora.nutshell3d.utils.io.IOUtils;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Mat4;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Matrices;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec3;

public class LightApp {
	private static final String APP_NAME = "Nutshell3D App";
	private static int width = 1600;
	private static int height = 900;

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
		transform.setLocalTranslate(Matrices.translation(0f, 0.5f, -3f));
		ICamera camera = new Camera(cameraEntity);
		camera.setWidth(width);
		camera.setHeight(height);
		camera.setNear(0.01f);
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
			private float moveSpeed = 4f;
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
			}

			@Override
			public void init() {
			}
		};
		SceneManager.getInstance().setActiveCamera(camera);

		//Create Lights
		//Cam Light
		IEntity camLightEntity = new Entity();
		transform = new Transform(camLightEntity);
		transform.setLocalTranslate(Matrices.translation(0f, 0f, 3f));
		transform.setLocalScale(Matrices.scale(0.2f, 0.2f, 0.2f));
		ILight light = new Light(camLightEntity);
		light.setAlbedo(new Color(255, 255, 255, 255));
		light.setIntensity(5f);
		cameraEntity.addChild(camLightEntity);
		IMeshDef sphereMeshDef = new MeshDef();
		sphereMeshDef.setPath(IOUtils.RES_FOLDER_PATH + "meshes\\sphere.obj");
		sphereMeshDef.load();
		IMesh whiteSphereMesh = new Mesh(camLightEntity, sphereMeshDef);
		IMaterial wightLightMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.frag");
		wightLightMat.setVec3Param("mat_color", 1f, 1f, 1f);
		whiteSphereMesh.setMaterial(wightLightMat);

		//Create Box Mesh
		IEntity boxEntity = new Entity();
		transform = new Transform(boxEntity);
		transform.setLocalTranslate(Matrices.translation(-2f, -2f, -2f));
		IMeshDef boxMeshDef = new MeshDef();
		boxMeshDef.setPath(IOUtils.RES_FOLDER_PATH + "meshes\\Scifi_Box_01.obj");
		boxMeshDef.load();
		IMesh boxMesh = new Mesh(boxEntity, boxMeshDef);
		rootEntity.addChild(boxEntity);
		IMaterial boxMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicLightTexNorm.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicLightTexNorm.frag");
		ITexture nutDiffTex = MaterialManager.getInstance().createTexture(IOUtils.RES_FOLDER_PATH + "textures\\Scifi_Box_03_D.png");
		boxMat.setTexParam("mat_diffTexture", nutDiffTex);
		ITexture nutNormalTex = MaterialManager.getInstance().createTexture(IOUtils.RES_FOLDER_PATH + "textures\\Scifi_Box_01_N.png");
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

		createRoom(rootEntity);
	}

	private static void createRoom(IEntity rootEntity) throws OperationNotSupportedException {
		ITransform transform;

		//Create Room Pivot
		IEntity roomrEntity = new Entity();
		transform = new Transform(roomrEntity);
		transform.setLocalTranslate(Matrices.translation(-1f, 1f, 1f));
		transform.setLocalRotate(Matrices.yRotation((float) (Math.PI / 4)));
		rootEntity.addChild(roomrEntity);

		IMaterial wallMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicLightTexNorm.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicLightTexNorm.frag");
		ITexture wallDiffTex = MaterialManager.getInstance().createTexture(IOUtils.RES_FOLDER_PATH + "textures\\Wall_02_D.png");
		wallMat.setTexParam("mat_diffTexture", wallDiffTex);
		ITexture wallNormalTex = MaterialManager.getInstance().createTexture(IOUtils.RES_FOLDER_PATH + "textures\\Wall_02_N.png");
		wallMat.setTexParam("mat_normTexture", wallNormalTex);

		//Create Floor
		IEntity floorEntity = new Entity();
		transform = new Transform(floorEntity);
		transform.setLocalTranslate(Matrices.translation(0f, -5f, 0f));
		transform.setLocalRotate(Matrices.xRotation((float) (3 * Math.PI / 2)));
		transform.setLocalScale(Matrices.scale(10f, 10f, 1f));
		IMeshDef wallMeshDef = new MeshDef();
		wallMeshDef.setPath(IOUtils.RES_FOLDER_PATH + "meshes\\square.mesh.csv");
		wallMeshDef.load();
		IMesh floorMesh = new Mesh(floorEntity, wallMeshDef);
		roomrEntity.addChild(floorEntity);
		floorMesh.setMaterial(wallMat);

		//Create roof
		IEntity roofEntity = new Entity();
		transform = new Transform(roofEntity);
		transform.setLocalTranslate(Matrices.translation(0f, 5f, 0f));
		transform.setLocalRotate(Matrices.xRotation((float) (Math.PI / 2)));
		transform.setLocalScale(Matrices.scale(10f, 10f, 1f));
		IMesh roofMesh = new Mesh(roofEntity, wallMeshDef);
		roomrEntity.addChild(roofEntity);
		roofMesh.setMaterial(wallMat);

		//Create front wall
		IEntity fWallEntity = new Entity();
		transform = new Transform(fWallEntity);
		transform.setLocalTranslate(Matrices.translation(0f, 0f, 5f));
		transform.setLocalRotate(Matrices.yRotation((float) (Math.PI)));
		transform.setLocalScale(Matrices.scale(10f, 10f, 1f));
		IMesh fWallMesh = new Mesh(fWallEntity, wallMeshDef);
		roomrEntity.addChild(fWallEntity);
		fWallMesh.setMaterial(wallMat);

		//Create back wall
		IEntity bWallEntity = new Entity();
		transform = new Transform(bWallEntity);
		transform.setLocalTranslate(Matrices.translation(0f, 0f, -5f));
		transform.setLocalScale(Matrices.scale(10f, 10f, 1f));
		IMesh bWallMesh = new Mesh(bWallEntity, wallMeshDef);
		roomrEntity.addChild(bWallEntity);
		bWallMesh.setMaterial(wallMat);

		//Create left wall
		IEntity lWallEntity = new Entity();
		transform = new Transform(lWallEntity);
		transform.setLocalTranslate(Matrices.translation(5f, 0f, 0f));
		transform.setLocalRotate(Matrices.yRotation((float) (3 * Math.PI / 2)));
		transform.setLocalScale(Matrices.scale(10f, 10f, 1f));
		IMesh lWallMesh = new Mesh(lWallEntity, wallMeshDef);
		roomrEntity.addChild(lWallEntity);
		lWallMesh.setMaterial(wallMat);

		//Create right wall
		IEntity rWallEntity = new Entity();
		transform = new Transform(rWallEntity);
		transform.setLocalTranslate(Matrices.translation(-5f, 0f, 0f));
		transform.setLocalRotate(Matrices.yRotation((float) (Math.PI / 2)));
		transform.setLocalScale(Matrices.scale(10f, 10f, 1f));
		IMesh rWallMesh = new Mesh(rWallEntity, wallMeshDef);
		roomrEntity.addChild(rWallEntity);
		rWallMesh.setMaterial(wallMat);

		//Lights 
		IMeshDef sphereMeshDef = new MeshDef();
		sphereMeshDef.setPath(IOUtils.RES_FOLDER_PATH + "meshes\\sphere.obj");
		sphereMeshDef.load();

		//Scene red light
		IEntity redLightEntity = new Entity();
		transform = new Transform(redLightEntity);
		transform.setLocalTranslate(Matrices.translation(-3f, 0f, -3f));
		transform.setLocalScale(Matrices.scale(0.2f, 0.2f, 0.2f));
		ILight redLight = new Light(redLightEntity);
		redLight.setAlbedo(new Color(255, 100, 100, 255));
		redLight.setIntensity(2f);
		roomrEntity.addChild(redLightEntity);
		IMesh redSphereMesh = new Mesh(redLightEntity, sphereMeshDef);
		IMaterial redLightMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.frag");
		redLightMat.setVec3Param("mat_color", 1f, 0f, 0f);
		redSphereMesh.setMaterial(redLightMat);

		//Scene green light
		IEntity greenLightEntity = new Entity();
		transform = new Transform(greenLightEntity);
		transform.setLocalTranslate(Matrices.translation(3f, 0f, -3f));
		transform.setLocalScale(Matrices.scale(0.2f, 0.2f, 0.2f));
		ILight greenLight = new Light(greenLightEntity);
		greenLight.setAlbedo(new Color(100, 255, 100, 255));
		greenLight.setIntensity(2f);
		roomrEntity.addChild(greenLightEntity);
		IMesh greenSphereMesh = new Mesh(greenLightEntity, sphereMeshDef);
		IMaterial greenLightMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.frag");
		greenLightMat.setVec3Param("mat_color", 0f, 1f, 0f);
		greenSphereMesh.setMaterial(greenLightMat);

		//Scene blue light
		IEntity blueLightEntity = new Entity();
		transform = new Transform(blueLightEntity);
		transform.setLocalTranslate(Matrices.translation(3f, 0f, 3f));
		transform.setLocalScale(Matrices.scale(0.2f, 0.2f, 0.2f));
		ILight blueLight = new Light(blueLightEntity);
		blueLight.setAlbedo(new Color(100, 100, 255, 255));
		blueLight.setIntensity(2f);
		roomrEntity.addChild(blueLightEntity);
		IMesh blueSphereMesh = new Mesh(blueLightEntity, sphereMeshDef);
		IMaterial blueLightMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.frag");
		blueLightMat.setVec3Param("mat_color", 0f, 0f, 1f);
		blueSphereMesh.setMaterial(blueLightMat);

		//Scene yellow light
		IEntity yellowLightEntity = new Entity();
		transform = new Transform(yellowLightEntity);
		transform.setLocalTranslate(Matrices.translation(-3f, 0f, 3f));
		transform.setLocalScale(Matrices.scale(0.2f, 0.2f, 0.2f));
		ILight yellowLight = new Light(yellowLightEntity);
		yellowLight.setAlbedo(new Color(255, 255, 100, 255));
		yellowLight.setIntensity(2f);
		roomrEntity.addChild(yellowLightEntity);
		IMesh yellowSphereMesh = new Mesh(yellowLightEntity, sphereMeshDef);
		IMaterial yellowLightMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.frag");
		yellowLightMat.setVec3Param("mat_color", 1f, 1f, 0f);
		yellowSphereMesh.setMaterial(yellowLightMat);

		//		//Create mirrors
		IMirror mirror = null;
		IMaterial mirrorMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\mirrorPostEffect.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\mirrorPostEffect.frag");
		mirrorMat.setVec3Param("mat_filter", 0.1f, 0.8f, 0.8f);
		
		
		mirror = new Mirror(lWallEntity);
		mirror.setMaterial(mirrorMat);
		//		
		//		mirror = new Mirror(bWallEntity);
		//		mirror.setMaterial(mirrorMat);
		//		
		//		mirror = new Mirror(roofEntity);
		//		mirror.setMaterial(mirrorMat);
	}
}
