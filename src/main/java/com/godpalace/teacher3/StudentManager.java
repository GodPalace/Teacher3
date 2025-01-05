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
        if (students.isEmpty()) return null;
        return students.get(0);
    }

    public static Student getFirstSelectedStudent() {
        if (selectedStudents.isEmpty()) return null;
        return selectedStudents.get(0);
    }

    public static void addStudent(Student student) {
        students.add(student);
    }

    public static boolean removeStudent(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                students.remove(student);
                deselectStudent(student);
                return true;
            }
        }

        return false;
    }

    public static void removeStudent(Student student) {
        if (!students.contains(student))
            return;

        students.remove(student);
        deselectStudent(student);
    }

    public static boolean selectStudent(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                selectedStudents.add(student);
                return true;
            }
        }

        return false;
    }

    public static boolean selectStudent(Student student) {
        if (!students.contains(student))
            return false;

        selectedStudents.add(student);
        return true;
    }

    public static boolean deselectStudent(int id) {
        for (Student student : selectedStudents) {
            if (student.getId() == id) {
                selectedStudents.remove(student);
                return true;
            }
        }

        return false;
    }

    public static void deselectStudent(Student student) {
        if (!selectedStudents.contains(student))
            return;

        selectedStudents.remove(student);
    }

    public static void clearSelectedStudents() {
        selectedStudents.clear();
    }

    public static Student connect(String ip) throws IOException {
        InetSocketAddress address = new InetSocketAddress(ip, 37000);
        if (!address.getAddress().isReachable(3000))
            throw new IOException("Cannot connect to " + ip);

        SocketChannel channel = SocketChannel.open(address);
        Student student = new Student(channel);
        students.add(student);

        return student;
    }
}
