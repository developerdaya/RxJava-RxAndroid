# RxJava-RxAndroid
To achieve real-time updates of a RecyclerView based on user input in an EditText using RxJava and RxAndroid in Kotlin, you can follow these steps:

1. **Add Dependencies**: Ensure you have the necessary dependencies for RxJava and RxAndroid in your `build.gradle` file:

   ```groovy
   implementation "io.reactivex.rxjava2:rxjava:2.2.21"
   implementation "io.reactivex.rxjava2:rxandroid:2.1.1"
   ```

2. **Set Up Your Layout**: Create a layout file with an `EditText` and a `RecyclerView`. For example, `activity_main.xml`:

   ```xml
   <LinearLayout
       xmlns:android="http://schemas.android.com/apk/res/android"
       android:layout_width="match_parent"
       android:layout_height="match_parent"
       android:orientation="vertical">

       <EditText
           android:id="@+id/editText"
           android:layout_width="match_parent"
           android:layout_height="wrap_content"
           android:hint="Type here" />

       <androidx.recyclerview.widget.RecyclerView
           android:id="@+id/recyclerView"
           android:layout_width="match_parent"
           android:layout_height="match_parent"/>
   </LinearLayout>
   ```

3. **Set Up the RecyclerView Adapter**: Create an adapter for your RecyclerView. For instance, `MyAdapter.kt`:

   ```kotlin
   class MyAdapter(private var items: List<String>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

       class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
           val textView: TextView = itemView.findViewById(android.R.id.text1)
       }

       override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
           val view = LayoutInflater.from(parent.context)
               .inflate(android.R.layout.simple_list_item_1, parent, false)
           return ViewHolder(view)
       }

       override fun onBindViewHolder(holder: ViewHolder, position: Int) {
           holder.textView.text = items[position]
       }

       override fun getItemCount(): Int {
           return items.size
       }

       fun updateList(newItems: List<String>) {
           items = newItems
           notifyDataSetChanged()
       }
   }
   ```

4. **Set Up the Main Activity**: In your `MainActivity.kt`, implement the logic to update the RecyclerView based on user input.

 ```
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
```

### Explanation:

1. **PublishSubject**: Used to emit items (in this case, the text typed in the `EditText`).
2. **debounce()**: It waits for a specified amount of time (300 ms) after the last item emitted before emitting the latest item. This helps in reducing the number of updates while typing.
3. **TextWatcher**: Captures text input from the `EditText` and emits the current text to the `PublishSubject`.
4. **filterData()**: Filters the original data list based on the input text and updates the RecyclerView.

The code you provided is part of a `TextWatcher`, specifically the `onTextChanged` method, which is triggered whenever the text in the `EditText` changes. Let’s break down what each line does and explain the significance of `onNext`:

### Code Breakdown

```kotlin
override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    val query = s.toString() // Convert the CharSequence to a String
    val currentList = dataList.value ?: ArrayList() // Get the current list of data from dataList, or initialize a new ArrayList if it's null
    dataList.value?.add(query) // Add the new query to the current list (this line should be improved for correct functionality)
    dataList.onNext(currentList) // Emit the updated list using onNext
}
```

### Explanation of Each Part

1. **`val query = s.toString()`**:
   - This line converts the `CharSequence` received from the `EditText` into a `String`. The variable `query` will contain the text that the user has currently entered.

2. **`val currentList = dataList.value ?: ArrayList()`**:
   - This retrieves the current value of `dataList`, which is a `BehaviorSubject` holding a list of strings. If `dataList.value` is `null`, it initializes a new `ArrayList`.
   - The `?:` operator is the Elvis operator in Kotlin, which provides a default value if the left-hand side is `null`.

3. **`dataList.value?.add(query)`**:
   - This line attempts to add the new `query` string to the current list (`currentList`).
   - However, this line does not actually modify `currentList`, as `dataList.value` refers to the list held by the `BehaviorSubject`. If `dataList.value` is `null`, it won't execute the add operation.
   - To properly modify the list, you should first add `query` to `currentList` and then emit that list.

4. **`dataList.onNext(currentList)`**:
   - This method is called to emit a new item to the observers of the `BehaviorSubject`.
   - When you call `onNext(currentList)`, you’re notifying all subscribers (like your adapter) that the value of `dataList` has changed.
   - This will trigger any observers that are set up to listen for changes to the `dataList`, causing the adapter to refresh and display the updated list.

### Important Note

The way the code is currently written will lead to issues because `dataList.value?.add(query)` modifies the list that is inside the `BehaviorSubject`, but it does not affect the `currentList` that you use for emitting with `onNext`. 

Here’s how to properly add the new query to the list and emit it:

```kotlin
override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    val query = s.toString()
    val currentList = dataList.value ?: ArrayList() // Get the current list of data
    currentList.add(query) // Add the new query to the current list
    dataList.onNext(currentList) // Emit the updated list using onNext
}
```

### Summary

- **`onNext`**: It is used to push the updated value (the current list) to all subscribers of the `BehaviorSubject`. This is a core part of the reactive programming model, allowing different components of your application to react to changes in data seamlessly.
- The code will now ensure that when you type in the `EditText`, the current input is added to the list, and the updated list is emitted for any observers to react to.
