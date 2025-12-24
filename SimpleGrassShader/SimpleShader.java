package JAVARuntime;

import java.util.*;

/*
//<JAVA-DOC>
Black Hole Shader
Visual gravitational distortion shader
Built on glslshadertemplate
//>JAVA-DOC<
*/

public class BlackHoleShader extends MaterialShader {

    private static final boolean DEBUG_MODE = false;

    @Override
    public String getShaderName() {
        return "CustomShaders/BlackHoleShader";
    }

    @Override
    public float getMinimalSupportedOGL() {
        return MaterialShader.OGL3;
    }

    /* ===== MATERIAL PARAMETERS ===== */
    public Color color = new Color(255,255,255);
    public Texture texture;
    public UIImage ui;
    public float centerX = 0.5f;
    public float centerY = 0.5f;

    public float radius = 0.15f;
    public float strength = 1.2f;

    public Color glowColor = new Color(255, 140, 40);
    public float glowWidth = 0.05f;

    /* ===== INTERNAL ===== */
    private Shader shader;
    private float time = 0f;

    @Override
    void start() {

        Shader.Builder builder = new Shader.Builder();
        builder.createProgram();

        builder.setVertexCode(
            VertexShader.loadFile(this, "SimpleShaderVertex")
        );
        builder.setFragmentCode(
            FragmentShader.loadFile(this, "SimpleShaderFragment")
        );

        if (DEBUG_MODE) {

            builder.tryCompileVertex(new CompileErrorListener() {
                public void onError(String message) {
                    Console.log("Vertex error: " + message);
                }
            });

            builder.tryCompileFragment(new CompileErrorListener() {
                public void onError(String message) {
                    Console.log("Fragment error: " + message);
                }
            });

            shader = builder.tryCreate(new ShaderErrorListener() {
                public void onError(String programError, String shaderError) {
                    Console.log("Program error: " + programError);
                    Console.log("Shader error: " + shaderError);
                }
            });

        } else {
            builder.compileVertex();
            builder.compileFragment();
            shader = builder.create();
        }
    }

    @Override
    void preRender(OGLES ogles) {
        time += Time.deltaTime();
    }

    @Override
    void render(OGLES ogles, Camera camera, MSRenderData renderData) {

        if (shader == null) return;

        OGLES3 ogl = (OGLES3) ogles;
        ogl.setIgnoreAttributeException(true);
        ogl.withShader(shader);

        /* Camera */
        ogl.uniformMatrix4("viewMatrix", camera.getViewMatrix());
        ogl.uniformMatrix4("projectionMatrix", camera.getProjectionMatrix());

        /* Base material */
        ogl.uniformColor("diffuse", color);
        ogl.uniformTexture(
            "albedo",
            Texture.isRenderable(texture) ? camera.getFrameBuffer().getColorTexture() : Texture.white()
        );

        /* Black hole uniforms */
        ogl.uniformVector2("u_center", centerX, centerY);
        ogl.uniformFloat("u_radius", radius);
        ogl.uniformFloat("u_strength", strength);
        ogl.uniformFloat("u_time", time);
        ogl.uniformFloat("u_glowWidth", glowWidth);

        ogl.uniformColor(
            "u_glowColor",
            glowColor
        );

        /* Draw */
        for (int rv = 0; rv < renderData.vertexCount(); rv++) {

            RenderableVertex rVertex = renderData.renderableVertexAt(rv);
            Vertex vertex = rVertex.vertex;

            if (rVertex.objectCount() == 0) continue;

            applyVertexAttributes(vertex, ogl);

            for (int ro = 0; ro < rVertex.objectCount(); ro++) {

                RenderableObject obj = rVertex.objectAt(ro);
                if (!obj.isVisibleByCamera()) continue;

                ogl.uniformMatrix4("modelMatrix", obj.getRenderMatrix());
                ogl.drawTriangles(vertex.getTrianglesBuffer());
            }
        }

        ogl.releaseAttributes();
        ogl.releaseShader();
    }

    private void applyVertexAttributes(Vertex vertex, OGLES ogl) {

        if (vertex.getVerticesBuffer() != null)
            ogl.attributeVector3("position", vertex.getVerticesBuffer());

        if (vertex.getNormalsBuffer() != null)
            ogl.attributeVector3("normal", vertex.getNormalsBuffer());

        if (vertex.getUVsBuffer() != null)
            ogl.attributeVector2("texCoord", vertex.getUVsBuffer());
    }

    @Override
    void posRender(OGLES ogles) {
    }
}