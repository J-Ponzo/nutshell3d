package fr.jponzo.gamagora.modelgeo.tpVol;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.naming.OperationNotSupportedException;

import com.jogamp.opengl.awt.GLCanvas;

import fr.jponzo.gamagora.modelgeo.MutableMeshDef;
import fr.jponzo.gamagora.modelgeo.tp5.CurveBezier;
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

public class MGTPVolEx1App {
	private static final String APP_NAME = "Nutshell3D App";
	private static int width = 800;
	private static int height = 600;
	private static CurveBezier curve1;
	private static CurveBezier curve2;
	private static CurveBezier curCurvEdit;

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
		camera.setNear(1);
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

		//Create Light
		IEntity lightEntity = new Entity();
		transform = new Transform(lightEntity);
		transform.setLocalTranslate(Matrices.translation(0f, 0f, 0f));
		ILight light = new Light(lightEntity);
		light.setAlbedo(
				new Color(255, 255, 255, 255)
				);
		light.setIntensity(5f);
		cameraEntity.addChild(lightEntity);

		createVoxels(rootEntity);
		createRoom(rootEntity);
	}

	private static void createVoxels(IEntity rootEntity) throws OperationNotSupportedException {
		
	}

	private static void createSurfFromCurves(MutableMeshDef surfDef, ICurve c2, ICurve c1) {	
		surfDef.clear();
		Vec3 c10 = new Vec3(c1.getPtsTable()[0][0], c1.getPtsTable()[0][1], c1.getPtsTable()[0][2]);
		for (int i = 0; i < c1.getPtsTable().length - 1; i++) {
			for (int j = 0; j < c2.getPtsTable().length - 1; j++) {
				float[] u1 = c1.getPtsTable()[i];
				float[] u2 = c1.getPtsTable()[i + 1];
				float[] v1 = c2.getPtsTable()[j];
				float[] v2 = c2.getPtsTable()[j + 1];
				
				Vec3 U1 = new Vec3(u1[0], u1[1], u1[2]);
				Vec3 U2 = new Vec3(u2[0], u2[1], u2[2]);
				Vec3 V1 = new Vec3(v1[0], v1[1], v1[2]);
				Vec3 V2 = new Vec3(v2[0], v2[1], v2[2]);
				
				Vec3 A = U1.add(V1).subtract(c10);
				Vec3 B = U1.add(V2).subtract(c10);
				Vec3 C = U2.add(V2).subtract(c10);
				Vec3 D = U2.add(V1).subtract(c10);
				
				float[] a = A.getArray();
				float[] b = B.getArray();
				float[] c = C.getArray();
				float[] d = D.getArray();
				
				surfDef.addFace(a, b, c, false, false);
				surfDef.addFace(a, c, d, false, false);
			}
		}
	}

	private static void createRoom(IEntity rootEntity) throws OperationNotSupportedException {
		ITransform transform;

		//Create Room Pivot
		IEntity roomrEntity = new Entity();
		transform = new Transform(roomrEntity);
		transform.setLocalTranslate(Matrices.translation(-1f, 1f, 1f));
		transform.setLocalRotate(Matrices.yRotation((float) (Math.PI / 4)));
		rootEntity.addChild(roomrEntity);

		//Create Floor
		IEntity floorEntity = new Entity();
		transform = new Transform(floorEntity);
		transform.setLocalTranslate(Matrices.translation(0f, -5f, 0f));
		transform.setLocalRotate(Matrices.xRotation((float) (Math.PI / 2)));
		transform.setLocalScale(Matrices.scale(10f, 10f, 1f));
		IMeshDef wallMeshDef = new MeshDef();
		wallMeshDef.setPath(IOUtils.RES_FOLDER_PATH + "meshes\\square.mesh.csv");
		wallMeshDef.load();
		IMesh floorMesh = new Mesh(floorEntity, wallMeshDef);
		roomrEntity.addChild(floorEntity);
		IMaterial floorMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.frag");
		floorMat.setVec3Param("mat_color", 0.8f, 0.8f, 0.2f);
		floorMesh.setMaterial(floorMat);

		//Create roof
		IEntity roofEntity = new Entity();
		transform = new Transform(roofEntity);
		transform.setLocalTranslate(Matrices.translation(0f, 5f, 0f));
		transform.setLocalRotate(Matrices.xRotation((float) (Math.PI / 2)));
		transform.setLocalScale(Matrices.scale(10f, 10f, 1f));
		IMesh roofMesh = new Mesh(roofEntity, wallMeshDef);
		roomrEntity.addChild(roofEntity);
		IMaterial roofMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.frag");
		roofMat.setVec3Param("mat_color", 0.2f, 0.8f, 0.8f);
		roofMesh.setMaterial(roofMat);

		//Create front wall
		IEntity fWallEntity = new Entity();
		transform = new Transform(fWallEntity);
		transform.setLocalTranslate(Matrices.translation(0f, 0f, 5f));
		transform.setLocalScale(Matrices.scale(10f, 10f, 1f));
		IMesh fWallMesh = new Mesh(fWallEntity, wallMeshDef);
		roomrEntity.addChild(fWallEntity);
		IMaterial fWallMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.frag");
		fWallMat.setVec3Param("mat_color", 0.8f, 0.2f, 0.8f);
		fWallMesh.setMaterial(fWallMat);

		//Create back wall
		IEntity bWallEntity = new Entity();
		transform = new Transform(bWallEntity);
		transform.setLocalTranslate(Matrices.translation(0f, 0f, -5f));
		transform.setLocalScale(Matrices.scale(10f, 10f, 1f));
		IMesh bWallMesh = new Mesh(bWallEntity, wallMeshDef);
		roomrEntity.addChild(bWallEntity);
		IMaterial bWallMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.frag");
		bWallMat.setVec3Param("mat_color", 0.8f, 0.2f, 0.2f);
		bWallMesh.setMaterial(bWallMat);

		//Create left wall
		IEntity lWallEntity = new Entity();
		transform = new Transform(lWallEntity);
		transform.setLocalTranslate(Matrices.translation(5f, 0f, 0f));
		transform.setLocalRotate(Matrices.yRotation((float) (Math.PI / 2)));
		transform.setLocalScale(Matrices.scale(10f, 10f, 1f));
		IMesh lWallMesh = new Mesh(lWallEntity, wallMeshDef);
		roomrEntity.addChild(lWallEntity);
		IMaterial lWallMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.frag");
		lWallMat.setVec3Param("mat_color", 0.2f, 0.8f, 0.2f);
		lWallMesh.setMaterial(lWallMat);

		//Create right wall
		IEntity rWallEntity = new Entity();
		transform = new Transform(rWallEntity);
		transform.setLocalTranslate(Matrices.translation(-5f, 0f, 0f));
		transform.setLocalRotate(Matrices.yRotation((float) (Math.PI / 2)));
		transform.setLocalScale(Matrices.scale(10f, 10f, 1f));
		IMesh rWallMesh = new Mesh(rWallEntity, wallMeshDef);
		roomrEntity.addChild(rWallEntity);
		IMaterial rWallMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.frag");
		rWallMat.setVec3Param("mat_color", 0.2f, 0.2f, 0.8f);
		rWallMesh.setMaterial(rWallMat);
	}
}
