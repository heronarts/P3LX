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

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.parameter.LXListenableParameter;
import heronarts.lx.parameter.LXNormalizedParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.modulation.LXCompoundModulation;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.p3lx.ui.UITimerTask;

public class UICompoundParameterControl extends UIParameterControl {
  private double lastParameterValue = 0;

  private final List<LXListenableParameter> modulationParameters = new ArrayList<LXListenableParameter>();

  private final LXParameterListener redrawListener = new LXParameterListener() {
    @Override
    public void onParameterChanged(LXParameter p) {
      redraw();
    }
  };

  private final UITimerTask checkRedrawTask = new UITimerTask(30, UITimerTask.Mode.FPS) {
    @Override
    public void run() {
      double parameterValue = getCompoundNormalized();
      if (parameterValue != lastParameterValue) {
        redraw();
      }
      lastParameterValue = parameterValue;
    }
  };

  protected UICompoundParameterControl(float x, float y, float w, float h) {
    super(x, y, w, h);
    addLoopTask(this.checkRedrawTask);
  }

  @Override
  public UIParameterControl setParameter(LXNormalizedParameter parameter) {
    for (LXListenableParameter p : this.modulationParameters) {
      p.removeListener(this.redrawListener);
    }
    this.modulationParameters.clear();
    return super.setParameter(parameter);
  }

  protected double getCompoundNormalized() {
    if (this.parameter != null) {
      if (this.parameter instanceof CompoundParameter) {
        return ((CompoundParameter) this.parameter).getNormalized();
      } else {
        return getNormalized();
      }
    }
    return 0;
  }

  protected void registerModulation(LXCompoundModulation modulation) {
    if (!this.modulationParameters.contains(modulation.range)) {
      this.modulationParameters.add(modulation.range);
      this.modulationParameters.add(modulation.polarity);
      this.modulationParameters.add(modulation.enabled);
      modulation.range.addListener(this.redrawListener);
      modulation.polarity.addListener(this.redrawListener);
      modulation.enabled.addListener(this.redrawListener);

      // Colors may be shared across multiple modulations from same source component
      if (!this.modulationParameters.contains(modulation.color)) {
        this.modulationParameters.add(modulation.color);
        modulation.color.addListener(this.redrawListener);
      }
    }
  }
}
