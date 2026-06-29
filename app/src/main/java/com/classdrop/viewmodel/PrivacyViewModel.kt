package com.classdrop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.classdrop.model.CommunityRule
import com.classdrop.repository.NormsRepository
import kotlinx.coroutines.launch

class PrivacyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NormsRepository(application)
    val privacyRules: LiveData<List<CommunityRule>> = repository.getPrivacyRulesFlow().asLiveData()

    fun saveRule(rule: CommunityRule) {
        viewModelScope.launch {
            repository.savePrivacyRule(rule)
        }
    }

    fun deleteRule(rule: CommunityRule) {
        viewModelScope.launch {
            repository.deletePrivacyRule(rule)
        }
    }

    fun saveAllRules(rules: List<CommunityRule>) {
        viewModelScope.launch {
            repository.savePrivacyRules(rules)
        }
    }
}
