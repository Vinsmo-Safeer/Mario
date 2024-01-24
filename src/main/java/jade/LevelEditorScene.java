package jade;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class LevelEditorScene extends Scene {

    private String vertexShaderSrc = "#version 330 core\n" +
            "layout (location = 0) in vec3 aPos;\n" +
            "layout (location = 1) in vec4 aColor;\n" +
            "\n" +
            "out vec4 fColor;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    fColor = aColor;\n" +
            "    gl_Position = vec4(aPos, 1.0);\n" +
            "}";

    private String fragmentShaderSrc= "#version 330 core\n" +
            "\n" +
            "in vec4 fColor;\n" +
            "\n" +
            "out vec4 color;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    color = fColor;\n" +
            "}";

    private int vertexId, fragmentId, shaderProgram;

    private float[] vertexArray = {
            // Position                      // Color
            0.5f,    -0.5f,   0.0f,          1.0f, 0.0f, 0.0f, 1.0f, // BOTTOM RIGHT, RED
            -0.5f,   0.5f,    0.0f,          0.0f, 1.0f, 0.0f, 1.0f, // TOP LEFT, GREEN
            0.5f,    0.5f,    0.0f,          0.0f, 0.0f, 1.0f, 1.0f, // TOP RIGHT, BLUE
            -0.5f,   -0.5f,   0.0f,          1.0f, 1.0f, 1.0f, 1.0f, // BOTTOM LEFT, WHITE

    };

    private int[] elementArray = {
            // IMPORTANT: Must be in counter-clockwise

            2, 1, 0,
            0, 1, 3

    };

    private int vboId, vaoId, eboId;

    public LevelEditorScene() {

    }

    @Override
    public void init() {

        // =========================|
        // COMPILE AND LINK SHADERS |
        // =========================|

        // first load and compile the vertex shader
        vertexId = glCreateShader(GL_VERTEX_SHADER);
        // pass the shader source to the GPU
        glShaderSource(vertexId, vertexShaderSrc);
        glCompileShader(vertexId);

        // check for errors in compilation
        int success = glGetShaderi(vertexId, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(vertexId, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: 'defaultShader.glsl'\n\tVertex Shader compilation failed");
            System.out.println(glGetShaderInfoLog(vertexId, len));
            assert false: "";
        }

        // first load and compile the fragment shader
        fragmentId = glCreateShader(GL_FRAGMENT_SHADER);
        // pass the shader source to the GPU
        glShaderSource(fragmentId, fragmentShaderSrc);
        glCompileShader(fragmentId);

        // check for errors in compilation
        success = glGetShaderi(fragmentId, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(fragmentId, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: 'defaultShader.glsl'\n\tFragment Shader compilation failed");
            System.out.println(glGetShaderInfoLog(fragmentId, len));
            assert false: "";
        }

        // Link shaders and check for errors
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexId);
        glAttachShader(shaderProgram, fragmentId);
        glLinkProgram(shaderProgram);

        // check for linking errors
        success = glGetProgrami(shaderProgram, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int len = glGetProgrami(shaderProgram, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: 'defaultShader.glsl'\n\tLinking of shaders failed");
            System.out.println(glGetProgramInfoLog(shaderProgram, len));
            assert false: "";
        }



        // ==========================================================|
        // Generate VBO, VAO and EBO buffer objects, and send to GPU |
        // ==========================================================|
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // Create a float buffer of vertices
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip();

        // Create VBO and upload vertex buffer
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // Create the indices and upload
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip();

        eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

        // Add the vertex attribute pointers
        int positionsSize = 3;
        int colorSize = 4;
        int floatSizeBytes = 4;
        int vertexSizebytes = (positionsSize + colorSize) * floatSizeBytes;

        glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizebytes, 0 );
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizebytes, positionsSize * floatSizeBytes);
        glEnableVertexAttribArray(1);

    }

    @Override
    public void update(float dt) {

        // Bind shader program
        glUseProgram(shaderProgram);
        // Bind VAO that we are using
        glBindVertexArray(vboId);

        // Enable the vertex attribute pointers
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);

        // Unbind everything
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        glBindVertexArray(0);

        glUseProgram(0);
    }
}
