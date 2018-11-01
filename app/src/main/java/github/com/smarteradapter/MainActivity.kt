package github.com.smarteradapter

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import github.com.smartadapter.SmartAdapter
import github.com.smartadapter.SmartViewHolder
import github.com.smartadapter.layoutManager.FlowLayoutManager2
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView.layoutManager = FlowLayoutManager2()
        initAdapter()
    }

    private fun initAdapter() {
        var smartAdapter = SmartAdapter<SmartViewHolder>(this)
        smartAdapter.register(R.layout.item_person,BR.person)
        val lis = mutableListOf<Person>()
        for (i in 1 .. 100){
            lis.add(Person().apply {
                name = "name = $i"
                address = "address = $i"
            })
        }

        smartAdapter.addData(lis)
        recyclerView.adapter = smartAdapter

    }
}
