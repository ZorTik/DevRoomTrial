package me.zort.devroomtrial

interface BootInclusionLoader<T> {

    fun load(base: T): MutableList<BootInclusion<T>>

}