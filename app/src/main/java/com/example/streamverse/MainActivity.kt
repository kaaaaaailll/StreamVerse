package com.example.streamverse

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var btnTabAnime: MaterialButton
    private lateinit var btnTabDrama: MaterialButton
    private lateinit var chipLove: Chip
    private lateinit var chipAction: Chip
    private lateinit var chipComedy: Chip
    private lateinit var btnAddContent: FloatingActionButton
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var rvContentList: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var btnClearSearch: ImageButton

    private var isAnimeTab = true
    private var selectedImageUri: Uri? = null
    private var selectedCategory = "Love"
    private var selectedStatus = "Ongoing"
    private var activeGenreFilter = "Love"
    private var searchQuery = ""

    private var currentPreviewImage: ImageView? = null
    private var currentPlaceholder: View? = null

    private val animeList = mutableListOf<ContentItem>()
    private val dramaList = mutableListOf<ContentItem>()
    private lateinit var animeAdapter: ContentAdapter
    private lateinit var dramaAdapter: ContentAdapter

    private lateinit var dbHelper: DatabaseHelper

    companion object {
        const val PICK_IMAGE_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dbHelper = DatabaseHelper(this)
        initViews()
        loadFromDatabase()
        setupAdapters()
        setupListeners()
        animateFab()
    }

    private fun loadFromDatabase() {
        animeList.clear()
        animeList.addAll(dbHelper.getAllAnime())
        dramaList.clear()
        dramaList.addAll(dbHelper.getAllDrama())
    }

    private fun animateFab() {
        btnAddContent.scaleX = 0f
        btnAddContent.scaleY = 0f
        btnAddContent.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setStartDelay(300)
            .setInterpolator(OvershootInterpolator(2f))
            .start()

        val pulse = ObjectAnimator.ofPropertyValuesHolder(
            btnAddContent,
            PropertyValuesHolder.ofFloat("scaleX", 1f, 1.08f, 1f),
            PropertyValuesHolder.ofFloat("scaleY", 1f, 1.08f, 1f)
        ).apply {
            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            startDelay = 1000
        }
        pulse.start()
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
        etSearch = findViewById(R.id.ET_Search)
        btnClearSearch = findViewById(R.id.BTN_ClearSearch)
    }

    private fun setupAdapters() {
        animeAdapter = ContentAdapter(animeList) { item -> showDetailDialog(item) }
        dramaAdapter = ContentAdapter(dramaList) { item -> showDetailDialog(item) }
        animeAdapter.setHasStableIds(true)
        dramaAdapter.setHasStableIds(true)
        rvContentList.itemAnimator = null
        rvContentList.adapter = animeAdapter
        applyFilter()
    }

    private fun applyFilter() {
        val sourceList = if (isAnimeTab) animeList else dramaList
        var filtered = sourceList.filter { it.category == activeGenreFilter }

        if (searchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }

        // Pinned items always on top
        val sorted = filtered.sortedByDescending { it.isPinned }

        if (isAnimeTab) {
            animeAdapter.updateList(sorted)
        } else {
            dramaAdapter.updateList(sorted)
        }

        val emptyState = findViewById<View>(R.id.LL_EmptyState)
        val recyclerView = findViewById<RecyclerView>(R.id.RV_ContentList)
        if (sorted.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun setupListeners() {
        btnTabAnime.setOnClickListener {
            isAnimeTab = true
            setActiveTab(isAnime = true)
            chipComedy.visibility = View.VISIBLE
            activeGenreFilter = "Love"
            setActiveChip(chipLove)
            rvContentList.adapter = animeAdapter
            applyFilter()
        }

        btnTabDrama.setOnClickListener {
            isAnimeTab = false
            setActiveTab(isAnime = false)
            chipComedy.visibility = View.GONE
            activeGenreFilter = "Love"
            setActiveChip(chipLove)
            rvContentList.adapter = dramaAdapter
            applyFilter()
        }

        chipLove.setOnClickListener {
            activeGenreFilter = "Love"
            setActiveChip(chipLove)
            applyFilter()
        }
        chipAction.setOnClickListener {
            activeGenreFilter = "Action"
            setActiveChip(chipAction)
            applyFilter()
        }
        chipComedy.setOnClickListener {
            activeGenreFilter = "Comedy"
            setActiveChip(chipComedy)
            applyFilter()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString()?.trim() ?: ""
                btnClearSearch.visibility = if (searchQuery.isNotEmpty()) View.VISIBLE else View.GONE
                applyFilter()
            }
        })

        btnClearSearch.setOnClickListener {
            etSearch.setText("")
            searchQuery = ""
            btnClearSearch.visibility = View.GONE
            applyFilter()
        }

        btnAddContent.setOnClickListener { showAddContentDialog() }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                else -> false
            }
        }
    }

    private fun showDetailDialog(item: ContentItem) {
        val dialog = Dialog(this, R.style.Theme_StreamVerse_FullDialog)
        dialog.setContentView(R.layout.dialog_detail)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(android.view.Gravity.BOTTOM)

        val tvDialogTitle = dialog.findViewById<TextView>(R.id.TV_DetailDialogTitle)
        val ivImage = dialog.findViewById<ImageView>(R.id.IV_DetailImage)
        val tvTitle = dialog.findViewById<TextView>(R.id.TV_DetailContentTitle)
        val tvDescription = dialog.findViewById<TextView>(R.id.TV_DetailDescription)
        val tvEpisode = dialog.findViewById<TextView>(R.id.TV_DetailEpisode)
        val tvRating = dialog.findViewById<TextView>(R.id.TV_DetailRating)
        val seekBarRating = dialog.findViewById<SeekBar>(R.id.SB_DetailRating)
        val chipCategory = dialog.findViewById<Chip>(R.id.CHIP_DetailCategory)
        val chipStatus = dialog.findViewById<Chip>(R.id.CHIP_DetailStatus)
        val btnClose = dialog.findViewById<ImageButton>(R.id.BTN_CloseDetail)
        val btnDelete = dialog.findViewById<ImageButton>(R.id.BTN_DeleteContent)
        val btnPin = dialog.findViewById<ImageButton>(R.id.BTN_PinContent)
        val btnEditEpisode = dialog.findViewById<MaterialButton>(R.id.BTN_EditEpisode)
        val btnMarkComplete = dialog.findViewById<MaterialButton>(R.id.BTN_MarkComplete)

        var currentItem = item
        var currentRating = item.rating.toIntOrNull() ?: 5

        tvDialogTitle.text = if (item.isAnime) "Anime Details" else "Drama Details"
        tvTitle.text = item.title
        tvDescription.text = item.description
        tvEpisode.text = item.episode
        tvRating.text = currentRating.toString()
        chipCategory.text = item.category
        chipStatus.text = item.status
        seekBarRating.progress = currentRating - 1

        // Set pin button color based on current state
        btnPin.setColorFilter(
            if (currentItem.isPinned)
                getColor(R.color.purple_light)
            else
                getColor(R.color.text_secondary)
        )

        if (item.status == "Complete") {
            btnMarkComplete.isEnabled = false
            btnMarkComplete.alpha = 0.5f
        }

        if (item.imageUri != null) {
            Glide.with(this)
                .load(Uri.parse(item.imageUri))
                .apply(
                    RequestOptions()
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                )
                .into(ivImage)
        }

        // Pin toggle
        btnPin.setOnClickListener {
            val updatedItem = currentItem.copy(isPinned = !currentItem.isPinned)
            dbHelper.updateContent(updatedItem)
            updateItemInList(currentItem, updatedItem)
            currentItem = updatedItem
            btnPin.setColorFilter(
                if (currentItem.isPinned)
                    getColor(R.color.purple_light)
                else
                    getColor(R.color.text_secondary)
            )
        }

        seekBarRating.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                currentRating = progress + 1
                tvRating.text = currentRating.toString()
                val updatedItem = currentItem.copy(rating = currentRating.toString())
                dbHelper.updateContent(updatedItem)
                updateItemInList(currentItem, updatedItem)
                currentItem = updatedItem
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        btnClose.setOnClickListener { dialog.dismiss() }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete \"${item.title}\"?")
                .setPositiveButton("Delete") { _, _ ->
                    dbHelper.deleteContent(item.id)
                    animeList.removeAll { it.id == item.id }
                    dramaList.removeAll { it.id == item.id }
                    applyFilter()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        btnEditEpisode.setOnClickListener {
            val editText = EditText(this)
            editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            editText.setText(currentItem.episode.replace("Episode ", "").split(" ·")[0].trim())
            editText.setTextColor(getColor(android.R.color.white))
            AlertDialog.Builder(this)
                .setTitle("Edit Episode")
                .setView(editText)
                .setPositiveButton("Save") { _, _ ->
                    val newEp = editText.text.toString().trim()
                    if (newEp.isNotEmpty()) {
                        val newEpisodeText = if (currentItem.isAnime) "Episode $newEp"
                        else "Episode $newEp · ${currentItem.status}"
                        val updatedItem = currentItem.copy(episode = newEpisodeText)
                        dbHelper.updateContent(updatedItem)
                        updateItemInList(currentItem, updatedItem)
                        currentItem = updatedItem
                        tvEpisode.text = updatedItem.episode
                        dialog.dismiss()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        btnMarkComplete.setOnClickListener {
            val updatedItem = currentItem.copy(
                status = "Complete",
                episode = currentItem.episode.replace("Ongoing", "Complete")
            )
            dbHelper.updateContent(updatedItem)
            updateItemInList(currentItem, updatedItem)
            currentItem = updatedItem
            chipStatus.text = "Complete"
            btnMarkComplete.isEnabled = false
            btnMarkComplete.alpha = 0.5f
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateItemInList(old: ContentItem, new: ContentItem) {
        val animeIdx = animeList.indexOfFirst { it.id == old.id }
        if (animeIdx >= 0) animeList[animeIdx] = new

        val dramaIdx = dramaList.indexOfFirst { it.id == old.id }
        if (dramaIdx >= 0) dramaList[dramaIdx] = new

        applyFilter()
    }

    private fun showAddContentDialog() {
        selectedImageUri = null
        selectedCategory = "Love"
        selectedStatus = "Ongoing"
        currentPreviewImage = null
        currentPlaceholder = null

        val dialog = Dialog(this, R.style.Theme_StreamVerse_FullDialog)
        dialog.setContentView(R.layout.dialog_add_content)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setGravity(android.view.Gravity.BOTTOM)

        val tvTitle = dialog.findViewById<TextView>(R.id.TV_DialogTitle)
        val btnSave = dialog.findViewById<MaterialButton>(R.id.BTN_SaveContent)
        val etTitle = dialog.findViewById<TextInputEditText>(R.id.ET_Title)
        val etDescription = dialog.findViewById<TextInputEditText>(R.id.ET_Description)
        val etEpisode = dialog.findViewById<TextInputEditText>(R.id.ET_Episode)
        val ivPreview = dialog.findViewById<ImageView>(R.id.IV_ImagePreview)
        val llPlaceholder = dialog.findViewById<View>(R.id.LL_ImagePlaceholder)
        val btnUpload = dialog.findViewById<MaterialButton>(R.id.BTN_UploadPhoto)
        val chipCatComedy = dialog.findViewById<Chip>(R.id.CHIP_CatComedy)

        currentPreviewImage = ivPreview
        currentPlaceholder = llPlaceholder

        if (isAnimeTab) {
            tvTitle.text = "Add Anime"
            btnSave.text = "Save Anime"
            chipCatComedy.visibility = View.VISIBLE
        } else {
            tvTitle.text = "Add Drama"
            btnSave.text = "Save Drama"
            chipCatComedy.visibility = View.GONE
        }

        btnUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        val chipCatLove = dialog.findViewById<Chip>(R.id.CHIP_CatLove)
        val chipCatAction = dialog.findViewById<Chip>(R.id.CHIP_CatAction)
        val catChips = listOf(chipCatLove, chipCatAction, chipCatComedy)

        chipCatLove.setOnClickListener { selectedCategory = "Love"; setDialogActiveChip(chipCatLove, catChips) }
        chipCatAction.setOnClickListener { selectedCategory = "Action"; setDialogActiveChip(chipCatAction, catChips) }
        chipCatComedy.setOnClickListener { selectedCategory = "Comedy"; setDialogActiveChip(chipCatComedy, catChips) }

        val btnOngoing = dialog.findViewById<MaterialButton>(R.id.BTN_StatusOngoing)
        val btnComplete = dialog.findViewById<MaterialButton>(R.id.BTN_StatusComplete)
        btnOngoing.setOnClickListener { selectedStatus = "Ongoing"; setDialogActiveStatus(true, btnOngoing, btnComplete) }
        btnComplete.setOnClickListener { selectedStatus = "Complete"; setDialogActiveStatus(false, btnOngoing, btnComplete) }

        dialog.findViewById<ImageButton>(R.id.BTN_CloseDialog).setOnClickListener {
            currentPreviewImage = null
            currentPlaceholder = null
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val episode = etEpisode.text.toString().trim()

            if (title.isEmpty()) { etTitle.error = "Please enter a title"; return@setOnClickListener }

            val episodeText = if (isAnimeTab) "Episode $episode"
            else "Episode $episode · $selectedStatus"

            val newItem = ContentItem(
                title = title,
                description = description,
                episode = episodeText,
                rating = "5",
                category = selectedCategory,
                status = selectedStatus,
                isAnime = isAnimeTab,
                imageUri = selectedImageUri?.toString()
            )

            val newId = dbHelper.insertContent(newItem)
            val savedItem = newItem.copy(id = newId)

            if (isAnimeTab) animeList.add(0, savedItem)
            else dramaList.add(0, savedItem)

            applyFilter()

            currentPreviewImage = null
            currentPlaceholder = null
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            selectedImageUri?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .apply(
                        RequestOptions()
                            .centerInside()
                            .override(800, 600)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                    )
                    .into(currentPreviewImage!!)
                currentPreviewImage?.visibility = View.VISIBLE
                currentPlaceholder?.visibility = View.GONE
            }
        }
    }

    private fun setDialogActiveChip(selected: Chip, allChips: List<Chip?>) {
        allChips.forEach { chip ->
            if (chip == selected) {
                chip?.chipBackgroundColor = getColorStateList(R.color.purple_500)
                chip?.setTextColor(getColor(android.R.color.white))
            } else {
                chip?.chipBackgroundColor = getColorStateList(R.color.bg_card)
                chip?.setTextColor(getColor(R.color.text_secondary))
            }
        }
    }

    private fun setDialogActiveStatus(isOngoing: Boolean, btnOngoing: MaterialButton, btnComplete: MaterialButton) {
        if (isOngoing) {
            btnOngoing.backgroundTintList = getColorStateList(R.color.purple_500)
            btnOngoing.setTextColor(getColor(android.R.color.white))
            btnComplete.backgroundTintList = getColorStateList(R.color.bg_card)
            btnComplete.setTextColor(getColor(R.color.text_secondary))
        } else {
            btnComplete.backgroundTintList = getColorStateList(R.color.purple_500)
            btnComplete.setTextColor(getColor(android.R.color.white))
            btnOngoing.backgroundTintList = getColorStateList(R.color.bg_card)
            btnOngoing.setTextColor(getColor(R.color.text_secondary))
        }
    }

    private fun setActiveTab(isAnime: Boolean) {
        if (isAnime) {
            btnTabAnime.backgroundTintList = getColorStateList(R.color.purple_500)
            btnTabAnime.setTextColor(getColor(android.R.color.white))
            btnTabAnime.iconTint = getColorStateList(android.R.color.white)
            btnTabDrama.backgroundTintList = getColorStateList(R.color.bg_card)
            btnTabDrama.setTextColor(getColor(R.color.text_secondary))
            btnTabDrama.iconTint = getColorStateList(R.color.text_secondary)
        } else {
            btnTabDrama.backgroundTintList = getColorStateList(R.color.purple_500)
            btnTabDrama.setTextColor(getColor(android.R.color.white))
            btnTabDrama.iconTint = getColorStateList(android.R.color.white)
            btnTabAnime.backgroundTintList = getColorStateList(R.color.bg_card)
            btnTabAnime.setTextColor(getColor(R.color.text_secondary))
            btnTabAnime.iconTint = getColorStateList(R.color.text_secondary)
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