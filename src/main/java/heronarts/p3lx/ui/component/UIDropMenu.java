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

import heronarts.lx.command.LXCommand;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UIContextActions;
import heronarts.p3lx.ui.UIControlTarget;
import heronarts.p3lx.ui.UIFocus;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIDropMenu extends UIParameterComponent implements UIFocus, UIControlTarget, LXParameterListener {

  private DiscreteParameter parameter = null;

  public enum Direction {
    DOWN,
    UP
  };

  private Direction direction = Direction.DOWN;

  private String[] options;
  private UIContextActions.Action[] actions;

  private boolean enabled = true;

  private final UIContextMenu contextMenu;

  public UIDropMenu(float x, float y, float w, float h) {
    this(x, y, w, h, null);
  }

  public UIDropMenu(float w, DiscreteParameter parameter) {
    this(w, DEFAULT_HEIGHT, parameter);
  }

  public UIDropMenu(float w, float h, DiscreteParameter parameter) {
    this(0, 0, w, h, parameter);
  }

  public UIDropMenu(float x, float y, float w, float h, DiscreteParameter parameter) {
    super(x, y, w, h);
    this.contextMenu = new UIContextMenu(x, y, w, h);
    setParameter(parameter);
    setBackgroundColor(UI.get().theme.getControlBackgroundColor());
    setBorderColor(UI.get().theme.getControlBorderColor());
  }

  @Override
  public String getDescription() {
    return UIParameterControl.getDescription(this.parameter);
  }

  @Override
  public DiscreteParameter getParameter() {
    return this.parameter;
  }

  public UIDropMenu setEnabled(boolean enabled) {
    if (this.enabled != enabled) {
      this.enabled = enabled;
      if (!enabled) {
        setExpanded(false);
      }
      redraw();
    }
    return this;
  }

  public UIDropMenu setParameter(DiscreteParameter parameter) {
    if (this.parameter != null) {
      this.parameter.removeListener(this);
    }
    this.parameter = parameter;
    this.actions = new UIContextActions.Action[parameter.getRange()];
    for (int i = 0; i < this.actions.length; ++i) {
      final int ii = i;
      this.actions[i] = new UIContextActions.Action(String.valueOf(i)) {
        @Override
        public void onContextAction(UI ui) {
          if (useCommandEngine) {
            getLX().command.perform(new LXCommand.Parameter.SetValue(parameter, ii));
          } else {
            parameter.setValue(ii);
          }
        }
      };
    }
    setOptions(this.parameter.getOptions());
    this.contextMenu.setActions(this.actions);
    this.contextMenu.setHighlight(this.parameter.getValuei());
    this.parameter.addListener(this);
    redraw();
    return this;
  }

  public void onParameterChanged(LXParameter p) {
    this.contextMenu.setHighlight(this.parameter.getValuei());
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
    for (int i = 0; i < options.length; ++i) {
      this.actions[i].setLabel(options[i]);
    }
    return this;
  }

  @Override
  protected int getFocusSize() {
    return 4;
  }

  @Override
  public void onDraw(UI ui, PGraphics pg) {
    if (!this.enabled) {
      pg.fill(ui.theme.getControlDisabledColor());
      pg.noStroke();
      pg.rect(1, 1, this.width-2, this.height-2);
    }

    String text;
    if (this.options != null) {
      text = this.options[this.parameter.getValuei()];
    } else {
      text = Integer.toString(this.parameter.getValuei());
    }

    pg.textFont(hasFont() ? getFont() : ui.theme.getControlFont());
    pg.fill(this.enabled ? ui.theme.getControlTextColor() : ui.theme.getControlDisabledTextColor());
    pg.textAlign(PConstants.LEFT, PConstants.TOP);
    pg.text(clipTextToWidth(pg, text, this.width - 12), 4 + this.textOffsetX, 4 + this.textOffsetY);
    pg.textAlign(PConstants.RIGHT, PConstants.TOP);
    pg.text("\u25BC", this.width-4, 4 + this.textOffsetY);

  }

  private void toggleExpanded() {
    setExpanded(!this.contextMenu.isVisible());
  }

  private void setExpanded(boolean expanded) {
    if (this.contextMenu.isVisible() != expanded) {
      if (expanded) {
        this.contextMenu.setHighlight(this.parameter.getValuei());
        if (this.direction == Direction.UP) {
          this.contextMenu.setPosition(this, 0, -this.contextMenu.getHeight());
        } else {
          this.contextMenu.setPosition(this, 0, this.height);
        }
        this.contextMenu.setWidth(this.width);
        getUI().showContextOverlay(this.contextMenu);
      } else {
        getUI().hideContextOverlay();
      }
    }
  }

  @Override
  public void onMousePressed(MouseEvent mouseEvent, float x, float y) {
    if (this.enabled) {
      toggleExpanded();
    }
  }

  @Override
  public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
    if (this.enabled) {
      if (keyCode == java.awt.event.KeyEvent.VK_ENTER || keyCode == java.awt.event.KeyEvent.VK_SPACE) {
        consumeKeyEvent();
        toggleExpanded();
      } else if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
        consumeKeyEvent();
        if (this.useCommandEngine) {
          getLX().command.perform(new LXCommand.Parameter.Increment(this.parameter));
        } else {
          this.parameter.increment();
        }
      } else if (keyCode == java.awt.event.KeyEvent.VK_UP) {
        consumeKeyEvent();
        if (this.useCommandEngine) {
          getLX().command.perform(new LXCommand.Parameter.Decrement(this.parameter));
        } else {
          this.parameter.decrement();
        }
      } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
        if (this.contextMenu.isVisible()) {
          consumeKeyEvent();
          setExpanded(false);
        }
      }
    }
  }

  @Override
  public LXParameter getControlTarget() {
    if (isMappable() && this.parameter != null && this.parameter.isMappable() && this.parameter.getParent() != null) {
      return this.parameter;
    }
    return null;
  }

}