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
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.UITriggerSource;
import heronarts.p3lx.ui.UITriggerTarget;
import heronarts.p3lx.ui.UIControlTarget;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIButton extends UI2dComponent implements UIControlTarget, UITriggerSource, UITriggerTarget, UIFocus {

  protected boolean active = false;
  protected boolean isMomentary = false;

  protected int inactiveColor = UI.get().theme.getControlBackgroundColor();
  protected int activeColor = UI.get().theme.getPrimaryColor();

  private String activeLabel = "";
  private String inactiveLabel = "";

  private PImage activeIcon = null;
  private PImage inactiveIcon = null;

  private boolean triggerable = false;
  protected boolean enabled = true;

  private EnumParameter<? extends Object> enumParameter = null;
  private BooleanParameter booleanParameter = null;

  private float iconOffsetX = 0, iconOffsetY = 0;

  protected long exactToggleTime = -1;

  private final LXParameterListener booleanParameterListener = new LXParameterListener() {
    public void onParameterChanged(LXParameter p) {
      setActive(booleanParameter.isOn(), false);
    }
  };

  private final LXParameterListener enumParameterListener = new LXParameterListener() {
    public void onParameterChanged(LXParameter p) {
      setLabel(enumParameter.getEnum().toString());
    }
  };

  public UIButton() {
    this(0, 0, 0, 0);
  }

  public UIButton(float x, float y, float w, float h) {
    super(x, y, w, h);
    setBorderColor(UI.get().theme.getControlBorderColor());
    setFontColor(UI.get().theme.getControlTextColor());
    setBackgroundColor(this.inactiveColor);
  }

  public UIButton setEnabled(boolean enabled) {
    if (this.enabled != enabled) {
      this.enabled = enabled;
      redraw();
    }
    return this;
  }

  public UIButton setTriggerable(boolean triggerable) {
    this.triggerable = triggerable;
    return this;
  }

  @Override
  public String getDescription() {
    if (this.booleanParameter != null) {
      return UIParameterControl.getDescription(this.booleanParameter);
    }
    if (this.enumParameter != null) {
      return UIParameterControl.getDescription(this.enumParameter);
    }
    return super.getDescription();
  }

  public BooleanParameter getParameter() {
    return this.booleanParameter;
  }

  private void removeParameter() {
    if (this.booleanParameter != null) {
      this.booleanParameter.removeListener(this.booleanParameterListener);
      this.booleanParameter = null;
    }
    if (this.enumParameter != null) {
      this.enumParameter.removeListener(this.enumParameterListener);
      this.enumParameter = null;
    }
  }

  public UIButton setParameter(EnumParameter<? extends Object> parameter) {
    removeParameter();
    if (parameter != null) {
      this.enumParameter = parameter;
      this.enumParameter.addListener(this.enumParameterListener);
      setActive(false);
      setMomentary(true);
      setLabel(this.enumParameter.getEnum().toString());
    }
    return this;
  }

  public UIButton setParameter(BooleanParameter parameter) {
    removeParameter();
    if (parameter != null) {
      this.booleanParameter = parameter;
      this.booleanParameter.addListener(this.booleanParameterListener);
      setMomentary(this.booleanParameter.getMode() == BooleanParameter.Mode.MOMENTARY);
      setActive(this.booleanParameter.isOn(), false);
    }
    return this;
  }

  public UIButton setMomentary(boolean momentary) {
    this.isMomentary = momentary;
    return this;
  }

  public UIButton setIconOffset(float iconOffsetX, float iconOffsetY) {
    boolean redraw = false;
    if (this.iconOffsetX != iconOffsetX) {
      this.iconOffsetX = iconOffsetX;
      redraw = true;
    }
    if (this.iconOffsetY != iconOffsetY) {
      this.iconOffsetY = iconOffsetY;
      redraw = true;
    }
    if (redraw) {
      redraw();
    }
    return this;
  }

  public UIButton setIconOffsetX(float iconOffsetX) {
    if (this.iconOffsetX != iconOffsetX) {
      this.iconOffsetX = iconOffsetX;
      redraw();
    }
    return this;
  }

  public UIButton setIconOffsetY(float iconOffsetY) {
    if (this.iconOffsetY != iconOffsetY) {
      this.iconOffsetY = iconOffsetY;
      redraw();
    }
    return this;
  }

  @Override
  protected void onDraw(UI ui, PGraphics pg) {
    if (!this.enabled) {
      pg.fill(ui.theme.getControlDisabledColor());
      pg.noStroke();
      pg.rect(1, 1, this.width-2, this.height-2);
    }

    PImage icon = this.active ? this.activeIcon : this.inactiveIcon;
    if (icon != null) {
      if (!this.active) {
        pg.tint(getFontColor());
      }
      pg.image(icon, this.width/2 - icon.width/2 + this.iconOffsetX, this.height/2 - icon.height/2 + this.iconOffsetY);
      pg.noTint();
    } else {
      String label = this.active ? this.activeLabel : this.inactiveLabel;
      if ((label != null) && (label.length() > 0)) {
        pg.fill(this.active ? UI.WHITE : getFontColor());
        pg.textFont(hasFont() ? getFont() : ui.theme.getControlFont());
        if (this.textAlignVertical == PConstants.CENTER) {
          pg.textAlign(PConstants.CENTER, PConstants.CENTER);
          pg.text(label, this.width / 2 + this.textOffsetX, this.height / 2 + this.textOffsetY);
        } else {
          pg.textAlign(PConstants.CENTER);
          pg.text(label, this.width / 2 + this.textOffsetX, (int) (this.height * .75) + this.textOffsetY);
        }
      }
    }
  }

  @Override
  protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
    if (this.enabled) {
      this.exactToggleTime = mouseEvent.getMillis();
      setActive(this.isMomentary ? true : !this.active);
    }
  }

  @Override
  protected void onMouseReleased(MouseEvent mouseEvent, float mx, float my) {
    if (this.enabled) {
      if (this.isMomentary) {
        this.exactToggleTime = mouseEvent.getMillis();
        setActive(false);
      }
    }
  }

  @Override
  protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
    if ((keyCode == java.awt.event.KeyEvent.VK_SPACE) || (keyCode == java.awt.event.KeyEvent.VK_ENTER)) {
      if (this.enabled) {
        this.exactToggleTime = keyEvent.getMillis();
        setActive(this.isMomentary ? true : !this.active);
      }
      consumeKeyEvent();
    }
  }

  @Override
  protected void onKeyReleased(KeyEvent keyEvent, char keyChar, int keyCode) {
    if ((keyCode == java.awt.event.KeyEvent.VK_SPACE) || (keyCode == java.awt.event.KeyEvent.VK_ENTER)) {
      if (this.enabled && this.isMomentary) {
        this.exactToggleTime = keyEvent.getMillis();
        setActive(false);
      }
      consumeKeyEvent();
    }
  }

  public boolean isActive() {
    return this.active;
  }

  public UIButton setActive(boolean active) {
    return setActive(active, true);
  }

  protected UIButton setActive(boolean active, boolean pushToParameter) {
    if (this.active != active) {
      this.active = active;
      setBackgroundColor(active ? this.activeColor : this.inactiveColor);
      if (pushToParameter) {
        if (this.enumParameter != null) {
          if (active) {
            this.enumParameter.increment();
          }
        } else if (this.booleanParameter != null) {
          this.booleanParameter.setValue(active);
        }
      }
      onToggle(active);
      redraw();
    }
    return this;
  }

  public UIButton toggle() {
    return setActive(!this.active);
  }

  /**
   * Subclasses may override this to handle changes to the button's state
   *
   * @param active Whether button is active
   */
  protected void onToggle(boolean active) {
  }

  public UIButton setActiveColor(int activeColor) {
    if (this.activeColor != activeColor) {
      this.activeColor = activeColor;
      if (this.active) {
        setBackgroundColor(activeColor);
      }
    }
    return this;
  }

  public UIButton setInactiveColor(int inactiveColor) {
    if (this.inactiveColor != inactiveColor) {
      this.inactiveColor = inactiveColor;
      if (!this.active) {
        setBackgroundColor(inactiveColor);
      }
    }
    return this;
  }

  public UIButton setLabel(String label) {
    setActiveLabel(label);
    setInactiveLabel(label);
    return this;
  }

  public UIButton setActiveLabel(String activeLabel) {
    if (!this.activeLabel.equals(activeLabel)) {
      this.activeLabel = activeLabel;
      if (this.active) {
        redraw();
      }
    }
    return this;
  }

  public UIButton setInactiveLabel(String inactiveLabel) {
    if (!this.inactiveLabel.equals(inactiveLabel)) {
      this.inactiveLabel = inactiveLabel;
      if (!this.active) {
        redraw();
      }
    }
    return this;
  }

  public UIButton setIcon(PImage icon) {
    setActiveIcon(icon);
    setInactiveIcon(icon);
    return this;
  }

  public UIButton setActiveIcon(PImage activeIcon) {
    if (this.activeIcon != activeIcon) {
      this.activeIcon = activeIcon;
      if (this.active) {
        redraw();
      }
    }
    return this;
  }

  public UIButton setInactiveIcon(PImage inactiveIcon) {
    if (this.inactiveIcon != inactiveIcon) {
      this.inactiveIcon = inactiveIcon;
      if (!this.active) {
        redraw();
      }
    }
    return this;
  }

  @Override
  public LXParameter getControlTarget() {
    return isMappable() ? this.booleanParameter : null;
  }

  @Override
  public BooleanParameter getTriggerSource() {
    return this.triggerable ? this.booleanParameter : null;
  }

  @Override
  public BooleanParameter getTriggerTarget() {
    return this.triggerable ? this.booleanParameter : null;
  }

}
