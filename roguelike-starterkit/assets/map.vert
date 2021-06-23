#version 300 es

precision mediump float;

vec2 CHANNEL_0_ATLAS_OFFSET;

// Placeholder
vec2 scaleCoordsWithOffset(vec2 texcoord, vec2 offset){
  return vec2(0.0);
}

//<indigo-vertex>

out vec2 TILEMAP_TL_TEX_COORDS;
out vec2 TILEMAP_BR_TEX_COORDS;

void vertex() {
  TILEMAP_TL_TEX_COORDS = scaleCoordsWithOffset(vec2(0.0, 0.0), CHANNEL_0_ATLAS_OFFSET);
  TILEMAP_BR_TEX_COORDS = scaleCoordsWithOffset(vec2(1.0, 1.0), CHANNEL_0_ATLAS_OFFSET);
}
//</indigo-vertex>
