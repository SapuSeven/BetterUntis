package com.sapuseven.untis.dialogs

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat
import ca.antonious.materialdaypicker.MaterialDayPicker
import ca.antonious.materialdaypicker.SelectionMode
import ca.antonious.materialdaypicker.SelectionState
import com.sapuseven.untis.R
import com.sapuseven.untis.preferences.WeekRangePickerPreference

class WeekRangePickerPreferenceDialog(var onCloseListener: ((positiveResult: Boolean, picker: MaterialDayPicker) -> Unit)? = null) : PreferenceDialogFragmentCompat() {
	private lateinit var picker: MaterialDayPicker

	companion object {
		fun newInstance(key: String): WeekRangePickerPreferenceDialog = newInstance(key, null)

		fun newInstance(key: String, onCloseListener: ((positiveResult: Boolean, picker: MaterialDayPicker) -> Unit)?): WeekRangePickerPreferenceDialog {
			val fragment = WeekRangePickerPreferenceDialog(onCloseListener)
			val bundle = Bundle(1)
			bundle.putString(ARG_KEY, key)
			fragment.arguments = bundle
			return fragment
		}
	}

	override fun onCreateDialogView(context: Context?): View {
		val root = super.onCreateDialogView(context)
		picker = root.findViewById(R.id.day_picker)
		picker.apply {
			selectionMode = RangeSelectionMode(this)
			val savedDays = preference.getPersistedStringSet(emptySet()).toList().map { MaterialDayPicker.Weekday.valueOf(it) }
			setSelectedDays(if (savedDays.size == 1) listOf(savedDays.first()) else listOfNotNull(savedDays.min(), savedDays.max()))
		}
		return root
	}

	override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
		super.onPrepareDialogBuilder(builder)
		// TODO: Localize
		builder.setNeutralButton("Reset") { dialog, _ ->
			preference.persistStringSet(emptySet())
			dialog.dismiss()
		}
	}

	override fun onDialogClosed(positiveResult: Boolean) {
		if (positiveResult) preference.persistStringSet(picker.selectedDays.map { it.name }.toSet())
		(preference as? WeekRangePickerPreference)?.refreshSummary()

		onCloseListener?.invoke(positiveResult, picker)
	}

	class RangeSelectionMode(private val materialDayPicker: MaterialDayPicker) : SelectionMode {
		override fun getSelectionStateAfterSelecting(lastSelectionState: SelectionState, dayToSelect: MaterialDayPicker.Weekday): SelectionState {
			return createRangedSelectionState(
					lastSelectionState = lastSelectionState,
					dayPressed = dayToSelect
			)
		}

		override fun getSelectionStateAfterDeselecting(lastSelectionState: SelectionState, dayToDeselect: MaterialDayPicker.Weekday): SelectionState {
			return createRangedSelectionState(
					lastSelectionState = lastSelectionState,
					dayPressed = dayToDeselect
			)
		}

		private fun createRangedSelectionState(lastSelectionState: SelectionState, dayPressed: MaterialDayPicker.Weekday): SelectionState {
			val previouslySelectedDays = lastSelectionState.selectedDays
			val orderedWeekdays = MaterialDayPicker.Weekday.getOrderedDaysOfWeek(materialDayPicker.locale)
			val ordinalsOfPreviouslySelectedDays = previouslySelectedDays.map { orderedWeekdays.indexOf(it) }

			val ordinalOfFirstDayInPreviousRange = ordinalsOfPreviouslySelectedDays.min()
			val ordinalOfLastDayInPreviousRange = ordinalsOfPreviouslySelectedDays.max()
			val ordinalOfSelectedDay = orderedWeekdays.indexOf(dayPressed)

			return when {
				ordinalOfFirstDayInPreviousRange == null || ordinalOfLastDayInPreviousRange == null -> {
					// We had no previous selection so just return the day pressed as the selection.
					SelectionState.withSingleDay(dayPressed)
				}
				ordinalOfFirstDayInPreviousRange == ordinalOfLastDayInPreviousRange && ordinalOfFirstDayInPreviousRange == ordinalOfSelectedDay -> {
					// User pressed the only day in the range selection. Return an empty selection.
					SelectionState()
				}
				ordinalOfSelectedDay == ordinalOfFirstDayInPreviousRange || ordinalOfSelectedDay == ordinalOfLastDayInPreviousRange -> {
					// User pressed the first or last item in range. Just deselect that item.
					lastSelectionState.withDayDeselected(dayPressed)
				}
				ordinalOfSelectedDay < ordinalOfFirstDayInPreviousRange -> {
					// User pressed a day on the left of the previous date range. Grow the starting point of the range to that.
					SelectionState(selectedDays = orderedWeekdays.subList(ordinalOfSelectedDay, ordinalOfLastDayInPreviousRange + 1))
				}
				else -> {
					// User pressed a day on the right of the start of the date range. Update the ending point to that position.
					SelectionState(selectedDays = orderedWeekdays.subList(ordinalOfFirstDayInPreviousRange, ordinalOfSelectedDay + 1))
				}
			}
		}
	}
}
