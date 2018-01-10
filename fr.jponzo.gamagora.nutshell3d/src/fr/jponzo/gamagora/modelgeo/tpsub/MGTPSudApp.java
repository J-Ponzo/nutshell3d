package fr.jponzo.gamagora.modelgeo.tpsub;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.naming.OperationNotSupportedException;

import com.jogamp.opengl.awt.GLCanvas;

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

public class MGTPSudApp {
	private static final String APP_NAME = "Nutshell3D App";
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
		transform.setLocalTranslate(Matrices.translation(0f, -2f, -7f));
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
		transform.setLocalTranslate(Matrices.translation(2f, 2f, -2f));
		ILight light = new Light(lightEntity);
		light.setAlbedo(
				new Color(255, 255, 255, 255)
				);
		light.setIntensity(10f);
		rootEntity.addChild(lightEntity);

		createCurves(rootEntity);
	}

	private static void createCurves(IEntity rootEntity) throws OperationNotSupportedException {
		ITransform transform;

		IEntity curveEntity = new Entity();
		transform = new Transform(curveEntity);
		ICurve curve = new ChaikinCurve(curveEntity);
		curve.getControlPts().add(new Vec3(-2f, -2f, 0f));
		curve.getControlPts().add(new Vec3(-1f, 1f, 0f));
		curve.getControlPts().add(new Vec3(0f, 0.5f, 0f));
		curve.getControlPts().add(new Vec3(1f, 1f, 0f));
		curve.getControlPts().add(new Vec3(2f, -2f, 0f));
		curve.getControlPts().add(new Vec3(2f, -3f, 0f));
		curve.getControlPts().add(new Vec3(-2f, -5f, 0f));
		curve.getControlPts().add(new Vec3(-1f, -5f, 0f));
		curve.getControlPts().add(new Vec3(2f, -0f, 0f));
		curve.getControlPts().add(new Vec3(0f, -2f, 0f));
		
		curve.setDiscrtisation(0);
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
				}
				//BCK
				if (InputManager.getInstance().getKeyDown(KeyCode.K)) {
					Vec3 newControlePt = new Vec3(controlPt.getX(), controlPt.getY(), controlPt.getZ() - offset);
					curve.moveControlPoint(selectedPt, newControlePt);
					curve.updateFromControl();
				}
				//LEFT
				if (InputManager.getInstance().getKeyDown(KeyCode.J)) {
					Vec3 newControlePt = new Vec3(controlPt.getX() - offset, controlPt.getY(), controlPt.getZ());
					curve.moveControlPoint(selectedPt, newControlePt);
					curve.updateFromControl();
				}
				//RIGHT
				if (InputManager.getInstance().getKeyDown(KeyCode.L)) {
					Vec3 newControlePt = new Vec3(controlPt.getX() + offset, controlPt.getY(), controlPt.getZ());
					curve.moveControlPoint(selectedPt, newControlePt);
					curve.updateFromControl();
				}
				//UP
				if (InputManager.getInstance().getKeyDown(KeyCode.P)) {
					Vec3 newControlePt = new Vec3(controlPt.getX(), controlPt.getY() + offset, controlPt.getZ());
					curve.moveControlPoint(selectedPt, newControlePt);
					curve.updateFromControl();
				}
				//DOWN
				if (InputManager.getInstance().getKeyDown(KeyCode.M)) {
					Vec3 newControlePt = new Vec3(controlPt.getX(), controlPt.getY() - offset, controlPt.getZ());
					curve.moveControlPoint(selectedPt, newControlePt);
					curve.updateFromControl();
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
				}
				if (InputManager.getInstance().getKeyDown(KeyCode.PageDown)) {
					if (curve.getDiscrtisation() > 0) {
						curve.setDiscrtisation(curve.getDiscrtisation() - 1);
						curve.updateFromControl();
					}
				}
			}

			@Override
			public void init() {
				curve = entity.getCurves().get(0);
			}
		};
	}
}