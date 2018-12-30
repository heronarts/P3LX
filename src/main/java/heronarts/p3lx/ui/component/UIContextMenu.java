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

package heronarts.p3lx.ui.component;

import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UI2dScrollContext;
import heronarts.p3lx.ui.UIContextActions;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIContextMenu extends UI2dComponent {

  private static final float ROW_HEIGHT = 18;

  private UIContextActions.Action[] actions;

  private int highlight = -1;

  public UIContextMenu(float x, float y, float w, float h) {
    super(x, y, w, h);
    setVisible(false);
    setBackgroundColor(UI.get().theme.getContextBackgroundColor());
    setBorderColor(UI.get().theme.getContextBorderColor());
  }

  public UIContextMenu setActions(UIContextActions.Action[] actions) {
    this.actions = actions;
    setHeight(this.actions.length * ROW_HEIGHT);
    return this;
  }

  public UIContextMenu setHighlight(int highlight) {
    if (this.highlight != highlight) {
      if (highlight >= 0 && highlight < this.actions.length) {
        this.highlight = highlight;
      } else {
        this.highlight = -1;
      }
      redraw();
    }
    return this;
  }

  /**
   * Subclasses may override to draw some other kind of drop menu
   *
   * @param ui UI context
   * @param pg PGraphics context
   */
  @Override
  public void onDraw(UI ui, PGraphics pg) {
    if (this.highlight >= 0) {
      pg.fill(ui.theme.getContextHighlightColor());
      pg.rect(0, this.highlight * ROW_HEIGHT, this.width, ROW_HEIGHT);
    }

    float yp = 0;
    for (UIContextActions.Action action : this.actions) {
      pg.textFont(hasFont() ? getFont() : ui.theme.getControlFont());
      pg.fill(ui.theme.getControlTextColor());
      pg.textAlign(PConstants.LEFT, PConstants.CENTER);
      pg.text(clipTextToWidth(pg, action.getLabel(), this.width - 6), 4, yp + ROW_HEIGHT / 2);
      yp += ROW_HEIGHT;
    }
  }

  public UIContextMenu setPosition(UI2dComponent parent, float mx, float my) {
    float x = mx, y = my;
    while (parent != null) {
      x += parent.getX();
      y += parent.getY();
      if (parent instanceof UI2dScrollContext) {
        UI2dScrollContext scrollContext = (UI2dScrollContext) parent;
        x += scrollContext.getScrollX();
        y += scrollContext.getScrollY();
      }
      parent = parent.getContainer();
    }
    setPosition(x, y);
    return this;
  }

  @Override
  public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
    if (keyCode == java.awt.event.KeyEvent.VK_UP) {
      consumeKeyEvent();
      setHighlight((this.highlight + this.actions.length - 1) % this.actions.length);
    } else if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
      consumeKeyEvent();
      setHighlight((this.highlight + 1) % this.actions.length);
    } else if (keyCode == java.awt.event.KeyEvent.VK_SPACE || keyCode == java.awt.event.KeyEvent.VK_ENTER) {
      consumeKeyEvent();
      if (this.highlight >= 0) {
        this.actions[this.highlight].onContextAction();
      }
      getUI().hideContextMenu();
    } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
      consumeKeyEvent();
      getUI().hideContextMenu();
    }
  }

  @Override
  public void onMouseOut(MouseEvent mouseEvent) {
    setHighlight(-1);
  }

  @Override
  public void onMouseMoved(MouseEvent mouseEvent, float x, float y) {
    setHighlight((int) (y / ROW_HEIGHT));
  }

  @Override
  public void onMousePressed(MouseEvent mouseEvent, float x, float y) {
    int index = (int) (y / ROW_HEIGHT);
    if (index >= 0 && index < this.actions.length) {
      this.actions[index].onContextAction();
    }
    getUI().hideContextMenu();
  }

}
