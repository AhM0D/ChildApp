package com.example.childapp

import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.childapp.databinding.FragmentFirstBinding
import java.util.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    var devicePolicyManager: DevicePolicyManager? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isAccessibilitySettingsOn(requireContext())) {
           resetTime()
        } else {
            setUsageTime()
        }
        binding.buttonFirst.setOnClickListener {
            if (!isAccessibilitySettingsOn(requireContext())) {
                alertDi(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            } else {
                val intent = Intent(requireActivity(), ActiveAppWatcher::class.java)
                requireActivity().startService(intent)
                val date = Date(System.currentTimeMillis())
                val pref: SharedPreferences = requireContext().getSharedPreferences("ChildApp", 0)
                val editor = pref.edit()
                editor.putLong("timeStamp", date.time)
                editor.apply()
                Toast.makeText(
                    requireContext(),
                    "???????????? ?????????? ????!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.adminAccess.setOnClickListener {
            val pref: SharedPreferences = requireActivity().getSharedPreferences("ChildApp", 0)
            val editor = pref.edit()
            editor.putBoolean("isSettingBlocked", false)
            editor.apply()
            openDeviceAdmin()

        }
        binding.setting.setOnClickListener {
            val pref: SharedPreferences = requireActivity().getSharedPreferences("ChildApp", 0)
            val editor = pref.edit()
            editor.putBoolean("isSettingBlocked", false)
            editor.apply()
            val intent = Intent(Settings.ACTION_SETTINGS)
            someActivityResultLauncher.launch(intent)
        }

        binding.limitTime.setOnClickListener {
            if (binding.timeEditText.text.isNotEmpty() && binding.timeEditText.text.toString()
                    .toInt() > 0
            ) {
                val pref: SharedPreferences = requireActivity().getSharedPreferences("ChildApp", 0)
                val editor = pref.edit()
                editor.putInt("limited_time", binding.timeEditText.text.toString().toInt())
                editor.apply()
                Toast.makeText(requireContext(), "???????? ?????????????? ???? ????!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "???? ?????????? ???????? ?????????? ???????? ????????!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.reLaunchPermission.setOnClickListener {
            AutoStartHelper.getInstance().getAutoStartPermission(requireContext());
        }

        binding.resetTimeLimit.setOnClickListener {
           resetTime()
        }
    }

    private fun resetTime() {
        val pref = requireActivity().getSharedPreferences("ChildApp", 0)
        val editor = pref.edit()
        editor.putLong("onDuration", 0)
        editor.apply()
        setUsageTime()
    }

    private fun setUsageTime() {
        val pref = requireActivity().getSharedPreferences("ChildApp", 0)
        val currentMinutes = pref.getLong("onDuration", 0)
        binding.phoneUsage.text = "?????? ???????? ?????????????? ?????? ???? ?????? ?????????? : ${currentMinutes}"
    }

    fun alertDi(action: String?) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("???????? ?????????? ?????? ?????? ???????? ???????????? ???????? ???????? ???? ???? ???????????? ??????????")
        builder.setTitle("???????????? ???? ???????????? ?????? ??????")
        builder.setPositiveButton(
            "????????"
        ) { dialog, which ->
            val intent = Intent(action)
            someActivityResultLauncher.launch(intent)
        }
        builder.setNegativeButton(
            "??????"
        ) { dialog, which -> requireActivity().finish() }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    private var someActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (isAccessibilitySettingsOn(requireContext())) {
                val pref: SharedPreferences = requireContext().getSharedPreferences("ChildApp", 0)
                val editor = pref.edit()
                editor.putBoolean("isSettingBlocked", true)
                editor.apply()
                Toast.makeText(requireContext(), "?????????????? ???????? ????!", Toast.LENGTH_SHORT).show()
            }
        }

    private fun openDeviceAdmin() {
        devicePolicyManager =
            requireActivity().getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager?
        val componentName = ComponentName(requireActivity(), DevAdmRec::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Disable app")
        someActivityResultLauncher.launch(intent)
    }

    private fun isAccessibilitySettingsOn(mContext: Context): Boolean {
        var accessibilityEnabled = 0
        //your package /   accesibility service path/class
        val service = "com.example.childapp/com.example.childapp.ActiveAppWatcher"
        val accessibilityFound = false
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                mContext.applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: Settings.SettingNotFoundException) {

        }
        val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')
        if (accessibilityEnabled == 1) {
            val settingValue = Settings.Secure.getString(
                mContext.applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue)
                while (mStringColonSplitter.hasNext()) {
                    val accessabilityService = mStringColonSplitter.next()
                    if (accessabilityService.equals(service, ignoreCase = true)) {
                        return true
                    }
                }
            }
        }
        return accessibilityFound
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}