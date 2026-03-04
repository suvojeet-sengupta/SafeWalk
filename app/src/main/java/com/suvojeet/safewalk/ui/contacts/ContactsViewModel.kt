package com.suvojeet.safewalk.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suvojeet.safewalk.data.local.db.dao.ContactDao
import com.suvojeet.safewalk.data.local.db.entity.ContactEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactDao: ContactDao,
) : ViewModel() {

    val contacts: StateFlow<List<ContactEntity>> = contactDao.getAllContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _editingContact = MutableStateFlow<ContactEntity?>(null)
    val editingContact: StateFlow<ContactEntity?> = _editingContact.asStateFlow()

    fun showAddDialog() {
        _editingContact.value = null
        _showAddDialog.value = true
    }

    fun showEditDialog(contact: ContactEntity) {
        _editingContact.value = contact
        _showAddDialog.value = true
    }

    fun dismissDialog() {
        _showAddDialog.value = false
        _editingContact.value = null
    }

    fun saveContact(name: String, phone: String, relationship: String) {
        viewModelScope.launch {
            val existing = _editingContact.value
            val contact = ContactEntity(
                id = existing?.id ?: UUID.randomUUID().toString(),
                name = name.trim(),
                phone = phone.trim(),
                relationship = relationship.trim(),
                priority = existing?.priority ?: (contactDao.getContactCount()),
                isActive = true,
            )
            contactDao.insertContact(contact)
            _showAddDialog.value = false
            _editingContact.value = null
        }
    }

    fun deleteContact(contact: ContactEntity) {
        viewModelScope.launch {
            contactDao.deleteContact(contact)
        }
    }

    fun toggleContactActive(contact: ContactEntity) {
        viewModelScope.launch {
            contactDao.updateContact(contact.copy(isActive = !contact.isActive))
        }
    }
}
