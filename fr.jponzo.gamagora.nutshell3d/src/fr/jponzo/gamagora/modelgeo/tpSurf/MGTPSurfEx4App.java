package fr.jponzo.gamagora.modelgeo.tpSurf;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;

import javax.naming.OperationNotSupportedException;

import com.jogamp.opengl.awt.GLCanvas;

import fr.jponzo.gamagora.modelgeo.tp4.MutableMeshDef;
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

public class MGTPSurfEx4App {
	private static final String APP_NAME = "Nutshell3D App";
	private static int width = 800;
	private static int height = 600;
	private static float circleRadius = 1;
	private static int circleDisc = 10;
	private static MutableMeshDef surfDef;

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

		createCurves(rootEntity);
		createRoom(rootEntity);
	}

	private static void createCurves(IEntity rootEntity) throws OperationNotSupportedException {
		ITransform transform;

		IEntity curveEntity = new Entity();
		transform = new Transform(curveEntity);
		ICurve curve = new CurveBezier(curveEntity);
		curve.getControlPts().add(new Vec3(-2f, -2f, 0f));
		curve.getControlPts().add(new Vec3(-1f, 1f, 0f));
		curve.getControlPts().add(new Vec3(1f, 1f, 1f));
		curve.getControlPts().add(new Vec3(2f, -2f, 5f));
		curve.setDiscrtisation(10);
		curve.updateFromControl();
		IMaterial ptsMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.frag");
		ptsMat.setVec3Param("mat_color", 0.8f, 0.2f, 0.2f);
		IMaterial ctrlMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicColor.frag");
		ctrlMat.setVec3Param("mat_color", 0.2f, 0.8f, 0.2f);
		curve.setPointsMaterial(ptsMat);
		curve.setControlMaterial(ctrlMat);
		rootEntity.addChild(curveEntity);
		new AbstractUpdator(curveEntity) {
			public int selectedPt = 0;
			private ICurve curve;
			private float offset = 0.1f;

			@Override
			public void update(long deltaTime) {
				Vec3 controlPt = curve.getControlPts().get(selectedPt);

				//FWD
				if (InputManager.getInstance().getKeyDown(KeyCode.I)) {
					Vec3 newControlePt = new Vec3(controlPt.getX(), controlPt.getY(), controlPt.getZ() + offset);
					curve.moveControlPoint(selectedPt, newControlePt);
					curve.updateFromControl();
					updateSurf(surfDef, curve);
				}
				//BCK
				if (InputManager.getInstance().getKeyDown(KeyCode.K)) {
					Vec3 newControlePt = new Vec3(controlPt.getX(), controlPt.getY(), controlPt.getZ() - offset);
					curve.moveControlPoint(selectedPt, newControlePt);
					curve.updateFromControl();
					updateSurf(surfDef, curve);
				}
				//LEFT
				if (InputManager.getInstance().getKeyDown(KeyCode.J)) {
					Vec3 newControlePt = new Vec3(controlPt.getX() - offset, controlPt.getY(), controlPt.getZ());
					curve.moveControlPoint(selectedPt, newControlePt);
					curve.updateFromControl();
					updateSurf(surfDef, curve);
				}
				//RIGHT
				if (InputManager.getInstance().getKeyDown(KeyCode.L)) {
					Vec3 newControlePt = new Vec3(controlPt.getX() + offset, controlPt.getY(), controlPt.getZ());
					curve.moveControlPoint(selectedPt, newControlePt);
					curve.updateFromControl();
					updateSurf(surfDef, curve);
				}
				//UP
				if (InputManager.getInstance().getKeyDown(KeyCode.P)) {
					Vec3 newControlePt = new Vec3(controlPt.getX(), controlPt.getY() + offset, controlPt.getZ());
					curve.moveControlPoint(selectedPt, newControlePt);
					curve.updateFromControl();
					updateSurf(surfDef, curve);
				}
				//DOWN
				if (InputManager.getInstance().getKeyDown(KeyCode.M)) {
					Vec3 newControlePt = new Vec3(controlPt.getX(), controlPt.getY() - offset, controlPt.getZ());
					curve.moveControlPoint(selectedPt, newControlePt);
					curve.updateFromControl();
					updateSurf(surfDef, curve);
				}

				//Select Ptc
				if (InputManager.getInstance().getKeyDown(KeyCode.W)) {
					if (selectedPt < curve.getControlPts().size() - 1) {
						selectedPt += 1;
					}
				}
				if (InputManager.getInstance().getKeyDown(KeyCode.X)) {
					if (selectedPt > 0) {
						selectedPt -= 1;
					}
				}

				//Disc
				if (InputManager.getInstance().getKeyDown(KeyCode.PageUp)) {
					curve.setDiscrtisation(curve.getDiscrtisation() + 1);
					curve.updateFromControl();
					updateSurf(surfDef, curve);
				}
				if (InputManager.getInstance().getKeyDown(KeyCode.PageDown)) {
					if (curve.getDiscrtisation() > 0) {
						curve.setDiscrtisation(curve.getDiscrtisation() - 1);
						curve.updateFromControl();
						updateSurf(surfDef, curve);
					}
				}
			}

			@Override
			public void init() {
				curve = entity.getCurves().get(0);
			}
		};

		IEntity surfaceEntity = new Entity();
		transform = new Transform(surfaceEntity);
		surfDef = new MutableMeshDef();
		createSurfFromCurvePattern(surfDef, curve);
		IMesh surfMesh = new Mesh(surfaceEntity, surfDef);
		IMaterial meshMat = MaterialManager.getInstance().createMaterial(
				IOUtils.RES_FOLDER_PATH + "shaders\\basicLight.vert", 
				IOUtils.RES_FOLDER_PATH + "shaders\\basicLight.frag");
		meshMat.setVec3Param("mat_diffuseColor", 0.5f, 0.5f, 0.5f);
		surfMesh.setMaterial(meshMat);
		rootEntity.addChild(surfaceEntity);
		new AbstractUpdator(surfaceEntity) {
			MutableMeshDef surfDef;

			@Override
			public void update(long deltaTime) {
				//Disc
				if (InputManager.getInstance().getKeyDown(KeyCode.Numpad1)) {
					circleDisc++;
					updateSurf(surfDef, curve);
				}
				if (InputManager.getInstance().getKeyDown(KeyCode.Numpad0)) {
					if (circleDisc > 0) {
						circleDisc--;
						updateSurf(surfDef, curve);
					}
				}
			}

			@Override
			public void init() {
				surfDef = (MutableMeshDef) entity.getMeshes().get(0).getMeshDef();
			}
		};
	}

	private static void updateSurf(MutableMeshDef surfDef, ICurve curve) {
		createSurfFromCurvePattern(surfDef, curve);
	}


	private static float[][] createCircle(float radius, int disc, Vec3 center, Vec3 tan) {
		float[][] circle = new float[disc + 1][3];
		Random r = new Random();
		Vec3 up = new Vec3(0f, 1f, 0f);
		Vec3 a = up.cross(tan).getUnitVector();
		Vec3 b = tan.cross(a).getUnitVector();
		
		float delta = (float) ((2 * Math.PI) / (float) disc);
		float teta = 0;
		for (int i = 0; i < disc; i++) {
			float x = center.getX() + (float) (radius * Math.cos(teta) * a.getX()) + (float) (radius * Math.sin(teta) * b.getX());
			float y = center.getY() + (float) (radius * Math.cos(teta) * a.getY()) + (float) (radius * Math.sin(teta) * b.getY());
			float z = center.getZ() + (float) (radius * Math.cos(teta) * a.getZ()) + (float) (radius * Math.sin(teta) * b.getZ());

			circle[i][0] = x;
			circle[i][1] = y;
			circle[i][2] = z;

			teta += delta;
		}

		circle[disc][0] = circle[0][0];
		circle[disc][1] = circle[0][1];
		circle[disc][2] = circle[0][2];

		return circle;
	}

	private static float[][] createCircle(float radius, int disc) {
		float[][] circle = new float[disc + 1][3];

		float delta = (float) ((2 * Math.PI) / (float) disc);
		float teta = 0;
		for (int i = 0; i < disc; i++) {
			float x = (float) (radius * Math.cos(teta));
			float y = (float) (radius * Math.sin(teta));

			circle[i][0] = x;
			circle[i][1] = y;
			circle[i][2] = 0;

			teta += delta;
		}

		circle[disc][0] = circle[0][0];
		circle[disc][1] = circle[0][1];
		circle[disc][2] = circle[0][2];

		return circle;
	}

	private static void createSurfFromCurvePattern(MutableMeshDef surfDef, ICurve curve) {	
		surfDef.clear();

		for (int i = 0; i < curve.getPtsTable().length - 2; i++) {
			float[] u1 = curve.getPtsTable()[i];
			float[] u2 = curve.getPtsTable()[i + 1];
			float[] u3 = curve.getPtsTable()[i + 2];
			Vec3 U1 = new Vec3(u1[0], u1[1], u1[2]);
			Vec3 U2 = new Vec3(u2[0], u2[1], u2[2]);
			Vec3 U3 = new Vec3(u3[0], u3[1], u3[2]);
			Vec3 tan1 = U2.subtract(U1).getUnitVector();
			Vec3 tan2 = U3.subtract(U2).getUnitVector();

			float[][] pattern1 = createCircle(circleRadius, circleDisc, U1, tan1);
			float[][] pattern2 = createCircle(circleRadius, circleDisc, U2, tan2);
			for(int j = 0; j < pattern1.length - 1; j++) {
				float[] c11 = pattern1[j];
				float[] c12 = pattern1[j + 1];
				float[] c21 = pattern2[j];
				float[] c22 = pattern2[j + 1];



				Vec3 C11 = new Vec3(c11[0], c11[1], c11[2]);
				Vec3 C12 = new Vec3(c12[0], c12[1], c12[2]);
				Vec3 C21 = new Vec3(c21[0], c21[1], c21[2]);
				Vec3 C22 = new Vec3(c22[0], c22[1], c22[2]);

				float[] v2 = C11.getArray();
				float[] v1 = C12.getArray();
				float[] v4 = C22.getArray();
				float[] v3 = C21.getArray();

				surfDef.addFace(v1, v2, v3, false, false);
				surfDef.addFace(v1, v3, v4, false, false);
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
