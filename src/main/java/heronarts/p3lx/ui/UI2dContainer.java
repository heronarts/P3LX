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

import java.util.Iterator;
import java.util.List;

import processing.event.KeyEvent;

public class UI2dContainer extends UI2dComponent implements UIContainer, Iterable<UIObject> {

  public enum Layout {
    NONE,
    VERTICAL,
    HORIZONTAL,
    VERTICAL_GRID,
    HORIZONTAL_GRID
  }

  public enum ArrowKeyFocus {
    NONE,
    VERTICAL,
    HORIZONTAL
  };

  private Layout layout = Layout.NONE;

  ArrowKeyFocus arrowKeyFocus = ArrowKeyFocus.NONE;

  private int topPadding = 0, rightPadding = 0, bottomPadding = 0, leftPadding = 0;

  private int childMarginX = 0, childMarginY = 0;

  private float minHeight = 0, minWidth = 0;

  private UI2dContainer contentTarget;

  public UI2dContainer(float x, float y, float w, float h) {
    super(x, y, w, h);
    this.contentTarget = this;
  }

  public UI2dContainer setPadding(int padding) {
    return setPadding(padding, padding, padding, padding);
  }

  public UI2dContainer setPadding(int topPadding, int rightPadding, int bottomPadding, int leftPadding) {
    boolean redraw = false;
    if (this.topPadding != topPadding) {
      this.topPadding = topPadding;
      redraw = true;
    }
    if (this.rightPadding != rightPadding) {
      this.rightPadding = rightPadding;
      redraw = true;
    }
    if (this.bottomPadding != bottomPadding) {
      this.bottomPadding = bottomPadding;
      redraw = true;
    }
    if (this.leftPadding != leftPadding) {
      this.leftPadding = leftPadding;
      redraw = true;
    }
    if (redraw) {
      redraw();
    }
    return this;
  }

  public UI2dContainer setChildMargin(int childMargin) {
    return setChildMargin(childMargin, childMargin);
  }

  public UI2dContainer setChildMargin(int childMarginY, int childMarginX) {
    if (this.contentTarget.childMarginX != childMarginX) {
      this.contentTarget.childMarginX = childMarginX;
      this.contentTarget.reflow();
    }
    if (this.contentTarget.childMarginY != childMarginY) {
      this.contentTarget.childMarginY = childMarginY;
      this.contentTarget.reflow();
    }
    return this;
  }

  public UI2dContainer setMinWidth(float minWidth) {
    if (this.contentTarget.minWidth != minWidth) {
      this.contentTarget.minWidth = minWidth;
      reflow();
    }
    return this;
  }

  public UI2dContainer setMinHeight(float minHeight) {
    if (this.contentTarget.minHeight != minHeight) {
      this.contentTarget.minHeight = minHeight;
      reflow();
    }
    return this;
  }

  public UI2dContainer setLayout(Layout layout) {
    if (this.contentTarget.layout != layout) {
      this.contentTarget.layout = layout;
      this.contentTarget.reflow();
    }
    return this;
  }

  public UI2dContainer setArrowKeyFocus(ArrowKeyFocus keyFocus) {
    this.contentTarget.arrowKeyFocus = keyFocus;
    return this;
  }

  protected void reflow() {
    if (this.layout == Layout.VERTICAL) {
      float y = this.topPadding;
      for (UIObject child : this) {
        if (child.isVisible()) {
          UI2dComponent component = (UI2dComponent) child;
          component.setY(y);
          y += component.getHeight() + this.childMarginY;
        }
      }
      y += this.bottomPadding;
      setContentHeight(Math.max(this.minHeight, y - this.childMarginY));
    } else if (this.layout == Layout.HORIZONTAL) {
      float x = this.leftPadding;
      for (UIObject child : this) {
        if (child.isVisible()) {
          UI2dComponent component = (UI2dComponent) child;
          component.setX(x);
          x += component.getWidth() + this.childMarginX;
        }
      }
      x += this.rightPadding;
      setContentWidth(Math.max(this.minWidth, x - this.childMarginX));
    } else if (this.layout == Layout.VERTICAL_GRID) {
      float x = this.leftPadding;
      float y = this.topPadding;
      float w = 0;
      for (UIObject child : this) {
        if (child.isVisible()) {
          UI2dComponent component = (UI2dComponent) child;
          if (y + component.getHeight() > getContentHeight()) {
            x += w + this.childMarginX;
            y = this.topPadding;
            w = 0;
          }
          component.setPosition(x, y);
          w = Math.max(0, component.getWidth());
          y += component.getHeight() + this.childMarginY;
        }
      }
      setContentWidth(Math.max(this.minWidth, x + w));
    } else if (this.layout == Layout.HORIZONTAL_GRID) {
      float x = this.leftPadding;
      float y = this.topPadding;
      float h = 0;
      for (UIObject child : this) {
        if (child.isVisible()) {
          UI2dComponent component = (UI2dComponent) child;
          if (x + component.getWidth() > getContentWidth()) {
            y += h + this.childMarginY;
            x = this.leftPadding;
            h = 0;
          }
          component.setPosition(x, y);
          h = Math.max(0, component.getHeight());
          x += component.getWidth() + this.childMarginX;
        }
      }
      setContentHeight(Math.max(this.minHeight, y + h));
    }
  }

  protected UI2dContainer setContentTarget(UI2dContainer contentTarget) {
    if (this.mutableChildren.contains(contentTarget)) {
      throw new IllegalStateException("contentTarget already belongs to container: " + contentTarget);
    }
    this.contentTarget = contentTarget;
    this.mutableChildren.add(contentTarget);
    contentTarget.parent = this;
    contentTarget.setUI(this.ui);
    redraw();
    return this;
  }

  protected UI2dContainer addTopLevelComponent(UI2dComponent child) {
    if (child.parent != null) {
      child.removeFromContainer();
    }
    this.mutableChildren.add(child);
    child.parent = this;
    child.setUI(this.ui);
    redraw();
    return this;
  }

  /**
   * Returns the object that elements are added to when placed in this container.
   * In most cases, it will be "this" - but some elements have special subcontainers.
   *
   * @return Element
   */
  @Override
  public UIObject getContentTarget() {
    return this.contentTarget;
  }

  @Override
  public float getContentWidth() {
    return getContentTarget().getWidth();
  }

  @Override
  public float getContentHeight() {
    return getContentTarget().getHeight();
  }

  public UI2dContainer setContentWidth(float w) {
    return setContentSize(w, getContentHeight());
  }

  public UI2dContainer setContentHeight(float h) {
    return setContentSize(getContentWidth(), h);
  }

  public UI2dContainer setContentSize(float w, float h) {
    this.contentTarget.setSize(w, h);
    return this;
  }

  @Override
  public Iterator<UIObject> iterator() {
    return this.contentTarget.mutableChildren.iterator();
  }

  public List<UIObject> getChildren() {
    return this.contentTarget.mutableChildren;
  }

  private void keyFocus(KeyEvent keyEvent, int delta) {
    if (this.children.size() > 0) {
      UIObject focusedChild = getFocusedChild();
      if (focusedChild == null) {
        for (UIObject object : this.children) {
          if (object.isVisible() && (object instanceof UIKeyFocus)) {
            object.focus(keyEvent);
            break;
          }
        }
      } else {
        int index = this.children.indexOf(focusedChild);
        while (true) {
          index += delta;
          if (index < 0 || index >= this.children.size()) {
            break;
          }
          UIObject object = this.children.get(index);
          if (object.isVisible() && (object instanceof UIKeyFocus)) {
            object.focus(keyEvent);
            break;
          }
        }
      }
    }
  }

  @Override
  protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
    super.onKeyPressed(keyEvent, keyChar, keyCode);
    if (this.arrowKeyFocus == ArrowKeyFocus.VERTICAL) {
      if (keyCode == java.awt.event.KeyEvent.VK_UP) {
        consumeKeyEvent();
        keyFocus(keyEvent, -1);
      } else if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
        consumeKeyEvent();
        keyFocus(keyEvent, 1);
      }
    } else if (this.arrowKeyFocus == ArrowKeyFocus.HORIZONTAL) {
      if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
        consumeKeyEvent();
        keyFocus(keyEvent, -1);
      } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
        consumeKeyEvent();
        keyFocus(keyEvent, 1);
      }
    }
  }
}
