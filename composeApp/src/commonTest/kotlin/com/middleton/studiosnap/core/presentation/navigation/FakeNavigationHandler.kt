package com.middleton.studiosnap.core.presentation.navigation

class FakeNavigationHandler : NavigationHandler {
    
    private val _capturedCommands = mutableListOf<NavigationCommand>()
    val capturedCommands: List<NavigationCommand> = _capturedCommands
    
    var shouldThrowError = false
    var errorMessage = "Navigation error"
    
    override fun handleNavigation(command: NavigationCommand) {
        if (shouldThrowError) {
            throw IllegalStateException(errorMessage)
        }
        _capturedCommands.add(command)
    }
    
    fun clear() {
        _capturedCommands.clear()
    }
    
    fun getLastCommand(): NavigationCommand? = _capturedCommands.lastOrNull()
    
    fun getCommandCount(): Int = _capturedCommands.size
    
    fun hasNavigated(): Boolean = _capturedCommands.isNotEmpty()
    
    fun hasNavigatedWith(command: NavigationCommand): Boolean = _capturedCommands.contains(command)
}