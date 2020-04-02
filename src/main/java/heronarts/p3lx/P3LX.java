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

package heronarts.p3lx;

import heronarts.p3lx.ui.UI;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.lang.reflect.Modifier;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.LXEngine;
import heronarts.lx.effect.LXEffect;
import heronarts.lx.model.LXModel;
import heronarts.lx.pattern.LXPattern;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

/**
 * Harness to run LX inside a Processing 3 sketch
 */
public class P3LX extends LX {

  public final static String VERSION = LX.VERSION;

  private final LXEngine.Frame uiFrame;

  /**
   * Returns the version of the library.
   *
   * @return String
   */
  public static String version() {
    return VERSION;
  }

  /**
   * A reference to the applet context.
   */
  public final PApplet applet;

  /**
   * The UI container.
   */
  public final UI ui;

  public static class Flags extends LX.Flags {
    public boolean keyboardTempo = false;
    public boolean showFramerate = false;

    public Flags() {
      this.isP3LX = true;
    }
  }

  public final Flags flags;

  public class Timer {
    public long drawNanos = 0;
    public long engineNanos = 0;
  }

  public final Timer timer = new Timer();

  public P3LX(PApplet applet) {
    this(applet, new LXModel());
  }

  public P3LX(PApplet applet, LXModel model) {
    this(applet, new Flags(), model);
  }

  public P3LX(PApplet applet, Flags flags) {
    this(applet, flags, null);
  }

  public P3LX(PApplet applet, Flags flags, LXModel model) {
    super(flags, model);
    this.flags = flags;
    this.flags.isP3LX = true;
    this.flags.mediaPath = applet.sketchPath();
    this.applet = applet;

    // Find patterns + effects declared in the Processing sketch
    for (Class<?> cls : applet.getClass().getDeclaredClasses()) {
      if (!Modifier.isAbstract(cls.getModifiers())) {
        if (LXPattern.class.isAssignableFrom(cls)) {
          this.registry.addPattern(cls.asSubclass(LXPattern.class));
        } else if (LXEffect.class.isAssignableFrom(cls)) {
          this.registry.addEffect(cls.asSubclass(LXEffect.class));
        }
      }
    }

    // Load additional fixture definitions local to Processing sketch
    this.registry.addFixtures(new File(getMediaPath(), "fixtures"));

    // Initialize frame
    this.uiFrame = new LXEngine.Frame(this);
    this.engine.getFrameNonThreadSafe(this.uiFrame);

    beforeBuildUI();

    this.ui = buildUI();
    LX.initTimer.log("P3LX: UI");

    applet.colorMode(PConstants.HSB, 360, 100, 100, 100);
    LX.initTimer.log("P3LX: colorMode");

    applet.registerMethod("draw", this);
    applet.registerMethod("dispose", this);
    LX.initTimer.log("P3LX: registerMethod");
  }

  /**
   * Subclasses may override for final initialization steps before UI construction
   */
  protected void beforeBuildUI() {

  }

  /**
   * Subclass may override.
   *
   * @return UI
   */
  protected UI buildUI() {
    return new UI(this);
  }

  /**
   * Redundant, but making it obvious that Processing depends on this
   * method being named dispose(). This protects us from a rename in LX
   * where someone doesn't realize the Processing naming dependency.
   */
  @Override
  public void dispose() {
    super.dispose();
  }

  /**
   * Enables the tempo to be controlled by the keyboard arrow keys. Left and
   * right arrows change the tempo by .1 BPM, and the space-bar taps the tempo.
   *
   * @return this
   */
  public LX enableKeyboardTempo() {
    this.flags.keyboardTempo = true;
    return this;
  }

  public final PGraphics getGraphics() {
    return this.applet.g;
  }

  /**
   * Returns the last rendered frame
   *
   * @return The frame to be shown on the UI
   */
  public final LXEngine.Frame getUIFrame() {
    return this.uiFrame;
  }

  /**
   * Core function invoked by the processing engine on each iteration of the run
   * cycle.
   */
  public void draw() {
    long drawStart = System.nanoTime();

    long engineStart = System.nanoTime();
    String frameRateStr = "";

    // Give the engine a chance to sort itself out each frame
    this.engine.onDraw();

    if (this.engine.isThreaded()) {
      // NOTE: because we don't hold a lock, it is *possible* that the
      // engine stops being in threading mode just between these lines,
      // triggered by some action on the engine thread itself. It's okay
      // if this happens, worst side effect is the UI getting the last frame
      // from the copy buffer.
      this.engine.copyFrameThreadSafe(this.uiFrame);
      if (this.flags.showFramerate) {
        frameRateStr =
          "Engine: " + this.engine.frameRate() + " " +
          "UI: " + this.applet.frameRate;
        if (this.engine.isNetworkMultithreaded.isOn()) {
          frameRateStr += " Network: " + this.engine.network.frameRate();
        }
      }
    } else {
      // If the engine is not threaded, then we run it ourselves, and
      // we can just use its color buffer, as there is no thread contention.
      // We don't need to worry about lock contention because we are
      // currently on the only thread that *could* start the engine.
      this.engine.run();
      this.engine.getFrameNonThreadSafe(this.uiFrame);
      if (this.flags.showFramerate) {
        frameRateStr = "Framerate: " + this.applet.frameRate;
        if (this.engine.isNetworkMultithreaded.isOn()) {
          frameRateStr += " Network: " + this.engine.network.frameRate();
        }
      }
    }
    this.timer.engineNanos = System.nanoTime() - engineStart;

    // Print framerate
    if (this.flags.showFramerate) {
      PApplet.println(frameRateStr);
    }

    this.timer.drawNanos = System.nanoTime() - drawStart;
  }

  @Override
  public <T extends LXComponent> T instantiateComponent(Class<? extends T> cls, Class<T> type) {
    try {
      try {
        return cls.getConstructor(LX.class).newInstance(this);
      } catch (NoSuchMethodException nsmx) {
        try {
          return cls.getConstructor().newInstance();
        } catch (NoSuchMethodException nsmx2) {
          try {
            return cls.getConstructor(this.applet.getClass(), LX.class).newInstance(this.applet, this);
          } catch (NoSuchMethodException nsmx3) {
            return cls.getConstructor(this.applet.getClass()).newInstance(this.applet);
          }
        }
      }
    } catch (Exception x) {
      error(x, "Component instantiation failed: " + x.getLocalizedMessage());
    }
    return null;
  }

  @Override
  public void setSystemClipboardString(String str) {
    try {
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(str), null);
    } catch (Exception x) {
      error(x, "Exception setting system clipboard");
    }
  }

  private static final String P3LX_PREFIX = "P3LX";

  public static void log(String message) {
    LX._log(P3LX_PREFIX, message);
  }

  public static void error(Exception x, String message) {
    LX._error(P3LX_PREFIX, x, message);
  }

  public static void error(String message) {
    LX._error(P3LX_PREFIX, message);
  }
}