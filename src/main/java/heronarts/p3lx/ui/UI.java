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

import heronarts.lx.LX;
import heronarts.lx.LXEngine;
import heronarts.lx.LXLoopTask;
import heronarts.lx.LXMappingEngine;
import heronarts.lx.midi.LXMidiEngine;
import heronarts.lx.midi.LXMidiMapping;
import heronarts.lx.mixer.LXBus;
import heronarts.lx.mixer.LXChannel;
import heronarts.lx.modulation.LXModulationEngine;
import heronarts.lx.modulation.LXParameterModulation;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.StringParameter;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.component.UILabel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.event.Event;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

/**
 * Top-level container for all overlay UI elements.
 */
public class UI implements LXEngine.Dispatch {

  public enum CoordinateSystem {
    LEFT_HANDED,
    RIGHT_HANDED;
  }

  private static UI instance = null;

  private class UIRoot extends UIObject implements UIContainer {

    private UIRoot() {
      this.ui = UI.this;
    }

    @Override
    public float getWidth() {
      return this.ui.applet.width;
    }

    @Override
    public float getHeight() {
      return this.ui.applet.height;
    }

    @Override
    void mousePressed(MouseEvent mouseEvent, float mx, float my) {
      // If a context menu is open, we'll want to close it on mouse-press
      // unless the mouse-press is within the context menu itself
      boolean hideContext = false;
      if (contextMenuOverlay.overlayContent != null) {
        hideContext = true;
        contextMenuOverlay.mousePressed = false;
      }
      super.mousePressed(mouseEvent, mx, my);

      // Note: check
      if (!this.mousePressContextMenu && hideContext && !contextMenuOverlay.mousePressed) {
        hideContextOverlay();
      }
    }

    @Override
    protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
      if (topLevelKeyEventHandler != null) {
        topLevelKeyEventHandler.onKeyPressed(keyEvent, keyChar, keyCode);
      }
      if (!keyEventConsumed()) {
        if (keyCode == java.awt.event.KeyEvent.VK_Z && (keyEvent.isMetaDown() || keyEvent.isControlDown())) {
          if (keyEvent.isShiftDown()) {
            lx.command.redo();
          } else {
            lx.command.undo();
          }
        } else if (keyCode == java.awt.event.KeyEvent.VK_TAB) {
          if (keyEvent.isShiftDown()) {
            focusPrev(keyEvent);
          } else {
            focusNext(keyEvent);
          }
        } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
          hideContextOverlay();
        }
      }
    }

    @Override
    protected void onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {
      if (topLevelKeyEventHandler != null) {
        topLevelKeyEventHandler.onKeyReleased(keyEvent, keyChar, keyCode);
      }
    }

    @Override
    protected void onKeyTyped(KeyEvent keyEvent, char keyChar, int keyCode) {
      if (topLevelKeyEventHandler != null) {
        topLevelKeyEventHandler.onKeyTyped(keyEvent, keyChar, keyCode);
      }
    }

    private void redraw() {
      for (UIObject child : this.mutableChildren) {
        if (child instanceof UI2dComponent) {
          ((UI2dComponent) child).redraw();
        }
      }
    }

    private UIObject findCurrentFocus() {
      UIObject currentFocus = this;
      while (currentFocus.focusedChild != null) {
        currentFocus = currentFocus.focusedChild;
      }
      return currentFocus;
    }

    private UIObject findNextFocusable() {
      // Identify the deepest focused object
      UIObject focus = findCurrentFocus();

      // Check if it has a child that is eligible for focus
      UIObject focusableChild = findNextFocusableChild(focus, 0);
      if (focusableChild != null) {
        return focusableChild;
      }

      // Work up the tree, trying siblings at each level
      while (focus.parent != null) {
        int focusIndex = focus.parent.mutableChildren.indexOf(focus);
        focusableChild = findNextFocusableChild(focus.parent, focusIndex + 1);
        if (focusableChild != null) {
          return focusableChild;
        }
        focus = focus.parent;
      }

      // We ran out! Loop around from the front...
      return findNextFocusableChild(this, 0);
    }

    private UIObject findNextFocusableChild(UIObject focus, int startIndex) {
      for (int i = startIndex; i < focus.mutableChildren.size(); ++i) {
        UIObject child = focus.mutableChildren.get(i);
        if (child.isVisible()) {
          if (child instanceof UITabFocus) {
            return child;
          }
          UIObject recurse = findNextFocusableChild(child, 0);
          if (recurse != null) {
            return recurse;
          }
        }
      }
      return null;
    }

    private UIObject findPrevFocusable() {
      // Identify the deepest focused object
      UIObject focus = findCurrentFocus();

      // Check its previous siblings, depth-first
      while (focus.parent != null) {
        int focusIndex = focus.parent.mutableChildren.indexOf(focus);
        UIObject focusableChild = findPrevFocusableChild(focus.parent, focusIndex - 1);
        if (focusableChild != null) {
          return focusableChild;
        }
        if (focus.parent instanceof UITabFocus) {
          return focus.parent;
        }
        focus = focus.parent;
      }

      // We failed! Wrap around to the end
      return findPrevFocusableChild(this, this.mutableChildren.size() - 1);
    }

    private UIObject findPrevFocusableChild(UIObject focus, int startIndex) {
      for (int i = startIndex; i >= 0; --i) {
        UIObject child = focus.mutableChildren.get(i);
        if (child.isVisible()) {
          UIObject recurse = findPrevFocusableChild(child, child.mutableChildren.size() - 1);
          if (recurse != null) {
            return recurse;
          }
          if (child instanceof UITabFocus) {
            return child;
          }
        }
      }
      return null;
    }

    @Override
    public UIObject getContentTarget() {
      return this;
    }

    @Override
    public float getContentWidth() {
      return this.ui.width;
    }

    @Override
    public float getContentHeight() {
      return this.ui.height;
    }
  }

  /**
   * Redraw may be called from any thread
   */
  private final List<UI2dComponent> threadSafeRedrawList =
    Collections.synchronizedList(new ArrayList<UI2dComponent>());

  /**
   * Objects to redraw on current pass thru animation thread
   */
  private final List<UI2dComponent> uiThreadRedrawList =
    new ArrayList<UI2dComponent>();

  /**
   * Input events coming from the event thread
   */
  private final List<Event> threadSafeInputEventQueue =
    Collections.synchronizedList(new ArrayList<Event>());

  /**
   * Events on the local processing thread
   */
  private final List<Event> engineThreadInputEvents = new ArrayList<Event>();

  public class Timer {
    public long drawNanos = 0;
  }

  public final Timer timer = new Timer();

  public final P3LX lx;

  public final PApplet applet;

  private UIRoot root;

  public final StringParameter contextualHelpText = new StringParameter("Contextual Help");

  private boolean hasBackground = false;

  private int backgroundColor = UI.BLACK;

  protected CoordinateSystem coordinateSystem = CoordinateSystem.LEFT_HANDED;

  private static final long INIT_RUN = -1;
  private long lastMillis = INIT_RUN;

  private UIEventHandler topLevelKeyEventHandler = null;

  private class UIContextOverlay extends UI2dContext {

    private boolean mousePressed = false;

    private UI2dComponent overlayContent = null;

    public UIContextOverlay() {
      super(UI.this, 0, 0, 0, 0);
      this.parent = root;
      setUI(UI.this);
      setBackgroundColor(UI.BLACK);
    }

    private void setContent(UI2dComponent overlayContent) {
      if (this.overlayContent != null) {
        this.overlayContent.setVisible(false);
        this.overlayContent.removeFromContainer();
        root.mutableChildren.remove(this);
      }
      this.overlayContent = overlayContent;
      if (overlayContent != null) {
        setSize(overlayContent.getWidth(), overlayContent.getHeight());
        float x = 0;
        float y = 0;
        UIObject component = overlayContent;
        while (component != root && component != null) {
          x += component.getX();
          y += component.getY();
          if (component instanceof UI2dScrollContext) {
            UI2dScrollContext scrollContext = (UI2dScrollContext) component;
            x += scrollContext.getScrollX();
            y += scrollContext.getScrollY();
          }
          component = component.getParent();
        }
        setPosition(x, y);
        overlayContent.setVisible(true);
        overlayContent.setPosition(0, 0);
        overlayContent.addToContainer(this);
        root.mutableChildren.add(this);
      }
    }

    @Override
    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
      super.onMousePressed(mouseEvent, mx, my);
      this.mousePressed = true;
    }
  }

  /**
   * Drop menu overlay object
   */
  private UIContextOverlay contextMenuOverlay;

  /**
   * UI look and feel
   */
  public final UITheme theme;

  /**
   * White color
   */
  public final static int WHITE = 0xffffffff;

  /**
   * Black color
   */
  public final static int BLACK = 0xff000000;

  /**
   * Width of the UI
   */
  int width;

  /**
   * Height of the UI
   */
  int height;

  private int resizeWidth;
  private int resizeHeight;

  private boolean resizable = false;

  boolean midiMapping = false;
  boolean modulationSourceMapping = false;
  boolean modulationTargetMapping = false;
  boolean triggerSourceMapping = false;
  boolean triggerTargetMapping = false;
  LXModulationEngine modulationEngine = null;
  LXParameterModulation highlightParameterModulation = null;

  private UIControlTarget controlTarget = null;
  private UITriggerSource triggerSource = null;
  private UIModulationSource modulationSource = null;

  public UI(P3LX lx) {
    this(lx.applet, lx);
  }

  /**
   * Creates a new UI instance
   *
   * @param applet The PApplet
   */
  public UI(PApplet applet) {
    this(applet, null);
  }

  private UI(PApplet applet, final P3LX lx) {
    if (UI.instance != null) {
      throw new IllegalStateException("May not create multiple instances of UI");
    }
    UI.instance = this;
    this.lx = lx;
    this.applet = applet;
    this.width = this.resizeWidth = lx.applet.width;
    this.height = this.resizeHeight = lx.applet.height;
    this.theme = new UITheme(applet);
    LX.initTimer.log("P3LX: UI: Theme");
    this.root = new UIRoot();
    this.contextMenuOverlay = new UIContextOverlay();
    LX.initTimer.log("P3LX: UI: Root");
    applet.registerMethod("pre", this);
    applet.registerMethod("draw", this);
    applet.registerMethod("keyEvent", this);
    applet.registerMethod("mouseEvent", this);
    LX.initTimer.log("P3LX: UI: register");
    if (lx != null) {
      lx.addProjectListener(new LX.ProjectListener() {
        @Override
        public void projectChanged(File file, Change change) {
          switch (change) {
          case TRY:
            contextualHelpText.setValue("Loading project: " + file.getName());
            break;
          case NEW:
            contextualHelpText.setValue("Created new project");
            break;
          case SAVE:
            contextualHelpText.setValue("Saved project file: " + file.getName());
            break;
          case OPEN:
            contextualHelpText.setValue("Opened project file: " + file.getName());
            break;

          }
        }

      });

      lx.engine.setInputDispatch(this);

      lx.engine.mapping.mode.addListener(new LXParameterListener() {
        public void onParameterChanged(LXParameter p) {
          midiMapping = lx.engine.mapping.getMode() == LXMappingEngine.Mode.MIDI;
          modulationSourceMapping = lx.engine.mapping.getMode() == LXMappingEngine.Mode.MODULATION_SOURCE;
          modulationTargetMapping = lx.engine.mapping.getMode() == LXMappingEngine.Mode.MODULATION_TARGET;
          triggerSourceMapping = lx.engine.mapping.getMode() == LXMappingEngine.Mode.TRIGGER_SOURCE;
          triggerTargetMapping = lx.engine.mapping.getMode() == LXMappingEngine.Mode.TRIGGER_TARGET;

          if (midiMapping) {
            contextualHelpText.setValue("Click on a control target to MIDI map, eligible controls are highlighted");
          } else if (modulationSourceMapping) {
            contextualHelpText.setValue("Click on a modulation source, eligible sources are highlighted ");
          } else if (modulationTargetMapping) {
            LXNormalizedParameter sourceParameter = modulationSource.getModulationSource();
            if (sourceParameter == null) {
              contextualHelpText.setValue("You are somehow mapping a non-existent source parameter, choose a destination");
            } else {
              contextualHelpText.setValue("Select a modulation destination for " + sourceParameter.getCanonicalLabel() + ", eligible targets are highlighted");
            }
          } else if (triggerSourceMapping) {
            contextualHelpText.setValue("Click on a trigger source, eligible sources are highlighted ");
          } else if (triggerTargetMapping) {
            contextualHelpText.setValue("Select a trigger destination for " + triggerSource.getTriggerSource().getCanonicalLabel() + ", eligible targets are highlighted");
          } else {
            contextualHelpText.setValue("");
          }

          root.redraw();
        }
      });
      lx.engine.midi.addMappingListener(new LXMidiEngine.MappingListener() {

        @Override
        public void mappingRemoved(LXMidiEngine engine, LXMidiMapping mapping) {
        }

        @Override
        public void mappingAdded(LXMidiEngine engine, LXMidiMapping mapping) {
          if (midiMapping) {
            contextualHelpText.setValue("Successfully mapped MIDI Ch." + (mapping.channel+1) + " " + mapping.getDescription() + " to " + mapping.parameter.getCanonicalLabel());
          }
        }
      });
    }

    lx.errorChanged.addListener((p) -> { showError(); });
    showError();

    lx.failure.addListener((p) -> {
      float width = getWidth() * .8f;
      float height = getHeight() * .8f;
      showContextOverlay(
        new UILabel(getWidth() * .1f, getHeight() * .1f, width, height)
        .setLabel(lx.failure.getString())
        .setBreakLines(true)
        .setPadding(8)
        .setTextAlignment(PConstants.LEFT, PConstants.TOP)
        .setBorderColor(this.theme.getAttentionColor())
        .setBackgroundColor(this.theme.getDarkBackgroundColor())
        .setBorderRounding(4)
        .setFontColor(this.theme.getAttentionColor())
      );
    });
  }

  public void showError() {
    final LX.Error error = lx.getError();
    if (error != null) {
      if (error.cause != null) {
        showContextOverlay(new UIDialogBox(
          this,
          error.message,
          new String[] { "Copy Stack Trace", "Okay" },
          new int[] { UIDialogBox.OPTION_WIDTH * 2, UIDialogBox.OPTION_WIDTH },
          new Runnable[] {
            () -> { lx.setSystemClipboardString(error.getStackTrace()); },
            () -> { lx.popError(); }
          }));
      } else {
        showContextOverlay(new UIDialogBox(this, error.message, () -> { lx.popError(); }));
      }
    }
  }

  public UI setHighlightParameterModulation(LXParameterModulation highlightParameterModulation) {
    if (this.highlightParameterModulation != highlightParameterModulation) {
      this.highlightParameterModulation = highlightParameterModulation;
      this.root.redraw();
    }
    return this;
  }

  public UI setCoordinateSystem(CoordinateSystem coordinateSystem) {
    this.coordinateSystem = coordinateSystem;
    return this;
  }

  public void reflow() {
    // Subclasses may override this method for top-level UI changes
  }

  public static UI get() {
    return UI.instance;
  }

  public UI setBackground(boolean hasBackground) {
    this.hasBackground = hasBackground;
    return this;
  }

  public UI setBackgroundColor(int backgroundColor) {
    this.hasBackground = true;
    this.backgroundColor = backgroundColor;
    return this;
  }

  public void focusPrev(Event event) {
    UIObject focusTarget = this.root.findPrevFocusable();
    if (focusTarget != null) {
      focusTarget.focus(event);
    }
  }

  public void focusNext(Event event) {
    UIObject focusTarget = this.root.findNextFocusable();
    if (focusTarget != null) {
      focusTarget.focus(event);
    }
  }

  private boolean isMapping() {
    return this.midiMapping || this.modulationSourceMapping || this.modulationTargetMapping || this.triggerSourceMapping || this.triggerTargetMapping;
  }

  void setMouseoverHelpText(String helpText) {
    if (!isMapping()) {
      this.contextualHelpText.setValue(helpText);
    }
  }

  void clearMouseoverHelpText() {
    if (!isMapping()) {
      this.contextualHelpText.setValue("");
    }
  }

  /**
   * Sets an object to handle top-level input events
   *
   * @param eventHandler Event handler
   * @return this
   */
  public UI setTopLevelKeyEventHandler(UIEventHandler eventHandler) {
    this.topLevelKeyEventHandler = eventHandler;
    return this;
  }

  UI setControlTarget(UIControlTarget controlTarget) {
    this.lx.engine.mapping.setControlTarget(controlTarget.getControlTarget());
    LXParameter midiParameter = controlTarget.getControlTarget();
    if (midiParameter == null) {
      this.contextualHelpText.setValue("Press a MIDI key or controller to map a non-existent parameter?");
    } else {
      this.contextualHelpText.setValue("Press a MIDI key or controller to map " + midiParameter.getCanonicalLabel());
    }
    if (this.controlTarget != controlTarget) {
      if (this.controlTarget != null) {
        ((UI2dComponent) this.controlTarget).redraw();
      }
      this.controlTarget = controlTarget;
      if (this.controlTarget != null) {
        ((UI2dComponent) this.controlTarget).redraw();
      }
    }
    return this;
  }

  UIControlTarget getControlTarget() {
    return this.controlTarget;
  }

  public UI mapTriggerSource(UITriggerSource triggerSource) {
    this.modulationEngine = this.lx.engine.modulation;
    this.triggerSource = triggerSource;
    this.lx.engine.mapping.setMode(triggerSource == null ? LXMappingEngine.Mode.OFF : LXMappingEngine.Mode.TRIGGER_TARGET);
    return this;
  }

  UITriggerSource getTriggerSource() {
    return this.triggerSource;
  }

  public UI mapModulationSource(UIModulationSource modulationSource) {
    return mapModulationSource(this.lx.engine.modulation, modulationSource);
  }

  public UI mapModulationSource(LXModulationEngine modulationEngine, UIModulationSource modulationSource) {
    this.modulationEngine = modulationEngine;
    this.modulationSource = modulationSource;
    this.lx.engine.mapping.setMode(modulationSource == null ? LXMappingEngine.Mode.OFF : LXMappingEngine.Mode.MODULATION_TARGET);
    return this;
  }

  UIModulationSource getModulationSource() {
    return this.modulationSource;
  }

  /**
   * Sets whether the UI should be resizable.
   *
   * @param resizable Whether UI is resizable
   * @return this
   */
  public UI setResizable(boolean resizable) {
    this.applet.getSurface().setResizable(this.resizable = resizable);
    return this;
  }

  /**
   * Get width of the UI
   *
   * @return width
   */
  public int getWidth() {
    return this.width;
  }

  /**
   * Get height of the UI
   *
   * @return height
   */
  public int getHeight() {
    return this.height;
  }

  /**
   * Add a task to be performed on every loop of the UI engine.
   *
   * @param loopTask Task to perform on every UI loop
   * @return this
   */
  public UI addLoopTask(LXLoopTask loopTask) {
    this.root.addLoopTask(loopTask);
    return this;
  }

  /**
   * Remove a task from the UI engine
   *
   * @param loopTask Task to stop performing on every UI loop
   * @return this
   */
  public UI removeLoopTask(LXLoopTask loopTask) {
    this.root.removeLoopTask(loopTask);
    return this;
  }

  /**
   * Add a 2d context to this UI
   *
   * @param layer UI layer
   * @return this
   */
  public UI addLayer(UI2dContext layer) {
    layer.addToContainer(this.root);
    return this;
  }

  /**
   * Remove a 2d context from this UI
   *
   * @param layer UI layer
   * @return this UI
   */
  public UI removeLayer(UI2dContext layer) {
    layer.removeFromContainer();
    return this;
  }

  /**
   * Add a 3d context to this UI
   *
   * @param layer 3d context
   * @return this UI
   */
  public UI addLayer(UI3dContext layer) {
    this.root.mutableChildren.add(layer);
    layer.parent = this.root;
    layer.setUI(this);
    return this;
  }

  public UI removeLayer(UI3dContext layer) {
    if (layer.parent != this.root) {
      throw new IllegalStateException("Cannot remove 3d layer which is not present");
    }
    this.root.mutableChildren.remove(layer);
    layer.parent = null;
    return this;
  }

  /**
   * Brings a layer to the top of the UI stack
   *
   * @param layer UI layer
   * @return this UI
   */
  public UI bringToTop(UI2dContext layer) {
    this.root.mutableChildren.remove(layer);
    this.root.mutableChildren.add(layer);
    return this;
  }

  public UI hideContextOverlay() {
    showContextOverlay(null);
    return this;
  }

  public UI showContextOverlay(UI2dComponent contextOverlay) {
    this.contextMenuOverlay.setContent(contextOverlay);
    return this;
  }

  public void showConfirmDialog(String message, Runnable confirm) {
    showContextOverlay(new UIDialogBox(this,
      message,
      new String[] { "No", "Yes" },
      new Runnable[] { null, confirm }
    ));
  }

  /**
   * Load a font file
   *
   * @param font Font name
   * @return PFont object
   */
  public PFont loadFont(String font) {
    return this.applet.loadFont(font);
  }

  void redraw(UI2dComponent object) {
    this.threadSafeRedrawList.add(object);
  }

  public void resize(int width, int height) {
    this.resizeWidth = width;
    this.resizeHeight = height;
  }

  public final void pre() {
    // Check for a resize event
    if (this.resizable) {
      // A resize has been requested, make it happen
      if (this.width != this.resizeWidth || this.height != this.resizeHeight) {
        this.applet.getSurface().setSize(this.resizeWidth, this.resizeHeight);
      }
      // The resize has actually happened, update ourselves
      if (this.applet.width != this.width || this.applet.height != this.height) {
        this.width = this.resizeWidth = this.applet.width;
        this.height = this.resizeHeight = this.applet.height;
        this.root.resize(this);
        onResize();
      }
    }
  }

  /**
   * Draws the UI
   */
  public final void draw() {

    beginDraw();

    long drawStart = System.nanoTime();

    long nowMillis = System.currentTimeMillis();
    if (this.lastMillis == INIT_RUN) {
      // Initial frame is arbitrarily 16 milliseconds (~60 fps)
      this.lastMillis = nowMillis - 16;
    }
    double deltaMs = nowMillis - this.lastMillis;
    this.lastMillis = nowMillis;

    // Run loop tasks through the UI tree
    this.root.loop(deltaMs);

    // Iterate through all objects that need redraw state marked
    this.uiThreadRedrawList.clear();
    synchronized (this.threadSafeRedrawList) {
      this.uiThreadRedrawList.addAll(this.threadSafeRedrawList);
      this.threadSafeRedrawList.clear();
    }
    for (UI2dComponent object : this.uiThreadRedrawList) {
      object._redraw();
    }

    // Draw from the root
    if (this.hasBackground) {
      this.applet.background(this.backgroundColor);
    }
    this.root.draw(this, this.applet.g);

    endDraw();

    this.timer.drawNanos = System.nanoTime() - drawStart;
  }


  protected void beginDraw() {
    // Subclasses may override...
  }

  protected void endDraw() {
    // Subclasses may override...
  }

  protected void onResize() {
    // Subclasses may override
  }

  private boolean isThreaded() {
    return (this.lx != null) && (this.lx.engine.isThreaded());
  }

  public void dispatch() {
    // This is invoked on the LXEngine thread, which may be different
    // from the Processing Animation thread. Events are always
    // processed on the engine thread to avoid bugs.
    engineThreadInputEvents.clear();
    synchronized (threadSafeInputEventQueue) {
      engineThreadInputEvents.addAll(threadSafeInputEventQueue);
      threadSafeInputEventQueue.clear();
    }
    for (Event event : engineThreadInputEvents) {
      if (event instanceof KeyEvent) {
        _keyEvent((KeyEvent) event);
      } else if (event instanceof MouseEvent) {
        _mouseEvent((MouseEvent) event);
      }
    }
  }

  public void mouseEvent(MouseEvent mouseEvent) {
    // NOTE: this method is invoked from the Processing thread! The LX engine
    // may be running on a separate thread.
    if (isThreaded()) {
      // NOTE: it's okay that no lock is held here, if threading mode changes
      // right here, the event queue will still be picked up by next iteration
      // of the EngineUILoopTask
      this.threadSafeInputEventQueue.add(mouseEvent);
    } else {
      // NOTE: also okay to be lock-free here, if threading mode was off then
      // there is no other thread that would have made a call to start the
      // threading engine
      _mouseEvent(mouseEvent);
    }
  }

  private float pmx, pmy;

  private void _mouseEvent(MouseEvent mouseEvent) {
    switch (mouseEvent.getAction()) {
    case MouseEvent.WHEEL:
      int wheelCount = mouseEvent.getCount();
      if (PApplet.platform == PConstants.WINDOWS) {
        wheelCount *= 50;
      }
      this.root.mouseWheel(mouseEvent, mouseEvent.getX(), mouseEvent.getY(), wheelCount);
      return;
    case MouseEvent.PRESS:
      this.pmx = mouseEvent.getX();
      this.pmy = mouseEvent.getY();
      this.root.mousePressed(mouseEvent, this.pmx, this.pmy);
      break;
    case processing.event.MouseEvent.RELEASE:
      this.root.mouseReleased(mouseEvent, mouseEvent.getX(), mouseEvent.getY());
      break;
    case processing.event.MouseEvent.CLICK:
      this.root.mouseClicked(mouseEvent, mouseEvent.getX(), mouseEvent.getY());
      break;
    case processing.event.MouseEvent.DRAG:
      float mx = mouseEvent.getX();
      float my = mouseEvent.getY();
      this.root.mouseDragged(mouseEvent, mx, my, mx - this.pmx, my - this.pmy);
      this.pmx = mx;
      this.pmy = my;
      break;
    case processing.event.MouseEvent.MOVE:
      this.root.mouseMoved(mouseEvent, mouseEvent.getX(), mouseEvent.getY());
      break;
    }
  }

  public void keyEvent(KeyEvent keyEvent) {
    // Do not close on the ESC key, P3LX UI uses it
    if (this.applet.key == PConstants.ESC) {
      this.applet.key = 0;
    }

    // Default handler for key events on the UI thread
    _uiThreadDefaultKeyEvent(keyEvent);

    // NOTE: this method is invoked from the Processing thread! The LX engine
    // may be running on a separate thread.
    if (isThreaded()) {
      this.threadSafeInputEventQueue.add(keyEvent);
    } else {
      _keyEvent(keyEvent);
    }
  }

  private void _keyEvent(KeyEvent keyEvent) {
    _engineThreadDefaultKeyEvent(keyEvent);

    char keyChar = keyEvent.getKey();
    int keyCode = keyEvent.getKeyCode();
    switch (keyEvent.getAction()) {
    case KeyEvent.RELEASE:
      this.root.keyReleased(keyEvent, keyChar, keyCode);
      break;
    case KeyEvent.PRESS:
      this.root.keyPressed(keyEvent, keyChar, keyCode);
      break;
    case KeyEvent.TYPE:
      this.root.keyTyped(keyEvent, keyChar, keyCode);
      break;
    default:
      throw new RuntimeException("Invalid keyEvent type: " + keyEvent.getAction());
    }
  }

  private void _uiThreadDefaultKeyEvent(KeyEvent keyEvent) {
    int action = keyEvent.getAction();
    if (action == KeyEvent.RELEASE) {
      if (keyEvent.getKeyCode() == java.awt.event.KeyEvent.VK_F) {
        this.lx.flags.showFramerate = false;
      }
    } else if (action == KeyEvent.PRESS) {
      if ((keyEvent.isControlDown() || keyEvent.isMetaDown()) && keyEvent.getKeyCode() == java.awt.event.KeyEvent.VK_F) {
        this.lx.flags.showFramerate = true;
      }
    }
  }

  public final void onSaveAs(final File file) {
    if (file != null) {
      this.lx.engine.addTask(new Runnable() {
        public void run() {
          lx.saveProject(file);
        }
      });
    }
  }

  public final void onLoad(final File file) {
    if (file != null) {
      this.lx.engine.addTask(new Runnable() {
        public void run() {
          lx.openProject(file);
        }
      });
    }
  }

  private void _engineThreadDefaultKeyEvent(KeyEvent keyEvent) {
    int keyCode = keyEvent.getKeyCode();
    int action = keyEvent.getAction();
    if (action == KeyEvent.RELEASE) {
      switch (keyCode) {
      case java.awt.event.KeyEvent.VK_S:
        if (keyEvent.isControlDown() || keyEvent.isMetaDown()) {
          if (keyEvent.isShiftDown() || lx.getProject() == null) {
            this.applet.selectOutput("Select a file to save:", "onSaveAs", this.lx.getMediaFile(LX.Media.PROJECTS, "Project.lxp"), this);
          } else {
            lx.saveProject();
          }
        }
        break;
      case java.awt.event.KeyEvent.VK_O:
        if (keyEvent.isControlDown() || keyEvent.isMetaDown()) {
          this.applet.selectInput("Select a file to load:", "onLoad", this.lx.getMediaFile(LX.Media.PROJECTS, "default.lxp", false), this);
        }
        break;
      case java.awt.event.KeyEvent.VK_BRACELEFT:
      case java.awt.event.KeyEvent.VK_BRACERIGHT:
        LXBus bus = this.lx.engine.mixer.getFocusedChannel();
        if (bus instanceof LXChannel) {
          if (keyCode == java.awt.event.KeyEvent.VK_BRACELEFT) {
            ((LXChannel) bus).goPreviousPattern();
          } else {
            ((LXChannel) bus).goNextPattern();
          }
        }
        break;
      case java.awt.event.KeyEvent.VK_SPACE:
        if (this.lx.flags.keyboardTempo) {
          this.lx.engine.tempo.tap();
        }
        break;
      }
    } else if (action == KeyEvent.PRESS) {
      if (keyCode == java.awt.event.KeyEvent.VK_T && (keyEvent.isMetaDown() || keyEvent.isControlDown())) {
        if (keyEvent.isShiftDown()) {
          this.lx.engine.isChannelMultithreaded.toggle();
        } else {
          this.lx.engine.isMultithreaded.toggle();
        }
      }
      switch (keyCode) {
      case java.awt.event.KeyEvent.VK_LEFT:
        if (this.lx.flags.keyboardTempo) {
          this.lx.engine.tempo.setBpm(this.lx.engine.tempo.bpm() - .1);
        }
        break;
      case java.awt.event.KeyEvent.VK_RIGHT:
        if (this.lx.flags.keyboardTempo) {
          this.lx.engine.tempo.setBpm(this.lx.engine.tempo.bpm() + .1);
        }
        break;
      }
    }
  }
}
