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

import heronarts.lx.LXLoopTask;
import heronarts.lx.clipboard.LXClipboardItem;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.LXParameterModulation;
import heronarts.lx.parameter.LXTriggerModulation;
import heronarts.lx.parameter.LXCompoundModulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public abstract class UIObject extends UIEventHandler implements LXLoopTask {

  UI ui = null;

  public final BooleanParameter visible = new BooleanParameter("Visible", true);

  final List<UIObject> mutableChildren = new CopyOnWriteArrayList<UIObject>();
  protected final List<UIObject> children = Collections.unmodifiableList(this.mutableChildren);

  UIObject parent = null;

  UIObject focusedChild = null;

  private UIObject pressedChild = null;
  private UIObject overChild = null;

  private boolean hasFocus = false;

  private final List<LXLoopTask> loopTasks = new ArrayList<LXLoopTask>();

  protected UIObject() {
    this.visible.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        if (!UIObject.this.visible.isOn()) {
          blur();
        }
      }
    });
  }

  protected UI getUI() {
    return this.ui;
  }

  /**
   * Add a task to be performed on every loop of the UI engine.
   *
   * @param loopTask Task to be performed on every UI frame
   * @return this
   */
  protected UIObject addLoopTask(LXLoopTask loopTask) {
    this.loopTasks.add(loopTask);
    return this;
  }

  /**
   * Remove a task from the UI engine
   *
   * @param loopTask Task to be removed from work list
   * @return this
   */
  protected UIObject removeLoopTask(LXLoopTask loopTask) {
    this.loopTasks.remove(loopTask);
    return this;
  }

  /**
   * Processes all the loop tasks in this object
   */
  @Override
  public final void loop(double deltaMs) {
    if (isVisible()) {
      for (LXLoopTask loopTask : this.loopTasks) {
        loopTask.loop(deltaMs);
      }
      for (UIObject child : this.mutableChildren) {
        child.loop(deltaMs);
      }
    }
  }

  /**
   * Internal method to track the UI that this is a part of
   *
   * @param ui UI context
   */
  void setUI(UI ui) {
    this.ui = ui;
    for (UIObject child : this.mutableChildren) {
      child.setUI(ui);
    }
  }

  /**
   * Subclasses may access the object that is containing this one
   *
   * @return Parent object
   */
  protected UIObject getParent() {
    return this.parent;
  }

  /**
   * Whether the given point is contained by this object
   *
   * @param x x-coordinate
   * @param y y-coordinate
   * @return True if the object contains this point
   */
  protected boolean contains(float x, float y) {
    return true;
  }

  float getX() {
    return 0;
  }

  float getY() {
    return 0;
  }

  public abstract float getWidth();

  public abstract float getHeight();

  private String description = null;

  public UIObject setDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * Objects are encouraged to override this method providing a helpful String displayed to the user explaining
   * the function of this UI component. If no help is available, return null rather than an empty String.
   *
   * @return Helpful contextual string explaining function of this element
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Whether this object is visible.
   *
   * @return True if this object is being displayed
   */
  public boolean isVisible() {
    return this.visible.isOn();
  }

  /**
   * Toggle visible state of this component
   *
   * @return this
   */
  public UIObject toggleVisible() {
    setVisible(!isVisible());
    return this;
  }

  /**
   * Set whether this object is visible
   *
   * @param visible Whether the object is visible
   * @return this
   */
  public UIObject setVisible(boolean visible) {
    this.visible.setValue(visible);
    return this;
  }

  /**
   * Whether this object has focus
   *
   * @return true or false
   */
  public boolean hasFocus() {
    return this.hasFocus;
  }

  /**
   * Whether this object has direct focus, meaning that no
   * child element is focused
   *
   * @return true or false
   */
  public boolean hasDirectFocus() {
    return hasFocus() && (this.focusedChild == null);
  }

  /**
   * Gets which immediate child of this object is focused, may be null. Child
   * may also have focused children.
   *
   * @return immediate child of this object which has focus
   */
  public UIObject getFocusedChild() {
    return this.focusedChild;
  }

  /**
   * Focuses on this object, giving focus to everything above
   * and whatever was previously focused below.
   *
   * @return this
   */
  public UIObject focus() {
    if (this.focusedChild != null) {
      this.focusedChild.blur();
    }
    _focusParents();
    return this;
  }

  private void _focusParents() {
    if (this.parent != null) {
      if (this.parent.focusedChild != this) {
        if (this.parent.focusedChild != null) {
          this.parent.focusedChild.blur();
        }
        this.parent.focusedChild = this;
      }
      this.parent._focusParents();
    }
    if (!this.hasFocus) {
      this.hasFocus = true;
      _onFocus();
    }
  }

  private void _onFocus() {
    onFocus();
    if (this instanceof UI2dComponent) {
      ((UI2dComponent) this).redraw();
    }
  }

  /**
   * Blur this object. Blurs its children from the bottom of
   * the tree up.
   *
   * @return this
   */
  public UIObject blur() {
    if (this.hasFocus) {
      for (UIObject child : this.mutableChildren) {
        child.blur();
      }
      if (this.parent != null) {
        if (this.parent.focusedChild == this) {
          this.parent.focusedChild = null;
        }
      }
      this.hasFocus = false;
      onBlur();
      if (this instanceof UI2dComponent) {
        ((UI2dComponent)this).redraw();
      }
    }
    return this;
  }

  /**
   * Brings this object to the front of its container.
   *
   * @return this
   */
  public UIObject bringToFront() {
    if (this.parent == null) {
      throw new IllegalStateException("Cannot bring to front when not in any container");
    }
    this.parent.mutableChildren.remove(this);
    this.parent.mutableChildren.add(this);
    return this;
  }

  void draw(UI ui, PGraphics pg) {
    if (isVisible()) {
      beginDraw(ui, pg);
      onDraw(ui, pg);
      for (UIObject child : this.mutableChildren) {
        float cx = child.getX();
        float cy = child.getY();
        pg.translate(cx, cy);
        child.draw(ui, pg);
        pg.translate(-cx, -cy);
      }
      endDraw(ui, pg);
    }
  }

  /**
   * Returns true if this is a MIDI control target that has been selected for
   * mapping. It is highlighted while the system waits for a MIDI event to map.
   *
   * @return true if this is a MIDI control target that has been selected for
   * mapping. It is highlighted while the system waits for a MIDI event to map.
   */
  boolean isControlTarget() {
    return this.ui.getControlTarget() == this;
  }

  /**
   * Returns true if this is a trigger source that has been selected for trigger
   * mapping. It is highlighted while the user selects a trigger target.
   *
   * @return true if this is a trigger source that has been selected for trigger
   * mapping. It is highlighted while the user selects a trigger target.
   */
  boolean isTriggerSource() {
    return
      this.ui.triggerTargetMapping &&
      (this == this.ui.getTriggerSource());
  }

  /**
   * Returns true if the UI is in a trigger source mapping state and this element
   * is an eligible trigger source. It is highlighted if so.
   *
   * @return true if the UI is in a trigger source mapping state and this element
   * is an eligible trigger source. It is highlighted if so.
   */
  boolean isTriggerSourceMapping() {
    return
      this.ui.triggerSourceMapping &&
      (this instanceof UITriggerSource) &&
      ((UITriggerSource) this).getTriggerSource() != null;
  }

  /**
   * Returns true if the UI is in trigger target mapping state and this element
   * is a valid, selectable trigger target.
   *
   * @return true if the UI is in trigger target mapping state and this element
   * is a valid, selectable trigger target.
   */
  boolean isTriggerTargetMapping() {
    return
      this.ui.triggerTargetMapping &&
      (this instanceof UITriggerTarget) &&
      ((UITriggerTarget) this).getTriggerTarget() != null;
  }

  /**
   * Returns true if the UI is in modulation target mapping mode and this element
   * is the modulation source that is being mapped.
   *
   * @return true if the UI is in modulation target mapping mode and this element
   * is the modulation source that is being mapped.
   */
  boolean isModulationSource() {
    if (this.ui.modulationTargetMapping) {
      UIModulationSource modulationSource = this.ui.getModulationSource();
      if (modulationSource == null) {
        return false;
      }
      if (this == modulationSource) {
        return true;
      }
      if (this instanceof UIModulationSource) {
        if (((UIModulationSource) this).getModulationSource() == modulationSource.getModulationSource()) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns true if the UI is in modulation source mapping mode and this element
   * is a valid modulation source.
   *
   * @return true if the UI is in modulation source mapping mode and this element
   * is a valid modulation source.
   */
  boolean isModulationSourceMapping() {
    return
      this.ui.modulationSourceMapping && (
        ((this instanceof UIModulationSource) && ((UIModulationSource) this).getModulationSource() != null) ||
        ((this instanceof UITriggerSource) && ((UITriggerSource) this).getTriggerSource() != null)
      );
  }

  /**
   * Returns true if the UI is in modulation target mapping mode and this element
   * is a valid modulation target.
   *
   * @return true if the UI is in modulation target mapping mode and this element
   * is a valid modulation target.
   */
  boolean isModulationTargetMapping() {
    if (this.ui.modulationTargetMapping && (this instanceof UIModulationTarget)) {
      CompoundParameter target = ((UIModulationTarget) this).getModulationTarget();
      return (target != null) && this.ui.modulationEngine.isValidTarget(target);
    }
    return false;
  }

  boolean isModulationHighlight() {
    if (this.ui.highlightParameterModulation != null) {
      LXParameter target = this.ui.highlightParameterModulation.getTarget();
      LXParameter thisParameter = null;
      if (this instanceof UITriggerTarget) {
        thisParameter = ((UITriggerTarget) this).getTriggerTarget();
      } else if (this instanceof UIModulationTarget) {
        thisParameter = ((UIModulationTarget) this).getModulationTarget();
      }
      return target == thisParameter;
    }
    return false;
  }

  /**
   * Returns true if the UI is in MIDI mapping mode and this element is a valid
   * control target that could be selected to receive MIDI input.
   *
   * @return true if the UI is in MIDI mapping mode and this element is a valid
   * control target that could be selected to receive MIDI input.
   */
  boolean isMidiMapping() {
    return
      this.ui.midiMapping &&
      (this instanceof UIControlTarget) &&
      ((UIControlTarget) this).getControlTarget() != null;
  }

  void resize(UI ui) {
    this.onUIResize(ui);
    for (UIObject child : this.mutableChildren) {
      child.resize(ui);
    }
  }

  /**
   * Subclasses may override this method to handle resize events on the global UI.
   * Called on the UI thread, only happens if ui.setResizable(true) has been called.
   *
   * @param ui The UI object
   */
  protected void onUIResize(UI ui) {}

  /**
   * Subclasses may override this method to perform operations before their
   * onDraw method is called or any children are drawn.
   *
   * @param ui UI context
   * @param pg Graphics context
   */
  protected void beginDraw(UI ui, PGraphics pg) {}

  /**
   * Subclasses should override this method to perform their drawing functions.
   *
   * @param ui UI context
   * @param pg Graphics context
   */
  protected void onDraw(UI ui, PGraphics pg) {}

  /**
   * Subclasses may override this method to perform operations after their onDraw
   * method has been called and after all children have been drawn
   *
   * @param ui UI context
   * @param pg Graphics context
   */
  protected void endDraw(UI ui, PGraphics pg) {}

  /**
   * Called in a key event handler to stop this event from bubbling up the
   * parent container chain. For example, a button which responds to a space bar
   * press should call consumeKeyEvent() to stop the event from being handled by its
   * container.
   *
   * @return this
   */
  protected UIObject consumeKeyEvent() {
    this.keyEventConsumed = true;
    return this;
  }

  /**
   * Checks whether key event was already consumed
   *
   * @return Whether the key event is already handled
   */
  protected boolean keyEventConsumed() {
    return this.keyEventConsumed;
  }

  /**
   * Called in a mouse wheel handler to stop this mouse wheel event from
   * bubbling. Invoked by nested scroll views.
   *
   * @return this
   */
  protected UIObject consumeMouseWheelEvent() {
    this.mouseWheelEventConsumed = true;
    return this;
  }

  private boolean keyEventConsumed = false;
  private boolean mouseWheelEventConsumed = false;
  protected boolean mousePressFocused = false;

  void mousePressed(MouseEvent mouseEvent, float mx, float my) {
    if (isMidiMapping()) {
      this.ui.setControlTarget((UIControlTarget) this);
      return;
    } else if (isModulationSourceMapping()) {
      if (this instanceof UIModulationSource) {
        this.ui.mapModulationSource((UIModulationSource) this);
      } else if (this instanceof UITriggerSource) {
        this.ui.mapTriggerSource((UITriggerSource) this);
      } else {
        throw new IllegalStateException("isModulationSourceMapping() was true but the element is not a modulation or trigger source: " + this);
      }
      return;
    } else if (isModulationTargetMapping() && !isModulationSource()) {
      LXNormalizedParameter source = this.ui.getModulationSource().getModulationSource();
      CompoundParameter target = ((UIModulationTarget)this).getModulationTarget();
      String error = null;
      if (source != null && target != null) {
        try {
          this.ui.modulationEngine.addModulation(new LXCompoundModulation(source, target));
        } catch (LXParameterModulation.CircularDependencyException cdx) {
          error = cdx.getMessage();
          System.err.println(cdx.getMessage());
        }
      }
      this.ui.mapModulationSource(null);
      if (error != null) {
        this.ui.contextualHelpText.setValue(error);
      }
      return;
    } else if (isTriggerSourceMapping()) {
      this.ui.mapTriggerSource((UITriggerSource) this);
      return;
    } else if (isTriggerTargetMapping() && !isTriggerSource()) {
      BooleanParameter source = this.ui.getTriggerSource().getTriggerSource();
      BooleanParameter target = ((UITriggerTarget)this).getTriggerTarget();
      String error = null;
      if (source != null && target != null) {
        try {
          this.ui.modulationEngine.addTrigger(new LXTriggerModulation(source, target));
        } catch (LXParameterModulation.CircularDependencyException cdx) {
          error = cdx.getMessage();
          System.err.println(cdx.getMessage());
        }
      }
      this.ui.mapTriggerSource(null);
      if (error != null) {
        this.ui.contextualHelpText.setValue(error);
      }
      return;
    }
    for (int i = this.mutableChildren.size() - 1; i >= 0; --i) {
      UIObject child = this.mutableChildren.get(i);
      if (child.isVisible() && child.contains(mx, my)) {
        child.mousePressed(mouseEvent, mx - child.getX(), my - child.getY());
        this.pressedChild = child;
        break;
      }
    }
    if (!hasFocus() && (this instanceof UIMouseFocus)) {
      this.mousePressFocused = true;
      focus();
    }
    onMousePressed(mouseEvent, mx, my);
    this.mousePressFocused = false;
  }

  void mouseReleased(MouseEvent mouseEvent, float mx, float my) {
    if (this.pressedChild != null) {
      this.pressedChild.mouseReleased(
        mouseEvent,
        mx - this.pressedChild.getX(),
        my - this.pressedChild.getY()
      );
      this.pressedChild = null;
    }
    onMouseReleased(mouseEvent, mx, my);

    // Check for case where we mouse-dragged outside of an element, now we've released
    if (this.overChild != null && !this.overChild.contains(mx, my)) {
      this.overChild.mouseOut(mouseEvent);
      this.overChild = null;
    }
  }

  void mouseClicked(MouseEvent mouseEvent, float mx, float my) {
    if (isMidiMapping()) {
      return;
    }
    for (int i = this.mutableChildren.size() - 1; i >= 0; --i) {
      UIObject child = this.mutableChildren.get(i);
      if (child.isVisible() && child.contains(mx, my)) {
        child.mouseClicked(mouseEvent, mx - child.getX(), my - child.getY());
        break;
      }
    }
    onMouseClicked(mouseEvent, mx, my);
  }

  void mouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
    if (isMidiMapping() || isModulationTargetMapping()) {
      return;
    }
    if (this.pressedChild != null) {
      this.pressedChild.mouseDragged(
        mouseEvent,
        mx - this.pressedChild.getX(),
        my - this.pressedChild.getY(),
        dx,
        dy
      );
    }
    onMouseDragged(mouseEvent, mx, my, dx, dy);
  }

  void mouseMoved(MouseEvent mouseEvent, float mx, float my) {
    boolean overAnyChild = false;
    for (int i = this.mutableChildren.size() - 1; i >= 0; --i) {
      UIObject child = this.mutableChildren.get(i);
      if (child.isVisible() && child.contains(mx, my)) {
        overAnyChild = true;
        if (child != this.overChild) {
          if (this.overChild != null) {
            this.overChild.mouseOut(mouseEvent);
          }
          this.overChild = child;
          child.mouseOver(mouseEvent);
        }
        child.mouseMoved(mouseEvent, mx - child.getX(), my - child.getY());
        break;
      }
    }
    if (!overAnyChild && (this.overChild != null)) {
      this.overChild.mouseOut(mouseEvent);
      this.overChild = null;
    }
    onMouseMoved(mouseEvent, mx, my);
  }

  private String setDescription;

  void mouseOver(MouseEvent mouseEvent) {
    this.setDescription = getDescription();
    if (this.setDescription != null) {
      getUI().setMouseoverHelpText(this.setDescription);
    }
    onMouseOver(mouseEvent);
  }

  void mouseOut(MouseEvent mouseEvent) {
    if (this.setDescription != null) {
      getUI().clearMouseoverHelpText();
      this.setDescription = null;
    }
    if (this.overChild != null) {
      this.overChild.mouseOut(mouseEvent);
      this.overChild = null;
    }
    onMouseOut(mouseEvent);
  }

  void mouseWheel(MouseEvent mouseEvent, float mx, float my, float delta) {
    this.mouseWheelEventConsumed = false;
    for (int i = this.mutableChildren.size() - 1; i >= 0; --i) {
      UIObject child = this.mutableChildren.get(i);
      if (child.isVisible() && child.contains(mx, my)) {
        child.mouseWheel(mouseEvent, mx - child.getX(), my - child.getY(), delta);
        this.mouseWheelEventConsumed = child.mouseWheelEventConsumed;
        break;
      }
    }
    if (!this.mouseWheelEventConsumed) {
      onMouseWheel(mouseEvent, mx, my, delta);
    }
  }

  void keyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
    this.keyEventConsumed = false;

    // First delegate to focused child elements which may handle this event
    if (this.focusedChild != null) {
      UIObject delegate = this.focusedChild;
      delegate.keyPressed(keyEvent, keyChar, keyCode);
      this.keyEventConsumed = delegate.keyEventConsumed;
    }

    // Next, check for copy/paste actions
    if (!this.keyEventConsumed) {
      if (keyEvent.isMetaDown() || keyEvent.isControlDown()) {
        if (keyCode == java.awt.event.KeyEvent.VK_C && this instanceof UICopy) {
          this.ui.lx.clipboard.setItem(((UICopy)this).onCopy());
          this.keyEventConsumed = true;
        } else if (keyCode == java.awt.event.KeyEvent.VK_V && this instanceof UIPaste) {
          LXClipboardItem item = this.ui.lx.clipboard.getItem();
          if (item != null) {
            ((UIPaste) this).onPaste(item);
            this.keyEventConsumed = true;
          }
        }
      }
    }

    // Finally, delegate to custom handler
    if (!this.keyEventConsumed) {
      onKeyPressed(keyEvent, keyChar, keyCode);
    }
  }

  void keyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {
    this.keyEventConsumed = false;
    if (this.focusedChild != null) {
      UIObject delegate = this.focusedChild;
      delegate.keyReleased(keyEvent, keyChar, keyCode);
      this.keyEventConsumed = delegate.keyEventConsumed;
    }
    if (!this.keyEventConsumed) {
      onKeyReleased(keyEvent, keyChar, keyCode);
    }
  }

  void keyTyped(KeyEvent keyEvent, char keyChar, int keyCode) {
    this.keyEventConsumed = false;
    if (this.focusedChild != null) {
      UIObject delegate = this.focusedChild;
      delegate.keyTyped(keyEvent, keyChar, keyCode);
      this.keyEventConsumed = delegate.keyEventConsumed;
    }
    if (!this.keyEventConsumed) {
      onKeyTyped(keyEvent, keyChar, keyCode);
    }
  }

  /**
   * Subclasses override when element is focused
   */
  protected void onFocus() {
  }

  /**
   * Subclasses override when element loses focus
   */
  protected void onBlur() {
  }
}
