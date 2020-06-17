/**
 * Copyright 2018- Mark C. Slee, Heron Arts LLC
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

package heronarts.p3lx.ui;

import processing.event.MouseEvent;

public class UIMouseEvent {
  private UIMouseEvent() {}

  public static boolean isCommand(MouseEvent mouseEvent) {
    return mouseEvent.isMetaDown() || mouseEvent.isControlDown();
  }

  public static boolean isMultiSelect(MouseEvent mouseEvent) {
    return mouseEvent.isMetaDown() || mouseEvent.isControlDown();
  }

  public static boolean isRangeSelect(MouseEvent mouseEvent) {
    return mouseEvent.isShiftDown();
  }
}
