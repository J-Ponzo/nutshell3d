package fr.jponzo.gamagora.castle;

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
import fr.jponzo.gamagora.nutshell3d.scene.impl.Portal;
import fr.jponzo.gamagora.nutshell3d.scene.impl.Transform;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ICamera;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IEntity;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ILight;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IMesh;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IMeshDef;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IMirror;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.IPortal;
import fr.jponzo.gamagora.nutshell3d.scene.interfaces.ITransform;
import fr.jponzo.gamagora.nutshell3d.utils.io.IOUtils;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Mat4;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Matrices;
import fr.jponzo.gamagora.nutshell3d.utils.jglm.Vec3;

public class CastleApp {
	private static final String APP_NAME = "Nutshell3D App";
	private static int width = 1600;
	private static int height = 900;

	private static Entity cameraEntity;

	private static MeshDef straightCorrMeshDef;
	private static MeshDef crossCorrMeshDef;
	private static MeshDef wallCornerMeshDef;
	private static MeshDef wallWindowMeshDef;
	private static MeshDef wallDoorMeshDef;
	private static IMeshDef chessboadMeshDef;
	private static IMeshDef squareMeshDef;
	private static IMeshDef armorMeshDef;
	private static IMeshDef shieldMeshDef;
	private static IMeshDef tableMeshDef;
	private static IMeshDef chairMeshDef;

	private static IMaterial stoneMat;
	private static IMaterial defaultMat;
	private static IMaterial blackMirrorMat;
	private static IMaterial whiteMirrorMat;
	private static IMaterial portalMat;
	private static IMaterial armorMat;
	private static IMaterial shieldMat;
	private static IMaterial woodMat;

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

		createPlayer(rootEntity);

		createMeshesAndMats();

		createCastle(rootEntity);
	}

	private static void createPlayer(IEntity rootEntity) throws OperationNotSupportedException {
		ITransform transform;
		//Create Camera
		cameraEntity = new Entity();
		transform = new Transform(cameraEntity);
		transform.setLocalTranslate(Matrices.translation(0f, 0f, 0f));
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
			private float moveSpeed = 2f;
			private float rotSpeed = (float) Math.PI / 4f;

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
		transform.setLocalTranslate(Matrices.translation(0f, 0f, 0.5f));
		ILight light = new Light(camLightEntity);
		light.setAlbedo(new Color(255, 255, 255, 255));
		light.setIntensity(0.1f);
		cameraEntity.addChild(camLightEntity);
	}

	private static void createCastle(IEntity rootEntity) throws OperationNotSupportedException {
		ITransform transform;

		//Create Castle Entity
		IEntity castleEntity = new Entity();
		transform = new Transform(castleEntity);
		transform.setLocalTranslate(Matrices.translation(0f, 0.5f, 0f));
		rootEntity.addChild(castleEntity);

		//Create rooms
		IEntity floorEntity = new Entity();
		transform = new Transform(floorEntity);
		transform.setLocalTranslate(Matrices.translation(0f, 0f, 0f));
		castleEntity.addChild(floorEntity);

		IEntity crossCorridorsEntity = new Entity();
		transform = new Transform(crossCorridorsEntity);
		transform.setLocalTranslate(Matrices.translation(0f, 0f, 0f));
		castleEntity.addChild(crossCorridorsEntity);

		IEntity northRoomEntity = new Entity();
		transform = new Transform(northRoomEntity);
		transform.setLocalTranslate(Matrices.translation(0f, 0f, 3f));
		castleEntity.addChild(northRoomEntity);

		IEntity westRoomEntity = new Entity();
		transform = new Transform(westRoomEntity);
		transform.setLocalTranslate(Matrices.translation(3f, 0f, 0f));
		castleEntity.addChild(westRoomEntity);

		createFloor(floorEntity);
		createCrossCoridors(crossCorridorsEntity);
		createNorthRoom(northRoomEntity);
		createWestRoom(westRoomEntity);

		//Portals
		//Portal 1
		IEntity portal1Entity = new Entity();
		transform = new Transform(portal1Entity);
		transform.setLocalTranslate(Matrices.translation(0f, -0.25f, 1.5f));
		transform.setLocalRotate(Matrices.yRotation((float) (Math.PI)));
		transform.setLocalScale(Matrices.scale(1f, 1f, 1f));
		IMesh portal1Mesh = new Mesh(portal1Entity, squareMeshDef);
		westRoomEntity.addChild(portal1Entity);
		portal1Mesh.setMaterial(defaultMat);
		IPortal portal1 = new Portal(portal1Entity);
		portal1.setMaterial(portalMat);
		//Portal 2
		IEntity portal2Entity = new Entity();
		transform = new Transform(portal2Entity);
		transform.setLocalTranslate(Matrices.translation(1.5f, -0.25f, 0f));
		transform.setLocalRotate(Matrices.yRotation((float) (-Math.PI / 2)));
		transform.setLocalScale(Matrices.scale(1f, 1f, 1f));
		IMesh portal2Mesh = new Mesh(portal2Entity, squareMeshDef);
		northRoomEntity.addChild(portal2Entity);
		portal2Mesh.setMaterial(defaultMat);
		IPortal portal2 = new Portal(portal2Entity);
		portal2.setMaterial(portalMat);
		//Link Portals
		portal1.setTarget(portal2);
		portal2.setTarget(portal1);

		//Lights 
		IMeshDef sphereMeshDef = new MeshDef();
		sphereMeshDef.setPath(IOUtils.RES_FOLDER_PATH + "meshes\\sphere.obj");
		sphereMeshDef.load();

		//Scene red light
		IEntity redLightEntity = new Entity();
		transform = new Transform(redLightEntity);
		transform.setLocalTranslate(Matrices.translation(3f, 0f, 0f));
		transform.setLocalScale(Matrices.scale(0.05f, 0.05f, 0.05f));
		ILight redLight = new Light(redLightEntity);
		redLight.setAlbedo(new Color(255, 100, 100, 255));
		redLight.setIntensity(0.5f);
		crossCorridorsEntity.addChild(redLightEntity);
		IMesh redSphereMesh = new Mesh(redLightEntity, sphereMeshDef);
		IMaterial redLightMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.frag");
		redLightMat.setVec3Param("mat_color", 1f, 0f, 0f);
		redSphereMesh.setMaterial(redLightMat);

		//		//Scene green light
		//		IEntity greenLightEntity = new Entity();
		//		transform = new Transform(greenLightEntity);
		//		transform.setLocalTranslate(Matrices.translation(3f, 0f, -3f));
		//		transform.setLocalScale(Matrices.scale(0.2f, 0.2f, 0.2f));
		//		ILight greenLight = new Light(greenLightEntity);
		//		greenLight.setAlbedo(new Color(100, 255, 100, 255));
		//		greenLight.setIntensity(2f);
		//		crossCorridorsEntity.addChild(greenLightEntity);
		//		IMesh greenSphereMesh = new Mesh(greenLightEntity, sphereMeshDef);
		//		IMaterial greenLightMat = MaterialManager.getInstance().createMaterial(
		//				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.vert", 
		//				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.frag");
		//		greenLightMat.setVec3Param("mat_color", 0f, 1f, 0f);
		//		greenSphereMesh.setMaterial(greenLightMat);

		//Scene blue light
		IEntity blueLightEntity = new Entity();
		transform = new Transform(blueLightEntity);
		transform.setLocalTranslate(Matrices.translation(0f, 0f, 3f));
		transform.setLocalScale(Matrices.scale(0.05f, 0.05f, 0.05f));
		ILight blueLight = new Light(blueLightEntity);
		blueLight.setAlbedo(new Color(100, 100, 255, 255));
		blueLight.setIntensity(0.5f);
		crossCorridorsEntity.addChild(blueLightEntity);
		IMesh blueSphereMesh = new Mesh(blueLightEntity, sphereMeshDef);
		IMaterial blueLightMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.frag");
		blueLightMat.setVec3Param("mat_color", 0f, 0f, 1f);
		blueSphereMesh.setMaterial(blueLightMat);

		//		//Scene yellow light
		//		IEntity yellowLightEntity = new Entity();
		//		transform = new Transform(yellowLightEntity);
		//		transform.setLocalTranslate(Matrices.translation(-3f, 0f, 3f));
		//		transform.setLocalScale(Matrices.scale(0.2f, 0.2f, 0.2f));
		//		ILight yellowLight = new Light(yellowLightEntity);
		//		yellowLight.setAlbedo(new Color(255, 255, 100, 255));
		//		yellowLight.setIntensity(2f);
		//		crossCorridorsEntity.addChild(yellowLightEntity);
		//		IMesh yellowSphereMesh = new Mesh(yellowLightEntity, sphereMeshDef);
		//		IMaterial yellowLightMat = MaterialManager.getInstance().createMaterial(
		//				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.vert", 
		//				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.frag");
		//		yellowLightMat.setVec3Param("mat_color", 1f, 1f, 0f);
		//		yellowSphereMesh.setMaterial(yellowLightMat);
	}

	private static void createFloor(IEntity roomEntity) {
		Transform transform;
		//Create black tiles floor
		IEntity blackTilesEntity = new Entity();
		transform = new Transform(blackTilesEntity);
		transform.setLocalTranslate(Matrices.translation(1.5f, -0.75f, 1.5f));
		transform.setLocalRotate(Matrices.yRotation((float) (-Math.PI / 2)).multiply(Matrices.xRotation((float) (-Math.PI / 2))));
		transform.setLocalScale(Matrices.scale(6f, 6f, 1f));
		IMesh floorMesh = new Mesh(blackTilesEntity, chessboadMeshDef);
		roomEntity.addChild(blackTilesEntity);
		floorMesh.setMaterial(defaultMat);
		IMirror mirror = new Mirror(blackTilesEntity);
		mirror.setMaterial(blackMirrorMat);

		IEntity whiteTilesEntity = new Entity();
		transform = new Transform(whiteTilesEntity);
		transform.setLocalTranslate(Matrices.translation(1.5f, -0.75f, 1.5f));
		transform.setLocalRotate(Matrices.xRotation((float) (-Math.PI / 2)));
		transform.setLocalScale(Matrices.scale(6f, 6f, 1f));
		IMesh floorMesh2 = new Mesh(whiteTilesEntity, chessboadMeshDef);
		roomEntity.addChild(whiteTilesEntity);
		floorMesh2.setMaterial(defaultMat);
		IMirror mirror2 = new Mirror(whiteTilesEntity);
		mirror2.setMaterial(whiteMirrorMat);
	}

	private static void createNorthRoom(IEntity roomEntity) {
		ITransform transform;

		//east window
		IEntity eastWindowEntity = new Entity();
		transform = new Transform(eastWindowEntity);
		transform.setLocalTranslate(Matrices.translation(-1.3585f, -0.041f, -0.002f));
		transform.setLocalRotate(Matrices.yRotation((float) (Math.PI) / 2));
		IMesh eastWindowMesh = new Mesh(eastWindowEntity, wallWindowMeshDef);
		roomEntity.addChild(eastWindowEntity);
		eastWindowMesh.setMaterial(stoneMat);

		//north door
		IEntity northDoorEntity = new Entity();
		transform = new Transform(northDoorEntity);
		transform.setLocalTranslate(Matrices.translation(-0.002f, -0.146f, 1.358f));
		transform.setLocalRotate(Matrices.yRotation((float) (Math.PI)));
		IMesh northDoorMesh = new Mesh(northDoorEntity, wallDoorMeshDef);
		roomEntity.addChild(northDoorEntity);
		northDoorMesh.setMaterial(stoneMat);

		//west door
		IEntity westDoorEntity = new Entity();
		transform = new Transform(westDoorEntity);
		transform.setLocalTranslate(Matrices.translation(1.358f, -0.146f, 0.002f));
		transform.setLocalRotate(Matrices.yRotation((float) (-Math.PI) / 2));
		IMesh westDoorMesh = new Mesh(westDoorEntity, wallDoorMeshDef);
		roomEntity.addChild(westDoorEntity);
		westDoorMesh.setMaterial(stoneMat);

		//south door
		IEntity southDoorEntity = new Entity();
		transform = new Transform(southDoorEntity);
		transform.setLocalTranslate(Matrices.translation(-0.002f, -0.146f, -1.358f));
		IMesh southDoorMesh = new Mesh(southDoorEntity, wallDoorMeshDef);
		roomEntity.addChild(southDoorEntity);
		southDoorMesh.setMaterial(stoneMat);

		//west-north corners
		IEntity wnCornerCorrEntity = new Entity();
		transform = new Transform(wnCornerCorrEntity);
		transform.setLocalTranslate(Matrices.translation(1.141f, -0.253f, 1.141f));
		IMesh wnCornerCorrMesh = new Mesh(wnCornerCorrEntity, wallCornerMeshDef);
		roomEntity.addChild(wnCornerCorrEntity);
		wnCornerCorrMesh.setMaterial(stoneMat);

		//east-north corners
		IEntity enCornerCorrEntity = new Entity();
		transform = new Transform(enCornerCorrEntity);
		transform.setLocalTranslate(Matrices.translation(-1.141f, -0.253f, 1.141f));
		transform.setLocalRotate(Matrices.yRotation((float) (-Math.PI / 2)));
		IMesh enCornerCorrMesh = new Mesh(enCornerCorrEntity, wallCornerMeshDef);
		roomEntity.addChild(enCornerCorrEntity);
		enCornerCorrMesh.setMaterial(stoneMat);

		//east-south corners
		IEntity esCornerCorrEntity = new Entity();
		transform = new Transform(esCornerCorrEntity);
		transform.setLocalTranslate(Matrices.translation(-1.141f, -0.253f, -1.141f));
		transform.setLocalRotate(Matrices.yRotation((float) (Math.PI)));
		IMesh esCornerCorrMesh = new Mesh(esCornerCorrEntity, wallCornerMeshDef);
		roomEntity.addChild(esCornerCorrEntity);
		esCornerCorrMesh.setMaterial(stoneMat);

		//west-south corners
		IEntity wsCornerCorrEntity = new Entity();
		transform = new Transform(wsCornerCorrEntity);
		transform.setLocalTranslate(Matrices.translation(1.141f, -0.253f, -1.141f));
		transform.setLocalRotate(Matrices.yRotation((float) (Math.PI / 2)));
		IMesh wsCornerCorrMesh = new Mesh(wsCornerCorrEntity, wallCornerMeshDef);
		roomEntity.addChild(wsCornerCorrEntity);
		wsCornerCorrMesh.setMaterial(stoneMat);

		//Armor 1
		IEntity armor1Entity = new Entity();
		transform = new Transform(armor1Entity);
		transform.setLocalTranslate(Matrices.translation(0.25f, -0.49f, 1f));
		transform.setLocalScale(Matrices.scale(0.5f, 0.5f, 0.5f));
		transform.setLocalRotate(Matrices.yRotation((float) (Math.PI)).multiply(
				Matrices.xRotation((float) (-Math.PI / 2))));
		IMesh armor1Mesh = new Mesh(armor1Entity, armorMeshDef);
		roomEntity.addChild(armor1Entity);
		armor1Mesh.setMaterial(armorMat);

		//Armor 2
		IEntity armor2Entity = new Entity();
		transform = new Transform(armor2Entity);
		transform.setLocalTranslate(Matrices.translation(-0.25f, -0.49f, 1f));
		transform.setLocalScale(Matrices.scale(0.5f, 0.5f, 0.5f));
		transform.setLocalRotate(Matrices.yRotation((float) (Math.PI)).multiply(
				Matrices.xRotation((float) (-Math.PI / 2))));
		IMesh armor2Mesh = new Mesh(armor2Entity, armorMeshDef);
		roomEntity.addChild(armor2Entity);
		armor2Mesh.setMaterial(armorMat);

		//Armor 3
		IEntity armor3Entity = new Entity();
		transform = new Transform(armor3Entity);
		transform.setLocalTranslate(Matrices.translation(0.25f, -0.49f, -1f));
		transform.setLocalScale(Matrices.scale(0.5f, 0.5f, 0.5f));
		transform.setLocalRotate(Matrices.xRotation((float) (-Math.PI / 2)));
		IMesh armor3Mesh = new Mesh(armor3Entity, armorMeshDef);
		roomEntity.addChild(armor3Entity);
		armor3Mesh.setMaterial(armorMat);

		//Armor 4
		IEntity armor4Entity = new Entity();
		transform = new Transform(armor4Entity);
		transform.setLocalTranslate(Matrices.translation(-0.25f, -0.49f, -1f));
		transform.setLocalScale(Matrices.scale(0.5f, 0.5f, 0.5f));
		transform.setLocalRotate(Matrices.xRotation((float) (-Math.PI / 2)));
		IMesh armor4Mesh = new Mesh(armor4Entity, armorMeshDef);
		roomEntity.addChild(armor4Entity);
		armor4Mesh.setMaterial(armorMat);

		//Armor 5
		IEntity armor5Entity = new Entity();
		transform = new Transform(armor5Entity);
		transform.setLocalTranslate(Matrices.translation(1f, -0.49f, 0.25f));
		transform.setLocalScale(Matrices.scale(0.5f, 0.5f, 0.5f));
		transform.setLocalRotate(Matrices.yRotation((float) (-Math.PI / 2)).multiply(
				Matrices.xRotation((float) (-Math.PI / 2))));
		IMesh armor5Mesh = new Mesh(armor5Entity, armorMeshDef);
		roomEntity.addChild(armor5Entity);
		armor5Mesh.setMaterial(armorMat);

		//Armor 4
		IEntity armor6Entity = new Entity();
		transform = new Transform(armor6Entity);
		transform.setLocalTranslate(Matrices.translation(1f, -0.49f, -0.25f));
		transform.setLocalScale(Matrices.scale(0.5f, 0.5f, 0.5f));
		transform.setLocalRotate(Matrices.yRotation((float) (-Math.PI / 2)).multiply(
				Matrices.xRotation((float) (-Math.PI / 2))));
		IMesh armor6Mesh = new Mesh(armor6Entity, armorMeshDef);
		roomEntity.addChild(armor6Entity);
		armor6Mesh.setMaterial(armorMat);

		//Shield 1
		IEntity shield1Entity = new Entity();
		transform = new Transform(shield1Entity);
		transform.setLocalTranslate(Matrices.translation(0f, 0f, 1f));
		transform.setLocalScale(Matrices.scale(0.2f, 0.2f, 0.2f));
		transform.setLocalRotate(Matrices.yRotation((float) (-Math.PI / 2)).multiply(
				Matrices.xRotation((float) (-Math.PI / 2))));
		IMesh sield1Mesh = new Mesh(shield1Entity, shieldMeshDef);
		roomEntity.addChild(shield1Entity);
		sield1Mesh.setMaterial(shieldMat);

		//Table
		IEntity tableEntity = new Entity();
		transform = new Transform(tableEntity);
		transform.setLocalTranslate(Matrices.translation(0f, -0.62f, 0f));
		transform.setLocalScale(Matrices.scale(1f, 0.75f, 1f));
		IMesh tableMesh = new Mesh(tableEntity, tableMeshDef);
		roomEntity.addChild(tableEntity);
		tableMesh.setMaterial(woodMat);
	}

	private static void createWestRoom(IEntity roomEntity) {
		ITransform transform;

		//south window
		IEntity southWindowEntity = new Entity();
		transform = new Transform(southWindowEntity);
		transform.setLocalTranslate(Matrices.translation(0.002f, -0.041f, -1.3585f));
		IMesh southWindowMesh = new Mesh(southWindowEntity, wallWindowMeshDef);
		roomEntity.addChild(southWindowEntity);
		southWindowMesh.setMaterial(stoneMat);

		//north door
		IEntity northDoorEntity = new Entity();
		transform = new Transform(northDoorEntity);
		transform.setLocalTranslate(Matrices.translation(-0.002f, -0.146f, 1.358f));
		transform.setLocalRotate(Matrices.yRotation((float) (Math.PI)));
		IMesh northDoorMesh = new Mesh(northDoorEntity, wallDoorMeshDef);
		roomEntity.addChild(northDoorEntity);
		northDoorMesh.setMaterial(stoneMat);

		//west door
		IEntity westDoorEntity = new Entity();
		transform = new Transform(westDoorEntity);
		transform.setLocalTranslate(Matrices.translation(1.358f, -0.146f, 0.002f));
		transform.setLocalRotate(Matrices.yRotation((float) (-Math.PI) / 2));
		IMesh westDoorMesh = new Mesh(westDoorEntity, wallDoorMeshDef);
		roomEntity.addChild(westDoorEntity);
		westDoorMesh.setMaterial(stoneMat);

		//west-north corners
		IEntity wnCornerCorrEntity = new Entity();
		transform = new Transform(wnCornerCorrEntity);
		transform.setLocalTranslate(Matrices.translation(1.141f, -0.253f, 1.141f));
		IMesh wnCornerCorrMesh = new Mesh(wnCornerCorrEntity, wallCornerMeshDef);
		roomEntity.addChild(wnCornerCorrEntity);
		wnCornerCorrMesh.setMaterial(stoneMat);

		//east-north corners
		IEntity enCornerCorrEntity = new Entity();
		transform = new Transform(enCornerCorrEntity);
		transform.setLocalTranslate(Matrices.translation(-1.141f, -0.253f, 1.141f));
		transform.setLocalRotate(Matrices.yRotation((float) (-Math.PI / 2)));
		IMesh enCornerCorrMesh = new Mesh(enCornerCorrEntity, wallCornerMeshDef);
		roomEntity.addChild(enCornerCorrEntity);
		enCornerCorrMesh.setMaterial(stoneMat);

		//east-south corners
		IEntity esCornerCorrEntity = new Entity();
		transform = new Transform(esCornerCorrEntity);
		transform.setLocalTranslate(Matrices.translation(-1.141f, -0.253f, -1.141f));
		transform.setLocalRotate(Matrices.yRotation((float) (Math.PI)));
		IMesh esCornerCorrMesh = new Mesh(esCornerCorrEntity, wallCornerMeshDef);
		roomEntity.addChild(esCornerCorrEntity);
		esCornerCorrMesh.setMaterial(stoneMat);

		//west-south corners
		IEntity wsCornerCorrEntity = new Entity();
		transform = new Transform(wsCornerCorrEntity);
		transform.setLocalTranslate(Matrices.translation(1.141f, -0.253f, -1.141f));
		transform.setLocalRotate(Matrices.yRotation((float) (Math.PI / 2)));
		IMesh wsCornerCorrMesh = new Mesh(wsCornerCorrEntity, wallCornerMeshDef);
		roomEntity.addChild(wsCornerCorrEntity);
		wsCornerCorrMesh.setMaterial(stoneMat);
	}

	private static void createCrossCoridors(IEntity roomEntity) throws OperationNotSupportedException {
		ITransform transform;
		//Cross corridor
		IEntity crossCorrEntity = new Entity();
		transform = new Transform(crossCorrEntity);
		transform.setLocalTranslate(Matrices.translation(0f, -0.015f, 0f));
		IMesh crossCorrMesh = new Mesh(crossCorrEntity, crossCorrMeshDef);
		roomEntity.addChild(crossCorrEntity);
		crossCorrMesh.setMaterial(stoneMat);

		//north corridor
		IEntity northCorrEntity = new Entity();
		transform = new Transform(northCorrEntity);
		transform.setLocalTranslate(Matrices.translation(0f, 0f, 1f));
		IMesh northCorrMesh = new Mesh(northCorrEntity, straightCorrMeshDef);
		roomEntity.addChild(northCorrEntity);
		northCorrMesh.setMaterial(stoneMat);

		//south corridor
		IEntity southCorrEntity = new Entity();
		transform = new Transform(southCorrEntity);
		transform.setLocalTranslate(Matrices.translation(0f, 0f, -1f));
		IMesh southCorrMesh = new Mesh(southCorrEntity, straightCorrMeshDef);
		roomEntity.addChild(southCorrEntity);
		southCorrMesh.setMaterial(stoneMat);

		//east corridor
		IEntity eastCorrEntity = new Entity();
		transform = new Transform(eastCorrEntity);
		transform.setLocalTranslate(Matrices.translation(-1f, 0f, 0f));
		transform.setLocalRotate(Matrices.yRotation((float) (Math.PI / 2)));
		IMesh eastCorrMesh = new Mesh(eastCorrEntity, straightCorrMeshDef);
		roomEntity.addChild(eastCorrEntity);
		eastCorrMesh.setMaterial(stoneMat);

		//west corridor
		IEntity westCorrEntity = new Entity();
		transform = new Transform(westCorrEntity);
		transform.setLocalTranslate(Matrices.translation(1f, 0f, 0f));
		transform.setLocalRotate(Matrices.yRotation((float) (Math.PI / 2)));
		IMesh westCorrMesh = new Mesh(westCorrEntity, straightCorrMeshDef);
		roomEntity.addChild(westCorrEntity);
		westCorrMesh.setMaterial(stoneMat);

		//west-north corners
		IEntity wnCornerCorrEntity = new Entity();
		transform = new Transform(wnCornerCorrEntity);
		transform.setLocalTranslate(Matrices.translation(1.141f, -0.253f, 1.141f));
		IMesh wnCornerCorrMesh = new Mesh(wnCornerCorrEntity, wallCornerMeshDef);
		roomEntity.addChild(wnCornerCorrEntity);
		wnCornerCorrMesh.setMaterial(stoneMat);

		//east-north corners
		IEntity enCornerCorrEntity = new Entity();
		transform = new Transform(enCornerCorrEntity);
		transform.setLocalTranslate(Matrices.translation(-1.141f, -0.253f, 1.141f));
		transform.setLocalRotate(Matrices.yRotation((float) (-Math.PI / 2)));
		IMesh enCornerCorrMesh = new Mesh(enCornerCorrEntity, wallCornerMeshDef);
		roomEntity.addChild(enCornerCorrEntity);
		enCornerCorrMesh.setMaterial(stoneMat);

		//east-south corners
		IEntity esCornerCorrEntity = new Entity();
		transform = new Transform(esCornerCorrEntity);
		transform.setLocalTranslate(Matrices.translation(-1.141f, -0.253f, -1.141f));
		transform.setLocalRotate(Matrices.yRotation((float) (Math.PI)));
		IMesh esCornerCorrMesh = new Mesh(esCornerCorrEntity, wallCornerMeshDef);
		roomEntity.addChild(esCornerCorrEntity);
		esCornerCorrMesh.setMaterial(stoneMat);

		//west-south corners
		IEntity wsCornerCorrEntity = new Entity();
		transform = new Transform(wsCornerCorrEntity);
		transform.setLocalTranslate(Matrices.translation(1.141f, -0.253f, -1.141f));
		transform.setLocalRotate(Matrices.yRotation((float) (Math.PI / 2)));
		IMesh wsCornerCorrMesh = new Mesh(wsCornerCorrEntity, wallCornerMeshDef);
		roomEntity.addChild(wsCornerCorrEntity);
		wsCornerCorrMesh.setMaterial(stoneMat);
	}

	private static void createMeshesAndMats() throws OperationNotSupportedException {
		//Create Mesh defs
		straightCorrMeshDef = new MeshDef();
		straightCorrMeshDef.setPath(IOUtils.RES_FOLDER_PATH + "meshes\\CastleArchStraightTile.obj");
		straightCorrMeshDef.load();

		crossCorrMeshDef = new MeshDef();
		crossCorrMeshDef.setPath(IOUtils.RES_FOLDER_PATH + "meshes\\CastleArchCrossTile.obj");
		crossCorrMeshDef.load();

		wallCornerMeshDef = new MeshDef();
		wallCornerMeshDef.setPath(IOUtils.RES_FOLDER_PATH + "meshes\\CastleWallCorner.obj");
		wallCornerMeshDef.load();

		wallWindowMeshDef = new MeshDef();
		wallWindowMeshDef.setPath(IOUtils.RES_FOLDER_PATH + "meshes\\CastleWallRoundWin.obj");
		wallWindowMeshDef.load();

		wallDoorMeshDef = new MeshDef();
		wallDoorMeshDef.setPath(IOUtils.RES_FOLDER_PATH + "meshes\\CastleWallRoundDoor.obj");
		wallDoorMeshDef.load();

		chessboadMeshDef = new MeshDef();
		chessboadMeshDef.setPath(IOUtils.RES_FOLDER_PATH + "meshes\\chessBoard.obj");
		chessboadMeshDef.load();

		squareMeshDef = new MeshDef();
		squareMeshDef.setPath(IOUtils.RES_FOLDER_PATH + "meshes\\square.mesh.csv");
		squareMeshDef.load();

		armorMeshDef = new MeshDef();
		armorMeshDef.setPath(IOUtils.RES_FOLDER_PATH + "meshes\\Armor.obj");
		armorMeshDef.load();

		shieldMeshDef = new MeshDef();
		shieldMeshDef.setPath(IOUtils.RES_FOLDER_PATH + "meshes\\Shield.obj");
		shieldMeshDef.load();

		tableMeshDef = new MeshDef();
		tableMeshDef.setPath(IOUtils.RES_FOLDER_PATH + "meshes\\Table.obj");
		tableMeshDef.load();

		chairMeshDef = new MeshDef();
		chairMeshDef.setPath(IOUtils.RES_FOLDER_PATH + "meshes\\Chair.obj");
		chairMeshDef.load();

		//Create Materials
		defaultMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.frag");
		defaultMat.setVec3Param("mat_color", 1f, 0f, 1f);

		stoneMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicLightTexNorm.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicLightTexNorm.frag");
		ITexture stoneDiffTex = MaterialManager.getInstance().createTexture(IOUtils.RES_FOLDER_PATH + "textures\\CastleStone_D.png");
		stoneMat.setTexParam("mat_diffTexture", stoneDiffTex);
		ITexture stoneNormalTex = MaterialManager.getInstance().createTexture(IOUtils.RES_FOLDER_PATH + "textures\\CastleStone_N.png");
		stoneMat.setTexParam("mat_normTexture", stoneNormalTex);

		woodMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicLightTexNorm.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicLightTexNorm.frag");
		ITexture woodDiffTex = MaterialManager.getInstance().createTexture(IOUtils.RES_FOLDER_PATH + "textures\\CastleWood_D.png");
		woodMat.setTexParam("mat_diffTexture", woodDiffTex);
		ITexture woodNormalTex = MaterialManager.getInstance().createTexture(IOUtils.RES_FOLDER_PATH + "textures\\CastleWood_N.png");
		woodMat.setTexParam("mat_normTexture", woodNormalTex);

		shieldMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicLightTexNorm.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicLightTexNorm.frag");
		ITexture shieldDiffTex = MaterialManager.getInstance().createTexture(IOUtils.RES_FOLDER_PATH + "textures\\Shield_D.png");
		shieldMat.setTexParam("mat_diffTexture", shieldDiffTex);
		ITexture shieldNormalTex = MaterialManager.getInstance().createTexture(IOUtils.RES_FOLDER_PATH + "textures\\Shield_N.png");
		shieldMat.setTexParam("mat_normTexture", shieldNormalTex);

		armorMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicLightTexNorm.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicLightTexNorm.frag");
		ITexture armorDiffTex = MaterialManager.getInstance().createTexture(IOUtils.RES_FOLDER_PATH + "textures\\Armor_D.png");
		armorMat.setTexParam("mat_diffTexture", armorDiffTex);

		whiteMirrorMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\mirrorTexPostEffect.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\mirrorTexPostEffect.frag");
		whiteMirrorMat.setVec3Param("mat_diffColor", 1f, 1f, 1f);
		whiteMirrorMat.setVec3Param("mat_filter", 1f, 1f, 1f);

		blackMirrorMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\mirrorTexPostEffect.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\mirrorTexPostEffect.frag");
		blackMirrorMat.setVec3Param("mat_diffColor", 0.1f, 0.1f, 0.1f);
		blackMirrorMat.setVec3Param("mat_filter", 1f, 1f, 1f);

		portalMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\portalPostEffect.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\portalPostEffect.frag");
		portalMat.setVec3Param("mat_filter", 0.8f, 0.8f, 0.8f);
	}
}
