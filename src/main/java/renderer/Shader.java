package renderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;

public class Shader {

    private int shaderProgramId;

    private String vertexSource;
    private String fragmentSource;
    private String filepath;

    public Shader(String filepath) {
        this.filepath = filepath;
        try {

            String source = new String(Files.readAllBytes(Paths.get(filepath)));
            String[] spiltString = source.split("(#type)( )+([a-zA-Z]+)"); // splits the source code into two parts: [vertex, fragment]

            int index = source.indexOf("#type") + 6; // +6 to skip "#type "
            int endOfLine = source.indexOf("\r\n", index); // find the end of the line
            String firstPattern = source.substring(index, endOfLine).trim(); // trim to remove whitespace


            // Find the second pattern after #type 'pattern'
            index = source.indexOf("#type", endOfLine) + 6;
            endOfLine = source.indexOf("\r\n", index);
            String secondPattern = source.substring(index, endOfLine).trim();

            if (firstPattern.equals("vertex")) {
                vertexSource = spiltString[1];
            }else if (firstPattern.equals("fragment")) {
                fragmentSource = spiltString[1];
            }else {
                throw new IOException("Unexpected token '" + firstPattern + "'");
            }

            if (secondPattern.equals("vertex")) {
                vertexSource = spiltString[2];
            }else if (secondPattern.equals("fragment")) {
                fragmentSource = spiltString[2];
            }else {
                throw new IOException("Unexpected token '" + secondPattern + "'");
            }

        } catch (IOException e) {
            e.printStackTrace();
            assert false : "Error: Could not open file for shader: '" + filepath + "'";
        }


    }

    public void compile() {

        // =========================|
        // COMPILE AND LINK SHADERS |
        // =========================|

        int vertexId, fragmentId;

        // first load and compile the vertex shader
        vertexId = glCreateShader(GL_VERTEX_SHADER);
        // pass the shader source to the GPU
        glShaderSource(vertexId, vertexSource);
        glCompileShader(vertexId);

        // check for errors in compilation
        int success = glGetShaderi(vertexId, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(vertexId, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + filepath + "'\n\tVertex Shader compilation failed");
            System.out.println(glGetShaderInfoLog(vertexId, len));
            assert false: "";
        }

        // first load and compile the fragment shader
        fragmentId = glCreateShader(GL_FRAGMENT_SHADER);
        // pass the shader source to the GPU
        glShaderSource(fragmentId, fragmentSource);
        glCompileShader(fragmentId);

        // check for errors in compilation
        success = glGetShaderi(fragmentId, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(fragmentId, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + filepath + "'\n\tFragment Shader compilation failed");
            System.out.println(glGetShaderInfoLog(fragmentId, len));
            assert false: "";
        }

        // Link shaders and check for errors
        shaderProgramId = glCreateProgram();
        glAttachShader(shaderProgramId, vertexId);
        glAttachShader(shaderProgramId, fragmentId);
        glLinkProgram(shaderProgramId);

        // check for linking errors
        success = glGetProgrami(shaderProgramId, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int len = glGetProgrami(shaderProgramId, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + filepath + "'\n\tLinking of shaders failed");
            System.out.println(glGetProgramInfoLog(shaderProgramId, len));
            assert false: "";
        }
    }

    public void use() {
        glUseProgram(shaderProgramId);
    }

    public void detach() {
        glUseProgram(0);
    }
}
