#version 150

in vec4 vertexColor;
out vec4 fragmentColor;

void main() {
  fragmentColor = vertexColor;
  float dist = distance(gl_PointCoord, vec2(0.5, 0.5));
  fragmentColor.w = clamp(20*(0.55 - dist), 0., 1.);
}
