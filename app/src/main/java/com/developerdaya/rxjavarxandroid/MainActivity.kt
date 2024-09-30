package com.developerdaya.rxjavarxandroid
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.developerdaya.rxjavarxandroid.ui.MyAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: MyAdapter
    private val dataList = BehaviorSubject.createDefault(ArrayList<String>())
    private val disposable = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val editText = findViewById<EditText>(R.id.editText)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        adapter = MyAdapter(dataList.value ?: ArrayList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        disposable.add(dataList
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { updatedList ->
                    Toast.makeText(this, "I am updating data", Toast.LENGTH_SHORT).show()
                    adapter.notifyDataSetChanged()
                })
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                val query = s.toString()
                val currentList = dataList.value ?: ArrayList()
                dataList.value?.add(query)
                dataList.onNext(currentList)
            }
        })
    }
    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }
}
