package com.ghostreborn.akira.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import com.ghostreborn.akira.Constants
import com.ghostreborn.akira.R

class SettingsFragment : Fragment() {

    private lateinit var allowAdultSwitch: SwitchCompat
    private lateinit var allowUnknownSwitch: SwitchCompat
    private lateinit var subDubSwitch: SwitchCompat

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        allowAdultSwitch = view.findViewById(R.id.allow_adult_switch)
        allowUnknownSwitch = view.findViewById(R.id.allow_unknown_switch)
        subDubSwitch = view.findViewById(R.id.sub_dub_switch)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        allowAdultSwitch.isChecked = Constants.preferences.getBoolean(Constants.PREF_ALLOW_ADULT, false)
        allowUnknownSwitch.isChecked = Constants.preferences.getBoolean(Constants.PREF_ALLOW_UNKNOWN, false)
        subDubSwitch.isChecked = Constants.preferences.getBoolean(Constants.PREF_DUB_ENABLED, false)
        allowAdultSwitch.setOnCheckedChangeListener{
            _, isChecked -> Constants.preferences.edit().putBoolean(Constants.PREF_ALLOW_ADULT, isChecked).apply()
        }
        allowUnknownSwitch.setOnCheckedChangeListener{
                _, isChecked -> Constants.preferences.edit().putBoolean(Constants.PREF_ALLOW_UNKNOWN, isChecked).apply()
        }
        subDubSwitch.setOnCheckedChangeListener{
                _, isChecked -> Constants.preferences.edit().putBoolean(Constants.PREF_DUB_ENABLED, isChecked).apply()
        }
    }

}