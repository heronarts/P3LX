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
 * @author Mark C. Slee <mark@heronarts.com>
 */

package heronarts.p3lx.pattern;

import java.io.IOException;

import heronarts.lx.LXCategory;
import heronarts.lx.LX;
import heronarts.lx.parameter.BoundedParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXListenableNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.script.LXScriptPattern;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIObject;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.component.UIKnob;
import heronarts.p3lx.ui.component.UITextBox;
import heronarts.p3lx.ui.CustomDeviceUI;
import processing.core.PConstants;

@LXCategory(LXCategory.OTHER)
public class JavascriptPattern extends LXScriptPattern implements CustomDeviceUI {

  private static final int MIN_WIDTH = 120;

  private UI2dContainer knobs;

  public JavascriptPattern(LX lx) {
    super(lx);
  }

  @Override
  public void buildDeviceUI(UI ui, final UI2dContainer device) {
    device.setContentWidth(MIN_WIDTH);

    new UITextBox(0, 0, device.getContentWidth() - 22, 20)
    .setParameter(this.scriptPath)
    .setTextAlignment(PConstants.CENTER)
    .addToContainer(device);

    new UIButton(device.getContentWidth() - 20, 0, 20, 20) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          initialize();
        }
      }
    }
    .setLabel("\u21BA")
    .setMomentary(true)
    .addToContainer(device);

    new UIButton(0, 24, device.getContentWidth(), 16) {
      @Override
      public void onToggle(boolean on) {
        if (on) {
          try {
            java.awt.Desktop.getDesktop().edit(getFile());
          } catch (IOException iox) {
            System.err.println(iox.getLocalizedMessage());
          }
        }
      }
    }
    .setLabel("Edit")
    .setMomentary(true)
    .addToContainer(device);

    this.knobs = new UI2dContainer(0, 44, device.getContentWidth(), device.getContentHeight() - 44) {
      @Override
      protected void onResize() {
        device.setContentWidth(getWidth());
      }
    };
    knobs.setLayout(UI2dContainer.Layout.VERTICAL_GRID);
    knobs.setPadding(2, 0, 0, 0);
    knobs.setChildMargin(2, 4);
    knobs.setMinWidth(MIN_WIDTH);
    knobs.addToContainer(device);

    resetKnobs();
  }

  @Override
  protected void initialize() {
    super.initialize();
    resetKnobs();
  }

  private void resetKnobs() {
    if (this.knobs != null) {
      for (UIObject child : this.knobs) {
        ((UI2dComponent) child).removeFromContainer();
      }
      for (LXParameter p : this.jsParameters) {
        if (p instanceof BoundedParameter || p instanceof DiscreteParameter) {
          new UIKnob((LXListenableNormalizedParameter) p).addToContainer(this.knobs);
        }
      }
    }
  }

}
