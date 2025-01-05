package com.godpalace.teacher3;

import lombok.Getter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CopyOnWriteArrayList;

public class StudentManager {
    @Getter
    private static final CopyOnWriteArrayList<Student> students = new CopyOnWriteArrayList<>();

    @Getter
    private static final CopyOnWriteArrayList<Student> selectedStudents = new CopyOnWriteArrayList<>();

    public static Student getFirstStudent() {
        return students.get(0);
    }

    public static Student getFirstSelectedStudent() {
        return selectedStudents.get(0);
    }

    public static void addStudent(Student student) {
        students.add(student);
    }

    public static void removeStudent(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                students.remove(student);
                return;
            }
        }
    }

    public static void removeStudent(Student student) {
        students.remove(student);
    }

    public static void selectStudent(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                selectedStudents.add(student);
                return;
            }
        }
    }

    public static void selectStudent(Student student) {
        selectedStudents.add(student);
    }

    public static void deselectStudent(int id) {
        for (Student student : selectedStudents) {
            if (student.getId() == id) {
                selectedStudents.remove(student);
                return;
            }
        }
    }

    public static void deselectStudent(Student student) {
        selectedStudents.remove(student);
    }

    public static void clearSelectedStudents() {
        selectedStudents.clear();
    }

    public static Student connect(String ip) throws IOException {
        SocketChannel channel = SocketChannel.open(new InetSocketAddress(ip, 37000));
        Student student = new Student(channel);
        students.add(student);

        return student;
    }
}
