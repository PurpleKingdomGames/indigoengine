#version 300 es

precision mediump float;

uniform sampler2D SRC_CHANNEL;
vec4 CHANNEL_0;
vec4 COLOR;
vec2 SIZE;
vec2 UV;

//<indigo-fragment>
layout (std140) uniform MapData {
  vec2 MAP_SIZE;
  vec2 CHAR_SIZE;
  float[9] CHARS;
};

in vec2 TILEMAP_TL_TEX_COORDS;
in vec2 TILEMAP_BR_TEX_COORDS;

void fragment() {

  /*
  Right - what I need to do is:
  Take the UV
  Use that to work out what box I'm in (x, y)
  Use the X, y to work out which index I need to look in
  the index gives me the character on the sheet
  but I need to turn that back into an X and y of the top left and bottom right of the character on the texture
  then I use the UV relative to the box I'm in (how far through this grid cell am I?) to
  read the right texture coords from the texture.
  */

  // Which char am I looking at?
  int x = int(UV.x * MAP_SIZE.x);
  int y = int(UV.y * MAP_SIZE.y);
  int index = int(float(y) * MAP_SIZE.x) + x;
  int charIndex = int(CHARS[index]);

  // Where on the sheet is that?
  vec2 tileMapSize = TILEMAP_BR_TEX_COORDS - TILEMAP_TL_TEX_COORDS;
  float xx = float(charIndex % 16) / 16.0;
  float yy = float(charIndex / 16) / 16.0;

  COLOR = texture(SRC_CHANNEL, TILEMAP_TL_TEX_COORDS + (tileMapSize * vec2(xx, yy)) + UV);
}
//</indigo-fragment>
