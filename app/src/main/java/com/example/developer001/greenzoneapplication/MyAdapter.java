package com.example.developer001.greenzoneapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private Context context;
    private List<Employer> employeeList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView id;
        public TextView state;
        public TextView salary;
        public TextView received_date;
        public TextView salary_month;

        public MyViewHolder(View view) {
            super(view);
            name= (TextView) view.findViewById(R.id.textName);
            id= (TextView) view.findViewById(R.id.textID);
            state= (TextView) view.findViewById(R.id.textState);
            salary= (TextView) view.findViewById(R.id.textSalary);
            received_date= (TextView) view.findViewById(R.id.textReceivedDate);
            salary_month= (TextView) view.findViewById(R.id.textSalaryMonth);
        }
    }

    public MyAdapter(Context context, List<Employer> employeeList) {
        this.context = context;
        this.employeeList = employeeList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.users_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Employer e = employeeList.get(position);

        holder.name.setText(e.getName());
        holder.state.setText(""+e.getState());
        holder.id.setText(""+e.getID());
        holder.salary.setText(""+e.getSalary());
        holder.received_date.setText(""+e.getReceived_date());
        holder.salary_month.setText(""+e.getSalary_month());

    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

}