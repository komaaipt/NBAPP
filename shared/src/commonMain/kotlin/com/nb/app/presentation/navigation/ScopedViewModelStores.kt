package com.nb.app.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

/**
 * Gives a screen that isn't a navigation destination (like the team games bottom sheet) its own
 * [ViewModelStore]. Its ViewModels survive configuration changes, because this holder lives in
 * the activity's store, and are cleared as soon as [clear] is called for its key.
 */
class ScopedViewModelStores : ViewModel() {

    private val stores = mutableMapOf<String, ViewModelStore>()

    fun ownerFor(key: String): ViewModelStoreOwner {
        val store = stores.getOrPut(key) { ViewModelStore() }
        return object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = store
        }
    }

    fun clear(key: String) {
        stores.remove(key)?.clear()
    }

    override fun onCleared() {
        stores.values.forEach(ViewModelStore::clear)
        stores.clear()
    }
}
