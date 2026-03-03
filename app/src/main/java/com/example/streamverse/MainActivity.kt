package com.example.streamverse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var btnTabAnime: Button
    private lateinit var btnTabDrama: Button
    private lateinit var chipLove: Chip
    private lateinit var chipAction: Chip
    private lateinit var chipComedy: Chip
    private lateinit var btnAddContent: FloatingActionButton
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var rvContentList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        btnTabAnime = findViewById(R.id.BTN_TabAnime)
        btnTabDrama = findViewById(R.id.BTN_TabDrama)
        chipLove = findViewById(R.id.CHIP_GenreLove)
        chipAction = findViewById(R.id.CHIP_GenreAction)
        chipComedy = findViewById(R.id.CHIP_GenreComedy)
        btnAddContent = findViewById(R.id.BTN_AddContent)
        bottomNav = findViewById(R.id.BNV_BottomNav)
        rvContentList = findViewById(R.id.RV_ContentList)
        rvContentList.layoutManager = LinearLayoutManager(this)
    }

    private fun setupListeners() {

        btnTabAnime.setOnClickListener {
            setActiveTab(isAnime = true)
        }

        btnTabDrama.setOnClickListener {
            setActiveTab(isAnime = false)
        }

        chipLove.setOnClickListener { setActiveChip(chipLove) }
        chipAction.setOnClickListener { setActiveChip(chipAction) }
        chipComedy.setOnClickListener { setActiveChip(chipComedy) }

        btnAddContent.setOnClickListener {
            // TODO: Open Add Content screen
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                else -> false
            }
        }
    }

    private fun setActiveTab(isAnime: Boolean) {
        if (isAnime) {
            btnTabAnime.backgroundTintList = getColorStateList(R.color.purple_500)
            btnTabAnime.setTextColor(getColor(android.R.color.white))
            btnTabDrama.backgroundTintList = getColorStateList(R.color.bg_card)
            btnTabDrama.setTextColor(getColor(R.color.text_secondary))
        } else {
            btnTabDrama.backgroundTintList = getColorStateList(R.color.purple_500)
            btnTabDrama.setTextColor(getColor(android.R.color.white))
            btnTabAnime.backgroundTintList = getColorStateList(R.color.bg_card)
            btnTabAnime.setTextColor(getColor(R.color.text_secondary))
        }
    }

    private fun setActiveChip(selectedChip: Chip) {
        val chips = listOf(chipLove, chipAction, chipComedy)
        chips.forEach { chip ->
            if (chip == selectedChip) {
                chip.chipBackgroundColor = getColorStateList(R.color.purple_500)
                chip.setTextColor(getColor(android.R.color.white))
            } else {
                chip.chipBackgroundColor = getColorStateList(R.color.bg_card)
                chip.setTextColor(getColor(R.color.text_secondary))
            }
        }
    }
}