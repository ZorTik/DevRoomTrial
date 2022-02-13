package me.zort.devroomtrial.data

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.jdbc.JdbcConnectionSource
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource
import me.zort.devroomtrial.DevRoomTrial
import me.zort.devroomtrial.data.annotation.EntityAutoConfigure
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder

class MysqlDataBridge: DataBridge<JdbcPooledConnectionSource, MysqlCredentials> {

    private var connection: JdbcPooledConnectionSource? = null
    private val daoCache: MutableMap<DaoIdentifier, Dao<*, *>> = mutableMapOf()
    private val refCache: MutableMap<String, Reflections> = mutableMapOf()

    override fun connect(credentials: MysqlCredentials): JdbcPooledConnectionSource? {
        return connect(credentials, null)
    }

    fun connect(credentials: MysqlCredentials, sqlArchitecture: String?): JdbcPooledConnectionSource? {
        return connect(credentials, sqlArchitecture, DevRoomTrial::class.java.`package`.name)
    }

    fun connect(credentials: MysqlCredentials, sqlArchitecture: String?, jpaEntitiesFinderPackageUrl: String?): JdbcPooledConnectionSource? {
        val jdbcUrl = if(sqlArchitecture != null) {
            credentials.buildJdbcUrl(sqlArchitecture)
        } else {
            credentials.buildJdbcUrl()
        }
        connection = JdbcPooledConnectionSource(jdbcUrl, credentials.user, credentials.pw)
        val cClass = JdbcConnectionSource::class.java
        val cIField = cClass.getDeclaredField("initialized");
        cIField.isAccessible = true
        if(!(cIField.get(connection) as Boolean)) {
            return null
        }
        if(jpaEntitiesFinderPackageUrl != null) {
            val ref = refCache[jpaEntitiesFinderPackageUrl].let {
                return@let if(it == null) {
                    val r = Reflections(
                        ConfigurationBuilder()
                            .filterInputsBy(FilterBuilder().includePackage(jpaEntitiesFinderPackageUrl))
                            .setUrls(ClasspathHelper.forPackage(jpaEntitiesFinderPackageUrl))
                            .setScanners(
                                SubTypesScanner(false),
                                TypeAnnotationsScanner()
                            )
                    )
                    refCache[jpaEntitiesFinderPackageUrl] = r
                    r
                } else it
            }
            val autoConfigureAnnotClass = EntityAutoConfigure::class.java
            val entityCandidates: MutableSet<Class<*>> = ref.getTypesAnnotatedWith(autoConfigureAnnotClass)
            entityCandidates.forEach { entityClass ->
                if(entityClass.isAnnotationPresent(autoConfigureAnnotClass)) {
                    val autoConfigureAnnot = entityClass.getDeclaredAnnotation(autoConfigureAnnotClass)
                    val idClass = autoConfigureAnnot.idClass.java
                    registerDao(entityClass, idClass)
                }
            }
        }
        return connection
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> findDao(entityClass: Class<T>): Dao<T, *>? {
        return daoCache.entries.firstOrNull { (identifier) ->
            identifier.entityClass == entityClass
        }.let { it?.value as Dao<T, *>? }
    }

    fun <T, ID> registerDao(entityClass: Class<T>, idClass: Class<ID>): Dao<T, ID> = getDao(entityClass, idClass)

    @Suppress("UNCHECKED_CAST")
    fun <T, ID> getDao(entityClass: Class<T>, idClass: Class<ID>): Dao<T, ID> = (daoCache.entries.firstOrNull { (id, _) ->
        id.compare(entityClass, idClass)
    }?.value?: let {
        val d: Dao<T, ID> = DaoManager.createDao(connection, entityClass)
        daoCache[DaoIdentifier(entityClass, idClass)] = d
        d
    }) as Dao<T, ID>

    override fun getConnection(): JdbcPooledConnectionSource? = connection

    override fun close() {
        if(isConnected()) {
            daoCache.values.forEach {
                it.clearObjectCache()
                it.closeLastIterator()
                DaoManager.unregisterDao(connection, it)
            }
            daoCache.clear()
            connection!!.close()
            connection = null
        }
    }

    data class DaoIdentifier(val entityClass: Class<*>, val idClass: Class<*>) {

        fun compare(other: DaoIdentifier): Boolean {
            return compare(other.entityClass, other.idClass)
        }

        fun compare(otherEntityClass: Class<*>, otherIdClass: Class<*>): Boolean {
            return otherEntityClass == entityClass && otherIdClass == idClass
        }

    }

}