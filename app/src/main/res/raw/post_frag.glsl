precision mediump float;
uniform sampler2D uTexture;
varying vec2 vTexCoord;

void main() {
    vec3 color = texture2D(uTexture, vTexCoord).rgb;
    float grayscale = dot(color, vec3(0.299, 0.587, 0.114));
    gl_FragColor = vec4(vec3(grayscale), 1.0);
}