package com.example.jessica.mars

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.jessica.mars.models.Photo
import com.example.jessica.mars.models.PhotoList
import com.example.jessica.mars.models.PhotoRow
import com.example.jessica.mars.models.RowType
import com.example.jessica.mars.recyclerview.PhotoAdapter
import com.example.jessica.mars.service.NasaPhotos
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

private const val TAG = "MarsRover"

class MainActivity : AppCompatActivity() {

    private var currentRover = "curiosity"
    private var currentRoverPosition = 0
    private var currentCameraPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler_view.visibility = View.GONE
       recycler_view.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recycler_view.layoutManager = LinearLayoutManager(this)
        setupSpinners()
        loadPhotos()


    }

    private fun setupSpinners() {
        setupCameraSpinner()
        setupRoverSpinner()
    }

    private fun setupCameraSpinner() {


        //Camera spinner
        val cameraStrings = resources.getStringArray(R.array.camera_values)
        val cameraAdapter =
            ArrayAdapter.createFromResource(this, R.array.camera_names, android.R.layout.simple_spinner_item)

        cameraAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cameras.adapter = cameraAdapter
        cameras.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(recycler_view.adapter != null && currentRoverPosition!=position) {
                    (recycler_view.adapter as PhotoAdapter).filterCamera(cameraStrings[position])
                }
                currentCameraPosition = position
            }
        }
    }

    private fun setupRoverSpinner() {
        //Setup the spinners for selecting different rovers and cameras
        val roverStrings = resources.getStringArray(R.array.rovers)
        val adapter = ArrayAdapter.createFromResource(this, R.array.rovers, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        rovers.adapter = adapter
        rovers.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Why this logical here? is rovers sitting under camera?
                if (currentRoverPosition!=position) {
                    currentRover = roverStrings[position].toLowerCase()
                    loadPhotos()
                }
                currentCameraPosition = position
            }

        }
    }

    private fun loadPhotos() {
        progress.visibility = View.VISIBLE
        recycler_view.visibility = View.GONE
        NasaPhotos.getPhotos(currentRover).enqueue(object : Callback<PhotoList> {
            override fun onFailure(call: Call<PhotoList>, t: Throwable) {
                Snackbar.make(recycler_view, R.string.api_error, Snackbar.LENGTH_LONG)
                Log.e(TAG, "Problems getting Photos with error: $t.msg")
            }

            override fun onResponse(call: Call<PhotoList>, response: Response<PhotoList>) {
                response.let { photoResponse ->
                    if(photoResponse.isSuccessful) {
                        val body = photoResponse.body()
                        body?.let {
                            Log.d(TAG, "Received ${body.photos.size} photos")
                            if (recycler_view.adapter == null) {
                                val adapter = PhotoAdapter(sortPhotos(body))
                                recycler_view.adapter = adapter
                            } else {
                                (recycler_view.adapter as PhotoAdapter).updatePhotos((sortPhotos(body)))
                            }
                        }
                        recycler_view.scrollToPosition(0)
                        recycler_view.visibility = View.VISIBLE
                        progress.visibility = View.GONE
                    }
                }
            }
        })
    }
    fun sortPhotos(photoList: PhotoList): ArrayList<PhotoRow> {
        val map = HashMap<String, ArrayList<Photo>>()
        for (photo in photoList.photos) {
            var photos = map[photo.camera.full_name]
            if (photos == null) {
                photos = ArrayList()
                map[photo.camera.full_name] = photos
            }
            photos.add(photo)
        }
        val newPhotos = ArrayList<PhotoRow>()
        for ((key, value) in map) {
            newPhotos.add(PhotoRow(RowType.HEADER, null, key))
            value.mapTo(newPhotos) { PhotoRow(RowType.PHOTO, it, null) }
        }
        return newPhotos
    }
}


class SwipeHandler(val adapter: PhotoAdapter, dragDirs : Int, swipeDirs : Int) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {
    override fun onMove(
        recyclerView: RecyclerView?,
        viewHolder: RecyclerView.ViewHolder?,
        target: RecyclerView.ViewHolder?
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        adapter.removeRow(viewHolder.adapterPosition)
    }

}