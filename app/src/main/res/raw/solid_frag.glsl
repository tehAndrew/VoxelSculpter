precision mediump float;

varying vec3 wPosition;
varying vec3 wNormal;

void main() {
    vec3 lightDir = normalize(vec3(1.1, 1.33, -1.7));
    vec3 lightColor = vec3(0.5, 0.5, 0.5);
    vec3 ambientColor = vec3(0.0, 0.2, 0.2);

    float diff = max(dot(wNormal, lightDir), 0.0);
    vec3 diffuse = lightColor * diff;
    vec3 ambient = ambientColor;
    vec3 color = ambient + diffuse;

    gl_FragColor = vec4(color, 1.0);
}