package me.zort.devroomtrial.data

import com.j256.ormlite.support.ConnectionSource
import java.io.Closeable

interface DataBridge<T: ConnectionSource, C: DataConnectionCredentials>: Closeable {

    fun connect(credentials: C): T?
    fun getConnection(): T?

    fun isConnected(): Boolean {
        return getConnection() != null
    }

}