const float pi=3.1415927;
void mainImage(out vec4 fragColor,in vec2 fragCoord){
  float i=fragCoord.x/iResolution.x;
  vec3 t=(iTime+iMouse.y)/vec3(63.0,78.0,45.0);
  vec3 cs=cos(((i*pi)*2.0)+((vec3(0.0,1.0,-0.5))*pi)+t);
  fragColor=vec4(0.5+(0.5*cs),1.0);
}