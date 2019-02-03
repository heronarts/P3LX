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

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.List;

import heronarts.lx.command.LXCommand;
import heronarts.lx.parameter.LXParameter;

public interface UIContextActions {

  public static abstract class Action {

    private String label;

    public Action(String label) {
      this.label = label;
    }

    public String getLabel() {
      return this.label;
    }

    public Action setLabel(String label) {
      this.label = label;
      return this;
    }

    @Override
    public String toString() {
      return this.label;
    }

    public abstract void onContextAction(UI ui);

    public static class ResetParameter extends Action {

      private final LXParameter parameter;

      public ResetParameter(LXParameter parameter) {
        super("Reset value");
        this.parameter = parameter;
      }

      @Override
      public void onContextAction(UI ui) {
        ui.lx.command.perform(new LXCommand.Parameter.Reset(this.parameter));
      }
    }

    public static class CopyOscAddress extends Action {
      private final String oscAddress;

      public CopyOscAddress(String oscAddress) {
        super("Copy OSC address");
        this.oscAddress = oscAddress;
      }

      @Override
      public void onContextAction(UI ui) {
        try {
          Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(oscAddress), null);
        } catch (Exception x) {
          System.err.println("Exception setting system clipboard");
          x.printStackTrace();
        }
      }

    }
  }

  /**
   * Returns a list of context actions that should be shown for this item
   *
   * @return List of context actions
   */
  public List<Action> getContextActions();

}
