package jade;

import org.lwjgl.BufferUtils;
import renderer.Shader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class LevelEditorScene extends Scene {

    Shader defaultShader;

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

        defaultShader = new Shader("assets/shaders/default.glsl");
        defaultShader.compile();

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

        defaultShader.use();
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

        defaultShader.detach();
    }
}