package com.example.developer001.greenzoneapplication.support;

import android.content.Context;
import android.widget.ArrayAdapter;

public class ConsolAdapter extends ArrayAdapter<String>{

	Context context;
	public ConsolAdapter(Context context, int resource, int textViewResourceId) {
		super(context, resource, textViewResourceId);
		this.context=context;
	}
		
	@Override
	public void add(String object) {
		// TODO Auto-generated method stub
		super.add(": > "+object);
	}
	
}
