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

import processing.core.PConstants;
import processing.event.KeyEvent;

public class UIKeyEvent {
  // Should never be instantiated
  private UIKeyEvent() {}

  public static boolean isCommand(KeyEvent keyEvent) {
    return keyEvent.isMetaDown() || keyEvent.isControlDown();
  }

  public static boolean isDelete(KeyEvent keyEvent, int keyCode) {
    // NOTE(mcslee): there is serious hackiness under here, P3D surface uses
    // JOGL key codes, and Processing remaps the character values but not the
    // key codes...
    //
    // See PSurfaceJOGL hackToChar:
    // https://github.com/processing/processing/blob/4cc297c66908899cd29480c202536ecf749854e8/core/src/processing/opengl/PSurfaceJOGL.java#L1187
    return
      (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) ||
      (keyEvent.getKey() == PConstants.DELETE);
  }
}
