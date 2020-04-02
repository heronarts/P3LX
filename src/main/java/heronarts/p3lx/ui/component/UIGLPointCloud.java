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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL2;
import processing.core.PGraphics;
import processing.opengl.PGL;
import processing.opengl.PJOGL;
import processing.opengl.PShader;
import heronarts.lx.LXEngine;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.p3lx.P3LX;
import heronarts.p3lx.ui.UI;

/**
 * Same as a UIPointCloud, except this version uses GLSL to draw
 * the points with a vertex shader.
 */
public class UIGLPointCloud extends UIPointCloud {

  private PShader shader;
  private FloatBuffer vertexData;
  private int vertexBufferObjectName;

  private FloatBuffer colorData;
  private int colorBufferObjectName;

  private int vertexLocation = -1;
  private int colorLocation = -1;

  private boolean alphaTestEnabled = false;

  private static final float[] NO_ATTENUATION = { 1, 0, 0 };

  private LXModel model = null;
  private int modelGeometryRevision = -1;

  /**
   * Point cloud for everything in the LX instance
   *
   * @param lx LX instance
   */
  public UIGLPointCloud(P3LX lx) {
    super(lx);
    loadShader();
  }

  private void buildModelBuffers(PGraphics pg) {
    // Create a buffer for vertex data
    this.vertexData = ByteBuffer
      .allocateDirect(this.model.size * 3 * Float.SIZE/8)
      .order(ByteOrder.nativeOrder())
      .asFloatBuffer();

    // Put all the points into the buffer
    this.vertexData.rewind();
    for (LXPoint p : this.model.points) {
      // Each point has 3 floats, XYZ
      this.vertexData.put(p.x);
      this.vertexData.put(p.y);
      this.vertexData.put(p.z);
    }
    this.vertexData.position(0);

    // Create a buffer for color data
    this.colorData = ByteBuffer
      .allocateDirect(this.model.size * 4 * Float.SIZE/8)
      .order(ByteOrder.nativeOrder())
      .asFloatBuffer();

    // Put all the points into the buffer
    this.colorData.rewind();
    for (int i = 0; i < this.model.points.length; ++i) {
      // Each point has 4 floats, RGBA
      this.colorData.put(0f);
      this.colorData.put(0f);
      this.colorData.put(0f);
      this.colorData.put(1f);
    }
    this.colorData.position(0);

    // Generate a buffer binding
    IntBuffer resultBuffer = ByteBuffer
      .allocateDirect(2 * Integer.SIZE/8)
      .order(ByteOrder.nativeOrder())
      .asIntBuffer();

    PGL pgl = pg.beginPGL();
    pgl.genBuffers(2, resultBuffer); // Generates a buffer, places its id in resultBuffer[0]
    this.vertexBufferObjectName = resultBuffer.get(0); // Grab our buffer name
    this.colorBufferObjectName = resultBuffer.get(1);
    pgl.bindBuffer(PGL.ARRAY_BUFFER, this.vertexBufferObjectName);
    pgl.bufferData(PGL.ARRAY_BUFFER, this.model.size * 3 * Float.SIZE/8, this.vertexData, PGL.STATIC_DRAW);
    pgl.bindBuffer(PGL.ARRAY_BUFFER, 0);
    pg.endPGL();
  }

  /**
   * Enable alpha testing for dense point clouds to minimize some forms of
   * visible billboard aliasing across overlapping points;
   *
   * @param alphaTestEnabled Whether alpha test enabled
   * @return this
   */
  public UIGLPointCloud setAlphaTestEnabled(boolean alphaTestEnabled) {
    this.alphaTestEnabled = alphaTestEnabled;
    return this;
  }

  @Override
  protected void onUIResize(UI ui) {
    loadShader();
  }

  public void loadShader() {
    this.shader = this.lx.applet.loadShader("frag.glsl", "vert.glsl");
    this.vertexLocation = this.colorLocation = -1;
  }

  @Override
  protected void onDraw(UI ui, PGraphics pg) {
    LXEngine.Frame frame = this.lx.getUIFrame();
    int[] colors = frame.getColors();
    LXModel frameModel = frame.getModel();
    int frameModelGeometryRevision = frameModel.getGeometryRevision();

    if (frameModel.size == 0) {
      // Nothing to see here! Don't render empty buffers...
      return;
    }

    boolean updateVertexPositions = false;

    // New model has been set!
    if (this.model != frameModel) {
      LXModel oldModel = this.model;
      this.model = frameModel;
      this.modelGeometryRevision = frameModelGeometryRevision;
      if ((oldModel == null) || (oldModel.size != frameModel.size)) {
        buildModelBuffers(pg);
      }
      updateVertexPositions = true;
    } else if (this.modelGeometryRevision != frameModelGeometryRevision) {
      updateVertexPositions = true;
      this.modelGeometryRevision = frameModelGeometryRevision;
    }

    // Put our new colors in the VBO
    int i = 0;
    for (LXPoint p : this.model.points) {
      int c = colors[p.index];
      this.colorData.put(4*i + 0, (0xff & (c >> 16)) / 255f); // R
      this.colorData.put(4*i + 1, (0xff & (c >> 8)) / 255f);  // G
      this.colorData.put(4*i + 2, (0xff & (c)) / 255f);       // B
      if (updateVertexPositions) {
        this.vertexData.put(3*i + 0, p.x);
        this.vertexData.put(3*i + 1, p.y);
        this.vertexData.put(3*i + 2, p.z);
      }
      ++i;
    }

    // Get PGL context
    PGL pgl = pg.beginPGL();

    // Set up shader
    this.shader.bind();
    if (this.vertexLocation < 0) {
      this.vertexLocation = pgl.getAttribLocation(this.shader.glProgram, "vertex");
      this.colorLocation = pgl.getAttribLocation(this.shader.glProgram, "color");
    }

    // Bind to our vertex buffer object, place the new color data
    pgl.bindBuffer(PGL.ARRAY_BUFFER, this.colorBufferObjectName);
    pgl.bufferData(PGL.ARRAY_BUFFER, this.model.size * 4 * Float.SIZE/8, this.colorData, PGL.STREAM_DRAW);
    pgl.enableVertexAttribArray(this.colorLocation);
    pgl.vertexAttribPointer(this.colorLocation, 4, PGL.FLOAT, false, 4 * Float.SIZE/8, 0);

    pgl.bindBuffer(PGL.ARRAY_BUFFER, this.vertexBufferObjectName);
    if (updateVertexPositions) {
      pgl.bufferData(PGL.ARRAY_BUFFER, this.model.size * 3 * Float.SIZE/8, this.vertexData, PGL.STREAM_DRAW);
    }
    pgl.enableVertexAttribArray(this.vertexLocation);
    pgl.vertexAttribPointer(this.vertexLocation, 3, PGL.FLOAT, false, 3 * Float.SIZE/8, 0);

    this.shader.set("pointSize", this.pointSize.getValuef());
    if (this.pointSizeAttenuation != null) {
      this.shader.set("attenuation", this.pointSizeAttenuation, 3);
    } else {
      this.shader.set("attenuation", NO_ATTENUATION, 3);
    }

    // GL2 properties
    GL2 gl2 = (com.jogamp.opengl.GL2) ((PJOGL)pgl).gl;
    gl2.glEnable(GL2.GL_POINT_SPRITE);
    gl2.glEnable(GL2.GL_POINT_SMOOTH);
    gl2.glDisable(GL2.GL_TEXTURE_2D);
    gl2.glPointSize(this.pointSize.getValuef());
    gl2.glEnable(GL2.GL_VERTEX_PROGRAM_POINT_SIZE);
    if (this.alphaTestEnabled) {
      gl2.glEnable(GL2.GL_ALPHA_TEST);
      gl2.glAlphaFunc(GL2.GL_NOTEQUAL, GL2.GL_ZERO);
    }

    // Draw the arrays
    pgl.drawArrays(PGL.POINTS, 0, this.model.size);

    gl2.glDisable(GL2.GL_VERTEX_PROGRAM_POINT_SIZE);
    gl2.glDisable(GL2.GL_POINT_SPRITE);

    // Unbind
    if (this.alphaTestEnabled) {
      gl2.glDisable(GL2.GL_ALPHA_TEST);
    }
    pgl.disableVertexAttribArray(this.vertexLocation);
    pgl.disableVertexAttribArray(this.colorLocation);
    this.shader.unbind();
    pgl.bindBuffer(PGL.ARRAY_BUFFER, 0);

    // Done!
    pg.endPGL();

  }

}
