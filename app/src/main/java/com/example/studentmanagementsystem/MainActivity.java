package com.example.studentmanagementsystem;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText etStudentName, etStudentID;
    private Spinner spMajor;
    private Button btnAdd, btnEdit, btnDelete, btnView;
    private ListView lvStudents;

    private DatabaseHelper dbHelper;
    private final String[] majors = {"Informatics", "Information Systems"};
    private final List<Student> studentList = new ArrayList<>();
    private ArrayAdapter<Student> listAdapter;

    static class Student {
        String id;
        String name;
        String major;

        Student(String id, String name, String major) {
            this.id = id;
            this.name = name;
            this.major = major;
        }

        @Override
        public String toString() {
            return id + " - " + name + " (" + major + ")";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        etStudentName = findViewById(R.id.etStudentName);
        etStudentID = findViewById(R.id.etStudentID);
        spMajor = findViewById(R.id.spMajor);
        btnAdd = findViewById(R.id.btnAdd);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnView = findViewById(R.id.btnView);
        lvStudents = findViewById(R.id.lvStudents);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, majors);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMajor.setAdapter(spinnerAdapter);

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentList);
        lvStudents.setAdapter(listAdapter);

        btnAdd.setOnClickListener(v -> addStudent());
        btnEdit.setOnClickListener(v -> editStudent());
        btnDelete.setOnClickListener(v -> deleteStudent());
        btnView.setOnClickListener(v -> viewStudents());

        lvStudents.setOnItemClickListener((parent, view, position, id) -> {
            Student selected = studentList.get(position);
            etStudentID.setText(selected.id);
            etStudentName.setText(selected.name);
            for (int i = 0; i < majors.length; i++) {
                if (majors[i].equalsIgnoreCase(selected.major)) {
                    spMajor.setSelection(i);
                    break;
                }
            }
        });

        loadStudentData();
    }

    private void addStudent() {
        String id = etStudentID.getText().toString().trim();
        String name = etStudentName.getText().toString().trim();
        String major = spMajor.getSelectedItem().toString();

        if (id.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Please enter both Student ID and Name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.checkStudentExists(id)) {
            Toast.makeText(this, "Error: Student ID must be UNIQUE! ID already exists.", Toast.LENGTH_LONG).show();
            return;
        }

        boolean success = dbHelper.insertStudent(id, name, major);
        if (success) {
            Toast.makeText(this, "Student added successfully", Toast.LENGTH_SHORT).show();
            clearInputs();
            loadStudentData();
        } else {
            Toast.makeText(this, "Failed to add student", Toast.LENGTH_SHORT).show();
        }
    }

    private void editStudent() {
        String id = etStudentID.getText().toString().trim();
        String name = etStudentName.getText().toString().trim();
        String major = spMajor.getSelectedItem().toString();

        if (id.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Please enter Student ID and Name to edit", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!dbHelper.checkStudentExists(id)) {
            Toast.makeText(this, "Student ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = dbHelper.updateStudent(id, name, major);
        if (success) {
            Toast.makeText(this, "Student updated successfully", Toast.LENGTH_SHORT).show();
            clearInputs();
            loadStudentData();
        } else {
            Toast.makeText(this, "Failed to update student", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteStudent() {
        String id = etStudentID.getText().toString().trim();

        if (id.isEmpty()) {
            Toast.makeText(this, "Please enter Student ID to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!dbHelper.checkStudentExists(id)) {
            Toast.makeText(this, "Student ID not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = dbHelper.deleteStudent(id);
        if (success) {
            Toast.makeText(this, "Student deleted successfully", Toast.LENGTH_SHORT).show();
            clearInputs();
            loadStudentData();
        } else {
            Toast.makeText(this, "Failed to delete student", Toast.LENGTH_SHORT).show();
        }
    }

    private void viewStudents() {
        loadStudentData();
        Cursor cursor = dbHelper.getAllStudents();
        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(this, "No student records found.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        while (cursor.moveToNext()) {
            sb.append("ID: ").append(cursor.getString(0)).append("\n");
            sb.append("Name: ").append(cursor.getString(1)).append("\n");
            sb.append("Major: ").append(cursor.getString(2)).append("\n\n");
        }
        cursor.close();

        new AlertDialog.Builder(this)
                .setTitle("Student Records")
                .setMessage(sb.toString().trim())
                .setPositiveButton("OK", null)
                .show();
    }

    private void loadStudentData() {
        studentList.clear();
        Cursor cursor = dbHelper.getAllStudents();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String name = cursor.getString(1);
                String major = cursor.getString(2);
                studentList.add(new Student(id, name, major));
            }
            cursor.close();
        }
        listAdapter.notifyDataSetChanged();
    }

    private void clearInputs() {
        etStudentID.setText("");
        etStudentName.setText("");
        spMajor.setSelection(0);
    }
}
