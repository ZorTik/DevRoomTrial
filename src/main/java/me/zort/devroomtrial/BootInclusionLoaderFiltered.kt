package me.zort.devroomtrial

import org.reflections.Reflections
import java.lang.reflect.Constructor

abstract class BootInclusionLoaderFiltered<T>(private val filters: MutableList<BootInclusionLoaderFilter<T>>): BootInclusionLoader<T> {

    constructor(loaderFilterFetchingSuperType: Class<out BootInclusionLoaderFilter<T>>, vararg currentAddons: BootInclusionLoaderFilter<T>)
            : this(DevRoomTrial.INSTANCE.reflections, loaderFilterFetchingSuperType, *currentAddons)

    constructor(ref: Reflections, loaderFilterFetchingSuperType: Class<out BootInclusionLoaderFilter<T>>?, vararg currentAddons: BootInclusionLoaderFilter<T>)
            : this(mutableListOf<BootInclusionLoaderFilter<T>>().also { l ->
        val cPred: (Constructor<*>) -> Boolean = { it.parameterCount == 0 }
        val st: List<Class<out BootInclusionLoaderFilter<T>>> = ref.getSubTypesOf(loaderFilterFetchingSuperType).filter {
            it.declaredConstructors.any(cPred)
        }
        l.addAll(
            st.map {
                val fc = it.declaredConstructors.first(cPred)
                @Suppress("UNCHECKED_CAST")
                fc.newInstance() as BootInclusionLoaderFilter<T>
            }.toMutableList().also {
                it.addAll(currentAddons)
            }
        )
    })

    override fun load(base: T): MutableList<BootInclusion<T>> {
        return loadUnfiltered(base).filter { inclusion ->
            filters.all { it.verify(base, inclusion) }
        }.toMutableList()
    }

    abstract fun loadUnfiltered(base: T): MutableList<BootInclusion<T>>

    interface BootInclusionLoaderFilter<T> {

        fun verify(base: T, inclusion: BootInclusion<T>): Boolean

    }

}