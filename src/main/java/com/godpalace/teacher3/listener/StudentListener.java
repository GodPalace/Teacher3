package com.godpalace.teacher3.listener;

import com.godpalace.teacher3.Student;

public interface StudentListener {
    default void onStudentAdded(Student student) {}
    default void onStudentRemoved(Student student) {}
    default void onStudentSelected(Student student) {}
    default void onStudentDeselected(Student student) {}
}
