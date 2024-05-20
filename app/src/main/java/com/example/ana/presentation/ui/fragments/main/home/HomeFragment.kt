package com.example.ana.presentation.ui.fragments.main.home

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ana.R
import com.example.ana.data.model.Advice
import com.example.ana.data.model.Child
import com.example.ana.databinding.FragmentHomeBinding
import com.example.ana.presentation.ui.adapters.ChildAdapter
import com.example.ana.presentation.ui.adapters.AdviceAdapter
import com.example.ana.presentation.ui.adapters.AdviceSelector
import com.example.ana.presentation.ui.adapters.ChildSelector
import com.example.ana.presentation.ui.fragments.main.home.child.ChildAuthBottomSheet
import com.example.ana.presentation.ui.fragments.main.home.child.UpdateData
import com.example.ana.view_model.HomeViewModel
import com.teenteen.teencash.presentation.base.BaseFragment

class HomeFragment : BaseFragment<FragmentHomeBinding>(), UpdateData, AdviceSelector, ChildSelector {

    private lateinit var viewModel: HomeViewModel
    private lateinit var childAdapter: ChildAdapter
    private lateinit var adviceAdapter: AdviceAdapter
    private lateinit var children: MutableList<Child>
    private lateinit var articles: MutableList<Advice>
    override fun attachBinding(
        list: MutableList<FragmentHomeBinding>,
        layoutInflater: LayoutInflater,
        container: ViewGroup?,
        attachToRoot: Boolean
    ) {
        list.add(FragmentHomeBinding.inflate(layoutInflater, container, attachToRoot))
    }

    override fun setupViews() {
        progressDialog.show()
        children = mutableListOf()
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        viewModel.getUserName(prefs.getCurrentUserId())
        setupArticles()
        binding.contact.setOnClickListener { openWhatsApp() }
        viewModel.getChildren(prefs.getCurrentUserId())
        binding.btnNotification.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToNotificationFragment())
        }
    }

    private fun setupArticles() {
        viewModel.getAdvices()
        binding.listArticles.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }

    private fun openWhatsApp() {
        val phoneNumber = "+77079671159"
        val message = getString(R.string.hello_i_need_assistance)
        val url = "https://wa.me/$phoneNumber?text=${Uri.encode(message)}"

        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun subscribeToLiveData() {
        viewModel.name.observe(viewLifecycleOwner) {
            binding.title.text = getString(R.string.hello) + ", $it!"
        }
        viewModel.children.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                setNoChildrenViews()
                binding.btnAddFirstChild.setOnClickListener {
                    ChildAuthBottomSheet(this).show(childFragmentManager, "Authorization")
                }
            } else {
                setChildrenViews()
                progressDialog.dismiss()
                children = it.toMutableList()
                childAdapter = ChildAdapter(children, this)
                binding.rvChildren.adapter = childAdapter
                binding.rvChildren.layoutManager = LinearLayoutManager(requireContext())
                binding.btnAddBaby.setOnClickListener {
                    ChildAuthBottomSheet(this).show(childFragmentManager, "Authorization")
                }

                binding.addBabyTitle.setOnClickListener {
                    ChildAuthBottomSheet(this).show(childFragmentManager, "Authorization")
                }
            }
        }
        viewModel.advices.observe(viewLifecycleOwner) {
            articles = it
            adviceAdapter = AdviceAdapter(articles, this)
            binding.listArticles.adapter = adviceAdapter
        }
    }

    private fun setNoChildrenViews() {
        binding.layoutEmpty.visibility = View.VISIBLE
        binding.layoutItems.visibility = View.GONE
        progressDialog.dismiss()
    }

    private fun setChildrenViews() {
        binding.layoutEmpty.visibility = View.GONE
        binding.layoutItems.visibility = View.VISIBLE
    }

    override fun updateChildrenList() {
        viewModel.getChildren(prefs.getCurrentUserId())
    }

    override fun onAdvicePressed(advice: Advice) {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToArticleFragment(advice.category, ""))
    }

    override fun onChildPressed(child: Child) {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToChildDetailFragment(child.name))
    }
}
