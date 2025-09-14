package com.demo.logger

object AppLogger {

    var enabled = true
    var minLevel = Level.INFO

    private const val DEFAULT_TAG = "CryptoExchange"

    inline fun <reified T : Any> tag(): String = T::class.simpleName.validTag()

    enum class Level { DEBUG, INFO, WARN, ERROR }

    private var impl: Logger = DefaultLogger()

    fun setLogger(logger: Logger) {
        impl = logger
    }

    fun String?.validTag() : String {
        return this?: DEFAULT_TAG
    }

    private fun printLog(level: Level, tag: String?, message: String, block: (String, String) -> Unit) {
        if (level.canShow()) block(tag.validTag(), message)
    }

    fun d(tag: String? = null, message: String) {
        printLog(Level.DEBUG, tag, message) { t, m -> impl.d(t, m) }
    }

    fun i(tag: String? = null, message: String) {
        printLog(Level.DEBUG, tag, message) { t, m -> impl.i(t, m) }
    }

    fun w(tag: String? = null, message: String) {
        printLog(Level.DEBUG, tag, message) { t, m -> impl.w(t, m) }
    }

    fun e(tag: String? = null, message: String) {
        printLog(Level.DEBUG, tag, message) { t, m -> impl.e(t, m) }
    }

    private fun Level.canShow() : Boolean {
         return enabled && ordinal >= minLevel.ordinal
    }


}
