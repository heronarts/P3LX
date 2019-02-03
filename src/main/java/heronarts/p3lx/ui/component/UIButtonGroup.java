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

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.command.LXCommand;
import heronarts.lx.osc.LXOscEngine;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI2dContainer;
import heronarts.p3lx.ui.UIContextActions;
import heronarts.p3lx.ui.UIControlTarget;

public class UIButtonGroup extends UI2dContainer implements UIControlTarget, UIContextActions {

  private final static int DEFAULT_BUTTON_MARGIN = 4;

  private final DiscreteParameter parameter;

  public final UIButton[] buttons;

  private boolean inParameterUpdate = false;

  public UIButtonGroup(DiscreteParameter parameter, float x, float y, float w, float h) {
    this(parameter, x, y, w, h, false);
  }

  public UIButtonGroup(final DiscreteParameter parameter, float x, float y, float w, float h, final boolean hideFirst) {
    super(x, y, w, h);
    setLayout(UI2dContainer.Layout.HORIZONTAL);
    setChildMargin(DEFAULT_BUTTON_MARGIN);

    this.parameter = parameter;
    int range = parameter.getRange();
    this.buttons = new UIButton[range];

    int numButtons = range - (hideFirst ? 1 : 0);
    int buttonWidth = (int) (w - (numButtons-1) * DEFAULT_BUTTON_MARGIN) / numButtons;

    for (int i = hideFirst ? 1 : 0; i < range; ++i) {
      final int iv = i;
      this.buttons[i] = new UIButton(0, 0, buttonWidth, h) {
        @Override
        public void onToggle(boolean enabled) {
          if (!inParameterUpdate) {
            if (enabled) {
              getLX().command.perform(new LXCommand.Parameter.SetValue(parameter, iv));
            } else if (hideFirst) {
              getLX().command.perform(new LXCommand.Parameter.SetValue(parameter, 0));
            }
          }
        }
      };
      this.buttons[i]
      .setLabel(parameter.getOptions()[i])
      .setActive(i == parameter.getValuei())
      .addToContainer(this);
    }

    parameter.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter p) {
        int active = parameter.getValuei();
        inParameterUpdate = true;
        for (int i = 0; i < buttons.length; ++i) {
          if (!hideFirst || i > 0) {
            buttons[i].setActive(i == active);
          }
        }
        inParameterUpdate = false;
      }
    });
  }

  @Override
  public String getDescription() {
    return UIParameterControl.getDescription(this.parameter);
  }

  @Override
  public LXParameter getControlTarget() {
    if (isMappable() && this.parameter != null && this.parameter.getComponent() != null) {
      return this.parameter;
    }
    return null;
  }

  @Override
  public List<Action> getContextActions() {
    String oscAddress = LXOscEngine.getOscAddress(this.parameter);
    if (oscAddress != null) {
      List<Action> list = new ArrayList<Action>();
      list.add(new UIContextActions.Action.CopyOscAddress(oscAddress));
    }
    return null;
  }

}
