package com.bctags.bcstocks

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.bctags.bcstocks.ui.orders.OrdersActivity
import com.bctags.bcstocks.ui.receives.NewReceiveActivity
import com.bctags.bcstocks.ui.settings.SettingsActivity


class MenuFragment : Fragment() {

    private lateinit var llMenu: LinearLayout
    private lateinit var llSettings: LinearLayout
    private lateinit var llReceives: LinearLayout
    private lateinit var llOrders: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    private fun initListeners() {
        llMenu.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
        }
        llSettings.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
        }
        llReceives.setOnClickListener {
            val intent = Intent(activity, NewReceiveActivity::class.java)
            startActivity(intent)
        }
        llOrders.setOnClickListener {
            val intent = Intent(activity, OrdersActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initUI() {
        llMenu = requireView().findViewById(R.id.llMenu)
        llSettings = requireView().findViewById(R.id.llSettings)
        llReceives = requireView().findViewById(R.id.llReceives)
        llOrders = requireView().findViewById(R.id.llOrders)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initListeners()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MenuFragment()
                .apply {
                    arguments = Bundle().apply {

                    }
                }
    }
}
