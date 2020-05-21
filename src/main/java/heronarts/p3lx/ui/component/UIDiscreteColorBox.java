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

import heronarts.lx.color.DiscreteColorParameter;
import heronarts.lx.utils.LXUtils;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UIFocus;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIDiscreteColorBox extends UI2dComponent implements UIFocus {

  private class UIDiscreteColorMenu extends UI2dComponent {

    private final static int SPACING = 4;
    private final static int BOX_SIZE = 10;

    private UIDiscreteColorMenu(UI ui) {
      super(0, 0, 8 * BOX_SIZE + SPACING * 9, 3 * BOX_SIZE + SPACING * 4);
      setBackgroundColor(ui.theme.getDarkBackgroundColor());
      setBorderColor(ui.theme.getControlBorderColor());
    }

    @Override
    public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
      int xi = LXUtils.constrain((int) ((mx - SPACING) / (BOX_SIZE + SPACING)), 0, 8);
      int yi = LXUtils.constrain((int) ((my - SPACING) / (BOX_SIZE + SPACING)), 0, 3);
      parameter.setValue(xi + yi * 8);
      getUI().hideContextOverlay();
    }

    @Override
    public void onDraw(UI ui, PGraphics pg) {
      int selectedI = parameter.getValuei();
      for (int i = 0; i < DiscreteColorParameter.COLORS.length; ++i) {
        int x = i % 8;
        int y = i / 8;
        pg.fill(DiscreteColorParameter.COLORS[i]);
        if (i == selectedI) {
          pg.strokeWeight(2);
          pg.stroke(0xffffffff);
          pg.rect((x+1) * SPACING + x * 10 - 1, (y+1) * SPACING + y * 10 - 1, 11, 11);
        } else {
          pg.noStroke();
          pg.rect((x+1) * SPACING + x * 10, (y+1) * SPACING + y * 10, 10, 10);
        }
      }
      pg.strokeWeight(1);
    }
  }

  private final UIDiscreteColorMenu colorMenu;

  private final DiscreteColorParameter parameter;

  public UIDiscreteColorBox(UI ui, final DiscreteColorParameter parameter, float x, float y, float w, float h) {
    super(x, y, w, h);
    setBorderColor(ui.theme.getControlBorderColor());
    setBackgroundColor(parameter.getColor());
    this.parameter = parameter;
    this.colorMenu = new UIDiscreteColorMenu(ui);
    this.colorMenu.setVisible(false);
    parameter.addListener((p) -> {
      setBackgroundColor(parameter.getColor());
      this.colorMenu.redraw();
    });
  }

  @Override
  public String getDescription() {
    return UIParameterControl.getDescription(this.parameter);
  }

  private void toggleExpanded() {
    setExpanded(!this.colorMenu.isVisible());
  }

  private void setExpanded(boolean expanded) {
    if (this.colorMenu.isVisible() != expanded) {
      if (expanded) {
        this.colorMenu.setPosition(this, -this.colorMenu.getWidth() + UIDiscreteColorMenu.BOX_SIZE + UIDiscreteColorMenu.SPACING, -UIDiscreteColorMenu.SPACING);
        getUI().showContextOverlay(this.colorMenu);
      } else {
        getUI().hideContextOverlay();
      }
    }
  }

  @Override
  public void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
    setExpanded(true);
  }

  @Override
  public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
    if (keyCode == java.awt.event.KeyEvent.VK_LEFT) {
      consumeKeyEvent();
      this.parameter.decrement();
    } else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT) {
      consumeKeyEvent();
      this.parameter.increment();
    } else if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
      consumeKeyEvent();
      this.parameter.increment(8);
    } else if (keyCode == java.awt.event.KeyEvent.VK_UP) {
      consumeKeyEvent();
      this.parameter.decrement(8);
    } else if (keyCode == java.awt.event.KeyEvent.VK_SPACE || keyCode == java.awt.event.KeyEvent.VK_ENTER) {
      consumeKeyEvent();
      toggleExpanded();
    } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
      if (this.colorMenu.isVisible()) {
        consumeKeyEvent();
        setExpanded(false);
      }
    }
  }
}