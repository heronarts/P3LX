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

package heronarts.p3lx.ui.control;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI3dContext;
import heronarts.p3lx.ui.UIWindow;
import heronarts.p3lx.ui.component.UIKnob;

public class UICameraControl extends UIWindow {

  public final static int WIDTH = 140;
  public final static int HEIGHT = 72;

  public UICameraControl(UI ui, UI3dContext context, float x, float y) {
    super(ui, "CAMERA", x, y, WIDTH, HEIGHT);

    float xp = 5;
    float yp = UIWindow.TITLE_LABEL_HEIGHT;
    new UIKnob(xp, yp).setParameter(context.perspective).addToContainer(this);
    xp += 34;
    new UIKnob(xp, yp).setParameter(context.depth).addToContainer(this);
  }

}
