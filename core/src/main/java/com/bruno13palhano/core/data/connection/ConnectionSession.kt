package com.bruno13palhano.core.data.connection

interface ConnectionSession {
    fun save(connection: Connection)
    fun get(): Connection?
    fun clear()
}