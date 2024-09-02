package se.umu.ad.anpa0292.voxelsculpter

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Loads the shader source code from the raw file with resource id [resourceId] from context
 * [context] and returns it.
 *
 * @param context the context from which to access the applications resources.
 * @param resourceId the resource id of the raw file containing the shader source code.
 * @throws Resources.NotFoundException if resource is not found.
 * @return a string of the shader source code.
 */
fun loadShaderSourceCode(context: Context, resourceId: Int): String {
    return context.resources.openRawResource(resourceId).bufferedReader().use {
        it.readText()
    }
}

/**
 * Creates a shader of type [type] with code [shaderCode] and compiles it.
 *
 * Possible shader types are GL_FRAGMENT_SHADER or GL_VERTEX_SHADER. Throws RuntimeException if
 * compilation fails.
 *
 * @param type the type of shader, either GL_FRAGMENT_SHADER or GL_VERTEX_SHADER.
 * @param shaderCode the source code of the shader.
 * @throws RuntimeException if the compilation fails.
 * @return the compiled shader.
 */
fun loadShader(type: Int, shaderCode: String): Int {
    return GLES20.glCreateShader(type).also { shader ->
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        // Workaround since kotlin does not support passing primitives by reference.
        val compileStatus = IntArray(1)

        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            Log.e("Shader Error", GLES20.glGetShaderInfoLog(shader))
            GLES20.glDeleteShader(shader)
            throw RuntimeException("Shader compilation failed")
        }
    }
}

/**
 * Links [vertexShader] and [fragmentShader] into a shader program.
 *
 * Throws RuntimeException if linking fails.
 *
 * @param vertexShader the compiled vertex shader to use.
 * @param fragmentShader the compiled fragment shader to use.
 * @throws RuntimeException if the linking fails.
 * @return the linked shader program.
 */
fun linkShaders(vertexShader: Int, fragmentShader: Int): Int {
    return GLES20.glCreateProgram().also {program ->
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        // Workaround since kotlin does not support passing primitives by reference.
        val linkStatus = IntArray(1)

        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            Log.e("Program Error", GLES20.glGetProgramInfoLog(program))
            GLES20.glDeleteProgram(program)
            throw RuntimeException("Program linking failed")
        }
    }
}

class EditorRenderer(private val context: Context) : GLSurfaceView.Renderer {
    lateinit var camera: PerspectiveCamera
    private lateinit var voxel: Voxel

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var indexBuffer: ShortBuffer
    private lateinit var normalBuffer: FloatBuffer

    private var vertexShader: Int = 0
    private var fragmentShader: Int = 0
    private var program: Int = 0

    private val vbo = IntArray(2)
    private val ebo = IntArray(1)

    private fun setupProgram() {
        val vertexShaderCode = loadShaderSourceCode(context, R.raw.vertexshader)
        vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)

        val fragmentShaderCode = loadShaderSourceCode(context, R.raw.fragmentshader)
        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = linkShaders(vertexShader, fragmentShader)
    }

    private fun setupBuffers() {
        GLES20.glGenBuffers(2, vbo, 0)
        GLES20.glGenBuffers(1, ebo, 0)

        vertexBuffer = ByteBuffer.allocateDirect(Voxel.vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(Voxel.vertices).position(0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            Voxel.vertices.size * 4,
            vertexBuffer,
            GLES20.GL_STATIC_DRAW
        )

        normalBuffer = ByteBuffer.allocateDirect(Voxel.normals.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        normalBuffer.put(Voxel.normals).position(0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[1])
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            Voxel.normals.size * 4,
            normalBuffer,
            GLES20.GL_STATIC_DRAW
        )

        indexBuffer = ByteBuffer.allocateDirect(Voxel.indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
        indexBuffer.put(Voxel.indices).position(0)

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ebo[0])
        GLES20.glBufferData(
            GLES20.GL_ELEMENT_ARRAY_BUFFER,
            Voxel.indices.size * 2,
            indexBuffer,
            GLES20.GL_STATIC_DRAW)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        camera = PerspectiveCamera(floatArrayOf(0f, 0f, 0f), 15f)
        voxel = Voxel(0f, 0f, 0f)
        setupProgram()
        setupBuffers()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        camera.setAspectRatio(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glUseProgram(program)

        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        val normalHandle = GLES20.glGetAttribLocation(program, "vNormal")
        val mMatrixHandle = GLES20.glGetUniformLocation(program, "uMMatrix")
        val vpMatrixHandle = GLES20.glGetUniformLocation(program, "uVPMatrix")

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
        GLES20.glVertexAttribPointer(
            positionHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            0,
            0
        )

        GLES20.glEnableVertexAttribArray(normalHandle)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[1])
        GLES20.glVertexAttribPointer(
            normalHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            0,
            0
        )

        GLES20.glUniformMatrix4fv(
            vpMatrixHandle,
            1,
            false,
            camera.getTransform(),
            0
        )

        GLES20.glUniformMatrix4fv(
            mMatrixHandle,
            1,
            false,
             voxel.transform,
            0
        )

        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // Bind the index buffer
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ebo[0])

        // Draw the cube
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            Voxel.indices.size,
            GLES20.GL_UNSIGNED_SHORT,
            0
        )

        // Unbind buffers and disable vertex attributes
        GLES20.glDisableVertexAttribArray(GLES20.glGetAttribLocation(program, "vPosition"))
        GLES20.glDisableVertexAttribArray(GLES20.glGetAttribLocation(program, "vNormal"))
    }
}