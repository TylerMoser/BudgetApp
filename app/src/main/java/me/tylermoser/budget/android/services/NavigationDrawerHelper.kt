package me.tylermoser.budget.android.services

import android.view.SubMenu
import me.tylermoser.budget.R
import me.tylermoser.budget.android.activities.BudgetActivity
import me.tylermoser.budget.android.fragments.*
import me.tylermoser.budget.android.models.SheetEntry

/**
 * A helper class for managing the navigation drawer UI and data
 *
 * @author Tyler Moser
 */
class NavigationDrawerHelper(val activity: BudgetActivity, val cache: Cache) {

    /**
     * If the user has not specified a sheet to open when the app loads through the open on load
     * button, the app will open the first active sheet on the spreadsheet
     *
     * @param entries A list of [SheetEntry] representing the sheets on the current spreadsheet
     */
    fun openSheetIfNoneSpecified(entries: List<SheetEntry>) {
        if (!activity.isCurrentFragmentInitialized() && !cache.isOpenOnLoadValid()) {
            openSheetFromMetadata(entries)
        }
    }

    /**
     * Opens the first active sheet found in a [SheetEntry] list. [SheetEntry] lists are created
     * from spreadsheet metadata.
     *
     * @return True if an active budget is found and loaded. False otherwise.
     */
    fun openSheetFromMetadata(entries: List<SheetEntry>, sheetID: String = ""): Boolean {
        for (entry in entries) {
            if (!entry.archived && entry.sheetId != sheetID) {
                activity.currentFragment = ActiveBudgetFragment.newInstance(activity, entry.sheetId, entry.sheetName)
                commitMainFragmentChange()
                return true
            }
        }
        return false
    }

    /**
     * Place the sheet that is designated as the sheet to open on load at the top of the active
     * sheets submenu
     */
    fun putOpenOnLoadSheetAtTopOfActiveSheetsSubMenu(
            subMenu: SubMenu,
            entries: MutableList<SheetEntry>
    ) {
        if (cache.isOpenOnLoadValid()) {
            val (openOnLoadSheetName, openOnLoadSheetID) = cache.getOpenOnLoad()
            entries.forEach {
                if (it.sheetId == openOnLoadSheetID && it.sheetName == openOnLoadSheetName) {
                    addMenuItem(subMenu, it)
                }
            }
            entries.removeAll {
                it.sheetId == openOnLoadSheetID && it.sheetName == openOnLoadSheetName
            }
        }
    }

    /**
     * Splits a list of [SheetEntry]s based on archival status and adds them to the correct submenu
     */
    fun populateNavigationDrawerSubmenus(
            activeSubMenu: SubMenu,
            archivedSubMenu: SubMenu,
            entries: List<SheetEntry>
    ) {
        for (entry in entries) {
            if (entry.archived) {
                addMenuItem(archivedSubMenu, entry)
            } else {
                addMenuItem(activeSubMenu, entry)
            }
        }
    }

    /**
     * Merges together the list of [SheetEntry]s from metadata, and the list from the spreadsheet
     * properties.
     *
     * @return A list of [SheetEntry]s that includes all of the sheets currently on the spreadsheet
     * with the archival status from the metadata
     */
    fun mergeSheetEntryLists(metadata: List<SheetEntry>, properties: List<SheetEntry>): MutableList<SheetEntry> {
        // create mutable copy of metadata to edit
        val newMetadata = mutableListOf<SheetEntry>()
        for (sheet in metadata) {
            newMetadata.add(sheet.copy())
        }

        // handle new sheets added to google sheets or sheets being renamed
        for (sheet in properties) {
            val sheetWithMatchingSheetID = newMetadata.findSheetWithID(sheet.sheetId)

            if (sheetWithMatchingSheetID == null) {
                newMetadata.add(SheetEntry(sheet.sheetName, sheet.sheetId))
            } else if (sheet.sheetName != sheetWithMatchingSheetID.sheetName) {
                sheetWithMatchingSheetID.sheetName = sheet.sheetName
            }
        }

        // handle sheets that have been deleted from the google sheet
        newMetadata.removeAll {
            properties.findSheetWithID(it.sheetId) == null
        }

        return newMetadata
    }

    /**
     * Returns a [SheetEntry] with the given id if one exists in the list. Otherwise return null.
     */
    private fun List<SheetEntry>.findSheetWithID(id: String): SheetEntry? {
        for (sheet in this) {
            if (sheet.sheetId == id) return sheet
        }
        return null
    }

    /**
     * Add a single [SheetEntry] to a [SubMenu]
     */
    fun addMenuItem(menu: SubMenu, entry: SheetEntry) {
        menu.add(entry.sheetName).apply {
            isCheckable = true

            if (entry.archived) {
                setIcon(R.drawable.ic_label_outline_black_24dp)
            } else {
                setIcon(R.drawable.ic_label_black_24dp)
            }

            setOnMenuItemClickListener {
                if (!activity.isCurrentFragmentInitialized() || (activity.isCurrentFragmentInitialized()
                        && (activity.currentFragment is AppSettingsFragment
                                || activity.currentFragment is ReorderActiveBudgetsFragment
                                || entry.sheetId != activity.budgetFragment.sheetID))) {
                    changeMainFragment(newFragment(entry))
                }
                false
            }
        }
    }

    /**
     * Change the [NavDrawerFragment] to be displayed in the main view of the [BudgetActivity]
     */
    fun changeMainFragment(navDrawerFragment: NavDrawerFragment) {
        activity.currentFragment = navDrawerFragment
        activity.stopRefreshing()
        commitMainFragmentChange()
    }

    /**
     * Creates an appropriate [ActiveBudgetFragment] or [ArchivedBudgetFragment] from a [SheetEntry]
     */
    private fun newFragment(entry: SheetEntry): BudgetFragment {
        return if (entry.archived) {
            ArchivedBudgetFragment.newInstance(activity, entry.sheetId, entry.sheetName)
        } else {
            ActiveBudgetFragment.newInstance(activity, entry.sheetId, entry.sheetName)
        }
    }

    /**
     * Actually replace the current [NavDrawerFragment] being displayed in the main view of the
     * [BudgetActivity] with a new one.
     */
    private fun commitMainFragmentChange() {
        with(activity) {
            supportFragmentManager.beginTransaction().replace(R.id.mainContentFrameLayout, currentFragment).commit()
        }
    }

}
