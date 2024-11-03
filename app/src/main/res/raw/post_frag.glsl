precision highp float;
uniform sampler2D uTexture;
uniform vec2 uResolution;
varying vec2 vTexCoord;

void main() {
    vec2 center = uResolution * 0.5;
    float radius = 0.004 * uResolution.y;

    vec3 color = texture2D(uTexture, vTexCoord).rgb;
    float brightness = dot(color, vec3(0.299, 0.587, 0.114));
    float distanceFromCenter = length(vTexCoord * uResolution - center);

    if (distanceFromCenter <= radius) {
        color = brightness < 0.5 ? vec3(1.0) : vec3(0.0);
    }

    gl_FragColor = vec4(color, 1.0);
}