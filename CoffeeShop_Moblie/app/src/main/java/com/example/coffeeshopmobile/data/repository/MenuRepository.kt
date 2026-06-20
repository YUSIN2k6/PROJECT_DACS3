package com.example.coffeeshopmobile.data.repository

import com.example.coffeeshopmobile.base.BaseRepository
import com.example.coffeeshopmobile.data.model.MenuItem
import com.example.coffeeshopmobile.data.remote.MenuRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MenuRepository : BaseRepository() {
    private val remoteDataSource = MenuRemoteDataSource()

    /**
     * Lấy danh sách thực đơn và tự động sắp xếp theo tên món nước
     */
    fun getMenu(): Flow<List<MenuItem>> {
        return remoteDataSource.listenToMenu().map { menuList ->
            menuList.sortedBy { it.name }
        }
    }
}