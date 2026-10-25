package com.groupa.coreui.car3d

import android.content.Context
import android.opengl.Matrix
import android.view.Choreographer
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.SurfaceView
import android.view.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.LightManager
import com.google.android.filament.RenderableManager
import com.google.android.filament.Renderer
import com.google.android.filament.Scene
import com.google.android.filament.SwapChain
import com.google.android.filament.View
import com.google.android.filament.Viewport
import com.google.android.filament.IndirectLight
import com.google.android.filament.Skybox
import com.google.android.filament.android.UiHelper
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.gltfio.UbershaderProvider
import com.google.android.filament.gltfio.Gltfio
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class CarPoint(
    val x: Float,
    val y: Float,
    val z: Float,
)

data class CarCameraPreset(
    val position: CarPoint,
    val target: CarPoint,
)

data class CarPaint(
    val mainColor: Color,
    val interiorColor: Color,
)

data class CarFrameState(
    val turntableEnabled: Boolean,
    val doorLeftProgress: Float,
    val doorRightProgress: Float,
    val hoodProgress: Float,
    val hatchProgress: Float,
    val explodeProgress: Float,
    val spinWheels: Boolean,
    val driveLoop: Boolean,
)

private fun initFilamentRuntime() {
    com.google.android.filament.Filament.init()
    Gltfio.init()
}

private data class EntityTransform(
    val entity: Int,
    val original: FloatArray,
)

private data class CarLightSpec(
    val type: LightManager.Type,
    val color: FloatArray,
    val intensity: Float,
    val direction: FloatArray,
)

private data class CarSceneLight(
    val entity: Int,
    val intensity: Float,
    val instance: Int,
)

private class CarFilamentController(
    private val context: Context,
    private val surfaceView: SurfaceView,
) {
    private val engine = Engine.create()
    private val renderer = engine.createRenderer()
    private val scene = engine.createScene()
    private val cameraEntity = EntityManager.get().create()
    private val camera = engine.createCamera(cameraEntity)
    private val view = engine.createView()
    private val uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
    private val materialProvider = UbershaderProvider(engine)
    private val assetLoader = AssetLoader(engine, materialProvider, EntityManager.get())
    private val resourceLoader = ResourceLoader(engine, true)
    private val lights: List<CarSceneLight>
    private var swapChain: SwapChain? = null
    private var asset: FilamentAsset? = null
    private var indirectLight: IndirectLight? = null
    private var skybox: Skybox? = null
    private val readyRenderables = IntArray(128)

    private var rootTransform: EntityTransform? = null
    private var doorLeftTransform: EntityTransform? = null
    private var doorRightTransform: EntityTransform? = null
    private var hoodTransform: EntityTransform? = null
    private var hatchTransform: EntityTransform? = null
    private val rimTransforms = mutableListOf<EntityTransform>()

    private var cameraPosition = floatArrayOf(3.5f, 1.8f, 5.5f)
    private var cameraTarget = floatArrayOf(0f, 0.3f, 0f)

    private var rootAngle = 0f
    private var wheelAngle = 0f
    private var elapsedSeconds = 0f
    private var lastFrameNanos = 0L
    private val surfaceCallback = object : UiHelper.RendererCallback {
        override fun onNativeWindowChanged(surface: Surface) {
            swapChain?.let(engine::destroySwapChain)
            swapChain = engine.createSwapChain(surface)
        }

        override fun onDetachedFromSurface() {
            swapChain?.let {
                engine.destroySwapChain(it)
                engine.flushAndWait()
            }
            swapChain = null
        }

        override fun onResized(width: Int, height: Int) {
            view.viewport = Viewport(0, 0, width, height)
            val aspect = width.toDouble() / height.toDouble().coerceAtLeast(1.0)
            camera.setLensProjection(28.0, aspect, 0.05, 1000.0)
        }
    }

    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                event1: MotionEvent?,
                event2: MotionEvent,
                distanceX: Float,
                distanceY: Float,
            ): Boolean {
                orbitCamera(distanceX, distanceY, 1f)
                return true
            }
        },
    )
    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                orbitCamera(0f, 0f, detector.scaleFactor)
                return true
            }
        },
    )

    init {
        view.scene = scene
        view.camera = camera
        camera.setExposure(4f, 1f / 125f, 100f)

        val lightSpecs = listOf(
            CarLightSpec(
                type = LightManager.Type.SUN,
                color = floatArrayOf(1f, 1f, 1f),
                intensity = 100_000f,
                direction = floatArrayOf(-0.485f, -0.776f, -0.485f),
            ),
            CarLightSpec(
                type = LightManager.Type.DIRECTIONAL,
                color = floatArrayOf(0.53f, 0.57f, 1f),
                intensity = 40_000f,
                direction = floatArrayOf(-0.743f, -0.495f, -0.495f),
            ),
            CarLightSpec(
                type = LightManager.Type.DIRECTIONAL,
                color = floatArrayOf(1f, 1f, 1f),
                intensity = 60_000f,
                direction = floatArrayOf(0f, -0.196f, -0.981f),
            ),
            CarLightSpec(
                type = LightManager.Type.DIRECTIONAL,
                color = floatArrayOf(0.32f, 0.48f, 0.8f),
                intensity = 30_000f,
                direction = floatArrayOf(0f, 0.316f, -0.949f),
            ),
        )
        lights = lightSpecs.map { spec ->
            val entity = EntityManager.get().create()
            LightManager.Builder(spec.type)
                .color(spec.color[0], spec.color[1], spec.color[2])
                .intensity(spec.intensity)
                .direction(spec.direction[0], spec.direction[1], spec.direction[2])
                .castShadows(spec.type == LightManager.Type.SUN)
                .build(engine, entity)
            scene.addEntity(entity)
            CarSceneLight(
                entity = entity,
                intensity = spec.intensity,
                instance = engine.lightManager.getInstance(entity),
            )
        }

        setupEnvironment()

        uiHelper.renderCallback = surfaceCallback
        uiHelper.attachTo(surfaceView)
        surfaceView.setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    fun setCamera(preset: CarCameraPreset) {
        cameraPosition = floatArrayOf(
            preset.position.x,
            preset.position.y,
            preset.position.z,
        )
        cameraTarget = floatArrayOf(
            preset.target.x,
            preset.target.y,
            preset.target.z,
        )
    }

    fun setBrightness(brightness: Float) {
        lights.forEach { light ->
            engine.lightManager.setIntensity(light.instance, light.intensity * brightness)
        }
    }

    fun setPaint(paint: CarPaint) {
        val asset = asset ?: return
        val mainColor = paint.mainColor.toLinearRgb()
        val interiorColor = paint.interiorColor.toLinearRgb()
        val renderableManager = engine.renderableManager

        asset.renderableEntities.forEach { renderableEntity ->
            val renderableInstance = renderableManager.getInstance(renderableEntity)
            val primitiveCount = renderableManager.getInstanceCount(renderableInstance)
            for (primitiveIndex in 0 until primitiveCount) {
                val material = renderableManager.getMaterialInstanceAt(
                    renderableInstance,
                    primitiveIndex,
                )
                val materialName = material.name.orEmpty()
                if (materialName.contains("Paint", ignoreCase = true)) {
                    material.setParameter(
                        "baseColorFactor",
                        mainColor[0],
                        mainColor[1],
                        mainColor[2],
                        1f,
                    )
                }
                if (materialName.contains("Interior 3", ignoreCase = true)) {
                    material.setParameter(
                        "baseColorFactor",
                        interiorColor[0],
                        interiorColor[1],
                        interiorColor[2],
                        1f,
                    )
                }
            }
        }
    }

    private fun setupEnvironment() {
        indirectLight = IndirectLight.Builder()
            .irradiance(1, floatArrayOf(0.24f, 0.28f, 0.38f))
            .radiance(1, floatArrayOf(0.24f, 0.28f, 0.38f))
            .intensity(60_000f)
            .build(engine)
        skybox = Skybox.Builder()
            .color(0.08f, 0.10f, 0.14f, 1f)
            .intensity(1f)
            .build(engine)

        scene.indirectLight = indirectLight
        scene.skybox = skybox
    }
    fun frame(frameTimeNanos: Long, state: CarFrameState) {
        val deltaSeconds = if (lastFrameNanos == 0L) {
            0f
        } else {
            (frameTimeNanos - lastFrameNanos) / 1_000_000_000f
        }
        lastFrameNanos = frameTimeNanos
        elapsedSeconds += deltaSeconds

        if (state.turntableEnabled) {
            rootAngle += 20f * deltaSeconds
        }
        if (state.spinWheels || state.driveLoop) {
            wheelAngle += 500f * deltaSeconds
        }

        updateRootTransform(state)
        updateBodyTransforms(state)
        updateRimTransforms(state)
        updateCamera()
        resourceLoader.asyncUpdateLoad()
        assetLoader.gc()
        asset?.let(::populateScene)
        updateCamera()
        val currentSwapChain = swapChain ?: return
        if (!uiHelper.isReadyToRender) {
            return
        }
        if (renderer.beginFrame(currentSwapChain, frameTimeNanos)) {
            renderer.render(view)
            renderer.endFrame()
        }
    }

    fun destroy() {
        uiHelper.detach()
        resourceLoader.asyncCancelLoad()
        resourceLoader.evictResourceData()
        asset?.let { loadedAsset ->
            scene.removeEntities(loadedAsset.entities)
            assetLoader.destroyAsset(loadedAsset)
        }
        asset = null
        assetLoader.destroy()
        materialProvider.destroyMaterials()
        materialProvider.destroy()
        resourceLoader.destroy()
        lights.forEach { light -> engine.destroyEntity(light.entity) }
        engine.destroyCameraComponent(cameraEntity)
        engine.destroyView(view)
        engine.destroyScene(scene)
        engine.destroyRenderer(renderer)
        EntityManager.get().destroy(cameraEntity)
        lights.forEach { light -> EntityManager.get().destroy(light.entity) }
        engine.destroy()
    }

    fun loadModel(modelFile: File) {
        val modelBytes = modelFile.readBytes()
        val loadedAsset = assetLoader.createAsset(ByteBuffer.wrap(modelBytes)) ?: return
        asset = loadedAsset
        resourceLoader.asyncBeginLoad(loadedAsset)
        loadedAsset.releaseSourceData()
        populateScene(loadedAsset)
        transformToUnitCube(loadedAsset)
        captureTransforms()
    }

    private fun populateScene(loadedAsset: FilamentAsset) {
        var count = loadedAsset.popRenderables(readyRenderables)
        while (count != 0) {
            scene.addEntities(readyRenderables.copyOf(count))
            count = loadedAsset.popRenderables(readyRenderables)
        }
        scene.addEntities(loadedAsset.lightEntities)
    }

    private fun transformToUnitCube(loadedAsset: FilamentAsset) {
        val bounds = loadedAsset.boundingBox
        if (bounds.isEmpty) {
            return
        }
        val center = bounds.center
        val halfExtent = bounds.halfExtent
        val maxExtent = 2f * maxOf(halfExtent[0], halfExtent[1], halfExtent[2])
        if (maxExtent <= 0f) {
            return
        }
        val scaleFactor = 2f / maxExtent
        val matrix = FloatArray(16)
        Matrix.setIdentityM(matrix, 0)
        Matrix.scaleM(matrix, 0, scaleFactor, scaleFactor, scaleFactor)
        Matrix.translateM(matrix, 0, -center[0], -center[1], -center[2])
        setTransform(EntityTransform(loadedAsset.root, matrix), matrix)
    }

    private fun captureTransforms() {
        val asset = asset ?: return
        rootTransform = transformFor(asset.root)
        doorLeftTransform = namedTransform(asset, "BodyDoorLColor1")
        doorRightTransform = namedTransform(asset, "BodyDoorRColor1")
        hoodTransform = namedTransform(asset, "BodyHood")
        hatchTransform = namedTransform(asset, "BodyRearPanelsColor1")

        rimTransforms.clear()
        asset.entities.forEach { entity ->
            val name = asset.getName(entity).orEmpty()
            if (name.contains("Rim", ignoreCase = true)) {
                transformFor(entity)?.let(rimTransforms::add)
            }
        }
    }

    private fun namedTransform(asset: FilamentAsset, name: String): EntityTransform? =
        asset.getEntitiesByName(name).firstOrNull()?.let(::transformFor)

    private fun transformFor(entity: Int): EntityTransform? {
        val transformInstance = engine.transformManager.getInstance(entity)
        if (transformInstance == 0) {
            return null
        }
        val matrix = FloatArray(16)
        engine.transformManager.getTransform(transformInstance, matrix)
        return EntityTransform(entity, matrix)
    }

    private fun updateRootTransform(state: CarFrameState) {
        val transform = rootTransform ?: return
        val matrix = transform.original.copyOf()
        if (state.turntableEnabled) {
            Matrix.rotateM(matrix, 0, rootAngle, 0f, 1f, 0f)
        }
        if (state.driveLoop) {
            Matrix.translateM(matrix, 0, sin(elapsedSeconds * 2f) * 1.5f, 0f, 0f)
        }
        setTransform(transform, matrix)
    }

    private fun updateBodyTransforms(state: CarFrameState) {
        val animation = carBodyAnimationSpec(state)
        updateDoorTransform(
            doorLeftTransform,
            rotationZ = animation.doorLeftRotationZ,
            pivotX = animation.doorLeftHingePivotX,
            pivotY = animation.doorHingePivotY,
            translationX = animation.doorLeftTranslationX,
        )
        updateDoorTransform(
            doorRightTransform,
            rotationZ = animation.doorRightRotationZ,
            pivotX = animation.doorRightHingePivotX,
            pivotY = animation.doorHingePivotY,
            translationX = animation.doorRightTranslationX,
        )
        updateBodyTransform(
            hoodTransform,
            rotationX = animation.hoodRotationX,
            pivotY = animation.hoodHingePivotY,
            pivotZ = animation.hoodHingePivotZ,
            translationZ = animation.hoodTranslationZ,
        )
        updateBodyTransform(
            hatchTransform,
            rotationX = animation.hatchRotationX,
            pivotY = animation.hatchHingePivotY,
            pivotZ = animation.hatchHingePivotZ,
            translationZ = animation.hatchTranslationZ,
        )
    }

    private fun updateDoorTransform(
        transform: EntityTransform?,
        rotationZ: Float,
        pivotX: Float,
        pivotY: Float,
        translationX: Float,
    ) {
        transform ?: return
        val pivotToOrigin = FloatArray(16)
        Matrix.setIdentityM(pivotToOrigin, 0)
        Matrix.translateM(pivotToOrigin, 0, -pivotX, -pivotY, 0f)

        val uprightRotation = FloatArray(16)
        Matrix.setIdentityM(uprightRotation, 0)
        Matrix.rotateM(uprightRotation, 0, rotationZ, 0f, 0f, 1f)

        val rotationAboutPivot = FloatArray(16)
        Matrix.multiplyMM(rotationAboutPivot, 0, uprightRotation, 0, pivotToOrigin, 0)

        val originToPivot = FloatArray(16)
        Matrix.setIdentityM(originToPivot, 0)
        Matrix.translateM(originToPivot, 0, pivotX, pivotY, 0f)

        val hingeTransform = FloatArray(16)
        Matrix.multiplyMM(hingeTransform, 0, originToPivot, 0, rotationAboutPivot, 0)

        val transformed = FloatArray(16)
        Matrix.multiplyMM(transformed, 0, hingeTransform, 0, transform.original, 0)

        if (translationX != 0f) {
            val outwardTranslation = FloatArray(16)
            Matrix.setIdentityM(outwardTranslation, 0)
            Matrix.translateM(outwardTranslation, 0, translationX, 0f, 0f)
            val translated = FloatArray(16)
            Matrix.multiplyMM(translated, 0, outwardTranslation, 0, transformed, 0)
            setTransform(transform, translated)
        } else {
            setTransform(transform, transformed)
        }
    }

    private fun updateBodyTransform(
        transform: EntityTransform?,
        rotationX: Float = 0f,
        pivotY: Float = 0f,
        pivotZ: Float = 0f,
        translationZ: Float = 0f,
    ) {
        transform ?: return

        val pivotToOrigin = FloatArray(16)
        Matrix.setIdentityM(pivotToOrigin, 0)
        Matrix.translateM(pivotToOrigin, 0, 0f, -pivotY, -pivotZ)

        val hingeRotation = FloatArray(16)
        Matrix.setIdentityM(hingeRotation, 0)
        Matrix.rotateM(hingeRotation, 0, rotationX, 1f, 0f, 0f)

        val rotationAboutPivot = FloatArray(16)
        Matrix.multiplyMM(rotationAboutPivot, 0, hingeRotation, 0, pivotToOrigin, 0)

        val originToPivot = FloatArray(16)
        Matrix.setIdentityM(originToPivot, 0)
        Matrix.translateM(originToPivot, 0, 0f, pivotY, pivotZ)

        val hingeTransform = FloatArray(16)
        Matrix.multiplyMM(hingeTransform, 0, originToPivot, 0, rotationAboutPivot, 0)

        val transformed = FloatArray(16)
        Matrix.multiplyMM(transformed, 0, hingeTransform, 0, transform.original, 0)

        if (translationZ != 0f) {
            val outwardTranslation = FloatArray(16)
            Matrix.setIdentityM(outwardTranslation, 0)
            Matrix.translateM(outwardTranslation, 0, 0f, 0f, translationZ)
            val translated = FloatArray(16)
            Matrix.multiplyMM(translated, 0, outwardTranslation, 0, transformed, 0)
            setTransform(transform, translated)
        } else {
            setTransform(transform, transformed)
        }
    }

    private fun updateRimTransforms(state: CarFrameState) {
        if (!state.spinWheels && !state.driveLoop) {
            wheelAngle = 0f
        }
        rimTransforms.forEach { transform ->
            val matrix = transform.original.copyOf()
            if (wheelAngle != 0f) {
                Matrix.rotateM(matrix, 0, wheelAngle, 1f, 0f, 0f)
            }
            setTransform(transform, matrix)
        }
    }

    private fun setTransform(transform: EntityTransform, matrix: FloatArray) {
        val transformInstance = engine.transformManager.getInstance(transform.entity)
        if (transformInstance != 0) {
            engine.transformManager.setTransform(transformInstance, matrix)
        }
    }

    private fun orbitCamera(deltaX: Float, deltaY: Float, zoom: Float) {
        val offsetX = cameraPosition[0] - cameraTarget[0]
        val offsetY = cameraPosition[1] - cameraTarget[1]
        val offsetZ = cameraPosition[2] - cameraTarget[2]
        val radius = sqrt(offsetX * offsetX + offsetY * offsetY + offsetZ * offsetZ)
        if (radius == 0f) {
            return
        }

        var yaw = atan2(offsetX, offsetZ)
        var pitch = asin((offsetY / radius).coerceIn(-1f, 1f))
        val newRadius = (radius / zoom).coerceIn(1.5f, 20f)
        yaw += deltaX * 0.01f
        pitch = (pitch - deltaY * 0.01f).coerceIn(-1.45f, 1.45f)

        cameraPosition = floatArrayOf(
            cameraTarget[0] + newRadius * cos(pitch) * sin(yaw),
            cameraTarget[1] + newRadius * sin(pitch),
            cameraTarget[2] + newRadius * cos(pitch) * cos(yaw),
        )
    }

    private fun updateCamera() {
        camera.lookAt(
            cameraPosition[0].toDouble(),
            cameraPosition[1].toDouble(),
            cameraPosition[2].toDouble(),
            cameraTarget[0].toDouble(),
            cameraTarget[1].toDouble(),
            cameraTarget[2].toDouble(),
            0.0,
            1.0,
            0.0,
        )
    }

    private fun Color.toLinearRgb(): FloatArray =
        floatArrayOf(
            linearChannel(red),
            linearChannel(green),
            linearChannel(blue),
        )

    private fun linearChannel(value: Float): Float =
        if (value <= 0.04045f) {
            value / 12.92f
        } else {
            ((value + 0.055f) / 1.055f).pow(2.4f)
        }
}

@Composable
fun FilamentCarViewer(
    cameraPreset: CarCameraPreset,
    paint: CarPaint,
    brightness: Float,
    frameState: CarFrameState,
    modelFile: File,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    initFilamentRuntime()
    val surfaceView = remember { SurfaceView(context) }
    val controller = remember(surfaceView) { CarFilamentController(context, surfaceView) }
    val currentFrameState by rememberUpdatedState(frameState)
    val currentPaint by rememberUpdatedState(paint)

    LaunchedEffect(cameraPreset) {
        controller.setCamera(cameraPreset)
    }
    LaunchedEffect(paint) {
        controller.setPaint(paint)
    }
    LaunchedEffect(brightness) {
        controller.setBrightness(brightness)
    }

    LaunchedEffect(modelFile) {
        controller.loadModel(modelFile)
        controller.setPaint(currentPaint)
    }

    DisposableEffect(controller) {
        val frameCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                controller.frame(frameTimeNanos, currentFrameState)
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
        Choreographer.getInstance().postFrameCallback(frameCallback)
        onDispose {
            Choreographer.getInstance().removeFrameCallback(frameCallback)
            controller.destroy()
        }
    }

    AndroidView(
        factory = { surfaceView },
        modifier = modifier.fillMaxSize(),
    )
}
