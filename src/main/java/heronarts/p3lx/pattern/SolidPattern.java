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
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.p3lx.pattern;

import heronarts.lx.LXCategory;
import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.pattern.LXPattern;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIControlTarget;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.CustomDeviceUI;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

@LXCategory(LXCategory.COLOR)
public class SolidPattern extends LXPattern implements CustomDeviceUI {

  public final ColorParameter color =
    new ColorParameter("Color")
    .setDescription("Color of the pattern");

  public SolidPattern(LX lx) {
    this(lx, LXColor.RED);
  }

  public SolidPattern(LX lx, int color) {
    super(lx);
    this.color.setColor(color);
    addParameter("color", this.color);
  }

  @Override
  public void run(double deltaMs) {
    setColors(LXColor.hsb(
      this.color.hue.getValue(),
      this.color.saturation.getValue(),
      this.color.brightness.getValue()
    ));
  }

  private static final int SLIDER_WIDTH = UIKnob.WIDTH;
  private static final int SLIDER_MARGIN = 6;
  private static final int SLIDER_SPACING = SLIDER_WIDTH + 4;

  @Override
  public void buildDeviceUI(UI ui, UI2dContainer device) {
    float knobY = device.getContentHeight() - UIKnob.HEIGHT;
    float xp = 0;
    new UIKnob(xp, knobY).setParameter(this.color.hue).addToContainer(device);
    new HueSlider(ui, xp, 0, SLIDER_WIDTH, knobY - SLIDER_MARGIN).addToContainer(device);
    xp += SLIDER_SPACING;
    new UIKnob(xp, knobY).setParameter(this.color.saturation).addToContainer(device);
    new SaturationSlider(ui, xp, 0, SLIDER_WIDTH, knobY - SLIDER_MARGIN).addToContainer(device);
    xp += SLIDER_SPACING;
    new UIKnob(xp, knobY).setParameter(this.color.brightness).addToContainer(device);
    new BrightnessSlider(ui, xp, 0, SLIDER_WIDTH, knobY - SLIDER_MARGIN).addToContainer(device);
    xp += SLIDER_SPACING;
    device.setContentWidth(xp - SLIDER_MARGIN);
  }

  private abstract class Slider extends UI2dComponent implements UIFocus, UIControlTarget {

    private final CompoundParameter parameter;

    Slider(UI ui, CompoundParameter parameter, float x, float y, float w, float h) {
      super(x, y, w, h);
      this.parameter = parameter;
      setBorderColor(ui.theme.getControlBorderColor());
      color.addListener(new LXParameterListener() {
        public void onParameterChanged(LXParameter p) {
          redraw();
        }
      });
    }

    private void updateParameter(float mx, float my) {
      this.parameter.setNormalized(1. - my / (this.height-1));
    }

    @Override
    public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
      double amt = .025;
      if (keyEvent.isShiftDown()) {
        amt = .1;
      }
      if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
        this.parameter.setNormalized(this.parameter.getBaseNormalized() - amt);
      } else if (keyCode == java.awt.event.KeyEvent.VK_UP) {
        this.parameter.setNormalized(this.parameter.getBaseNormalized() + amt);
      }
    }

    @Override
    public void onMousePressed(MouseEvent MouseEvent, float mx, float my) {
      updateParameter(mx, my);
    }

    @Override
    public void onMouseDragged(MouseEvent MouseEvent, float mx, float my, float dx, float dy) {
      updateParameter(mx, my);
    }

    protected void drawValue(UI ui, PGraphics pg) {
      int y = (int) LXUtils.constrainf(this.height - 1 - this.parameter.getBaseNormalizedf() * (this.height-1), 1, this.height-2);

      pg.noFill();
      pg.stroke(0xff111111);
      pg.point(1, y-1);
      pg.point(1, y);
      pg.point(1, y+1);
      pg.point(2, y);

      pg.point(this.width-2, y-1);
      pg.point(this.width-2, y);
      pg.point(this.width-2, y+1);
      pg.point(this.width-3, y);

      pg.stroke(0x66ffffff);
      pg.line(3, y, this.width-4, y);

    }

    @Override
    public LXParameter getControlTarget() {
      return this.parameter;
    }

  }

  private class HueSlider extends Slider {
    HueSlider(UI ui, float x, float y, float w, float h) {
      super(ui, color.hue, x, y, w, h);
    }

    @Override
    public void onDraw(UI ui, PGraphics pg) {
      for (int i = 0; i < this.height-1; ++i) {
        pg.stroke(LX.hsb(i * 360.f / (this.height-1), 100, 100));
        pg.line(0, this.height-1-i, this.width-1, this.height-1-i);
      }
      drawValue(ui, pg);
    }
  }

  private class SaturationSlider extends Slider {
    SaturationSlider(UI ui, float x, float y, float w, float h) {
      super(ui, color.saturation, x, y, w, h);
    }

    @Override
    public void onDraw(UI ui, PGraphics pg) {
      for (int i = 0; i < this.height; ++i) {
        pg.stroke(LX.hsb(color.hue.getBaseValuef(), i * 100.f / (this.height-1), color.brightness.getValuef()));
        pg.line(0, this.height-1-i, this.width-1, this.height-1-i);
      }
      drawValue(ui, pg);
    }
  }

  private class BrightnessSlider extends Slider {
    BrightnessSlider(UI ui, float x, float y, float w, float h) {
      super(ui, color.brightness, x, y, w, h);
    }

    @Override
    public void onDraw(UI ui, PGraphics pg) {
      for (int i = 0; i < this.height; ++i) {
        pg.stroke(LX.hsb(color.hue.getBaseValuef(), color.saturation.getBaseValuef(), i * 100.f / (this.height-1)));
        pg.line(0, this.height-1-i, this.width-1, this.height-1-i);
      }
      drawValue(ui, pg);
    }
  }

}
