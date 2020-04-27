/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This file is part of the LX Studio software library. By using
 * LX, you agree to the terms of the LX Studio Software License
 * and Distribution Agreement, available at: http://lx.studio/license
 *
 * Please note that the LX license is not open-source. The license
 * allows for free, non-commercial use.
 *
 * HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR
 * OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF
 * MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR
 * PURPOSE, WITH RESPECT TO THE SOFTWARE.
 *
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.p3lx.ui;

import com.google.gson.JsonObject;

import heronarts.lx.LX;
import heronarts.lx.LXSerializable;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.Click;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.modulator.LXPeriodicModulator;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.MutableParameter;
import heronarts.lx.parameter.ObjectParameter;
import heronarts.p3lx.ui.component.UIInputBox;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * This is a layer that contains a 3d scene with a camera. Mouse movements
 * control the camera, and the scene can contain components.
 */
public class UI3dContext extends UIObject implements LXSerializable, UITabFocus {

  public static final int NUM_CAMERA_POSITIONS = 6;

  /**
   * Mode of interaction from keyboard mouse events
   */
  public enum InteractionMode {
    /**
     * Camera has a fixed center point, eye rotates around this point and zooms on it
     */
    ZOOM,

    /**
     * Camera has a fixed radius, eye moves around like a FPS video-game
     */
    MOVE
  };

  public enum ProjectionMode {
    /**
     * Perspective projection
     */
    PERSPECTIVE,

    /**
     * Orthographic projection
     */
    ORTHOGRAPHIC;

    @Override
    public String toString() {
      switch (this) {
      case ORTHOGRAPHIC:
        return "Orthographic";
      case PERSPECTIVE:
      default:
        return "Perspective";
      }
    }
  };

  /**
   * Camera motion mode
   */
  private EnumParameter<InteractionMode> interactionMode =
    new EnumParameter<InteractionMode>("Mode", InteractionMode.ZOOM)
    .setDescription("Camera interaction mode");

  /**
   * Projection mode
   */
  public final EnumParameter<ProjectionMode> projection =
    new EnumParameter<ProjectionMode>("Projection", ProjectionMode.PERSPECTIVE)
    .setDescription("Projection mode");

  /**
   * Perspective of view
   */
  public final BoundedParameter perspective = (BoundedParameter)
    new BoundedParameter("Perspective", 60, 15, 150)
    .setExponent(2)
    .setDescription("Camera perspective factor");

  /**
   * Depth of perspective field, exponential factor of radius by exp(10, Depth)
   */
  public final BoundedParameter depth =
    new BoundedParameter("Depth", 1, 0, 4)
    .setDescription("Camera's depth of perspective field");

  /**
   * Phi lock prevents crazy vertical rotations
   */
  public final BooleanParameter phiLock =
    new BooleanParameter("PhiLock", true)
    .setDescription("Locks phi to reasonable bounds");

  public final BooleanParameter showCenterPoint =
    new BooleanParameter("ShowCenter", false)
    .setDescription("Shows the center point of the scene");

  /**
   * Whether to animate between camera positions
   */
  public final BooleanParameter animation =
    new BooleanParameter("Animation", false)
    .setDescription("Whether animation between camera positions is enabled");

  /**
   * Animation time
   */
  public final BoundedParameter animationTime = (BoundedParameter)
    new BoundedParameter("Animation Time", 1000, 100, 300000)
    .setExponent(2)
    .setUnits(LXParameter.Units.MILLISECONDS)
    .setDescription("Animation duration between camera positions");

  /**
   * Max velocity used to damp changes to radius (zoom)
   */
  public final MutableParameter cameraVelocity = new MutableParameter("CVel", Float.MAX_VALUE);

  /**
   * Acceleration used to change camera radius (zoom)
   */
  public final MutableParameter cameraAcceleration = new MutableParameter("CAcl", 0);

  /**
   * Max velocity used to damp changes to rotation (theta/phi)
   */
  public final MutableParameter rotationVelocity = new MutableParameter("RVel", 4*Math.PI);

  /**
   * Acceleration used to change rotation (theta/phi)
   */
  public final MutableParameter rotationAcceleration = new MutableParameter("RAcl", 0);

  public class Camera implements LXSerializable {

    public final BooleanParameter active = new BooleanParameter("Active", false);

    public final MutableParameter theta = new MutableParameter("Theta", 0);
    public final MutableParameter phi = new MutableParameter("Phi", 0);
    public final MutableParameter radius = new MutableParameter("Radius", 120);

    private final MutableParameter x = new MutableParameter();
    private final MutableParameter y = new MutableParameter();
    private final MutableParameter z = new MutableParameter();

    private Camera() {}

    private void set(Camera that) {
      set(that, true);
    }

    private void set(Camera that, boolean active) {
      this.theta.setValue(that.theta.getValue());
      this.phi.setValue(that.phi.getValue());
      this.radius.setValue(that.radius.getValue());
      this.x.setValue(that.x.getValue());
      this.y.setValue(that.y.getValue());
      this.z.setValue(that.z.getValue());
      if (active) {
        this.active.setValue(true);
      }
    }

    private void lerp(Camera one, Camera two, double amt) {
      this.theta.setValue(LXUtils.lerp(one.theta.getValue(), two.theta.getValue(), amt));
      this.phi.setValue(LXUtils.lerp(one.phi.getValue(), two.phi.getValue(), amt));
      this.radius.setValue(LXUtils.lerp(one.radius.getValue(), two.radius.getValue(), amt));
      this.x.setValue(LXUtils.lerp(one.x.getValue(), two.x.getValue(), amt));
      this.y.setValue(LXUtils.lerp(one.y.getValue(), two.y.getValue(), amt));
      this.z.setValue(LXUtils.lerp(one.z.getValue(), two.z.getValue(), amt));
    }

    private static final String KEY_ACTIVE = "active";
    private static final String KEY_RADIUS = "radius";
    private static final String KEY_THETA = "theta";
    private static final String KEY_PHI = "phi";
    private static final String KEY_X = "x";
    private static final String KEY_Y = "y";
    private static final String KEY_Z = "z";

    @Override
    public void save(LX lx, JsonObject object) {
      object.addProperty(KEY_ACTIVE, this.active.isOn());
      object.addProperty(KEY_RADIUS, this.radius.getValue());
      object.addProperty(KEY_THETA, this.theta.getValue());
      object.addProperty(KEY_PHI, this.phi.getValue());
      object.addProperty(KEY_X, this.x.getValue());
      object.addProperty(KEY_Y, this.y.getValue());
      object.addProperty(KEY_Z, this.z.getValue());
    }

    @Override
    public void load(LX lx, JsonObject object) {
      LXSerializable.Utils.loadBoolean(this.active, object, KEY_ACTIVE);
      LXSerializable.Utils.loadDouble(this.radius, object, KEY_RADIUS);
      LXSerializable.Utils.loadDouble(this.theta, object, KEY_THETA);
      LXSerializable.Utils.loadDouble(this.phi, object, KEY_PHI);
      LXSerializable.Utils.loadDouble(this.x, object, KEY_X);
      LXSerializable.Utils.loadDouble(this.y, object, KEY_Y);
      LXSerializable.Utils.loadDouble(this.z, object, KEY_Z);
    }
  }

  public final Camera[] cue = new Camera[NUM_CAMERA_POSITIONS];

  public final ObjectParameter<Camera> focusCamera;

  private Camera camera = new Camera();

  private Camera prevCamera = null;
  private Camera cameraFrom = new Camera();
  private Camera cameraTo = new Camera();

  private final LXPeriodicModulator animating = new Click(this.animationTime).setLooping(false);

  public final UIInputBox.ProgressIndicator animationProgress = new UIInputBox.ProgressIndicator() {

    @Override
    public boolean hasProgress() {
      return animating.isRunning();
    }

    @Override
    public double getProgress() {
      return animating.getBasis();
    }

  };

  private final DampedParameter thetaDamped =
    new DampedParameter(this.camera.theta, this.rotationVelocity, this.rotationAcceleration);

  private final DampedParameter phiDamped =
    new DampedParameter(this.camera.phi, this.rotationVelocity, this.rotationAcceleration);

  private final DampedParameter radiusDamped =
    new DampedParameter(this.camera.radius, this.cameraVelocity, this.cameraAcceleration);

  private final DampedParameter xDamped = new DampedParameter(
    this.camera.x, this.cameraVelocity, this.cameraAcceleration
  );

  private final DampedParameter yDamped = new DampedParameter(
    this.camera.y, this.cameraVelocity, this.cameraAcceleration
  );

  private final DampedParameter zDamped = new DampedParameter(
    this.camera.z, this.cameraVelocity, this.cameraAcceleration
  );

  // These are derived from positionX based upon the camera mode
  private final PVector center = new PVector(0, 0, 0);
  private final PVector eye = new PVector(0, 0, 0);
  private final PVector centerDamped = new PVector(0, 0, 0);
  private final PVector eyeDamped = new PVector(0, 0, 0);

  // Radius bounds
  private float minRadius = 1, maxRadius = Float.MAX_VALUE;

  private static final float MAX_PHI = PConstants.HALF_PI * .9f;

  private final int x;
  private final int y;
  private PGraphics pg;

  public UI3dContext(UI ui) {
    this(ui, null, 0, 0);
  }

  public UI3dContext(UI ui, int x, int y, int w, int h) {
    this(ui, ui.applet.createGraphics(w, h, PConstants.P3D), x, y);
  }

  protected UI3dContext(UI ui, PGraphics pg, int x, int y) {
    setUI(ui);
    this.pg = pg;
    this.x = x;
    this.y = y;

    for (int i = 0; i < this.cue.length; ++i) {
      this.cue[i] = new Camera();
    }

    this.focusCamera = new ObjectParameter<Camera>("Camera", this.cue);
    this.focusCamera.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter p) {
        Camera selectCamera = focusCamera.getObject();
        if (!selectCamera.active.isOn()) {
          // Store state into the camera
          selectCamera.set(camera);
        } else {
          if (animation.isOn() && (selectCamera != prevCamera)) {
            // Trigger animation from current camera to the next
            cameraFrom.set(camera);
            cameraTo.set(selectCamera);
            animating.trigger();
          } else {
            // Immediately update all camera state
            animating.stop();
            camera.set(selectCamera);
            thetaDamped.setValue(camera.theta.getValue());
            phiDamped.setValue(camera.phi.getValue());
            radiusDamped.setValue(camera.radius.getValue());
            xDamped.setValue(camera.x.getValue());
            yDamped.setValue(camera.y.getValue());
            zDamped.setValue(camera.z.getValue());
            computeCamera(true);
          }
        }
        prevCamera = selectCamera;
      }
    });

    addLoopTask(this.animating);
    addLoopTask(this.thetaDamped);
    addLoopTask(this.phiDamped);
    addLoopTask(this.radiusDamped);
    addLoopTask(this.xDamped);
    addLoopTask(this.yDamped);
    addLoopTask(this.zDamped);

    this.thetaDamped.start();
    this.radiusDamped.start();
    this.phiDamped.start();
    this.xDamped.start();
    this.yDamped.start();
    this.zDamped.start();

    computeCamera(true);

    this.camera.radius.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter p) {
        double value = camera.radius.getValue();
        if (value < minRadius || value > maxRadius) {
          camera.radius.setValue(LXUtils.constrain(value, minRadius, maxRadius));
        }
      }
    });

    this.camera.phi.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter p) {
        if (phiLock.isOn()) {
          double value = camera.phi.getValue();
          if (value < -MAX_PHI || value > MAX_PHI) {
            camera.phi.setValue(LXUtils.constrain(value, -MAX_PHI, MAX_PHI));
          }
        }
      }
    });

    this.interactionMode.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter p) {
        PVector pos = center;
        switch (interactionMode.getEnum()) {
        case ZOOM:
          pos = center;
          break;
        case MOVE:
          pos = eye;
          break;
        }
        camera.x.setValue(pos.x);
        camera.y.setValue(pos.y);
        camera.z.setValue(pos.z);
        xDamped.setValue(pos.x);
        yDamped.setValue(pos.y);
        zDamped.setValue(pos.z);
      }
    });
  }

  public PGraphics getGraphics() {
    return this.pg;
  }

  @Override
  public float getWidth() {
    return (this.pg == null) ? this.ui.applet.width : this.pg.width;
  }

  @Override
  public float getHeight() {
    return (this.pg == null) ? this.ui.applet.height : this.pg.height;
  }

  public UI3dContext setSize(float width, float height) {
    if (this.pg == null) {
      throw new UnsupportedOperationException("Cannot resize UI3dContext created with no size.");
    } else {
      this.pg.dispose();
      this.pg = this.ui.applet.createGraphics((int) width, (int) height, PConstants.P3D);
      onResize();
    }
    return this;
  }

  protected void onResize() {}


  /**
   * Adds a component to the layer
   *
   * @param component Component
   * @return this
   */
  public final UI3dContext addComponent(UI3dComponent component) {
    this.mutableChildren.add(component);
    return this;
  }

  /**
   * Removes a component from the layer
   *
   * @param component Component
   * @return this
   */
  public final UI3dContext removeComponent(UI3dComponent component) {
    this.mutableChildren.remove(component);
    return this;
  }

  /**
   * Clears the camera stored at the given index
   *
   * @param index Camera index to clear
   * @return this
   */
  public UI3dContext clearCamera(int index) {
    this.cue[index].active.setValue(false);
    return this;
  }

  /**
   * Sets the cue position index of the camera
   *
   * @param index Camera index
   * @return this
   */
  public UI3dContext setCamera(int index) {
    if (this.focusCamera.getValuei() != index) {
      this.focusCamera.setValue(index);
    } else {
      this.focusCamera.bang();
    }
    return this;
  }

  /**
   * Set radius of the camera
   *
   * @param radius Camera radius
   * @return this
   */
  public UI3dContext setRadius(float radius) {
    this.camera.radius.setValue(radius);
    return this;
  }

  /**
   * Set interaction mode for mouse/key events.
   *
   * @param interactionMode Interaction mode
   * @return this
   */
  public UI3dContext setInteractionMode(InteractionMode interactionMode) {
    this.interactionMode.setValue(interactionMode);
    return this;
  }

  /**
   * Sets perspective angle of the camera in degrees
   *
   * @param perspective Angle in degrees
   * @return this
   */
  public UI3dContext setPerspective(float perspective) {
    this.perspective.setValue(perspective);
    return this;
  }

  /**
   * Sets the camera's maximum zoom speed
   *
   * @param cameraVelocity Max units/per second radius may change by
   * @return this
   */
  public UI3dContext setCameraVelocity(float cameraVelocity) {
    this.cameraVelocity.setValue(cameraVelocity);
    return this;
  }

  /**
   * Set's the camera's zoom acceleration, 0 is infinite
   *
   * @param cameraAcceleration Acceleration for camera
   * @return this
   */
  public UI3dContext setCameraAcceleration(float cameraAcceleration) {
    this.cameraAcceleration.setValue(cameraAcceleration);
    return this;
  }

  /**
   * Sets the camera's maximum rotation speed
   *
   * @param rotationVelocity Max radians/per second viewing angle may change by
   * @return this
   */
  public UI3dContext setRotationVelocity(float rotationVelocity) {
    this.rotationVelocity.setValue(rotationVelocity);
    return this;
  }

  /**
   * Set's the camera's rotational acceleration, 0 is infinite
   *
   * @param rotationAcceleration Acceleration of camera rotation
   * @return this
   */
  public UI3dContext setRotationAcceleration(float rotationAcceleration) {
    this.rotationAcceleration.setValue(rotationAcceleration);
    return this;
  }

  /**
   * Set the theta angle of viewing
   *
   * @param theta Angle about the y axis
   * @return this
   */
  public UI3dContext setTheta(double theta) {
    this.camera.theta.setValue(theta);
    return this;
  }

  /**
   * Set the phi angle of viewing
   *
   * @param phi Angle about the y axis
   * @return this
   */
  public UI3dContext setPhi(float phi) {
    this.camera.phi.setValue(phi);
    return this;
  }

  /**
   * Sets bounds on the radius
   *
   * @param minRadius Minimum camera radius
   * @param maxRadius Maximum camera radius
   * @return this
   */
  public UI3dContext setRadiusBounds(float minRadius, float maxRadius) {
    this.minRadius = minRadius;
    this.maxRadius = maxRadius;
    setRadius(LXUtils.constrainf(this.camera.radius.getValuef(), minRadius, maxRadius));
    return this;
  }

  /**
   * Set minimum radius
   *
   * @param minRadius Minimum camera radius
   * @return this
   */
  public UI3dContext setMinRadius(float minRadius) {
    return setRadiusBounds(minRadius, this.maxRadius);
  }

  /**
   * Set maximum radius
   *
   * @param maxRadius Maximum camera radius
   * @return this
   */
  public UI3dContext setMaxRadius(float maxRadius) {
    return setRadiusBounds(this.minRadius, maxRadius);
  }

  /**
   * Sets the center of the scene, only respected in ZOOM mode
   *
   * @param x X-coordinate
   * @param y Y-coordinate
   * @param z Z-coordinate
   * @return this
   */
  public UI3dContext setCenter(float x, float y, float z) {
    if (this.interactionMode.getEnum() != InteractionMode.ZOOM) {
      throw new IllegalStateException("setCenter() only allowed in ZOOM mode");
    }
    this.camera.x.setValue(this.center.x = x);
    this.camera.y.setValue(this.center.y = y);
    this.camera.z.setValue(this.center.z = z);
    return this;
  }

  /**
   * Sets the eye position, only respected in MOVE mode
   *
   * @param x X-coordinate
   * @param y Y-coordinate
   * @param z Z-coordinate
   * @return this
   */
  public UI3dContext setEye(float x, float y, float z) {
    if (this.interactionMode.getEnum() != InteractionMode.MOVE) {
      throw new IllegalStateException("setCenter() only allowed in MOVE mode");
    }
    this.camera.x.setValue(this.eye.x = x);
    this.camera.y.setValue(this.eye.y = y);
    this.camera.z.setValue(this.eye.z = z);
    return this;
  }

  /**
   * Gets the center position of the scene
   *
   * @return center of scene
   */
  public PVector getCenter() {
    return this.center;
  }

  /**
   * Gets the latest computed eye position
   *
   * @return eye position
   */
  public PVector getEye() {
    return this.eye;
  }

  private void computeCamera(boolean initialize) {
    if (this.animating.isRunning() || this.animating.finished()) {
      this.camera.lerp(this.cameraFrom, this.cameraTo, this.animating.getBasis());
    }

    float rv = this.radiusDamped.getValuef();
    double tv = this.thetaDamped.getValue();
    double pv = this.phiDamped.getValue();

    float sintheta = (float) Math.sin(tv);
    float costheta = (float) Math.cos(tv);
    float sinphi = (float) Math.sin(pv);
    float cosphi = (float) Math.cos(pv);

    float px = this.xDamped.getValuef();
    float py = this.yDamped.getValuef();
    float pz = this.zDamped.getValuef();

    switch (this.interactionMode.getEnum()) {
    case ZOOM:
      this.centerDamped.set(px, py, pz);
      if (initialize) {
        this.center.set(this.centerDamped);
      }
      this.eyeDamped.set(
        px + rv * cosphi * sintheta,
        py + rv * sinphi,
        pz - rv * cosphi * costheta
      );
      this.eye.set(this.eyeDamped);
      break;
    case MOVE:
      this.eyeDamped.set(px, py, pz);
      if (initialize) {
        this.eye.set(this.eyeDamped);
      }
      this.centerDamped.set(
        px + rv * cosphi * sintheta,
        py + rv * sinphi,
        pz + rv * cosphi * costheta
      );
      this.center.set(this.centerDamped);
      break;
    }
  }

  @Override
  public final void draw(UI ui, PGraphics dstPg) {
    if (!isVisible()) {
      return;
    }

    PGraphics pg = dstPg;
    if (this.pg != null) {
      pg = this.pg;
      pg.beginDraw();
      pg.clear();
    }

    // Set the camera
    computeCamera(false);
    pg.camera(
      this.eyeDamped.x, this.eyeDamped.y, this.eyeDamped.z,
      this.centerDamped.x, this.centerDamped.y, this.centerDamped.z,
      0, -1, 0
    );

    // Set perspective projection
    float radiusValue = this.radiusDamped.getValuef();
    if (this.projection.getEnum() == ProjectionMode.ORTHOGRAPHIC) {
      float halfRadiusWidth = radiusValue * .5f;
      float halfRadiusHeight = halfRadiusWidth * pg.height / pg.width;
      pg.ortho(-halfRadiusWidth, halfRadiusWidth, -halfRadiusHeight, halfRadiusHeight);
    } else {
      float depthFactor = (float) Math.pow(10, this.depth.getValue());
      pg.perspective(
        this.perspective.getValuef() / 180.f * PConstants.PI,
        pg.width / (float) pg.height,
        radiusValue / depthFactor,
        radiusValue * depthFactor
      );
    }

    if (ui.coordinateSystem == UI.CoordinateSystem.RIGHT_HANDED) {
      pg.scale(1, 1, -1);
    }

    // Enable depth test
    pg.hint(PConstants.ENABLE_DEPTH_TEST);

    // Draw all the components in the scene
    beginDraw(ui, pg);
    if (this.showCenterPoint.isOn()) {
      drawCenterDot(pg);
    }
    for (UIObject child : this.mutableChildren) {
      child.draw(ui, pg);
    }
    endDraw(ui, pg);

    // Reset the depth test, camera and perspective
    pg.hint(PConstants.DISABLE_DEPTH_TEST);
    pg.camera();
    pg.perspective();

    if (hasFocus()) {
      drawFocusBorder(ui, pg);
    }

    if (this.pg != null) {
      this.pg.endDraw();
      dstPg.image(this.pg, this.x, this.y);
    }
  }

  private void drawCenterDot(PGraphics pg) {
    pg.stroke(LXColor.RED);
    pg.strokeWeight(10);
    pg.beginShape(PConstants.POINTS);
    pg.vertex(this.xDamped.getValuef(), this.yDamped.getValuef(), this.zDamped.getValuef());
    pg.endShape();
    pg.strokeWeight(1);
  }

  private void drawFocusBorder(UI ui, PGraphics pg) {
    pg.strokeWeight(1);
    pg.stroke(ui.theme.getFocusColor());
    float focusInset = .5f;
    int focusDash = 10;
    // Top left
    pg.line(focusInset, focusInset, focusInset + focusDash, focusInset);
    pg.line(focusInset, focusInset, focusInset, focusInset + focusDash);
    // Top right
    pg.line(pg.width - focusInset, focusInset, pg.width - focusInset - focusDash, focusInset);
    pg.line(pg.width - focusInset, focusInset, pg.width - focusInset, focusInset + focusDash);
    // Bottom left
    pg.line(focusInset, pg.height - focusInset, focusInset + focusDash, pg.height - focusInset);
    pg.line(focusInset, pg.height - focusInset, focusInset, pg.height - focusInset - focusDash);
    // Bottom right
    pg.line(pg.width - focusInset, pg.height - focusInset, pg.width - focusInset - focusDash, pg.height - focusInset);
    pg.line(pg.width - focusInset, pg.height - focusInset, pg.width - focusInset, pg.height - focusInset - focusDash);
  }

  @Override
  protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
    if (mouseEvent.getCount() > 1) {
      focus(mouseEvent);
    }
  }

  private void updateFocusedCamera() {
    this.focusCamera.getObject().set(this.camera, false);
    this.animating.stop();
  }

  @Override
  protected void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
    switch (this.interactionMode.getEnum()) {
    case ZOOM:
      if (mouseEvent.isShiftDown()) {
        float tanPerspective = (float) Math.tan(.5 * this.perspective.getValue() * Math.PI / 180.);
        this.camera.radius.incrementValue(this.camera.radius.getValue() * dy * 2.f / getHeight() * tanPerspective);
      } else if (mouseEvent.isMetaDown() || mouseEvent.isControlDown()) {
        float tanPerspective = (float) Math.tan(.5 * this.perspective.getValue() * Math.PI / 180.);
        float sinTheta = (float) Math.sin(this.thetaDamped.getValue());
        float cosTheta = (float) Math.cos(this.thetaDamped.getValue());
        float sinPhi = (float) Math.sin(this.phiDamped.getValue());
        float cosPhi = (float) Math.cos(this.phiDamped.getValue());

        float dcx = dx * 2.f / getWidth() * this.radiusDamped.getValuef() * tanPerspective;
        float dcy = dy * 2.f / getHeight() * this.radiusDamped.getValuef() * tanPerspective;

        this.camera.x.incrementValue(-dcx * cosTheta - dcy * sinTheta * sinPhi);
        this.camera.y.incrementValue(dcy * cosPhi);
        this.camera.z.incrementValue(-dcx * sinTheta + dcy * cosTheta * sinPhi);

      } else {
        this.camera.theta.incrementValue(-dx / getWidth() * 1.5 * Math.PI);
        this.camera.phi.incrementValue(dy / getHeight() * 1.5 * Math.PI);
      }
      break;
    case MOVE:
      if (mouseEvent.isMetaDown() || mouseEvent.isControlDown() || mouseEvent.isShiftDown()) {

        float sinTheta = (float) Math.sin(this.thetaDamped.getValue());
        float cosTheta = (float) Math.cos(this.thetaDamped.getValue());
        float cosPhi = (float) Math.cos(this.phiDamped.getValue());
        float tanPerspective = (float) Math.tan(.5 * this.perspective.getValue() * Math.PI / 180.);

        float dcx = dx * 2.f / getWidth() * this.radiusDamped.getValuef() * tanPerspective;
        float dcy = dy * 2.f / getHeight() * this.radiusDamped.getValuef() * tanPerspective;

        float dex = dcx * cosTheta;
        float dez = -dcx * sinTheta;
        float dey = -dcy * cosPhi;

        if (mouseEvent.isShiftDown()) {
          dex -= dy * sinTheta;
          dez -= dy * cosTheta;
          dey = 0;
        }
        this.camera.x.incrementValue(dex);
        this.camera.y.incrementValue(dey);
        this.camera.z.incrementValue(dez);
      } else {
        this.camera.theta.incrementValue(dx / getWidth() * Math.PI);
        this.camera.phi.incrementValue(-dy / getHeight() * Math.PI);
      }
      break;
    }
    updateFocusedCamera();
  }

  @Override
  protected void onMouseWheel(MouseEvent mouseEvent, float mx, float my, float delta) {
    switch (this.interactionMode.getEnum()) {
    case ZOOM:
      this.camera.radius.incrementValue(delta * this.camera.radius.getValue() / 1000.);
      break;
    case MOVE:
      float dcx = delta * this.camera.radius.getValuef() / 1000.f * (float) Math.sin(this.thetaDamped.getValuef());
      float dcz = delta * this.camera.radius.getValuef() / 1000.f * (float) Math.cos(this.thetaDamped.getValuef());
      setEye(this.eye.x - dcx, this.eye.y, this.eye.z - dcz);
      break;
    }
    updateFocusedCamera();
  }

  @Override
  protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
    float amount = .02f;
    if (keyEvent.isShiftDown()) {
      amount *= 10.f;
    }
    if (this.interactionMode.getEnum() == InteractionMode.MOVE) {
      amount *= -1;
    }
    if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
      consumeKeyEvent();
      this.camera.theta.incrementValue(amount);
      updateFocusedCamera();
    } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
      consumeKeyEvent();
      this.camera.theta.incrementValue(-amount);
      updateFocusedCamera();
    } else if (keyCode == java.awt.event.KeyEvent.VK_UP) {
      consumeKeyEvent();
      this.camera.phi.incrementValue(-amount);
      updateFocusedCamera();
    } else if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
      consumeKeyEvent();
      this.camera.phi.incrementValue(amount);
      updateFocusedCamera();
    }
  }

  private static final String KEY_MODE = "mode";
  private static final String KEY_ANIMATION = "animation";
  private static final String KEY_ANIMATION_TIME = "animationTime";
  private static final String KEY_CAMERA = "camera";
  private static final String KEY_CUE = "cue";
  private static final String KEY_FOCUS = "focus";
  private static final String KEY_PROJECTION = "projection";
  private static final String KEY_PERSPECTIVE = "perspective";
  private static final String KEY_DEPTH = "depth";
  private static final String KEY_PHI_LOCK = "phiLock";
  private static final String KEY_CENTER_POINT = "centerPoint";


  @Override
  public void save(LX lx, JsonObject object) {
    object.addProperty(KEY_MODE, this.interactionMode.getValuei());
    object.addProperty(KEY_ANIMATION, this.animation.isOn());
    object.addProperty(KEY_ANIMATION_TIME, this.animationTime.getValue());
    object.addProperty(KEY_PROJECTION, this.projection.getValuei());
    object.addProperty(KEY_PERSPECTIVE, this.perspective.getValue());
    object.addProperty(KEY_DEPTH, this.depth.getValue());
    object.addProperty(KEY_PHI_LOCK, this.phiLock.isOn());
    object.addProperty(KEY_CENTER_POINT, this.showCenterPoint.isOn());
    object.add(KEY_CAMERA, LXSerializable.Utils.toObject(lx, this.camera));
    object.add(KEY_CUE, LXSerializable.Utils.toArray(lx, this.cue));
    object.addProperty(KEY_FOCUS, this.focusCamera.getValuei());
  }

  @Override
  public void load(LX lx, JsonObject object) {
    // Stop animation
    this.animating.stop();
    this.animation.setValue(false);

    LXSerializable.Utils.loadInt(this.interactionMode, object, KEY_MODE);
    LXSerializable.Utils.loadDouble(this.animationTime, object, KEY_ANIMATION_TIME);
    LXSerializable.Utils.loadInt(this.projection, object, KEY_PROJECTION);
    LXSerializable.Utils.loadDouble(this.perspective, object, KEY_PERSPECTIVE);
    LXSerializable.Utils.loadDouble(this.depth, object, KEY_DEPTH);
    LXSerializable.Utils.loadBoolean(this.phiLock, object, KEY_PHI_LOCK);
    LXSerializable.Utils.loadBoolean(this.showCenterPoint, object, KEY_CENTER_POINT);
    LXSerializable.Utils.loadObject(lx, this.camera, object, KEY_CAMERA);
    LXSerializable.Utils.loadArray(lx, this.cue, object, KEY_CUE);
    LXSerializable.Utils.loadInt(this.focusCamera, object, KEY_FOCUS);

    // Updated damped values from loading
    this.radiusDamped.setValue(this.camera.radius.getValue());
    this.thetaDamped.setValue(this.camera.theta.getValue());
    this.phiDamped.setValue(this.camera.phi.getValue());
    this.xDamped.setValue(this.camera.x.getValue());
    this.yDamped.setValue(this.camera.y.getValue());
    this.zDamped.setValue(this.camera.z.getValue());

    // Re-initialize position
    computeCamera(true);

    // Load animation setting
    LXSerializable.Utils.loadBoolean(this.animation, object, KEY_ANIMATION);
  }

}
