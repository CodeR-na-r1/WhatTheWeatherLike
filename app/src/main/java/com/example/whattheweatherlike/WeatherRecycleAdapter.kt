package com.example.whattheweatherlike

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class WeatherRecycleAdapter(private var weatherList: ArrayList<Weather>) :  RecyclerView.Adapter<WeatherRecycleAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var city = view.findViewById(R.id.cityName) as TextView
        var temp = view.findViewById(R.id.cityTemp) as TextView
        var humidity = view.findViewById(R.id.cityHumidity) as TextView
        var icon: ImageView?= view.findViewById(R.id.weatherView)?: null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.city.text = this.weatherList[position].city
        holder.temp.text = this.weatherList[position].temp
        holder.humidity.text = this.weatherList[position].humidity
        if (holder.icon != null) {
            holder.icon?.setImageResource(this.weatherList[position].iconId)
        }
    }

    override fun getItemCount(): Int {
        return this.weatherList.size
    }

    fun updateData(newData: ArrayList<Weather>){
        this.weatherList.clear()
        this.weatherList.addAll(newData)
        notifyDataSetChanged()
    }
}