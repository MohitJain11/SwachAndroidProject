package com.android.swach.Adapters;

import android.content.Context;
//import android.support.annotation.NonNull;
//import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.swach.R;
import com.android.swach.models.StudentListModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StudentListAdapter  extends RecyclerView.Adapter<StudentListAdapter.MyViewHolder> {

    private List<StudentListModel> studentListModel;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_class, tv_aadhar_number, tv_father_name;
        public TextView tv_dob, tv_center_name, tv_student_name;
        public TextView tv_student_no, tv_sex_name;

        public MyViewHolder(View view) {
            super(view);
            tv_class = (TextView) view.findViewById(R.id.tv_class);
            tv_aadhar_number = (TextView) view.findViewById(R.id.tv_aadhar_number);
            tv_father_name = (TextView) view.findViewById(R.id.tv_father_name);
            tv_dob = (TextView) view.findViewById(R.id.tv_dob);
            tv_center_name = (TextView) view.findViewById(R.id.tv_center_name);
            tv_student_name = (TextView) view.findViewById(R.id.tv_student_name);
            tv_sex_name = (TextView) view.findViewById(R.id.tv_sex_name);
            tv_student_no = (TextView) view.findViewById(R.id.tv_student_no);
        }
    }

    public StudentListAdapter(List<StudentListModel> studentListModel, Context context) {
        this.studentListModel = studentListModel;
        this.context = context;
    }


    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        StudentListModel StudentListModel = studentListModel.get(position);
        holder.tv_class.setText(StudentListModel.ClassName);
        holder.tv_center_name.setText(StudentListModel.CentreName);
        holder.tv_dob.setText(StudentListModel.StudentDOB);
        holder.tv_father_name.setText(StudentListModel.StudentFatherName);
        holder.tv_aadhar_number.setText(StudentListModel.AadharNo);
        holder.tv_student_name.setText(StudentListModel.StudentName);
        holder.tv_student_no.setText(StudentListModel.StudentsNo);
        holder.tv_sex_name.setText(StudentListModel.StudentSex);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_student_list, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return studentListModel.size();
    }
}
