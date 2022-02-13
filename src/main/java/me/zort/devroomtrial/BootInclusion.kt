package me.zort.devroomtrial

interface BootInclusion<T> {

    fun load(base: T): Boolean
    fun unload(base: T, err: Boolean)

    /**
     * Verifies if this inclusion can start load phase.
     * @return Verification error message
     */
    fun canBoot(base: T): String?
    fun getPriority(): Int {
        return 1
    }

}