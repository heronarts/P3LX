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

package heronarts.p3lx.ui.component;

import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.UITriggerSource;
import heronarts.p3lx.ui.UITriggerTarget;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UISwitch extends UIParameterControl implements UIFocus, UITriggerTarget, UITriggerSource {

  public final static int SWITCH_MARGIN = 6;
  public final static int SWITCH_SIZE = 28;
  public final static int WIDTH = SWITCH_SIZE + 2*SWITCH_MARGIN;

  protected boolean isMomentary = false;

  public UISwitch(float x, float y) {
    super(x, y, WIDTH, SWITCH_SIZE);
  }

  public UISwitch setMomentary(boolean momentary) {
    this.isMomentary = momentary;
    return this;
  }

  @Override
  public UIParameterControl setParameter(LXNormalizedParameter parameter) {
    if (!(parameter instanceof BooleanParameter)) {
      throw new IllegalArgumentException("UISwitch may only take BooleanParameter");
    }
    super.setParameter(parameter);
    setMomentary(getBooleanParameter().getMode() == BooleanParameter.Mode.MOMENTARY);
    return this;
  }

  private BooleanParameter getBooleanParameter() {
    return (BooleanParameter) this.parameter;
  }

  @Override
  public BooleanParameter getTriggerTarget() {
    return isMappable() ? getTriggerParameter() : null;
  }

  @Override
  public BooleanParameter getTriggerSource() {
    return isMappable() ? getTriggerParameter() : null;
  }

  private BooleanParameter getTriggerParameter() {
    if (this.parameter != null && this.parameter.getComponent() != null) {
      return getBooleanParameter();
    }
    return null;
  }

  @Override
  protected void onDraw(UI ui, PGraphics pg) {
    pg.stroke(ui.theme.getControlBorderColor());
    if (isEnabled() && (this.parameter != null)) {
      pg.fill((this.parameter.getValue() > 0) ? ui.theme.getPrimaryColor() :
        ui.theme.getControlBackgroundColor());
    } else {
      pg.fill(ui.theme.getControlDisabledColor());
    }
    pg.rect(SWITCH_MARGIN, 0, SWITCH_SIZE, SWITCH_SIZE);

    super.onDraw(ui, pg);
  }

  @Override
  protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
    super.onKeyPressed(keyEvent, keyChar, keyCode);
    if ((keyCode == java.awt.event.KeyEvent.VK_SPACE) || (keyCode == java.awt.event.KeyEvent.VK_ENTER)) {
      if (this.parameter != null) {
        consumeKeyEvent();
        if (this.isMomentary) {
          getBooleanParameter().setValue(true);
        } else {
          pushUndoCommand(getBooleanParameter());
          getBooleanParameter().toggle();
        }
      }
    }
  }

  @Override
  protected void onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {
    super.onKeyReleased(keyEvent, keyChar, keyCode);
    if ((keyCode == java.awt.event.KeyEvent.VK_SPACE) || (keyCode == java.awt.event.KeyEvent.VK_ENTER)) {
      if ((this.parameter != null) && this.isMomentary) {
        consumeKeyEvent();
        getBooleanParameter().setValue(false);
      }
    }
  }

  private boolean isOnSwitch(float mx, float my) {
    return
      (mx >= SWITCH_MARGIN) &&
      (mx < SWITCH_SIZE + SWITCH_MARGIN) &&
      (my < SWITCH_SIZE);
  }

  @Override
  protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
    super.onMousePressed(mouseEvent, mx, my);
    if (this.parameter != null && isOnSwitch(mx, my)) {
      if (this.isMomentary) {
        getBooleanParameter().setValue(true);
      } else {
        pushUndoCommand(getBooleanParameter());
        getBooleanParameter().toggle();
      }
    }
  }

  @Override
  protected void onMouseReleased(MouseEvent mouseEvent, float mx, float my) {
    super.onMouseReleased(mouseEvent, mx, my);
    if (this.isMomentary && (this.parameter != null) && isOnSwitch(mx, my)) {
      getBooleanParameter().setValue(false);
    }
  }

}
