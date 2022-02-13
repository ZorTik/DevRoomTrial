package me.zort.devroomtrial

open class BootstrapSequence<T>(private val base: T, private val inclusions: MutableList<BootInclusion<T>>): Bootstrap {

    override var meta: BootstrapSessionMeta? = null

    val sessionLoadMillis: Long?
        get() = meta?.lastMillis

    constructor(base: T, loader: BootInclusionLoader<T>): this(base, loader.load(base))

    override fun load(): MutableList<String> {
        inclusions.mapNotNull { it.canBoot(base) }.also {
            if(it.isNotEmpty()) {
                return it.toMutableList()
            }
        }
        val errMessages = inclusions
            .map { it.canBoot(base) }
        if(errMessages.any {
                it != null
            }) {
            return errMessages
                .filterNotNull()
                .toMutableList()
        }
        for (inclusion in inclusions.sortedByDescending {
            it.getPriority()
        }) {
            if(!try {
                    inclusion.load(base)
                } catch(ex: Exception) {
                    ex.printStackTrace()
                    false
                }) {
                unload(true)
                return mutableListOf(
                    "Internal: An unexpected error occurred!"
                )
            }
        }
        meta = BootstrapSessionMeta()
        return mutableListOf()
    }

    override fun unload() = unload(false)

    private fun unload(err: Boolean) = unload(inclusions, err)

    private fun unload(toUnload: MutableList<BootInclusion<T>>, err: Boolean) {
        if(!isLoaded()) return
        toUnload.forEach {
            it.unload(base, err)
        }
    }

    override fun isLoaded(): Boolean = meta != null

}