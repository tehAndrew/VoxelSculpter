precision highp float;

attribute vec4 vPosition; // Vertex position attribute

uniform mat4 uMMatrix;    // Model matrix
uniform mat4 uVPMatrix;   // View-Projection matrix

void main() {
    gl_Position = uVPMatrix * uMMatrix * vPosition;
}