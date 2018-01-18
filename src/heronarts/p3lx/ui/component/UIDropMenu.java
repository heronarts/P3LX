/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
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

package heronarts.p3lx.ui.component;

import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UIControlTarget;
import heronarts.p3lx.ui.UIFocus;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIDropMenu extends UI2dComponent implements UIFocus, UIControlTarget, LXParameterListener {

  private DiscreteParameter parameter = null;

  private boolean expanded = false;

  public enum Direction {
    DOWN,
    UP
  };

  private Direction direction = Direction.DOWN;
  private float closedY;
  private float closedHeight;

  private int highlight = -1;

  private String[] options;

  public UIDropMenu(float x, float y, float w, float h, DiscreteParameter parameter) {
    super(x, y, w, h);
    this.closedY = y;
    this.closedHeight = h;
    setParameter(parameter);
  }

  @Override
  public String getDescription() {
    return UIParameterControl.getDescription(this.parameter);
  }

  public UIDropMenu setParameter(DiscreteParameter parameter) {
    if (this.parameter != null) {
      this.parameter.removeListener(this);
    }
    this.parameter = parameter;
    setOptions(this.parameter.getOptions());
    this.parameter.addListener(this);
    return this;
  }

  public void onParameterChanged(LXParameter p) {
    this.highlight = this.parameter.getValuei();
    redraw();
  }

  /**
   * Sets the direction that this drop menu opens, up or down
   *
   * @param direction Direction menu should open
   * @return this
   */
  public UIDropMenu setDirection(Direction direction) {
    this.direction = direction;
    return this;
  }

  /**
   * Sets the list of string options to display in the menu
   *
   * @param options Options array
   * @return this
   */
  public UIDropMenu setOptions(String[] options) {
    this.options = options;
    return this;
  }

  @Override
  protected int getFocusSize() {
    return 4;
  }

  @Override
  public void onBlur() {
    setExpanded(false);
  }

  @Override
  public void onDraw(UI ui, PGraphics pg) {
    onDraw(ui, pg, false);
  }

  public void onDraw(UI ui, PGraphics pg, boolean overlay) {
    if (this.expanded && !overlay) {
      // If we are expanded, then we only need signal to the
      // overlay to redraw appropriately
      getUI().redrawDropMenu();
      return;
    }

    String text;
    if (this.options != null) {
      text = this.options[this.parameter.getValuei()];
    } else {
      text = Integer.toString(this.parameter.getValuei());
    }
    pg.stroke(ui.theme.getControlBorderColor());
    pg.fill(ui.theme.getControlBackgroundColor());
    pg.rect(0, 0, this.width-1, this.height-1);

    float textY;
    float lineY;
    float highlightY;
    float highlightHeight = this.closedHeight;
    switch (this.direction) {
    case UP:
      lineY = textY = this.height - this.closedHeight;
      highlightY = this.closedHeight * this.highlight;
      if (this.highlight == 0) {
        ++highlightY;
        --highlightHeight;
      }
      break;
    default:
    case DOWN:
      textY = 0;
      lineY = this.closedHeight;
      highlightY = this.closedHeight * (1 + this.highlight);
      if (this.highlight == this.parameter.getRange() - 1) {
        --highlightHeight;
      }
      break;
    }

    if (this.expanded) {
      if (this.highlight == 0) {
        ++highlightY;
        --highlightHeight;
      }
      pg.line(1, lineY, this.width-2, lineY);
      pg.noStroke();
      pg.fill(ui.theme.getPrimaryColor());
      pg.rect(1, highlightY, this.width-2, highlightHeight);
    }

    pg.textFont(hasFont() ? getFont() : ui.theme.getControlFont());
    pg.fill(ui.theme.getControlTextColor());
    pg.textAlign(PConstants.LEFT, PConstants.TOP);
    pg.text(clipTextToWidth(pg, text, this.width - 12), 4 + this.textOffsetX, 4 + textY + this.textOffsetY);
    pg.textAlign(PConstants.RIGHT, PConstants.TOP);
    pg.text("▼", this.width-4, 4 + textY + this.textOffsetY);

    if (this.expanded) {
      int range = this.parameter.getRange();
      float yp = (this.direction == Direction.DOWN) ? this.closedHeight : 0;
      for (int i = 0; i < range; ++i) {
        String label = (this.options != null) ? this.options[i] : ("" + i);
        pg.fill(i == this.highlight ? UI.WHITE : ui.theme.getControlTextColor());
        pg.textAlign(PConstants.LEFT, PConstants.TOP);
        pg.text(clipTextToWidth(pg, label, this.width - 6), 4, yp + 4);
        yp += this.closedHeight;
      }
    }
  }

  private void toggleExpanded() {
    setExpanded(!this.expanded);
  }

  private void setExpanded(boolean expanded) {
    if (this.expanded != expanded) {
      this.expanded = expanded;
      if (expanded) {
        this.highlight = this.parameter.getValuei();
        if (this.direction == Direction.UP) {
          setPosition(this.x, this.closedY - this.closedHeight * this.parameter.getRange());
        }
        setSize(this.width, this.closedHeight * (this.parameter.getRange() + 1));
        getUI().showDropMenu(this);
      } else {
        getUI().hideDropMenu();
        setPosition(this.x, this.closedY);
        setSize(this.width, this.closedHeight);
      }
    }
  }

  private int getSelectedIndex(float y) {
    switch (this.direction) {
    case UP:
      if (y >= this.height - this.closedHeight) {
        return -1;
      }
      return (int) (y / this.closedHeight);
    default:
    case DOWN:
      if (y < this.closedHeight) {
        return -1;
      }
      return (int) ((y - this.closedHeight) / this.closedHeight);
    }
  }

  @Override
  public void onMousePressed(MouseEvent mouseEvent, float x, float y) {
    if (!this.expanded) {
      toggleExpanded();
    } else {
      int selected = this.getSelectedIndex(y);
      if (selected >= 0) {
        this.parameter.setValue(highlight);
      }
      toggleExpanded();
    }
  }

  @Override
  public void onMouseMoved(MouseEvent mouseEvent, float x, float y) {
    int selected = this.getSelectedIndex(y);
    if (selected >= 0 && (this.highlight != selected)) {
      this.highlight = selected;
      redraw();
    }
  }

  @Override
  public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
    if (keyCode == java.awt.event.KeyEvent.VK_ENTER ||
        keyCode == java.awt.event.KeyEvent.VK_SPACE) {
      consumeKeyEvent();
      toggleExpanded();
    } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
      consumeKeyEvent();
      setExpanded(false);
    } else if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
      consumeKeyEvent();
      this.parameter.increment();
    } else if (keyCode == java.awt.event.KeyEvent.VK_UP) {
      consumeKeyEvent();
      this.parameter.decrement();
    }
  }

  @Override
  public LXParameter getControlTarget() {
    return this.parameter;
  }

}