package com.openfooddata.app.adapter;

import java.util.List;

import co.oepeneats.app.R;

import com.openfooddata.app.model.News;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class NewsArrayAdapter extends ArrayAdapter<News> {
    
    public NewsArrayAdapter(Context context, 
                              int textViewResourceId, 
                              List<News> objects //, ImageLoader imageLoader
                              ) {
        super(context, textViewResourceId, objects);
    }

    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.lv_news_list, null);
        }
        
        ViewHolder holder = (ViewHolder) v.getTag(R.id.id_holder);//where the hell is the id_holder from  
         
        if (holder == null) {
            holder = new ViewHolder(v);
            v.setTag(R.id.id_holder, holder);
        }        
         
        final News entry = getItem(position);
        holder.news_title.setText(entry.getTitle());
        holder.news_abstract.setText(entry.getNews_abstract());
       
        return v;
    }
     
    
    private class ViewHolder {
        TextView news_title; 
        TextView news_abstract; 
        
        public ViewHolder(View v) {
            //image = (NetworkImageView) v.findViewById(R.id.iv_thumb);
            news_title = (TextView) v.findViewById(R.id.tv_news_title);
            news_abstract = (TextView) v.findViewById(R.id.tv_news_abstract);
            v.setTag(this);
        }
    }
}
