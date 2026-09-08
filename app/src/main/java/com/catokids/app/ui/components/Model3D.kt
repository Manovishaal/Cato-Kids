package com.catokids.app.ui.components

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.sqrt

/**
 * A tiny, dependency-free 3D toy viewer.
 *
 * The models are Cato Kids' own exported toy assets (rocket, treasure chest, alphabet
 * block, the Cato mascot, a coin token, a shape sorter), pre-parsed offline from their
 * source .obj/.mtl/.glb files into a compact custom binary (see tools/ if ever
 * regenerated) — a flat, already-triangulated, already-colored vertex soup per named
 * part. That means everything below is plain android.opengl (part of the platform,
 * ships on every device) — no Filament, no SceneView, no model-viewer/WebView, nothing
 * that needs a Gradle dependency at all.
 */

// ---------------------------------------------------------------- data / loading

data class ModelPart(
    val name: String,
    /** Interleaved x,y,z, nx,ny,nz, r,g,b — 9 floats per vertex, already triangle-expanded. */
    val vertexData: FloatArray,
) {
    val vertexCount: Int get() = vertexData.size / 9
}

class Model3D(
    val parts: List<ModelPart>,
    val centerX: Float,
    val centerY: Float,
    val centerZ: Float,
    val radius: Float,
)

private val modelCache = HashMap<String, Model3D>()

/** Loads and caches `assets/models/<name>.c3d`. Cheap enough to call from a Composable. */
fun loadModel3D(context: Context, name: String): Model3D {
    modelCache[name]?.let { return it }
    val bytes = try {
        context.assets.open("models/$name.c3d").use { it.readBytes() }
    } catch (e: IOException) {
        // Should never happen for a bundled asset — fail soft with an empty model
        // rather than crashing a child's screen.
        return Model3D(emptyList(), 0f, 0f, 0f, 1f)
    }
    val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
    val magic = ByteArray(4)
    buf.get(magic)
    val nodeCount = buf.int
    val parts = ArrayList<ModelPart>(nodeCount)

    var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE; var minZ = Float.MAX_VALUE
    var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE

    repeat(nodeCount) {
        val nameLen = buf.short.toInt() and 0xFFFF
        val nameBytes = ByteArray(nameLen)
        buf.get(nameBytes)
        val partName = String(nameBytes, Charsets.UTF_8)
        val vertexCount = buf.int
        val data = FloatArray(vertexCount * 9)
        for (i in data.indices) data[i] = buf.float
        var i = 0
        while (i < data.size) {
            val x = data[i]; val y = data[i + 1]; val z = data[i + 2]
            if (x < minX) minX = x; if (x > maxX) maxX = x
            if (y < minY) minY = y; if (y > maxY) maxY = y
            if (z < minZ) minZ = z; if (z > maxZ) maxZ = z
            i += 9
        }
        parts.add(ModelPart(partName, data))
    }

    val cx = (minX + maxX) / 2f
    val cy = (minY + maxY) / 2f
    val cz = (minZ + maxZ) / 2f
    val dx = maxX - minX; val dy = maxY - minY; val dz = maxZ - minZ
    val radius = (0.5f * sqrt((dx * dx + dy * dy + dz * dz).toDouble())).toFloat().coerceAtLeast(0.05f)

    val model = Model3D(parts, cx, cy, cz, radius)
    modelCache[name] = model
    return model
}

// ---------------------------------------------------------------- renderer

/**
 * The chest is the only model with a moving part: its lid, hinged along the back
 * (bottom-back) edge, opposite the lock on the front. Everything else about a part's
 * geometry is exactly as exported — no other model needs a special case.
 */
private const val CHEST_LID_PART = "chest_lid"
private val CHEST_LID_PIVOT = floatArrayOf(0f, 0.168f, -0.1f)
private const val CHEST_LID_OPEN_DEGREES = -108f

private const val VERTEX_SHADER = """
    uniform mat4 uMVP;
    uniform mat4 uModel;
    attribute vec3 aPosition;
    attribute vec3 aNormal;
    attribute vec3 aColor;
    varying vec3 vColor;
    varying vec3 vNormal;
    void main() {
        gl_Position = uMVP * vec4(aPosition, 1.0);
        vNormal = mat3(uModel[0].xyz, uModel[1].xyz, uModel[2].xyz) * aNormal;
        vColor = aColor;
    }
"""

private const val FRAGMENT_SHADER = """
    precision mediump float;
    varying vec3 vColor;
    varying vec3 vNormal;
    uniform vec3 uLightDir;
    void main() {
        vec3 n = normalize(vNormal);
        float diff = max(dot(n, normalize(uLightDir)), 0.0);
        float light = 0.55 + 0.45 * diff;
        gl_FragColor = vec4(vColor * light, 1.0);
    }
"""

/** Render/interaction state for one [Model3DView]. Plain volatile fields: written from
 * the Compose/UI thread by gestures and LaunchedEffects, read every frame on the GL
 * thread. None of it needs to be exact from one frame to the next, so no locking. */
class Model3DController {
    @Volatile var rotationY = 20f
    @Volatile var tiltX = 18f
    @Volatile var autoRotate = true
    @Volatile var lidOpenFraction = 0f
    @Volatile var hiddenParts: Set<String> = emptySet()
    @Volatile var spotlightPart: String? = null
    @Volatile var clearColor: FloatArray = floatArrayOf(1f, 1f, 1f, 0f)
    @Volatile var lastInteractionAtMs = 0L
}

private class Model3DRenderer(
    private val model: Model3D,
    private val controller: Model3DController,
) : GLSurfaceView.Renderer {

    private class GlPart(val name: String, val buffer: FloatBuffer, val vertexCount: Int)

    private var glParts: List<GlPart> = emptyList()
    private var program = 0
    private var aPosition = 0
    private var aNormal = 0
    private var aColor = 0
    private var uMVP = 0
    private var uModel = 0
    private var uLightDir = 0

    private var width = 1
    private var height = 1

    private val projection = FloatArray(16)
    private val view = FloatArray(16)
    private val vp = FloatArray(16)
    private val baseModel = FloatArray(16)
    private val partModel = FloatArray(16)
    private val hinge = FloatArray(16)
    private val mvp = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDisable(GLES20.GL_CULL_FACE)

        val vs = compileShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fs = compileShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vs)
            GLES20.glAttachShader(it, fs)
            GLES20.glLinkProgram(it)
        }
        aPosition = GLES20.glGetAttribLocation(program, "aPosition")
        aNormal = GLES20.glGetAttribLocation(program, "aNormal")
        aColor = GLES20.glGetAttribLocation(program, "aColor")
        uMVP = GLES20.glGetUniformLocation(program, "uMVP")
        uModel = GLES20.glGetUniformLocation(program, "uModel")
        uLightDir = GLES20.glGetUniformLocation(program, "uLightDir")

        glParts = model.parts.map { part ->
            val buffer = ByteBuffer.allocateDirect(part.vertexData.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
            buffer.put(part.vertexData)
            buffer.position(0)
            GlPart(part.name, buffer, part.vertexCount)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) {
        width = w.coerceAtLeast(1)
        height = h.coerceAtLeast(1)
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        val cc = controller.clearColor
        GLES20.glClearColor(cc[0], cc[1], cc[2], cc[3])
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        if (glParts.isEmpty()) return

        val now = SystemClock.uptimeMillis()
        if (controller.autoRotate && now - controller.lastInteractionAtMs > 900) {
            controller.rotationY += 0.35f
        }

        val radius = model.radius
        val eyeDist = radius * 2.6f
        val eyeY = radius * 0.35f
        val aspect = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projection, 0, 42f, aspect, radius * 0.05f, radius * 10f)
        Matrix.setLookAtM(view, 0, 0f, eyeY, eyeDist, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(vp, 0, projection, 0, view, 0)

        Matrix.setIdentityM(baseModel, 0)
        Matrix.rotateM(baseModel, 0, controller.tiltX, 1f, 0f, 0f)
        Matrix.rotateM(baseModel, 0, controller.rotationY, 0f, 1f, 0f)
        Matrix.translateM(baseModel, 0, -model.centerX, -model.centerY, -model.centerZ)

        GLES20.glUseProgram(program)
        GLES20.glUniform3f(uLightDir, 0.4f, 0.9f, 0.6f)

        val hidden = controller.hiddenParts
        val spotlight = controller.spotlightPart

        for (part in glParts) {
            if (hidden.contains(part.name)) continue
            if (spotlight != null && part.name != spotlight) continue

            val m = if (part.name == CHEST_LID_PART) {
                Matrix.setIdentityM(hinge, 0)
                Matrix.translateM(hinge, 0, CHEST_LID_PIVOT[0], CHEST_LID_PIVOT[1], CHEST_LID_PIVOT[2])
                Matrix.rotateM(hinge, 0, CHEST_LID_OPEN_DEGREES * controller.lidOpenFraction, 1f, 0f, 0f)
                Matrix.translateM(hinge, 0, -CHEST_LID_PIVOT[0], -CHEST_LID_PIVOT[1], -CHEST_LID_PIVOT[2])
                Matrix.multiplyMM(partModel, 0, baseModel, 0, hinge, 0)
                partModel
            } else {
                baseModel
            }

            Matrix.multiplyMM(mvp, 0, vp, 0, m, 0)
            GLES20.glUniformMatrix4fv(uMVP, 1, false, mvp, 0)
            GLES20.glUniformMatrix4fv(uModel, 1, false, m, 0)

            part.buffer.position(0)
            GLES20.glEnableVertexAttribArray(aPosition)
            GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false, 36, part.buffer)

            part.buffer.position(3)
            GLES20.glEnableVertexAttribArray(aNormal)
            GLES20.glVertexAttribPointer(aNormal, 3, GLES20.GL_FLOAT, false, 36, part.buffer)

            part.buffer.position(6)
            GLES20.glEnableVertexAttribArray(aColor)
            GLES20.glVertexAttribPointer(aColor, 3, GLES20.GL_FLOAT, false, 36, part.buffer)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, part.vertexCount)
        }
    }

    private fun compileShader(type: Int, src: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, src)
        GLES20.glCompileShader(shader)
        return shader
    }
}

// ---------------------------------------------------------------- composable

/**
 * A rotatable, self-lit 3D viewer for one of the bundled toy models.
 *
 * Drag left/right to spin it, up/down for a gentle tilt; it idles into a slow auto-spin
 * a moment after you let go. [spotlightPart] draws only that one named part (e.g. just
 * "peg_cube" out of the sorter model) — handy for "which piece is this?" moments.
 * [lidOpenFraction] (0..1) only affects the chest model, animating its lid open.
 */
@Composable
fun Model3DView(
    modelAsset: String,
    modifier: Modifier = Modifier,
    autoRotate: Boolean = true,
    spotlightPart: String? = null,
    hiddenParts: Set<String> = emptySet(),
    lidOpenFraction: Float = 0f,
    background: Color = Color.White,
    onTap: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val model = remember(modelAsset) { loadModel3D(context, modelAsset) }
    val controller = remember(modelAsset) { Model3DController() }
    var glView by remember { mutableStateOf<GLSurfaceView?>(null) }

    LaunchedEffect(autoRotate) { controller.autoRotate = autoRotate }
    LaunchedEffect(spotlightPart) { controller.spotlightPart = spotlightPart }
    LaunchedEffect(hiddenParts) { controller.hiddenParts = hiddenParts }
    LaunchedEffect(lidOpenFraction) { controller.lidOpenFraction = lidOpenFraction }
    LaunchedEffect(background) {
        // The surface itself is always opaque (see the factory below) — true
        // transparency needs setZOrderOnTop, which lifts the surface above the
        // normal Compose z-order and can then draw over things that should sit
        // above it (dialogs, top bars). Passing a solid color that matches the
        // surrounding card/background reads as transparent without that risk.
        controller.clearColor = floatArrayOf(background.red, background.green, background.blue, 1f)
    }

    DisposableEffect(lifecycleOwner, glView) {
        val view = glView
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> view?.onResume()
                Lifecycle.Event.ON_PAUSE -> view?.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(
        modifier = modifier
            .pointerInput(modelAsset) {
                detectDragGestures(
                    onDragStart = { controller.lastInteractionAtMs = SystemClock.uptimeMillis() },
                ) { _, dragAmount ->
                    controller.lastInteractionAtMs = SystemClock.uptimeMillis()
                    controller.rotationY += dragAmount.x * 0.4f
                    controller.tiltX = (controller.tiltX - dragAmount.y * 0.3f).coerceIn(-25f, 60f)
                }
            }
            .then(
                if (onTap != null) {
                    Modifier.pointerInput(modelAsset) {
                        detectTapGestures {
                            controller.lastInteractionAtMs = SystemClock.uptimeMillis()
                            onTap()
                        }
                    }
                } else {
                    Modifier
                },
            ),
        factory = { ctx ->
            GLSurfaceView(ctx).apply {
                setEGLContextClientVersion(2)
                setRenderer(Model3DRenderer(model, controller))
                renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
            }.also { glView = it }
        },
    )
}
