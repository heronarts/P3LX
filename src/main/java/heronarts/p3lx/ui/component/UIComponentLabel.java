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

import heronarts.lx.LXComponent;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.StringParameter;

public class UIComponentLabel extends UILabel implements LXParameterListener {

  private LXComponent component;
  private String prefix = "";
  private boolean canonical = true;

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

  public UIComponentLabel setCanonical(boolean canonical) {
    if (this.canonical != canonical) {
      this.canonical = canonical;
      if (this.component != null) {
        setComponent(this.component, true);
      }
    }
    return this;
  }

  private List<StringParameter> listenTargets = new ArrayList<StringParameter>();

  public UIComponentLabel setComponent(LXComponent component) {
    return setComponent(component, false);
  }

  private UIComponentLabel setComponent(LXComponent component, boolean forceUpdate) {
    if (forceUpdate || (this.component != component)) {
      for (StringParameter listenTarget : this.listenTargets) {
        listenTarget.removeListener(this);
      }
      this.listenTargets.clear();
      this.component = component;
      while (component != null) {
        component.label.addListener(this);
        component = this.canonical ? component.getParent() : null;
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
      setLabel((this.prefix != null ? this.prefix : "") + (this.canonical ? this.component.getCanonicalLabel() : this.component.getLabel()));
    }
  }
}

