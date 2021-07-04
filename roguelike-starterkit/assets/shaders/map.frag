#version 300 es

precision mediump float;

uniform sampler2D SRC_CHANNEL;
vec4 COLOR;
vec2 UV;
vec2 CHANNEL_0_SIZE;
vec2 CHANNEL_0_POSITION;
vec2 TEXTURE_SIZE;

//<indigo-fragment>
#define MAX_TILE_COUNT 4096

layout (std140) uniform RogueLikeData {
  vec4 GRID_DIMENSIONS_CHAR_SIZE;
  vec4 MASK;
};

layout (std140) uniform RogueLikeMapForeground {
  vec4[MAX_TILE_COUNT] CHAR_FOREGROUND;
};

layout (std140) uniform RogueLikeMapBackground {
  vec4[MAX_TILE_COUNT] BACKGROUND;
};

void fragment() {
  vec2 GRID_DIMENSIONS = GRID_DIMENSIONS_CHAR_SIZE.xy;
  vec2 CHAR_SIZE = GRID_DIMENSIONS_CHAR_SIZE.zw;

  vec2 ONE_TEXEL = CHANNEL_0_SIZE / TEXTURE_SIZE;

  // Which grid square am I in on the map? e.g. 3x3, coords (1,1)
  vec2 gridSquare = UV * GRID_DIMENSIONS;

  // Which sequential box is that? e.g. 4 of 9
  int index = int(floor(gridSquare.y) * GRID_DIMENSIONS.x + floor(gridSquare.x));

  // Which character is that? e.g. position 4 in the array is for char 64, which is '@'
  int charIndex = int(CHAR_FOREGROUND[index].x);

  // Where on the texture is the top left of the relevant character cell?
  float cellX = float(charIndex % 16) / 16.0;
  float cellY = floor(float(charIndex) / 16.0) * (1.0 / 16.0);
  vec2 cell = vec2(cellX, cellY);

  // What are the relative UV coords?
  vec2 tileSize = ONE_TEXEL * CHAR_SIZE;
  vec2 relUV = CHANNEL_0_POSITION + (cell * CHANNEL_0_SIZE) + (tileSize * fract(gridSquare));
  
  vec4 color = texture(SRC_CHANNEL, relUV);

  bool maskDiff = abs(color.x - MASK.x) < 0.001 &&
                  abs(color.y - MASK.y) < 0.001 &&
                  abs(color.z - MASK.z) < 0.001 &&
                  abs(color.w - MASK.w) < 0.001;

  if(maskDiff) {
    COLOR = BACKGROUND[index];
  } else {
    COLOR = vec4(color.rgb * (CHAR_FOREGROUND[index].gba * color.a), color.a);
  }

}
//</indigo-fragment>
