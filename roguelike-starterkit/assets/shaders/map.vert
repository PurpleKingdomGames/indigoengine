#version 300 es

precision mediump float;

vec2 CHANNEL_0_ATLAS_OFFSET;
vec2 SIZE;

// Placeholder
mat4 scale2d(vec2 s){
    return mat4(0);
}
// Placeholder
vec2 scaleCoordsWithOffset(vec2 texcoord, vec2 offset){
  return vec2(0.0);
}

//<indigo-vertex>
layout (std140) uniform RogueLikeMapData {
  vec4 GRID_DIMENSIONS_CHAR_SIZE;
  vec4 MASK;
  vec4[2000] CHAR_FOREGROUND;
  vec4[2000] BACKGROUND;
};

out vec2 TILEMAP_TL_TEX_COORDS;
out vec2 ONE_TEXEL;
out vec2 TEXTURE_SIZE;

void vertex() {
  vec2 CHAR_SIZE = GRID_DIMENSIONS_CHAR_SIZE.zw;

  vec2 TILEMAP_BR_TEX_COORDS = scaleCoordsWithOffset((CHAR_SIZE * 16.0) / SIZE, CHANNEL_0_ATLAS_OFFSET);

  TILEMAP_TL_TEX_COORDS = scaleCoordsWithOffset(vec2(0.0, 0.0), CHANNEL_0_ATLAS_OFFSET);
  TEXTURE_SIZE = TILEMAP_BR_TEX_COORDS - TILEMAP_TL_TEX_COORDS;
  ONE_TEXEL = TEXTURE_SIZE / vec2(16.0) / CHAR_SIZE;

}
//</indigo-vertex>
