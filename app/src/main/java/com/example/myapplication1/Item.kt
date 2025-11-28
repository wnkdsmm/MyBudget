package com.example.myapplication1

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
@Entity(tableName = "items")

class Item {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "itemId")
    var id: Int = 0
    @ColumnInfo(name = "incomeExpense")
    var incomeExpense: String = ""

    var date: Date = Date()
    var category: String = ""
    var comment: String = ""
    var summ: Int = 0

    constructor()

    // Основной конструктор для создания записей
    constructor(incomeExpense: String, date: Date, category: String, comment: String, summ: Int) {
        this.incomeExpense = incomeExpense
        this.date = date
        this.category = category
        this.comment = comment
        this.summ = summ
    }

    // Упрощенный конструктор (без комментария)
    constructor(incomeExpense: String, date: Date, category: String, summ: Int) {
        this.incomeExpense = incomeExpense
        this.date = date
        this.category = category
        this.summ = summ
    }
}