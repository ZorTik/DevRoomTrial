package me.zort.devroomtrial

interface Bootstrap {

    var meta: BootstrapSessionMeta?

    /**
     * Boots target.
     * @return List of errors
     */
    fun load(): MutableList<String>
    fun unload()
    fun isLoaded(): Boolean

    fun reload(): MutableList<String> {
        unload()
        return load()
    }

}