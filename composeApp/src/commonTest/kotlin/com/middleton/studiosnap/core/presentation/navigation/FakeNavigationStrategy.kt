package com.middleton.studiosnap.core.presentation.navigation

class FakeNavigationStrategy<T : NavigationAction> : NavigationStrategy<T> {
    
    private val _navigatedActions = mutableListOf<T>()
    val navigatedActions: List<T> = _navigatedActions
    
    var shouldThrowError = false
    var errorMessage = "Navigation error"
    
    override fun navigate(action: T) {
        if (shouldThrowError) {
            throw IllegalStateException(errorMessage)
        }
        _navigatedActions.add(action)
    }
    
    fun clear() {
        _navigatedActions.clear()
    }
    
    fun getLastNavigatedAction(): T? = _navigatedActions.lastOrNull()
    
    fun getNavigationCount(): Int = _navigatedActions.size
    
    fun hasNavigated(): Boolean = _navigatedActions.isNotEmpty()
    
    fun hasNavigatedTo(action: T): Boolean = _navigatedActions.contains(action)
}