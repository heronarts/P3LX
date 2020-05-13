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

package heronarts.p3lx.ui;

import processing.event.Event;
import processing.event.MouseEvent;
import heronarts.lx.utils.LXUtils;
import heronarts.p3lx.ui.component.UILabel;

/**
 * A UIWindow is a UIContext that by default has a title bar and can be dragged
 * around when the mouse is pressed on the title bar.
 */
public class UIWindow extends UI2dContext {

  public final static int TITLE_LABEL_HEIGHT = 24;

  private final static int TITLE_PADDING = 6;

  /**
   * The label object
   */
  private final UILabel label;

  /**
   * Constructs a window object
   *
   * @param ui UI to place in
   * @param title Title for this window
   * @param x X-coordinate
   * @param y Y-coordinate
   * @param w Width
   * @param h Height
   */
  public UIWindow(final UI ui, String title, float x, float y, float w, float h) {
    super(ui, x, y, w, h);
    setBackgroundColor(ui.theme.getDeviceBackgroundColor());
    setBorderColor(ui.theme.getDeviceBorderColor());
    this.label = new UILabel(0, 0, w, TITLE_LABEL_HEIGHT);
    this.label
      .setLabel(title)
      .setPadding(TITLE_PADDING)
      .setFontColor(ui.theme.getWindowTitleColor())
      .setFont(ui.theme.getWindowTitleFont())
      .addToContainer(this);
  }

  private boolean movingWindow = false;

  @Override
  protected void onFocus(Event event) {
    this.label.setFontColor(ui.theme.getFocusColor());
  }

  @Override
  protected void onBlur() {
    this.label.setFontColor(ui.theme.getWindowTitleColor());
  }

  @Override
  protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
    this.movingWindow = (my < TITLE_LABEL_HEIGHT);
    bringToFront();
    if (!hasFocus()) {
      focus(mouseEvent);
    }
  }

  @Override
  protected void onMouseDragged(MouseEvent mouseEvent, float mx, float my, float dx, float dy) {
    if (this.movingWindow) {
      float newX = LXUtils.constrainf(this.x + dx, 0, this.parent.getWidth() - this.width);
      float newY = LXUtils.constrainf(this.y + dy, 0, this.parent.getHeight() - this.height);
      setPosition(newX, newY);
    }
  }

  /**
   * Set the title of the window.
   *
   * @param title Title of the window
   * @return this window
   */
  public UIWindow setTitle(String title) {
    this.label.setLabel(title);
    return this;
  }
}
