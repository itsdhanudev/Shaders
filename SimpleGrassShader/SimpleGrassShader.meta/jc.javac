package JAVARuntime;

// Useful imports
import java.util.*;

/*
//<JAVA-DOC>
Made By Lucas Leandro (ITsMagic Founder)
This text will appear when user opens the .java file after building the .itjar package for selling the scripts
//>JAVA-DOC<
*/

public class SimpleGrassShader extends MaterialShader {
  private static final boolean DEBUG_MODE = false;

  /// Define shader configs
  @Override
  public String getShaderName() {
    return "CustomShaders/SimpleGrassShader";
  }

  @Override
  public float getMinimalSupportedOGL() {
    return MaterialShader.OGL31;
  }

  /// Declare public variables to appear in the material
  public Color color = Color.GREEN();
  public Texture texture;

  public float grassHeight = 0.5f;
  public float grassWidth = 0.1f;
  public float windStrength = 0.2f;
  public float windSpeed = 1.0f;
  
  /// Shader instance
  private Shader shader;
  private float time;
  /// Run only once
  @Override
  void start() {
    Shader.Builder builder = new Shader.Builder();
    builder.activateGeometryShader(true);
    builder.createProgram();

    VertexShader vs = VertexShader.loadFile(this, "SimpleGrassShaderVertex");
    builder.setVertexCode(vs);

    GeometryShader gs = GeometryShader.loadFile(this, "SimpleGrassShaderGeometry");
    builder.setGeometryCode(gs);

    FragmentShader fs = FragmentShader.loadFile(this, "SimpleGrassShaderFragment");
    builder.setFragmentCode(fs);

    if (DEBUG_MODE) {
      builder.tryCompileVertex(
          new CompileErrorListener() {
            public void onError(String message) {
              Console.log("Vertex shader error: " + message);
            }
          });
      builder.tryCompileGeometry(
          new CompileErrorListener() {
            public void onError(String message) {
              Console.log("Geometry shader error: " + message);
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
      builder.compileGeometry();
      builder.compileFragment();

      shader = builder.create();
    }
  }

  /// Repeat every frame
  @Override
  void preRender(OGLES ogles) {}

  /// Repeat every camera rendering
  @Override
  void render(OGLES ogles, Camera camera, MSRenderData renderData) {
    if (shader == null) return; // ignore when shader was not compiled
    time += Time.deltaTime();
    OGLES3 ogl = (OGLES3) ogles;

    ogl.setIgnoreAttributeException(true);
    ogl.withShader(shader);

    ogl.disable(OGLES2.GL_CULL_FACE);
    float[] viewMatrix = camera.getViewMatrix();
    float[] projectionMatrix = camera.getProjectionMatrix();

    ogl.uniformMatrix4("viewMatrix", viewMatrix);
    ogl.uniformMatrix4("projectionMatrix", projectionMatrix);

    ogl.uniformFloat("u_grassHeight", grassHeight);
    ogl.uniformFloat("u_grassWidth", grassWidth);
    ogl.uniformFloat("u_windStrength", windStrength);
    ogl.uniformFloat("u_windSpeed", windSpeed);
    ogl.uniformFloat("u_time", time);

    ogl.uniformColor("diffuse", color);
    if (Texture.isRenderable(texture)) {
      ogl.uniformTexture("albedo", texture);
    } else {
      ogl.uniformTexture("albedo", Texture.white());
    }

    // run over all vertexes from material
    for (int rv = 0; rv < renderData.vertexCount(); rv++) {
      RenderableVertex rVertex = renderData.renderableVertexAt(rv);
      Vertex vertex = rVertex.vertex;

      if (rVertex.objectCount() > 0) {

        // apply attributes, like:
        // vertices buffers, UV buffers, Normals buffers, Tangent buffers, Bitangets buffers
        applyVertexAttributes(vertex, ogl);

        for (int ro = 0; ro < rVertex.objectCount(); ro++) {
          RenderableObject rObject = rVertex.objectAt(ro);
          if (rObject.isVisibleByCamera()) {

            float[] modelMatrix = rObject.getRenderMatrix();
            ogl.uniformMatrix4("modelMatrix", modelMatrix);

            // then we call opengl to draw the vertex triangles
            NativeIntBuffer triangles = vertex.getTrianglesBuffer();
            ogl.drawTriangles(triangles);
          }
        }
      }
    }
    ogl.enable(OGLES2.GL_CULL_FACE);
    ogl.releaseAttributes();
    ogl.releaseShader();
  }

  private void applyVertexAttributes(Vertex vertex, OGLES ogl) {
    NativeFloatBuffer vertices = vertex.getVerticesBuffer();
    if (vertices != null) {
      ogl.attributeVector3("position", vertices);
    }

    NativeFloatBuffer normals = vertex.getNormalsBuffer();
    if (normals != null) {
      ogl.attributeVector3("normal", normals);
    }

    NativeFloatBuffer uv = vertex.getUVsBuffer();
    if (uv != null) {
      ogl.attributeVector2("texCoord", uv);
    } 
  }

  /// Repeat every frame
  @Override
  void posRender(OGLES ogles) {}
}