package com.hilton.jobsearch.ui

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hilton.jobsearch.R
import com.hilton.jobsearch.data.Pokemon
import com.hilton.jobsearch.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    companion object {
        const val TAG = "HomeFragment"
    }

    @Inject
    lateinit var viewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.vm = viewModel
        binding.lifecycleOwner = this

        val adapter = PokemonSpeciesAdapter(object: PokemonAdapter.PokemonClickListener {
            override fun onPokemonClick(pokemon: Pokemon) {
//                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPokemonDetailFragment(
//                    pokemon.toPokemonData()
//                ))
            }
        })

        binding.list.adapter = adapter

        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.list.layoutManager = layoutManager

        val divider = DividerItemDecoration(context, layoutManager.orientation)
        divider.setDrawable(ColorDrawable(resources.getColor(R.color.divider)))
        binding.list.addItemDecoration(divider)


        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                when(val state = loadStates.source.refresh) {
                    is LoadState.Error -> {
                    }

                    LoadState.Loading -> {

                    }
                    is LoadState.NotLoading -> {

                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collectLatest {
                when(it) {
                    is UiState.Success -> {
                        adapter.submitData(it.result)
                    }

                    UiState.Default -> {
                        //nothing to do
                    }
                    is UiState.Error -> {
                        //show retry and error message
                    }
                    UiState.Loading -> {
                        //show loading
                    }
                }
            }
        }

        binding.search.setOnClickListener {
            lifecycleScope.launch {
                viewModel.searchPokemon(binding.searchBox.text.toString().trim())
            }
        }

        binding.searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.search.isEnabled = s?.isNotEmpty() == true && s.isNotBlank() == true
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }
}