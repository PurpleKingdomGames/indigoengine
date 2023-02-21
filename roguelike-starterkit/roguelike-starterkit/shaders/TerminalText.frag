#version 300 es

precision mediump float;

uniform sampler2D SRC_CHANNEL;

vec4 CHANNEL_0;
vec4 COLOR;
vec2 UV;
vec2 SIZE;

//<indigo-fragment>
layout (std140) uniform RogueLikeTextData {
  vec3 FOREGROUND;
  vec4 BACKGROUND;
  vec4 MASK;
};

vec4 fragment(vec4 color){

  bool maskDiff = abs(CHANNEL_0.x - MASK.x) < 0.001 &&
                  abs(CHANNEL_0.y - MASK.y) < 0.001 &&
                  abs(CHANNEL_0.z - MASK.z) < 0.001 &&
                  abs(CHANNEL_0.w - MASK.w) < 0.001;
  vec4 c;

  if(maskDiff) {
    c = BACKGROUND;
  } else {
    c = vec4(CHANNEL_0.rgb * (FOREGROUND.rgb * CHANNEL_0.a), CHANNEL_0.a);
  }

  return c;
}
//</indigo-fragment>
