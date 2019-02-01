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

import heronarts.lx.LXComponent;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;

public class UIComponentLabel extends UILabel implements LXParameterListener {

  private LXComponent component;
  private String prefix = "";

  public UIComponentLabel(float x, float y, float w, float h) {
    super(x, y, w, h);
  }

  public UIComponentLabel setPrefix(String prefix) {
    if (this.prefix != prefix) {
      this.prefix = prefix;
      updateLabel();
    }
    return this;
  }

  public UIComponentLabel setComponent(LXComponent component) {
    if (this.component != component) {
      if (this.component != null) {
        LXComponent parent = this.component;
        while (parent != null) {
          parent.label.removeListener(this);
          parent = parent.getParent();
        }
      }
      this.component = component;
      while (component != null) {
        component.label.addListener(this);
        component = component.getParent();
      }
      updateLabel();
    }
    return this;
  }

  public void onParameterChanged(LXParameter p) {
    updateLabel();
  }

  private void updateLabel() {
    if (this.component == null) {
      setLabel("");
    } else {
      setLabel((this.prefix != null ? this.prefix : "") + this.component.getCanonicalLabel());
    }
  }
}

