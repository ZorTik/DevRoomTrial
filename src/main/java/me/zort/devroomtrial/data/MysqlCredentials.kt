package me.zort.devroomtrial.data

class MysqlCredentials(val url: String, val port: Int, val db: String, val user: String, val pw: String): DataConnectionCredentials {

    fun buildJdbcUrl(): String {
        return buildJdbcUrl("mysql")
    }

    fun buildJdbcUrl(sqlArchitecture: String): String {
        return "jdbc:$sqlArchitecture://$url:$port/$db"
    }

    override fun getData(): MutableMap<String, Any> {
        return mutableMapOf(
            Pair("url", url),
            Pair("port", port),
            Pair("db", db),
            Pair("user", user),
            Pair("pw", pw)
        )
    }

}