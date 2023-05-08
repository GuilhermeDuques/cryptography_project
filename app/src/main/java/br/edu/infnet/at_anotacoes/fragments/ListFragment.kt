package br.edu.infnet.at_anotacoes.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import br.edu.infnet.at_anotacoes.R
import br.edu.infnet.at_anotacoes.databinding.FragmentListBinding


class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }

    private fun setup() {
        setupClickListener()
    }

    private fun setupClickListener() {
        binding.fabAdd.setOnClickListener {
            nav(R.id.action_listFragment_to_anotacaoFragment)
        }
    }

    private fun nav(id: Int) {
        this.findNavController().navigate(id)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}