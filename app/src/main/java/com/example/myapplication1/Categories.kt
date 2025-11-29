object Categories {
    private val _incomeCategories = mutableListOf(
        "Зарплата",
        "Инвестиции",
        "Подарки",
        "Премия",
        "Другое"
    )

    private val _expenseCategories = mutableListOf(
        "Еда",
        "Транспорт",
        "Жилье",
        "Развлечения",
        "Здоровье",
        "Одежда",
        "Образование",
        "Другое"
    )

    val incomeCategories: List<String>
        get() = _incomeCategories.toList()

    val expenseCategories: List<String>
        get() = _expenseCategories.toList()

    fun editIncomeCategory(oldCategory: String, newCategory: String): Boolean {
        val index = _incomeCategories.indexOf(oldCategory)
        if (index != -1) {
            _incomeCategories[index] = newCategory
            return true
        }
        return false
    }

    fun editExpenseCategory(oldCategory: String, newCategory: String): Boolean {
        val index = _expenseCategories.indexOf(oldCategory)
        if (index != -1) {
            _expenseCategories[index] = newCategory
            return true
        }
        return false
    }

    fun deleteIncomeCategory(category: String): Boolean {
        return _incomeCategories.remove(category)
    }

    fun deleteExpenseCategory(category: String): Boolean {
        return _expenseCategories.remove(category)
    }

    fun addIncomeCategory(category: String) {
        if (!_incomeCategories.contains(category)) {
            _incomeCategories.add(category)
        }
    }

    fun addExpenseCategory(category: String) {
        if (!_expenseCategories.contains(category)) {
            _expenseCategories.add(category)
        }
    }
}