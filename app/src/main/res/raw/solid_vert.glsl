precision highp float;

attribute vec4 vPosition; // Vertex position attribute
attribute vec3 vNormal;   // Vertex normal attribute

uniform mat4 uMMatrix;    // Model matrix
uniform mat4 uVPMatrix;   // View-Projection matrix

varying vec3 wPosition;   // Varying to pass world position to the fragment shader
varying vec3 wNormal;     // Varying to pass world normal to the fragment shader

void main() {
    // Transform vertex position to world space
    vec4 worldPos = uMMatrix * vPosition;
    wPosition = worldPos.xyz;

    // Transform vertex normal to world space
    // For orthogonal matrices, directly transform the normal
    vec3 worldNorm = normalize((uMMatrix * vec4(vNormal, 0.0)).xyz);

    wNormal = worldNorm;

    // Transform vertex position to clip space
    gl_Position = uVPMatrix * worldPos; // Apply view-projection matrix to the world position
}