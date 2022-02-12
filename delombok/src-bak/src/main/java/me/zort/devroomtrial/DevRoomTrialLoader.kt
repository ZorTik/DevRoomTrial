package me.zort.devroomtrial

import org.reflections.Reflections

class DevRoomTrialLoader: BootInclusionLoaderFiltered<DevRoomTrial>(DevRoomTrialLoaderFilter::class.java) {

    @Suppress("UNCHECKED_CAST")
    override fun loadUnfiltered(base: DevRoomTrial): MutableList<BootInclusion<DevRoomTrial>> {
        val ref: Reflections = base.reflections
        val inclusionClass = DevRoomTrialInclusion::class.java
        val baseClass = DevRoomTrial::class.java
        return ref.getSubTypesOf(inclusionClass)
            .mapNotNull {
                it.declaredConstructors.firstOrNull { c ->
                    c.parameterCount == 0 || (c.parameterCount == 1 && c.parameters.all { p ->
                        p.type == baseClass
                    })
                }
            }.map {
                if(it.parameterCount == 0) { it.newInstance() } else { it.newInstance(base) }
                        as DevRoomTrialInclusion
            }
            .toMutableList()
    }

}