package com.example.vinilos.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.vinilos.R
import com.example.vinilos.adapters.AlbumsAdapter
import com.example.vinilos.databinding.FragmentAlbumsBinding
import com.example.vinilos.models.Album
import com.example.vinilos.viewmodels.AlbumsViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText


@BindingAdapter("app:imageUri")
fun loadImageWithUri(imageView: ShapeableImageView, imageUri: String) {
    Glide.with(imageView.context).load(
        Uri.parse(imageUri)
    ).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .into(imageView)
}

class AlbumsFragment : Fragment() {
    private var userType: String? = null
    private var _binding: FragmentAlbumsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: AlbumsViewModel

    private lateinit var albumsAdapter: AlbumsAdapter
    private lateinit var albumRecyclerView: RecyclerView
    private lateinit var albumInputText: TextInputEditText
    private lateinit var btnTryAgain: MaterialButton
    private lateinit var floatingButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userType = it.getString("userType")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAlbumsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        val view = binding.root
        albumsAdapter = AlbumsAdapter(requireActivity().applicationContext)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val retryView = binding.retryView
        albumRecyclerView = binding.albumRecyclerView
        albumRecyclerView.layoutManager = GridLayoutManager(requireActivity().applicationContext,2)
        albumRecyclerView.adapter = albumsAdapter
        albumInputText = binding.searchBoxField
        btnTryAgain = retryView.btnTryAgain
        albumInputText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                viewModel.filterByAlbumName(s.toString())
            }
        })

        btnTryAgain.setOnClickListener {
            viewModel.refreshDataFromNetwork()
        }

        floatingButton = view.findViewById(R.id.floating_add_button)
        floatingButton.setOnClickListener {
            val transaction = this.activity?.supportFragmentManager?.beginTransaction()
            transaction?.replace(R.id.frame_layout, CreateAlbumFragment())
            transaction?.disallowAddToBackStack()
            transaction?.commit()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val activity = requireNotNull(this.activity)
        viewModel = ViewModelProvider(this, AlbumsViewModel.Factory(activity.application)).get(AlbumsViewModel::class.java)
        viewModel.isUser = userType?.equals("user") == true
        binding.also {
            it.viewModel = viewModel
        }
        viewModel.albums.observe(viewLifecycleOwner, Observer<List<Album>> {
            it.apply {
                albumsAdapter.albums = this
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(userType: String) =
            AlbumsFragment().apply {
                arguments = Bundle().apply {
                    putString("userType", userType)
                }
            }
    }
}