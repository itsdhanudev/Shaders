package JAVARuntime;

// Useful imports
import java.util.*;
import java.text.*;
import java.net.*;
import java.math.*;
import java.io.*;
import java.nio.*;

/*
//<JAVA-DOC>
Made By Lucas Leandro (ITsMagic Founder)
This text will appear when user opens the .java file after building the .itjar package for selling the scripts
//>JAVA-DOC<
*/
public class SimplePixelation extends CameraFilter {
  private static final boolean DEBUG_MODE = false;

  /// Define filter configs
  @Override
  public float getMinimalSupportedOGL() {
    return MaterialShader.OGL3;
  }

  @Override
  public String getFilterMenu() {
    return "CustomFilters/";
  }

  public float pixelSize = 12.0f;
  public Vector2 pixelScale = new Vector2(1.0f, 1.0f);

  public float centerWeight = 0.0f;
  public float softEdges = 0.0f;
  public float jitter = 0.0f;

  public float posterize = 0.0f;

  private float time;
  /// Shader instance
  private Shader shader;
  private FrameBuffer fb;

  /// Run only once
  @Override
  void start(OGLES ogles) {
    Shader.Builder builder = new Shader.Builder();
    builder.createProgram();

    VertexShader vs = VertexShader.loadFile(this, "SimplePixelationVertex");
    builder.setVertexCode(vs);

    FragmentShader fs = FragmentShader.loadFile(this, "SimplePixelationFragment");
    builder.setFragmentCode(fs);

    if (DEBUG_MODE) {
      builder.tryCompileVertex(
          new CompileErrorListener() {
            public void onError(String message) {
              Console.log("Vertex shader error: " + message);
            }
          });

      builder.tryCompileFragment(
          new CompileErrorListener() {
            public void onError(String message) {
              Console.log("Fragment shader error: " + message);
            }
          });

      shader =
          builder.tryCreate(
              new ShaderErrorListener() {
                public void onError(String programError, String shaderError) {
                  Console.log("Program error " + programError);
                  Console.log("Shader error " + shaderError);
                }
              });
    } else {
      builder.compileVertex();
      builder.compileFragment();
      shader = builder.create();
    }
  }

  /// Repeat every frame before rendering camera framebuffer to screen
  @Override
  void preDraw(OGLES ogles) {
    if (shader == null) return; // ignore when shader was not compiled

    // prepare buffers
    FrameBuffer cameraFB = myCamera.getFrameBuffer();
    time += Time.deltaTime();
    // keep local framebuffer the same size than camera.
    if (fb == null) {
      fb = new FrameBuffer(cameraFB.getWidth(), cameraFB.getHeight());
    } else {
      fb.resize(cameraFB.getWidth(), cameraFB.getHeight());
    }

    // draw offscreen
    OGLES3 ogl = (OGLES3) ogles;
    ogl.setIgnoreAttributeException(true);
    ogl.withShader(shader);

    // bind offscreen buffer
    fb.bind();
    ogl.clearColorDepthBuffer();

    ogl.uniformTexture("u_cameraImage", cameraFB.getColorTexture());

    ogl.uniformFloat("u_pixelSize", pixelSize);
    ogl.uniformVector2("u_pixelScale", pixelScale);

    ogl.uniformFloat("u_centerWeight", centerWeight);
    ogl.uniformFloat("u_softEdges", softEdges);
    ogl.uniformFloat("u_jitter", jitter);

    ogl.uniformFloat("u_posterize", posterize);
    ogl.uniformFloat("u_time", time);

    // draw to offscreen buffer
    super.drawQuad(shader);

    ogl.releaseAttributes();
    ogl.releaseShader();

    fb.unbind();

    // debug if you want to see how offscreen image is
    // GUI.drawImage(fb.getColorTexture(), 0,0,100,100);

    // fill cameraFB with offscreen rendered image
    super.fill(fb.getColorTexture(), cameraFB);
  } 

  /// Repeat every frame after rendering camera framebuffer to screen
  @Override
  void posDraw(OGLES ogles) {}

  /// Called when removed from the camera
  @Override
  void onDestroy() {}
}
