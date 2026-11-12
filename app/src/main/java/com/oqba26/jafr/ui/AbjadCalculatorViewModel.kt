package com.oqba26.jafr.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.oqba26.jafr.AbjadResult
import com.oqba26.jafr.AbjadType
import com.oqba26.jafr.AbjadUtils
import com.oqba26.jafr.HistoryManager
import com.oqba26.jafr.Jafr15Result
import com.oqba26.jafr.NadhiraType
import com.oqba26.jafr.TabayeAnalysis
import com.oqba26.jafr.model.HistoryItem
import com.oqba26.jafr.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import saman.zamani.persiandate.PersianDate
import saman.zamani.persiandate.PersianDateFormat
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
class AbjadCalculatorViewModel(
    private val historyManager: HistoryManager
) : ViewModel() {

    private val _rawText = MutableStateFlow("")
    private val _selectedType = MutableStateFlow(AbjadType.JAFR_15)

    private val _selectedNadhira = MutableStateFlow(NadhiraType.ABJAD)
    val selectedNadhira: StateFlow<NadhiraType> = _selectedNadhira.asStateFlow()

    private val _cleanText = MutableStateFlow("")
    val cleanText: StateFlow<String> = _cleanText.asStateFlow()

    private val _isQuestionComplete = MutableStateFlow(false)

    private val _abjadResult = MutableStateFlow<AbjadResult?>(null)
    val abjadResult: StateFlow<AbjadResult?> = _abjadResult.asStateFlow()

    private val _jafrResult = MutableStateFlow<Jafr15Result?>(null)
    val jafrResult: StateFlow<Jafr15Result?> = _jafrResult.asStateFlow()

    private val _jafrNumericalResult = MutableStateFlow<JafrNumericalResult?>(null)
    val jafrNumericalResult: StateFlow<JafrNumericalResult?> = _jafrNumericalResult.asStateFlow()

    private val _tabayeResult = MutableStateFlow<TabayeAnalysis?>(null)
    val tabayeResult: StateFlow<TabayeAnalysis?> = _tabayeResult.asStateFlow()

    // اسرار و محاسبات مستخرج از کتاب مفتاح طلاسم کنوز
    private val _muwakkilResult = MutableStateFlow<MuwakkilResult?>(null)
    val muwakkilResult: StateFlow<MuwakkilResult?> = _muwakkilResult.asStateFlow()

    private val _kawakibHourInfo = MutableStateFlow<KawakibHourInfo?>(null)
    val kawakibHourInfo: StateFlow<KawakibHourInfo?> = _kawakibHourInfo.asStateFlow()

    private val _rijalAlGhaibInfo = MutableStateFlow<RijalAlGhaibInfo?>(null)
    val rijalAlGhaibInfo: StateFlow<RijalAlGhaibInfo?> = _rijalAlGhaibInfo.asStateFlow()

    private val _tabayeMizanResult = MutableStateFlow<TabayeMizanResult?>(null)
    val tabayeMizanResult: StateFlow<TabayeMizanResult?> = _tabayeMizanResult.asStateFlow()

    private val _ancientScriptResult = MutableStateFlow<AncientScriptResult?>(null)
    val ancientScriptResult: StateFlow<AncientScriptResult?> = _ancientScriptResult.asStateFlow()

    private val _selectedOfaqSize = MutableStateFlow(3)

    private val _ofaqExtendedResult = MutableStateFlow<OfaqExtendedResult?>(null)
    val ofaqExtendedResult: StateFlow<OfaqExtendedResult?> = _ofaqExtendedResult.asStateFlow()

    private var lastSavedText: String = ""

    data class StateInputs(
        val raw: String,
        val type: AbjadType,
        val nadhira: NadhiraType,
        val ofaqSize: Int
    )

    init {
        // Continuous asynchronous calculation pipeline off the main thread
        viewModelScope.launch {
            combine(
                _rawText,
                _selectedType,
                _selectedNadhira,
                _selectedOfaqSize
            ) { raw, type, nadhira, ofaqSize ->
                StateInputs(raw, type, nadhira, ofaqSize)
            }.collectLatest { (raw, type, nadhira, ofaqSize) ->
                withContext(Dispatchers.Default) {
                    val clean = AbjadUtils.stripYaHoo(raw)
                    _cleanText.value = clean

                    val trimmed = clean.trim()
                    val complete = (trimmed.endsWith("؟") || trimmed.endsWith("?")) && (trimmed.length > 10)
                    _isQuestionComplete.value = complete

                    if (clean.isBlank()) {
                        _abjadResult.value = null
                        _jafrResult.value = null
                        _jafrNumericalResult.value = null
                        _tabayeResult.value = null
                        _muwakkilResult.value = null
                        _kawakibHourInfo.value = null
                        _rijalAlGhaibInfo.value = null
                        _tabayeMizanResult.value = null
                        _ancientScriptResult.value = null
                        _ofaqExtendedResult.value = null
                        return@withContext
                    }

                    // Calculate basic Abjad result
                    _abjadResult.value = AbjadUtils.calculate(clean, type)
                    _tabayeResult.value = AbjadUtils.analyzeTabaye(clean)

                    // محاسبات مفتاح طلاسم کنوز
                    _kawakibHourInfo.value = MiftahJafrUtils.calculateCurrentKawakibHour()
                    _rijalAlGhaibInfo.value = MiftahJafrUtils.calculateRijalAlGhaib()
                    _tabayeMizanResult.value = MiftahJafrUtils.calculateTabayeMizan(clean)
                    _ancientScriptResult.value = MiftahJafrUtils.convertToAncientScript(clean)

                    val names = AbjadUtils.extractNames(clean)
                    val pName = names.first ?: clean.take(8)
                    val mName = names.second ?: "حوا"
                    _muwakkilResult.value = MiftahJafrUtils.extractMuwakkil(pName, mName)

                    val totalSum = AbjadUtils.calculate(clean, AbjadType.KABIR).total
                    _ofaqExtendedResult.value = MiftahJafrUtils.generateOfaqExtended(totalSum, ofaqSize)

                    // Calculate Jafr 15 result
                    if (type == AbjadType.JAFR_15 && complete) {
                        _jafrResult.value = AbjadUtils.calculateJafr15(clean, nadhira, PersianDate())
                    } else {
                        _jafrResult.value = null
                    }

                    // Calculate Jafr Numerical result
                    if ((type == AbjadType.JAFR_15 || type == AbjadType.JAFR_NUMERICAL) && complete) {
                        _jafrNumericalResult.value = JafrNumericalUtils.calculateNumericalJafr(clean)
                    } else {
                        _jafrNumericalResult.value = null
                    }
                }
            }
        }

        // Auto-save pipeline with debounce to prevent duplicate/laggy saves
        viewModelScope.launch {
            _cleanText
                .debounce(1500.milliseconds)
                .distinctUntilChanged()
                .collectLatest { clean ->
                    val trimmed = clean.trim()
                    val complete = (trimmed.endsWith("؟") || trimmed.endsWith("?")) && (trimmed.length > 10)
                    if (complete && trimmed.isNotEmpty() && trimmed != lastSavedText) {
                        withContext(Dispatchers.Default) {
                            val names = AbjadUtils.extractNames(trimmed)
                            val pDate = PersianDate()
                            val formatter = PersianDateFormat("Y/m/d H:i:s")
                            val timestamp = formatter.format(pDate)

                            val j15Res = AbjadUtils.calculateJafr15(trimmed, _selectedNadhira.value, pDate)
                            val item = HistoryItem(
                                text = trimmed,
                                firstName = names.first,
                                motherName = names.second,
                                result = 0,
                                answer = j15Res.answer,
                                type = AbjadType.JAFR_15,
                                timestamp = timestamp
                            )
                            historyManager.addHistoryItem(item)
                            lastSavedText = trimmed
                        }
                    }
                }
        }
    }

    fun onTextChanged(newText: String) {
        _rawText.value = newText
    }

    fun onTypeSelected(type: AbjadType) {
        _selectedType.value = type
    }

    fun onNadhiraSelected(nadhira: NadhiraType) {
        _selectedNadhira.value = nadhira
    }

    fun onOfaqSizeSelected(size: Int) {
        _selectedOfaqSize.value = size
    }

    companion object {
        fun provideFactory(historyManager: HistoryManager): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AbjadCalculatorViewModel(historyManager) as T
                }
            }
    }
}
