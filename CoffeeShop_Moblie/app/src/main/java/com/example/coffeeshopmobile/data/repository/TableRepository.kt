package com.example.coffeeshopmobile.data.repository

import com.example.coffeeshopmobile.base.BaseRepository
import com.example.coffeeshopmobile.data.model.Table
import com.example.coffeeshopmobile.data.remote.TableRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TableRepository : BaseRepository() {
    private val remoteDataSource = TableRemoteDataSource()

    /**
     * Lấy danh sách bàn từ Remote và sắp xếp tăng dần theo tableNumber
     */
    fun getTables(): Flow<List<Table>> {
        return remoteDataSource.listenToTables().map { tableList ->
            tableList.sortedBy { it.tableNumber }
        }
    }

    suspend fun updateTableStatus(tableId: String, newStatus: String): Boolean {
        return remoteDataSource.updateTableStatus(tableId, newStatus)
    }

    suspend fun mergeTable(currentTableId: String, targetTableNumber: Int): Boolean {
        return remoteDataSource.mergeTable(currentTableId, targetTableNumber)
    }

    suspend fun unmergeTable(tableId: String): Boolean {
        return remoteDataSource.unmergeTable(tableId)
    }

    suspend fun unmergeAllFromTarget(tablesToUnmerge: List<String>): Boolean {
        return remoteDataSource.unmergeAllFromTarget(tablesToUnmerge)
    }

    suspend fun transferTable(originTable: Table, targetTable: Table, allTables: List<Table>): Boolean {
        return remoteDataSource.transferTable(originTable, targetTable, allTables)
    }

    suspend fun updateTableNotes(tableId: String, notes: String): Boolean {
        return remoteDataSource.updateTableNotes(tableId, notes)
    }

    suspend fun updateTableOrder(tableId: String, order: com.example.coffeeshopmobile.data.model.ActiveOrder?): Boolean {
        return remoteDataSource.updateTableOrder(tableId, order)
    }
}