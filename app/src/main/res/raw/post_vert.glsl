attribute vec2 aPosition;
varying vec2 vTexCoord;

void main() {
    vTexCoord = (aPosition + 1.0) * 0.5; // Map [-1, 1] to [0, 1]
    gl_Position = vec4(aPosition, 0.0, 1.0);
}