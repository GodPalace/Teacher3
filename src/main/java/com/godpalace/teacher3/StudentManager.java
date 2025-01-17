package com.godpalace.teacher3;

import com.godpalace.teacher3.listener.StudentListener;
import lombok.Getter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

public class StudentManager {
    @Getter
    private static final CopyOnWriteArrayList<Student> students = new CopyOnWriteArrayList<>();

    @Getter
    private static final CopyOnWriteArrayList<Student> selectedStudents = new CopyOnWriteArrayList<>();

    private static final LinkedList<StudentListener> listeners = new LinkedList<>();

    public static void addListener(StudentListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(StudentListener listener) {
        listeners.remove(listener);
    }

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

        for (StudentListener listener : listeners) {
            listener.onStudentAdded(student);
        }
    }

    public static boolean removeStudent(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                students.remove(student);
                deselectStudent(student);

                for (StudentListener listener : listeners) {
                    listener.onStudentRemoved(student);
                }

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

        for (StudentListener listener : listeners) {
            listener.onStudentRemoved(student);
        }
    }

    public static boolean selectStudent(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                selectedStudents.add(student);

                for (StudentListener listener : listeners) {
                    listener.onStudentSelected(student);
                }

                return true;
            }
        }

        return false;
    }

    public static boolean selectStudent(Student student) {
        if (!students.contains(student))
            return false;

        selectedStudents.add(student);
        for (StudentListener listener : listeners) {
            listener.onStudentSelected(student);
        }

        return true;
    }

    public static boolean deselectStudent(int id) {
        for (Student student : selectedStudents) {
            if (student.getId() == id) {
                selectedStudents.remove(student);

                for (StudentListener listener : listeners) {
                    listener.onStudentDeselected(student);
                }

                return true;
            }
        }

        return false;
    }

    public static void deselectStudent(Student student) {
        if (!selectedStudents.contains(student))
            return;

        for (StudentListener listener : listeners) {
            listener.onStudentDeselected(student);
        }

        selectedStudents.remove(student);
    }

    public static void selectAllStudents() {
        clearSelectedStudents();
        selectedStudents.addAll(students);

        for (StudentListener listener : listeners) {
            for (Student student : students) {
                listener.onStudentSelected(student);
            }
        }
    }

    public static void clearSelectedStudents() {
        for (StudentListener listener : listeners) {
            for (Student student : selectedStudents) {
                listener.onStudentDeselected(student);
            }
        }

        selectedStudents.clear();
    }

    public static Student getStudent(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                return student;
            }
        }

        return null;
    }

    public static Student connect(String ip) throws IOException {
        InetSocketAddress address = new InetSocketAddress(ip, 37000);
        if (!address.getAddress().isReachable(3000))
            throw new IOException(ip + "不是一个可达的IP地址");

        SocketChannel channel = SocketChannel.open(address);
        Student student = new Student(channel);
        students.add(student);

        return student;
    }
}
