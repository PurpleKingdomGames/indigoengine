#version 300 es

precision mediump float;

uniform sampler2D SRC_CHANNEL;
vec4 COLOR;
vec2 UV;

//<indigo-fragment>
layout (std140) uniform RogueLikeMapData {
  vec4 GRID_DIMENSIONS_CHAR_SIZE;
  vec4 MASK;
  vec4[2000] CHAR_FOREGROUND;
  vec4[2000] BACKGROUND;
};

in vec2 TILEMAP_TL_TEX_COORDS;
in vec2 ONE_TEXEL;
in vec2 TEXTURE_SIZE;

void fragment() {
  vec2 GRID_DIMENSIONS = GRID_DIMENSIONS_CHAR_SIZE.xy;
  vec2 CHAR_SIZE = GRID_DIMENSIONS_CHAR_SIZE.zw;

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
  vec2 relUV = TILEMAP_TL_TEX_COORDS + (cell * TEXTURE_SIZE) + (tileSize * fract(gridSquare));
  
  vec4 color = texture(SRC_CHANNEL, relUV);

  if(color == MASK) {
    COLOR = BACKGROUND[index];
  } else {
    COLOR = vec4(color.rgb * (CHAR_FOREGROUND[index].gba * color.a), color.a);
  }

}
//</indigo-fragment>
