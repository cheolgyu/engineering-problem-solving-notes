// Exact source excerpts from blink; original repository is private.
// Original commit: d2e90698ccf7f45154979f3cd4bc9ce5fb7ec0fa
// Original path: eye-unity-bridge/src/main/java/com/kullstudio/eye/unity/UnityCatchDirectionControl.kt
// Selected regions only; dependencies and omitted code are not included.
// Reading context: Numeric copy only; stale observations, session reset and face/closure clear have separate gates.

// Original lines 9-47
/** Stable product API: numeric observations only, no research models or image export. */
object UnityCatchDirectionControl {
    @Volatile internal var enabled = false
    private val owner = Any()
    private var values: DoubleArray? = null
    private var at = 0L
    private var active = false
    private var revision = 0
    private var validSince = 0L

    @JvmStatic @Synchronized fun configure() { enabled = true }
    @JvmStatic @Synchronized fun snapshot(): DoubleArray {
        val now = SystemClock.elapsedRealtime()
        val source = values
        if (!active || source == null || now < at || now - at > 250L) return doubleArrayOf(revision.toDouble(), 0.0)
        return doubleArrayOf(revision.toDouble(), at.toDouble()) + source
    }
    internal fun config(base: MediaPipeEyeInputConfig) = base.withFrameCadence(33L, 30).copy(
        targetWidth = 1280, targetHeight = 1280, analysisWidth = 1280, analysisHeight = 720,
        collectEyeRoiMetrics = false, outputFaceBlendshapes = false,
        adaptiveBilateralGazeRecovery = true, preferGpuDelegate = true)

    internal fun start(context: Context) {
        require(context.packageName in setOf("com.kullstudio.blinkgame", "com.kullstudio.blinkgame.debug",
            "com.kullstudio.blinkgame.smoke", "com.kullstudio.blinkgame.adsmoke"))
        reset(); synchronized(this) { active = true }
        EyeDirectionObservationPort.attach(owner) { time, source ->
            synchronized(this) {
                if (active && time >= validSince) { values?.fill(0.0); values = source?.copyOf(); at = time }
            }
        }
    }
    @Synchronized internal fun observe(input: EyeInputSnapshot) {
        if (!input.faceObserved || input.closureActive) clear()
    }
    @Synchronized internal fun reset() { revision++; validSince = SystemClock.elapsedRealtime(); clear() }
    @Synchronized internal fun pause(paused: Boolean) { active = !paused; reset() }
    @Synchronized internal fun stop() { active = false; reset(); EyeDirectionObservationPort.detach(owner) }
    private fun clear() { values?.fill(0.0); values = null; at = 0 }

