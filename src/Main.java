import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        //z1
        System.out.println(Person.fromCsvLine("Ewa Kowalska,03.11.1901,05.03.1990,,"));

        //z2
        try {
            List<Person> people = Person.fromCsv("family.csv");
            for (Person person : people)
                System.out.println(person);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}