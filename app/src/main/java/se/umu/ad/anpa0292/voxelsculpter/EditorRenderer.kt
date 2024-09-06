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
    private lateinit var voxels: Array<Voxel>

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var solidIndexBuffer: ShortBuffer
    private lateinit var wireFrameIndexBuffer: ShortBuffer
    private lateinit var normalBuffer: FloatBuffer

    private var solidVertexShader: Int = 0
    private var solidFragmentShader: Int = 0
    private var wireFrameVertexShader: Int = 0
    private var wireFrameFragmentShader: Int = 0

    private var solidProgram: Int = 0
    private var wireFrameProgram: Int = 0

    private val vbo = IntArray(2)
    private val ebo = IntArray(2)

    private fun setupPrograms() {
        val solidVertCode = loadShaderSourceCode(context, R.raw.solid_vert)
        solidVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, solidVertCode)

        val solidFragCode = loadShaderSourceCode(context, R.raw.solid_frag)
        solidFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, solidFragCode)

        solidProgram = linkShaders(solidVertexShader, solidFragmentShader)

        val wireFrameVertCode = loadShaderSourceCode(context, R.raw.wireframe_vert)
        wireFrameVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, wireFrameVertCode)

        val wireFrameFragCode = loadShaderSourceCode(context, R.raw.wireframe_frag)
        wireFrameFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, wireFrameFragCode)

        wireFrameProgram = linkShaders(wireFrameVertexShader, wireFrameFragmentShader)
    }

    private fun setupBuffers() {
        GLES20.glGenBuffers(2, vbo, 0)
        GLES20.glGenBuffers(2, ebo, 0)

        vertexBuffer = ByteBuffer.allocateDirect(Voxel.vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(Voxel.vertices)
                position(0)
            }

        normalBuffer = ByteBuffer.allocateDirect(Voxel.normals.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(Voxel.normals)
                position(0)
            }

        solidIndexBuffer = ByteBuffer.allocateDirect(Voxel.solidIndices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply {
                put(Voxel.solidIndices)
                position(0)
            }

        wireFrameIndexBuffer = ByteBuffer.allocateDirect(Voxel.wireframeIndices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .apply {
                put(Voxel.wireframeIndices)
                position(0)
            }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            Voxel.vertices.size * 4,
            vertexBuffer,
            GLES20.GL_STATIC_DRAW
        )

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[1])
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            Voxel.normals.size * 4,
            normalBuffer,
            GLES20.GL_STATIC_DRAW
        )

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ebo[0])
        GLES20.glBufferData(
            GLES20.GL_ELEMENT_ARRAY_BUFFER,
            Voxel.solidIndices.size * 2,
            solidIndexBuffer,
            GLES20.GL_STATIC_DRAW
        )

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ebo[1])
        GLES20.glBufferData(
            GLES20.GL_ELEMENT_ARRAY_BUFFER,
            Voxel.wireframeIndices.size * 2,
            wireFrameIndexBuffer,
            GLES20.GL_STATIC_DRAW)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        camera = PerspectiveCamera(Vector3D(0f, 0f, 0f), 100f)

        voxels = arrayOf(
            Voxel(0f, 0f, 0f),
            Voxel(1f, 0f, 0f),
            Voxel(-1f, 0f, 0f),
            Voxel(0f, 1f, 0f)
        )

        setupPrograms()
        setupBuffers()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        camera.setAspectRatio(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        drawSolidVoxels()
        drawWireFrameVoxels()
    }

    private fun drawSolidVoxels() {
        GLES20.glUseProgram(solidProgram)

        val positionHandle = GLES20.glGetAttribLocation(solidProgram, "vPosition")
        val normalHandle = GLES20.glGetAttribLocation(solidProgram, "vNormal")
        val mMatrixHandle = GLES20.glGetUniformLocation(solidProgram, "uMMatrix")
        val vpMatrixHandle = GLES20.glGetUniformLocation(solidProgram, "uVPMatrix")

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

        // Bind the index buffer
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ebo[0])

        GLES20.glEnable(GLES20.GL_POLYGON_OFFSET_FILL)
        GLES20.glPolygonOffset(1.0f, 1.0f)

        for (voxel in voxels) {
            GLES20.glUniformMatrix4fv(
                mMatrixHandle,
                1,
                false,
                voxel.transform,
                0
            )

            // Draw the cube
            GLES20.glDrawElements(
                GLES20.GL_TRIANGLES,
                Voxel.solidIndices.size,
                GLES20.GL_UNSIGNED_SHORT,
                0
            )
        }

        GLES20.glDisable(GLES20.GL_POLYGON_OFFSET_FILL)

        // Unbind buffers and disable vertex attributes
        GLES20.glDisableVertexAttribArray(GLES20.glGetAttribLocation(solidProgram, "vPosition"))
        GLES20.glDisableVertexAttribArray(GLES20.glGetAttribLocation(solidProgram, "vNormal"))

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    private fun drawWireFrameVoxels() {
        GLES20.glUseProgram(wireFrameProgram)

        val positionHandle = GLES20.glGetAttribLocation(wireFrameProgram, "vPosition")
        val mMatrixHandle = GLES20.glGetUniformLocation(wireFrameProgram, "uMMatrix")
        val vpMatrixHandle = GLES20.glGetUniformLocation(wireFrameProgram, "uVPMatrix")

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

        GLES20.glUniformMatrix4fv(
            vpMatrixHandle,
            1,
            false,
            camera.getTransform(),
            0
        )

        // Bind the index buffer
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, ebo[1])

        // Draw the wireframe
        GLES20.glLineWidth(4.0f)

        GLES20.glDepthFunc(GLES20.GL_LEQUAL)

        for (voxel in voxels) {
            GLES20.glUniformMatrix4fv(
                mMatrixHandle,
                1,
                false,
                voxel.transform,
                0
            )

            // Draw the cube
            GLES20.glDrawElements(
                GLES20.GL_LINES,
                Voxel.wireframeIndices.size,
                GLES20.GL_UNSIGNED_SHORT,
                0
            )
        }

        GLES20.glDepthFunc(GLES20.GL_LESS)

        // Unbind buffers and disable vertex attributes
        GLES20.glDisableVertexAttribArray(positionHandle)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
    }
}