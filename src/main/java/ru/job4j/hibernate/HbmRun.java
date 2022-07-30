package ru.job4j.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;
import ru.job4j.hibernate.model.Student;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import static ru.job4j.hibernate.service.LoggerService.LOGGER;

public class HbmRun {

    private static Session session;

    public static void main(String[] args) {
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.OFF);

        Student studentOne = Student.of(1, "Alex", "junior", 1000);
        Student studentTwo = Student.of(2, "Nikolay", "middle", 2100);
        Student studentThree = Student.of(3, "Nikita", "senior", 3200);
        List<Student> listOfItems = List.of(studentOne, studentTwo, studentThree);

        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure().build();
        try {
            SessionFactory sessionFactory =
                    new MetadataSources(registry).buildMetadata().buildSessionFactory();
            session = sessionFactory.openSession();

            deleteAllEntriesInDbAndRestartIndexFromOne();

            saveInDbFromList(listOfItems);
            displayAll();

            save(Student.of(0, "Student for save", "low", 5));
            displayAll();

            save(Student.of(0, "Student for save", "critical low", 7));
            findByName("Student for save");

            Student studentForPersist = Student.of(0, "Student for persist", "weak", 3);
            persist(studentForPersist);
            displayAll();
            
            studentForPersist.setName("Student for persist name updated");

            updateById(Student.of(1, "name updated", "exp updated", 22));
            displayAll();

            insertSelect(2);
            displayAll();

            findByName("name updated");
            displayAll();

            deleteById(2);
            displayAll();

            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    private static int save(Student student) {
        LOGGER.info(String.format("Saving new student: %s", student));
        int idFromDb = (int) session.save(student);
        LOGGER.info(String.format("From DB was received id: %d", idFromDb));
        return idFromDb;
    }

    private static void persist(Student student) {
        LOGGER.info(String.format("Persisting new student: %s", student));
        Transaction transaction = session.beginTransaction();
        session.persist(student);
        LOGGER.info(String.format("Id was assigned to the persisted object: %d", student.getId()));
        transaction.commit();
    }

    private static void saveInDbFromList(List<Student> list) {
        LOGGER.info(String.format("Saving objects from the list: %s", list.toString()));
        for (Student student : list) {
            session.save(student);
        }
    }

    private static Student findById(int id) {
        LOGGER.info(String.format("Searching object by id: %s", id));
        Query query = session.createQuery("from Student s where s.id = :id");
        query.setParameter("id", id);
        Student studentFoundedInDB = (Student) query.uniqueResult();
        LOGGER.info(String.format("Following object was found: %s", studentFoundedInDB.toString()));
        return studentFoundedInDB;
    }

    private static List<Student> findByName(String name) {
        LOGGER.info(String.format("Searching objects by name: %s", name));
        List<Student> result = new ArrayList<>();
        Query query = session.createQuery("from Student s where s.name = :name");
        query.setParameter("name", name);
        for (Object student : query.list()) {
            result.add((Student) student);
        }
        LOGGER.info(String.format("The following objects were found: %s", result.toString()));
        return result;
    }

    private static void updateById(Student student) {
        LOGGER.info(String.format("Updating student, new version of object: %s", student));
        LOGGER.info(String.format("Updating student, old version of object: %s",
                findById(student.getId())));
        Transaction transaction = session.beginTransaction();
        session.createQuery(
                "update Student s set "
                        + "s.name = :newName,"
                        + "s.experience = :newExperience,"
                        + "s.salary = :newSalary "
                        + "where s.id = :fid").
                setParameter("newName", student.getName()).
                setParameter("newExperience", student.getExperience()).
                setParameter("newSalary", student.getSalary()).
                setParameter("fid", student.getId()).
                executeUpdate();
        transaction.commit();
    }

    private static void deleteById(int id) {
        LOGGER.info(String.format("Deleting Student with id %s", id));
        Transaction transaction = session.beginTransaction();
        session.createQuery("delete from Student where id = :id")
                .setParameter("id", id)
                .executeUpdate();
        transaction.commit();
    }

    private static void displayAll() {
        session.clear();
        Query query = session.createQuery("from Student");
        LOGGER.info("--- Printing out all students from database");
        for (Object student : query.list()) {
            LOGGER.info(student.toString());
        }
        LOGGER.info("--- end of list ");
    }

    private static void insertSelect(int id) {
       LOGGER.info(String.format("insert select %d", id));

        Transaction transaction = session.beginTransaction();
        session.createQuery("insert into Student (name, experience, salary) "
                + "select concat(s.name, ' INSERT-SELECT creation'), s.experience, s.salary "
                + "from Student s where s.id = :fId")
                .setParameter("fId", id)
                .executeUpdate();
        transaction.commit();
    }

    private static void deleteAllEntriesInDbAndRestartIndexFromOne() {
        LOGGER.info("Deleting all entries...");
        Transaction transaction = session.beginTransaction();
        session.createSQLQuery("TRUNCATE TABLE students;").executeUpdate();
        transaction.commit();
        LOGGER.info("Restarting indexes with 1...");
        session.beginTransaction();
        session.createSQLQuery("ALTER SEQUENCE students_id_seq RESTART WITH 1;").executeUpdate();
        transaction.commit();
    }
}